FROM node:lts-bullseye AS builder
LABEL authors="vtu"

WORKDIR /app

COPY package.json package-lock.json ./
RUN rm -rf node_modules package-lock.json && npm install && npm cache clean --force

COPY . .
RUN npm run build

FROM nginx:stable-perl
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=builder /app/dist /usr/share/nginx/html

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]