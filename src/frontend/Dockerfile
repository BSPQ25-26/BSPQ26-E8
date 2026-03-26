FROM nginx:1.27-alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY index.html /usr/share/nginx/html/index.html
COPY template.html /usr/share/nginx/html/template.html
COPY css/ /usr/share/nginx/html/css/
EXPOSE 80
