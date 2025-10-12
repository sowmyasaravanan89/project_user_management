# Use an official Node image as the base
FROM node:18-alpine

# Create app directory
WORKDIR /app

# Copy package.json and package-lock.json (if present)
COPY backend/package.json .

# Install dependencies
RUN npm install

# Copy source code
COPY backend .

# Expose the backend port (adjust if your app uses a different port)
EXPOSE 5000

# Start the application
CMD ["node", "server.js"]
