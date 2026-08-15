#!/bin/sh

# Railway 提供 DATABASE_URL 格式: postgresql://user:pass@host:port/db
# Spring Boot 需要: jdbc:postgresql://host:port/db
# 此脚本自动转换

if [ -n "$DATABASE_URL" ]; then
    # 从 DATABASE_URL 提取 host:port/db 部分
    DB_HOST_PORT_DB=$(echo "$DATABASE_URL" | sed -E 's|postgresql://[^@]+@||')
    export JDBC_DATABASE_URL="jdbc:postgresql://${DB_HOST_PORT_DB}"
    echo "JDBC_DATABASE_URL set from DATABASE_URL: $JDBC_DATABASE_URL"
fi

echo "Starting application on port ${PORT:-8080}..."
exec java -jar app.jar --spring.profiles.active=prod
