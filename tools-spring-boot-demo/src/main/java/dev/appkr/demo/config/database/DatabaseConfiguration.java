package dev.appkr.demo.config.database;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = {"dev.appkr.demo.port.out", "dev.appkr.tools"})
public class DatabaseConfiguration {
}
