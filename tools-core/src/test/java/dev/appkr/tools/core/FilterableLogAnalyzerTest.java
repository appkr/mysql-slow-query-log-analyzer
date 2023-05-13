package dev.appkr.tools.core;

import dev.appkr.tools.core.model.AnalysisReport;
import dev.appkr.tools.core.model.Fixtures;
import dev.appkr.tools.core.model.SlowQueryLog;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.assertj.core.api.Assertions;
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
    Assertions.assertThat(filteredReport.getLogEntries().size()).isEqualTo(2);
  }

  @Test
  void filter1KMillis_thenExpectOneEntry() throws IOException {
    final AnalysisReport filteredReport = sut.analyze(PATH, 1000, null, null);
    Assertions.assertThat(filteredReport.getLogEntries().size()).isEqualTo(1);
  }

  @Test
  void sortByQueryTime_thenExpectFirstItemQueryIdIs2() throws IOException {
    final AnalysisReport filteredReport = sut.analyze(PATH, null, null, Sort.by("queryTime"));
    Assertions.assertThat(filteredReport.getLogEntries().get(0).getId()).isEqualTo("2");
  }

  @BeforeEach
  void setup() {
    final SlowQueryLogAnalyzer innerAnalyzer = Mockito.mock(SlowQueryLogAnalyzer.class);
    final AnalysisReportRepository mockRepository = Mockito.mock(AnalysisReportRepository.class);

    sut = new FilterableLogAnalyzer(new MockAnalyzer(innerAnalyzer, mockRepository));
  }

  static class MockAnalyzer extends CacheableLogAnalyzer {

    public MockAnalyzer(SlowQueryLogAnalyzer delegate, AnalysisReportRepository repository) {
      super(delegate, repository);
    }

    @Override
    public AnalysisReport analyze(Path path) throws IOException {
      return new AnalysisReport(
          List.of(new SlowQueryLog(Fixtures.aLogString1()), new SlowQueryLog(Fixtures.aLogString2())));
    }
  }
}
