FROM node:current-alpine AS build-frontend
LABEL maintainer Ascensio System SIA <support@onlyoffice.com>
ARG BACKEND_URL
ENV BACKEND_URL=$BASE_URL
WORKDIR /usr/src/app
COPY ./package*.json ./
COPY ./onlyoffice-docspace-react*.tgz ./
RUN npm install
COPY . .
RUN npm run build

FROM nginx:alpine AS frontend
COPY --from=build-frontend \
    /usr/src/app/build \
    /usr/share/nginx/html
COPY --from=build-frontend \
    /usr/src/app/nginx/nginx.conf \
    /etc/nginx/conf.d/default.conf
EXPOSE 80