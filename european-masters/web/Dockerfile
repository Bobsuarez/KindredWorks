# ---- Stage 1: Build ----
FROM node:22-alpine AS builder

WORKDIR /app

COPY package*.json ./
RUN npm ci

ARG PUBLIC_API_URL
ARG PUBLIC_SITE_NAME
ENV PUBLIC_API_URL=$PUBLIC_API_URL
ENV PUBLIC_SITE_NAME=$PUBLIC_SITE_NAME

COPY . .
RUN npm run build

# ---- Stage 2: Serve con Nginx ----
FROM nginx:alpine AS dist

COPY --from=builder /app/dist /usr/share/nginx/html

# Config opcional pero recomendada para SPAs/Astro static
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80