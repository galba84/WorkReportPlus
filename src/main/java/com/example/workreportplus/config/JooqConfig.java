package com.example.workreportplus.config;

import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import org.jooq.impl.DefaultConfiguration;
import org.jooq.impl.DefaultRecordMapperProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;

public class JooqConfig {

    @Bean
    public DSLContext dslContext(DataSource dataSource, Environment env) {
        DefaultConfiguration configuration = new DefaultConfiguration();
        configuration.setDataSource(dataSource);

        // Use the default constructor instead
        configuration.set(new DefaultRecordMapperProvider());

        return DSL.using(configuration);
    }
}
