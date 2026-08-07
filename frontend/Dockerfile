# ---- Build stage ----
FROM node:20-alpine AS builder

WORKDIR /app

# Accept the API base URL as a build-time argument
ARG VITE_API_BASE_URL=http://localhost:8080/api/v1

COPY package.json package-lock.json ./
RUN npm ci --ignore-scripts

COPY . .
RUN npm run build

# ---- Production stage ----
FROM nginx:1.27-alpine

# Copy custom nginx config
COPY --from=builder /app/dist /usr/share/nginx/html

# Escuta em ${PORT} — em vez de porta fixa, para funcionar tanto em docker-compose
# local (PORT não definida, cai no default 80) quanto em PaaS como o Railway, que
# injeta PORT dinamicamente e espera o container escutar nela. O template abaixo é
# processado pelo entrypoint oficial da imagem nginx (envsubst automático em
# /etc/nginx/templates/*.template -> /etc/nginx/conf.d/*.conf) na hora que o
# container sobe, não no build — por isso PORT pode variar por ambiente sem rebuild.
ENV PORT=80
RUN mkdir -p /etc/nginx/templates && printf 'server {\n\
    listen ${PORT};\n\
    server_name _;\n\
    root /usr/share/nginx/html;\n\
    index index.html;\n\
\n\
    location / {\n\
        try_files $uri $uri/ /index.html;\n\
    }\n\
\n\
    # Cache static assets\n\
    location ~* \\.(js|css|png|jpg|jpeg|gif|ico|svg|woff2?)$ {\n\
        expires 1y;\n\
        add_header Cache-Control "public, immutable";\n\
    }\n\
\n\
    # Security headers\n\
    add_header X-Frame-Options "SAMEORIGIN" always;\n\
    add_header X-Content-Type-Options "nosniff" always;\n\
    add_header Referrer-Policy "strict-origin-when-cross-origin" always;\n\
}\n' > /etc/nginx/templates/default.conf.template

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
