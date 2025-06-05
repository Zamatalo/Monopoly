FROM node:lts-alpine3.22
LABEL authors="vtu"

WORKDIR /app

COPY package.json package-lock.json ./

RUN npm ci --omit=dev && \
    npm cache clean --force

COPY . .

RUN npm install

#FROM nginx:alpine
#COPY --from=0 /app/dist /usr/share/nginx/html
#COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 5173
CMD ["npm", "run", "dev"]