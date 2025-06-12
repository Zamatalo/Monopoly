FROM node:lts-alpine3.22
LABEL authors="vtu"

WORKDIR /build

COPY package.json ./
RUN rm -rf node_modules package-lock.json && npm install && npm cache clean --force

COPY . .

EXPOSE 3000
CMD ["node", "src/server/server.js"]
