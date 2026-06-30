# ==========================================
# Stage 1: Build Phase
# ==========================================
FROM node:20-alpine AS build

# Set working directory inside container
WORKDIR /app

# Copy package configurations
COPY package*.json ./

# Install npm dependencies. Leverages Docker layer caching.
RUN npm install

# Copy frontend source files
COPY . .

# Run compilation build command. Generates production-ready 'dist/' assets.
RUN npm run build

# ==========================================
# Stage 2: Serving Phase
# ==========================================
FROM nginx:1.25-alpine AS runtime

# Remove the default Nginx web site configuration
RUN rm /etc/nginx/conf.d/default.conf

# Copy our custom Nginx reverse proxy configuration
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Copy built frontend assets from builder stage
COPY --from=build /app/dist /usr/share/nginx/html

# Expose port 80 (HTTP)
EXPOSE 80

# Run nginx in foreground so docker can track the process
CMD ["nginx", "-g", "daemon off;"]
