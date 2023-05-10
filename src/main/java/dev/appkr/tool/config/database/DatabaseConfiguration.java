package dev.appkr.tool.config.database;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = {"dev.appkr.tool.application.port.out"})
public class DatabaseConfiguration {
}
