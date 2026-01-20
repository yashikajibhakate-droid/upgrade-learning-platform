# Build Stage
FROM node:18-alpine AS builder
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
# Accept API URL as a build argument (default to /api if not provided)
ARG VITE_API_URL
ENV VITE_API_URL=${VITE_API_URL}
RUN npm run build

# Serve Stage
FROM nginx:alpine
COPY --from=builder /app/dist /usr/share/nginx/html
# Use Nginx template feature for auto-env-substitution (Requires .template extension)
COPY nginx.conf /etc/nginx/templates/default.conf.template
EXPOSE 80
# Default Nginx entrypoint handles the templating and startup automatically
