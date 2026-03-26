FROM nginx:1.27-alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY css/ /usr/share/nginx/html/css/
COPY . /usr/share/nginx/html/
EXPOSE 80
