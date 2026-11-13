package com.example.javaaiagentdev.config;

import javax.sql.DataSource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ReadOnlyDataSourceConfig {

    @Bean
    @ConditionalOnExpression("'${app.readonly-datasource.url:}' != ''")
    public DataSource readOnlyDataSource(ReadOnlyDataSourceProperties properties) {
        return DataSourceBuilder.create()
                .url(properties.url())
                .username(properties.username())
                .password(properties.password())
                .build();
    }
}
