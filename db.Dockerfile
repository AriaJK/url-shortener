FROM mysql:8
LABEL maintainer="ahmad.zeeshaan@gmail.com"

ENV MYSQL_DATABASE=urldb \
    MYSQL_ROOT_PASSWORD=gg20050105

COPY schema.sql /docker-entrypoint-initdb.d

EXPOSE 3306