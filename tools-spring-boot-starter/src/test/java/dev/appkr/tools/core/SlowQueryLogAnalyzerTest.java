package dev.appkr.tools.core;

import dev.appkr.tools.core.model.AnalysisReport;
import dev.appkr.tools.core.model.ExecutionPlanVisitor;
import dev.appkr.tools.core.model.Fixtures;
import dev.appkr.tools.core.model.SlowQueryLog;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.lang.reflect.Method;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.ReflectionUtils;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

@Slf4j
class SlowQueryLogAnalyzerTest {

  SlowQueryLogAnalyzer sut;

  @Test
  void analyze() {
    final Method method = ReflectionUtils.findMethod(sut.getClass(), "analyze", String.class).get();
    final AnalysisReport result = (AnalysisReport)ReflectionUtils.invokeMethod(method, sut, Fixtures.aLogString3());

    Assertions.assertThat(result).isNotNull();
    Assertions.assertThat(result.getLogEntries()).isNotEmpty();
    final SlowQueryLog firstEntry = result.getLogEntries().iterator().next();
    Assertions.assertThat(firstEntry).isNotNull();
    Assertions.assertThat(firstEntry.getSql()).contains(List.of("SELECT", "FROM", "WHERE"));
    log.info("result: {}", result);
  }

  @BeforeEach
  void setup() {
    Query mockQuery = Mockito.mock(Query.class);
    Mockito.when(mockQuery.getResultList()).thenReturn(Fixtures.anExecutionPlan());

    EntityManager mockEntityManager = Mockito.mock(EntityManager.class);
    Mockito.when(mockEntityManager.createNativeQuery(ArgumentMatchers.anyString()))
        .thenReturn(mockQuery);

    sut = new SlowQueryLogAnalyzer(new ExecutionPlanVisitor(mockEntityManager));
  }
}
