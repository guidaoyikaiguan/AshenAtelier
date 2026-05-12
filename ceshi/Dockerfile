FROM node:18-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci --legacy-peer-deps
COPY . .
ARG VUE_APP_API_BASE_URL=
ENV VUE_APP_API_BASE_URL=${VUE_APP_API_BASE_URL}
ARG VUE_APP_QINIU_DOMAIN=http://tet31ocbs.hn-bkt.clouddn.com
ENV VUE_APP_QINIU_DOMAIN=${VUE_APP_QINIU_DOMAIN}
RUN npx vue-cli-service build

FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
