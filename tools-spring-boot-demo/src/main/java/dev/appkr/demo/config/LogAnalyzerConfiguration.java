package dev.appkr.demo.config;

import dev.appkr.demo.application.FilterableLogAnalyzer;
import dev.appkr.demo.application.PersistableLogAnalyzer;
import dev.appkr.demo.application.SlowQueryLogAnalyzer;
import dev.appkr.demo.application.port.in.LogAnalyzer;
import dev.appkr.demo.domain.ExplainVisitor;
import dev.appkr.demo.application.port.out.AnalysisReportJpaRepository;
import jakarta.persistence.EntityManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class LogAnalyzerConfiguration {

  @Bean
  @Primary
  @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
  public FilterableLogAnalyzer logAnalyzer(EntityManager em, AnalysisReportJpaRepository repository) {
    final LogAnalyzer innerAnalyzer = new SlowQueryLogAnalyzer(new ExplainVisitor(em));
    final LogAnalyzer outerAnalyzer = new PersistableLogAnalyzer(innerAnalyzer, repository);
    return new FilterableLogAnalyzer(outerAnalyzer);
  }

  @Bean
  @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
  public FilterableLogAnalyzer logAnalyzerWithoutPersistence(EntityManager em) {
    final LogAnalyzer innerAnalyzer = new SlowQueryLogAnalyzer(new ExplainVisitor(em));
    return new FilterableLogAnalyzer(innerAnalyzer);
  }
}
