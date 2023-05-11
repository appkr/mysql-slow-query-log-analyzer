package dev.appkr.tools.config;

import dev.appkr.tools.application.CacheableLogAnalyzer;
import dev.appkr.tools.application.FilterableLogAnalyzer;
import dev.appkr.tools.application.SlowQueryLogAnalyzer;
import dev.appkr.tools.application.port.out.AnalysisReportJpaRepository;
import dev.appkr.tools.domain.ExplainVisitor;
import jakarta.persistence.EntityManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LogAnalyzerConfiguration {

  @Bean
  @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
  public FilterableLogAnalyzer logAnalyzer(EntityManager em, AnalysisReportJpaRepository repository) {
    final SlowQueryLogAnalyzer innerAnalyzer = new SlowQueryLogAnalyzer(new ExplainVisitor(em));
    final CacheableLogAnalyzer outerAnalyzer = new CacheableLogAnalyzer(innerAnalyzer, repository);
    return new FilterableLogAnalyzer(outerAnalyzer);
  }
}
