# Development Dockerfile for Angular
FROM node:20-alpine

WORKDIR /app

# Copy package files
COPY package*.json ./

# Install dependencies
RUN npm ci

# Copy application files
COPY . .

# Expose port
EXPOSE 4200

# Start development server with host binding for Docker
CMD ["npm", "start", "--", "--host", "0.0.0.0", "--poll", "2000"]
