package dev.appkr.tools.application;

import dev.appkr.tools.domain.AnalysisReport;
import dev.appkr.tools.domain.SlowQueryLog;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import org.springframework.data.domain.Sort;

public class FilterableLogAnalyzer {

  final CacheableLogAnalyzer delegate;

  public FilterableLogAnalyzer(CacheableLogAnalyzer delegate) {
    this.delegate = delegate;
  }

  public AnalysisReport analyze(String reportId, Path path, Integer filterMillis, String filterQuery, Sort sort)
      throws IOException {
    AnalysisReport report = delegate.analyze(reportId, path);
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
