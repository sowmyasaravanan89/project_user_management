const express = require('express');
const cors = require('cors');
const fs = require('fs').promises;
const path = require('path');

const app = express();
const PORT = process.env.PORT || 5000;
const DATA_FILE = path.join(__dirname, 'data', 'users.json');
const AUTH_FILE = path.join(__dirname, 'data', 'auth.json');

// Middleware
app.use(cors());
app.use(express.json());

// Email validation function
const isValidEmail = (email) => {
  // Check if email contains @ symbol
  if (!email || typeof email !== 'string' || !email.includes('@')) {
    return false;
  }
  
  // Basic email format validation
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return emailRegex.test(email);
};

// Age validation function
const isValidAge = (age) => {
  // Age is optional, so undefined is allowed
  if (age === undefined) {
    return true;
  }
  
  // Don't allow null, empty string, or non-numeric values
  if (age === null || age === '' || isNaN(age)) {
    return false;
  }
  
  // Convert to number and check if it's a positive integer
  const numAge = Number(age);
  return Number.isInteger(numAge) && numAge >= 0 && numAge <= 150;
};

// Ensure data directory exists
const ensureDataDir = async () => {
  const dataDir = path.dirname(DATA_FILE);
  try {
    await fs.access(dataDir);
  } catch {
    await fs.mkdir(dataDir, { recursive: true });
  }
};

// Read data from JSON file
const readData = async () => {
  try {
    await ensureDataDir();
    const data = await fs.readFile(DATA_FILE, 'utf8');
    return JSON.parse(data);
  } catch (error) {
    // If file doesn't exist, return empty array
    return [];
  }
};

// Read auth data from JSON file
const readAuthData = async () => {
  try {
    await ensureDataDir();
    const data = await fs.readFile(AUTH_FILE, 'utf8');
    return JSON.parse(data);
  } catch (error) {
    // If file doesn't exist, create default admin user
    const defaultAuth = {
      sessions: {},
      users: {
        admin: {
          username: 'admin',       // This is just for testing purposes
          password: 'password123', // In production, this should be hashed
          role: 'admin'
        }
      }
    };
    await writeAuthData(defaultAuth);
    return defaultAuth;
  }
};

// Write auth data to JSON file
const writeAuthData = async (data) => {
  await ensureDataDir();
  await fs.writeFile(AUTH_FILE, JSON.stringify(data, null, 2));
};

// Write data to JSON file
const writeData = async (data) => {
  await ensureDataDir();
  await fs.writeFile(DATA_FILE, JSON.stringify(data, null, 2));
};

// Generate unique ID
const generateId = () => {
  return Date.now().toString() + Math.random().toString(36).substr(2, 9);
};

// Generate session token
const generateToken = () => {
  return Date.now().toString() + Math.random().toString(36).substr(2, 16);
};

// Middleware to check authentication
const authenticateToken = async (req, res, next) => {
  const authHeader = req.headers['authorization'];
  const token = authHeader && authHeader.split(' ')[1]; // Bearer TOKEN

  if (!token) {
    return res.status(401).json({ error: 'Access token required' });
  }

  try {
    const authData = await readAuthData();
    const session = authData.sessions[token];
    
    
    if (!session || session.expiresAt < Date.now()) {
      return res.status(403).json({ error: 'Invalid or expired token' });
    }

    req.user = session.user;
    next();
  } catch (error) {
    return res.status(403).json({ error: 'Invalid token' });
  }
};

// Authentication Routes

// POST /api/auth/login - Login user
app.post('/api/auth/login', async (req, res) => {
  try {
    const { username, password } = req.body;
    
    if (!username || !password) {
      return res.status(400).json({ error: 'Username and password are required' });
    }

    const authData = await readAuthData();
    const user = authData.users[username];
    
    if (!user || user.password !== password) {
      return res.status(401).json({ error: 'Invalid username or password' });
    }

    // Generate session token
    const token = generateToken();
    const expiresAt = Date.now() + (24 * 60 * 60 * 1000); // 24 hours

    authData.sessions[token] = {
      user: {
        username: user.username,
        role: user.role
      },
      expiresAt
    };

    await writeAuthData(authData);

    res.json({
      token,
      user: {
        username: user.username,
        role: user.role
      },
      expiresAt
    });
  } catch (error) {
    res.status(500).json({ error: 'Login failed' });
  }
});

// POST /api/auth/logout - Logout user
app.post('/api/auth/logout', async (req, res) => {
  try {
    const authHeader = req.headers['authorization'];
    const token = authHeader && authHeader.split(' ')[1];

    if (token) {
      const authData = await readAuthData();
      delete authData.sessions[token];
      await writeAuthData(authData);
    }

    res.json({ message: 'Logged out successfully' });
  } catch (error) {
    res.status(500).json({ error: 'Logout failed' });
  }
});

// GET /api/auth/verify - Verify token
app.get('/api/auth/verify', authenticateToken, (req, res) => {
  res.json({ user: req.user });
});

// Routes

// GET /api/users - Get all users
app.get('/api/users', authenticateToken, async (req, res) => {
  try {
    const users = await readData();
    res.json(users);
  } catch (error) {
    res.status(500).json({ error: 'Failed to fetch users' });
  }
});

// GET /api/users/:id - Get user by ID
app.get('/api/users/:id', authenticateToken, async (req, res) => {
  try {
    const users = await readData();
    const user = users.find(u => u.id === req.params.id);
    
    if (!user) {
      return res.status(404).json({ error: 'User not found' });
    }
    
    res.json(user);
  } catch (error) {
    res.status(500).json({ error: 'Failed to fetch user' });
  }
});

// POST /api/users - Create new user
app.post('/api/users', authenticateToken, async (req, res) => {
  try {
    const { name, email, age } = req.body;
    
    // Basic validation
    if (!name || !email) {
      return res.status(400).json({ error: 'Name and email are required' });
    }
    
    // Email validation - check for @ symbol and proper format
    if (!isValidEmail(email)) {
      return res.status(400).json({ error: 'Email must contain @ symbol and be in valid format' });
    }
    
    // Age validation - optional but must be valid if provided
    if (!isValidAge(age)) {
      return res.status(400).json({ error: 'Age must be a valid number between 0 and 150, or omitted entirely' });
    }
    
    const users = await readData();
    
    // Check if email already exists
    const existingUser = users.find(u => u.email === email);
    if (existingUser) {
      return res.status(400).json({ error: 'Email already exists' });
    }
    
    const newUser = {
      id: generateId(),
      name,
      email,
      age: age === undefined ? undefined : Number(age),
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString()
    };
    
    users.push(newUser);
    await writeData(users);
    
    res.status(201).json(newUser);
  } catch (error) {
    res.status(500).json({ error: 'Failed to create user' });
  }
});

// PUT /api/users/:id - Update user
app.put('/api/users/:id', authenticateToken, async (req, res) => {
  try {
    const { name, email, age } = req.body;
    const users = await readData();
    const userIndex = users.findIndex(u => u.id === req.params.id);
    
    if (userIndex === -1) {
      return res.status(404).json({ error: 'User not found' });
    }
    
    // Basic validation
    if (!name || !email) {
      return res.status(400).json({ error: 'Name and email are required' });
    }
    
    // Email validation - check for @ symbol and proper format
    if (!isValidEmail(email)) {
      return res.status(400).json({ error: 'Email must contain @ symbol and be in valid format' });
    }
    
    // Age validation - optional but must be valid if provided
    if (!isValidAge(age)) {
      return res.status(400).json({ error: 'Age must be a valid number between 0 and 150, or omitted entirely' });
    }
    
    // Check if email already exists (excluding current user)
    const existingUser = users.find(u => u.email === email && u.id !== req.params.id);
    if (existingUser) {
      return res.status(400).json({ error: 'Email already exists' });
    }
    
    users[userIndex] = {
      ...users[userIndex],
      name,
      email,
      age: age === undefined ? undefined : Number(age),
      updatedAt: new Date().toISOString()
    };
    
    await writeData(users);
    res.json(users[userIndex]);
  } catch (error) {
    res.status(500).json({ error: 'Failed to update user' });
  }
});

// DELETE /api/users/:id - Delete user
app.delete('/api/users/:id', authenticateToken, async (req, res) => {
  try {
    const users = await readData();
    const userIndex = users.findIndex(u => u.id === req.params.id);
    
    if (userIndex === -1) {
      return res.status(404).json({ error: 'User not found' });
    }
    
    const deletedUser = users[userIndex];
    users.splice(userIndex, 1);
    await writeData(users);
    
    res.json({ message: 'User deleted successfully', user: deletedUser });
  } catch (error) {
    res.status(500).json({ error: 'Failed to delete user' });
  }
});

// Health check endpoint
app.get('/api/health', (req, res) => {
  res.json({ status: 'OK', timestamp: new Date().toISOString() });
});

// Error handling middleware
app.use((err, req, res, next) => {
  console.error(err.stack);
  res.status(500).json({ error: 'Something went wrong!' });
});

// Start server
app.listen(PORT, () => {
  console.log(`Server running on port ${PORT}`);
  console.log(`Health check: http://localhost:${PORT}/api/health`);
});