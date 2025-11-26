//package com.obligatorio2025;
//
//import com.zaxxer.hikari.HikariConfig;
//import com.zaxxer.hikari.HikariDataSource;
//import jakarta.annotation.PreDestroy;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//import javax.sql.DataSource;
//
//@Configuration
//public class DataSourceConfig {
//
//    @Value("${app.datasource.url:jdbc:mysql://localhost:3306/obligatorio2025?useSSL=false&serverTimezone=UTC}")
//    private String jdbcUrl;
//
//    @Value("${app.datasource.username:root}")
//    private String username;
//
//    @Value("${app.datasource.password:}")
//    private String password;
//
//    @Value("${app.datasource.driver-class-name:com.mysql.cj.jdbc.Driver}")
//    private String driverClassName;
//
//    private HikariDataSource ds;
//
//    @Bean
//    @ConditionalOnMissingBean(DataSource.class)
//    public DataSource dataSource() {
//        HikariConfig cfg = new HikariConfig();
//        cfg.setJdbcUrl(jdbcUrl);
//        cfg.setUsername(username);
//        cfg.setPassword(password);
//        cfg.setDriverClassName(driverClassName);
//
//        // tuning mínimo
//        cfg.setMaximumPoolSize(5);
//        cfg.setMinimumIdle(1);
//        cfg.setPoolName("ObligatorioHikari");
//
//        ds = new HikariDataSource(cfg);
//
//        System.out.println("Using manual DataSource -> " + jdbcUrl); // confirma en logs
//        return ds;
//    }
//
//    @PreDestroy
//    public void shutdown() {
//        if (ds != null && !ds.isClosed()) ds.close();
//    }
//}


