# --- Build stage ---
FROM node:18-alpine AS builder
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci
COPY . .
RUN npm run build

# --- Serve stage ---
FROM nginx:stable-alpine

# SSL certs
RUN mkdir -p /etc/nginx/ssl
COPY localhost.pem     /etc/nginx/ssl/localhost.pem
COPY localhost-key.pem /etc/nginx/ssl/localhost-key.pem

# Remove default content
RUN rm -rf /usr/share/nginx/html/*

# Config
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Static assets from builder
COPY --from=builder /app/dist /usr/share/nginx/html

EXPOSE 5178
CMD ["nginx", "-g", "daemon off;"]
