package com.project.dev.code_sports_rui_and_fei.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

/**
 * 生产环境数据库配置
 * 自动将 Railway 的 DATABASE_URL 转换为 JDBC URL
 */
@Configuration
public class DataSourceConfig {

    @Bean
    @Primary
    public DataSource dataSource() {
        HikariDataSource ds = new HikariDataSource();
        ds.setMaximumPoolSize(10);
        ds.setConnectionTimeout(30000);
        ds.setDriverClassName("org.postgresql.Driver");

        // Railway 提供 DATABASE_URL: postgresql://user:pass@host:port/db
        String databaseUrl = System.getenv("DATABASE_URL");
        if (databaseUrl != null && !databaseUrl.isEmpty()) {
            // 转换为 JDBC URL: jdbc:postgresql://host:port/db
            String jdbcUrl = databaseUrl
                    .replaceFirst("^postgresql://", "jdbc:postgresql://")
                    .replaceFirst("://[^@]+@", "://");
            ds.setJdbcUrl(jdbcUrl);

            // 从 URL 中提取用户名密码
            String userInfo = databaseUrl
                    .replaceFirst("^postgresql://", "")
                    .replaceFirst("@.*", "");
            if (userInfo.contains(":")) {
                String[] parts = userInfo.split(":", 2);
                ds.setUsername(parts[0]);
                ds.setPassword(parts[1]);
            }
        } else {
            // 使用 PG* 单独变量
            String pgHost = System.getenv("PGHOST");
            String pgPort = System.getenv("PGPORT");
            String pgDb = System.getenv("PGDATABASE");

            if (pgHost != null) {
                ds.setJdbcUrl(String.format("jdbc:postgresql://%s:%s/%s",
                        pgHost, pgPort != null ? pgPort : "5432",
                        pgDb != null ? pgDb : "fitness"));
                String pgUser = System.getenv("PGUSER");
                String pgPass = System.getenv("PGPASSWORD");
                if (pgUser != null) ds.setUsername(pgUser);
                if (pgPass != null) ds.setPassword(pgPass);
            }
        }

        return ds;
    }
}
