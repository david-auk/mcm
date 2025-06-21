# Stage 1: Base
FROM node:20 AS base
WORKDIR /app
COPY package*.json ./
COPY . .
RUN npm install

# Development Stage 2 (Serve stage, with live building)
FROM base AS development
CMD ["npm", "run", "dev"]

# Production Stage 2: (Build stage)
FROM base AS builder
RUN npm run build

# Production Stage 3: (Serve stage)
FROM nginx:alpine AS production

# Add custom nginx config
COPY nginx.conf /etc/nginx/nginx.conf

# Copy built frontend
COPY --from=builder /app/dist /usr/share/nginx/html

CMD ["nginx", "-g", "daemon off;"]