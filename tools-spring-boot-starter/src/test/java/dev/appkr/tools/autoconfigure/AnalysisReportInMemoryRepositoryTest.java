package dev.appkr.tools.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;

import dev.appkr.tools.core.model.AnalysisReport;
import dev.appkr.tools.core.model.Fixtures;
import dev.appkr.tools.core.model.SlowQueryLog;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AnalysisReportInMemoryRepositoryTest {

  final String ID = "foo";
  AnalysisReportInMemoryRepository sut;

  @Test
  void findById() {
    final Optional<AnalysisReport> reportOptional = sut.findById(ID);

    assertThat(reportOptional.isPresent()).isTrue();
    assertThat(reportOptional.get().getId()).isEqualTo(ID);
  }

  @Test
  void findAll() {
    final List<AnalysisReport> all = sut.findAll();

    assertThat(all.isEmpty()).isFalse();
    assertThat(all.size()).isEqualTo(1);
  }

  @BeforeEach
  void setup() {
    sut = new AnalysisReportInMemoryRepository();
    final List<SlowQueryLog> logEntries = List.of(new SlowQueryLog(Fixtures.aLogString1()), new SlowQueryLog(Fixtures.aLogString2()));
    sut.save(new AnalysisReport(ID, null, logEntries));
  }
}
