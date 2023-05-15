package dev.appkr.tools.core.model;

import static dev.appkr.tools.core.model.Fixtures.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;

import java.time.Duration;
import java.util.List;
import java.util.LongSummaryStatistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class AnalysisReportTest {

  AnalysisReport sut;

  @Test
  void summary() {
    final LongSummaryStatistics summary = sut.getSummary();

    assertThat(summary.getCount()).isEqualTo(3L);
    assertThat(summary.getMin()).isEqualTo(MIN_QUERY_TIME);
    assertThat(summary.getMax()).isEqualTo(MAX_QUERY_TIME);
    assertThat(summary.getSum()).isEqualTo(7_000L); // 1000 + 2000 + 4000
    assertThat(summary.getAverage()).isGreaterThan(2_333.0).isLessThan(2_334.0); // 7000 / 3 = 2333.3333...
  }

  @Test
  void collectFingerprint() {
    final FingerprintVisitor mockVisitor = Mockito.mock(FingerprintVisitor.class);

    // aLogString1() and aLogString2() are the same, but aLogString3() is different.
    // But for the purpose of testing, we assume that those are all same.
    Mockito
        .when(mockVisitor.visit(any(SlowQueryLog.class)))
        .thenReturn(aTuple1())
        .thenReturn(aTuple2())
        .thenReturn(aTuple3());

    sut.collectFingerprint(mockVisitor);

    final TokenizedQuery tokenizedQuery = sut.getTokenizedQueries().get(aTuple1().getKey());
    assertThat(tokenizedQuery).isNotNull();
    assertThat(tokenizedQuery.getCalls()).isEqualTo(3);
    assertThat(tokenizedQuery.getCumQueryTime()).isEqualTo(Duration.ofMillis(7_000L));
    assertThat(tokenizedQuery.getCumLockTime()).isEqualTo(Duration.ofMillis(305));
    assertThat(tokenizedQuery.getCumRowsExamined()).isEqualTo(50_030);
    assertThat(tokenizedQuery.getCumRowsSent()).isEqualTo(23);
    assertThat(tokenizedQuery.getMinTime()).isEqualTo(Duration.ofMillis(MIN_QUERY_TIME));
    assertThat(tokenizedQuery.getMaxTime()).isEqualTo(Duration.ofMillis(MAX_QUERY_TIME));
    assertThat(tokenizedQuery.getAvgTime()).isGreaterThan(Duration.ofMillis(2_333L))
        .isLessThan(Duration.ofMillis(2_334L));
    assertThat(tokenizedQuery.getP50Time()).isEqualTo(Duration.ofMillis(2_000L));
    assertThat(tokenizedQuery.getP95Time()).isEqualTo(Duration.ofMillis(MAX_QUERY_TIME));
  }

  @BeforeEach
  void setup() {
    final List<SlowQueryLog> logEntries = List.of(new SlowQueryLog(aLogString1()),
        new SlowQueryLog(aLogString2()), new SlowQueryLog(aLogString3()));
    sut = new AnalysisReport(logEntries);
  }
}
