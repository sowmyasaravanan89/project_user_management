# Stage 1: build the React app
FROM node:18-alpine AS builder

WORKDIR /app

COPY frontend/package.json ./
RUN npm install

COPY frontend .
RUN npm run build

# Stage 2: serve with nginx (or you could use a lightweight server)
FROM nginx:stable-alpine

# Copy built assets from builder
COPY --from=builder /app/build /usr/share/nginx/html

# Optionally, if you have a custom nginx.conf:
# COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
