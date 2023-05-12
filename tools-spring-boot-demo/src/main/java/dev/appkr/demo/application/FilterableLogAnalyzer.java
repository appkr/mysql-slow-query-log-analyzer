package dev.appkr.demo.application;

import dev.appkr.demo.application.port.in.LogAnalyzer;
import dev.appkr.demo.domain.AnalysisReport;
import dev.appkr.demo.domain.SlowQueryLog;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import org.springframework.data.domain.Sort;

public class FilterableLogAnalyzer {

  final LogAnalyzer delegate;

  public FilterableLogAnalyzer(LogAnalyzer delegate) {
    this.delegate = delegate;
  }

  public AnalysisReport analyze(Path path, Integer filterMillis, String filterQuery, Sort sort) throws IOException {
    AnalysisReport report = delegate.analyze(path);
    if (report != null) {
      final List<SlowQueryLog> filteredLogEntries = report.getLogEntries().stream()
          .filter(AnalysisReport.filterSpecBy(filterMillis, filterQuery))
          .sorted(AnalysisReport.sortSpecBy(sort))
          .toList();

      report = new AnalysisReport(report.getMd5hash(), filteredLogEntries);
    }

    return report;
  }
}
