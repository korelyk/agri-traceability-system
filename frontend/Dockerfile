FROM node:22-alpine AS build
WORKDIR /app

COPY package*.json ./
RUN npm ci

COPY . .

ARG VUE_APP_API_BASE_URL=/api
ENV VUE_APP_API_BASE_URL=${VUE_APP_API_BASE_URL}
ARG VUE_APP_PUBLIC_BASE_URL=https://bishe.yyy999.my
ENV VUE_APP_PUBLIC_BASE_URL=${VUE_APP_PUBLIC_BASE_URL}

RUN npm run build

FROM nginx:1.27-alpine
COPY deploy/nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist /usr/share/nginx/html

EXPOSE 80
