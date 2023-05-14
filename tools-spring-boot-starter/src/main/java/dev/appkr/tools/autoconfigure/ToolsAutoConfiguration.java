package dev.appkr.tools.autoconfigure;

import dev.appkr.tools.core.*;
import dev.appkr.tools.core.model.ExplainVisitor;
import jakarta.persistence.EntityManager;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class ToolsAutoConfiguration {

  @Bean
  @ConditionalOnMissingBean
  public AnalysisReportRepository defaultAnalysisReportRepository() {
    return new AnalysisReportInMemoryRepository();
  }

  @Bean
  @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
  public FilterableLogAnalyzer defaultLogAnalyzer(EntityManager em, AnalysisReportRepository repository) {
    final LogAnalyzer innerAnalyzer = new SlowQueryLogAnalyzer(new ExplainVisitor(em));
    final LogAnalyzer outerAnalyzer = new CacheableLogAnalyzer(innerAnalyzer, repository);
    return new FilterableLogAnalyzer(outerAnalyzer);
  }
}
