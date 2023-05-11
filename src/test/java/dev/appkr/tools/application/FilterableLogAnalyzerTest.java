package dev.appkr.tools.application;

import static org.assertj.core.api.Assertions.assertThat;

import dev.appkr.tools.application.port.out.AnalysisReportJpaRepository;
import dev.appkr.tools.domain.AnalysisReport;
import dev.appkr.tools.domain.SlowQueryLog;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.Sort;

class FilterableLogAnalyzerTest {

  static final Path PATH = Paths.get("");
  FilterableLogAnalyzer sut;

  @Test
  void noFilter() throws IOException {
    final AnalysisReport filteredReport = sut.analyze(PATH, null, null, null);
    assertThat(filteredReport.getLogEntries().size()).isEqualTo(2);
  }

  @Test
  void filter1KMillis_thenExpectOneEntry() throws IOException {
    final AnalysisReport filteredReport = sut.analyze(PATH, 1000, null, null);
    assertThat(filteredReport.getLogEntries().size()).isEqualTo(1);
  }

  @Test
  void sortByQueryTime_thenExpectFirstItemQueryIdIs2() throws IOException {
    final AnalysisReport filteredReport = sut.analyze(PATH, null, null, Sort.by("queryTime"));
    assertThat(filteredReport.getLogEntries().get(0).getLogEntry().getId()).isEqualTo("2");
  }

  @BeforeEach
  void setup() {
    final SlowQueryLogAnalyzer innerAnalyzer = Mockito.mock(SlowQueryLogAnalyzer.class);
    final AnalysisReportJpaRepository mockRepository = Mockito.mock(AnalysisReportJpaRepository.class);

    sut = new FilterableLogAnalyzer(new MockAnalyzer(innerAnalyzer, mockRepository));
  }

  static class MockAnalyzer extends PersistableLogAnalyzer {

    public MockAnalyzer(SlowQueryLogAnalyzer delegate, AnalysisReportJpaRepository repository) {
      super(delegate, repository);
    }

    @Override
    public AnalysisReport analyze(Path path) throws IOException {
      final String logString1 = """
# Time: 2023-01-01T00:00:00.000000Z
# User@Host: root[root] @  [172.18.0.1]  Id:  1
# Query_time: 2.000000  Lock_time: 0.100000 Rows_sent: 5  Rows_examined: 10
use local_tools;
SELECT 1 FROM DUAL;
        """;
      final String logString2 = """
# Time: 2023-01-01T00:00:00.000001Z
# User@Host: root[root] @  [172.18.0.1]  Id:  2
# Query_time: 1.000000  Lock_time: 0.200000 Rows_sent: 10  Rows_examined: 20
use local_tools;
SELECT 1 FROM DUAL;
        """;

      return new AnalysisReport(List.of(new SlowQueryLog(logString1), new SlowQueryLog(logString2)));
    }
  }
}
