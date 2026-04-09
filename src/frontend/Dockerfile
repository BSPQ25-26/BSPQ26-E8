FROM nginx:1.27-alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY css/ /usr/share/nginx/html/css/
COPY . /usr/share/nginx/html/
EXPOSE 80

CMD /bin/sh -c "find /usr/share/nginx/html/js -type f -name '*.js' -exec sed -i \"s|http://localhost:8080|\${API_BASE:-http://localhost:8080}|g\" {} + && nginx -g 'daemon off;'"
