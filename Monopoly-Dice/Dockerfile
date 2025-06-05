FROM node:lts-alpine3.22
LABEL authors="vtu"

WORKDIR /build
COPY package*.json ./
RUN npm ci --omit=dev && npm cache clean --force

COPY . .

EXPOSE 3000
CMD ["node", "src/server/server.js"]
