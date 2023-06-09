package dev.appkr.tools.demo.config.scheduler;

import java.util.concurrent.Executor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.task.TaskExecutionProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
@EnableScheduling
@ConditionalOnProperty(name = "application.scheduler.enabled", havingValue = "true", matchIfMissing = false)
public class SchedulerConfiguration {

  private final TaskExecutionProperties properties;

  public SchedulerConfiguration(TaskExecutionProperties properties) {
    this.properties = properties;
  }

  @Bean
  public Executor taskExecutor() {
    final TaskExecutionProperties.Pool config = properties.getPool();

    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(config.getCoreSize());
    executor.setMaxPoolSize(config.getMaxSize());
    executor.setQueueCapacity(config.getQueueCapacity());
    executor.setThreadNamePrefix("scheduler-");
    executor.setWaitForTasksToCompleteOnShutdown(true);
    executor.setAwaitTerminationSeconds(10);
    executor.setTaskDecorator(new AsyncTaskDecorator());

    return executor;
  }
}
