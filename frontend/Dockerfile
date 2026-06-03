# syntax=docker/dockerfile:1
# =============================================================================
# ResumAIner Frontend — Multi-stage Docker Build
# =============================================================================
# Stage 1: Build Vue SPA with Node.js
# Stage 2: Serve built assets with Nginx (alpine, minimal footprint)
# =============================================================================

ARG NODE_VERSION=22-alpine
ARG NGINX_VERSION=1.27-alpine

# ---------- Stage 1: Build ----------
FROM node:${NODE_VERSION} AS build

WORKDIR /app

# Cache npm dependencies across builds
COPY package.json package-lock.json ./
RUN npm ci

# Copy source and build
COPY . .
RUN npm run build

# ---------- Stage 2: Runtime ----------
FROM nginx:${NGINX_VERSION}

# Copy built assets from build stage
COPY --from=build /app/dist /usr/share/nginx/html

# Copy custom nginx config (for SPA routing and API proxy)
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
