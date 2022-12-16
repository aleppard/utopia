FROM postgres:alpine3.17
COPY database/create.sql /docker-entrypoint-initdb.d/
