# User Management Application

A full-stack user management application with testing suite, featuring a React frontend and Node.js backend with authentication capabilities.

## 🏗️ Application Overview

The User Management Application consists of:
- **Frontend**: React-based user interface
- **Backend**: Node.js API with authentication
- **Storage**: JSON file-based data persistence
- **Testing**: Comprehensive UI and API test automation

> **Note**: This application was created by Claude (AI tool) for testing purposes as i didnt manage to find suitable application.

## 🧪 Testing Scope

### UI Testing
- Admin login functionality
- User creation, editing, and deletion capabilities

### API Testing
- User retrieval, creation, updating, and deletion through API endpoints

## 📋 Test Coverage Areas

### Frontend
- **Functional Testing**: Core application functionality
- **Security Testing**: Authentication 

### Backend
- **Functional Testing**: API endpoint functionality
- **Schema Testing**: Data structure validation
- **Security Testing**: Authentication and authorization 

## 🛠️ Testing Tools

| Tool | Purpose | Reason |
|------|---------|-----------|
| **Playwright** | UI Testing | Fast execution, cross-browser support, reliable auto-waiting features, easy development |
| **REST Assured** | API Testing | Robust framework with excellent Java integration, extensive experience |
| **Postman** | Manual Testing | Quick CRUD operation validation |

## 🚀 Setup and Installation

### Prerequisites
- Java 8 or higher
- Maven
- Node.js (latest version)
- Dependencies as specified in `pom.xml`

### Project Structure
```
project_user_management/
├── backend/           # Server files (server.js, package.json)
├── frontend/          # React app (Login.js, UserApp.js)
└── tests/
    ├── ui/            # Playwright tests and configuration
    └── api/           # REST Assured tests and utilities
```

### Backend Setup
```bash
# Navigate to backend directory
cd backend

# Install dependencies
npm install

# Start development server
npm run dev
```
Server will be available at: `http://localhost:5000`

### Frontend Setup
```bash
# Navigate to frontend directory
cd frontend

# Install dependencies
npm install

# Start application
npm start
```
Application will be available at: `http://localhost:3000`

## 🧪 Running Tests

### Execute All Tests
```bash
# Navigate to tests directory
cd tests

# Run complete test suite
mvn clean test
```

### Execute Specific Test Class
```bash
# Run specific test class
mvn test -Dtest="ClassName"

# Example
mvn test -Dtest=TestGetUser.java
```

## 📊 Reporting and Documentation

### Allure Reports
Test results are automatically generated and stored in `tests/allure-results`.

To view reports:
```bash
allure serve tests/allure-results
```

### Screenshots
Playwright automatically captures screenshots after each action, saved to `targets/screenshots/` for debugging and documentation.

## ⚠️ Known Limitations

### Test Coverage Constraints
Due to time limitations:
- **Frontend**: Only positive scenarios covered
- **Backend**:  Positive and few negative scenarios implemented

### Validation Gaps
Current implementation lacks field-level validation:
- Email field doesn't enforce proper format validation (missing "@" and "." requirements)

### Data Storage
JSON file-based storage was chosen over database implementation due to time constraints.

### CI/CD Pipeline
GitHub Actions CI pipeline was not implemented due to time constraints, though the framework supports single-command test execution.

## 🎯 Future Enhancements

- [ ] Implement comprehensive input validation
- [ ] Add database integration
- [ ] Set up CI/CD pipeline with GitHub Actions
- [ ] Expand test coverage for edge cases
- [ ] Add performance testing
- [ ] Implement comprehensive error handling

## 📝 Usage

1. Start both backend and frontend servers
2. Navigate to `http://localhost:3000`
3. Use admin credentials to log in
4. Perform user management operations (create, read, update, delete)
5. Run test suite to validate functionality