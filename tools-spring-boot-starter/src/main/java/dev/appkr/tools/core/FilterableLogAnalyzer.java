package dev.appkr.tools.core;

import static dev.appkr.tools.core.model.AnalysisReport.getFilterSpec;
import static dev.appkr.tools.core.model.AnalysisReport.getSortSpec;

import dev.appkr.tools.core.model.AnalysisReport;
import dev.appkr.tools.core.model.FingerprintVisitor;
import dev.appkr.tools.core.model.LogFilter;
import dev.appkr.tools.core.model.SlowQueryLog;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class FilterableLogAnalyzer {

  final LogAnalyzer delegate;
  final FingerprintVisitor visitor;

  public FilterableLogAnalyzer(LogAnalyzer delegate, FingerprintVisitor visitor) {
    this.delegate = delegate;
    this.visitor = visitor;
  }

  public AnalysisReport analyze(Path path, LogFilter filter) throws IOException {
    AnalysisReport report = delegate.analyze(path);
    if (report != null && filter != null) {
      final List<SlowQueryLog> filteredLogEntries = report.getLogEntries().stream()
          .filter(getFilterSpec(filter.getSlowerThanMillis(), filter.getQueryType()))
          .sorted(getSortSpec(filter.getSort()))
          .toList();

      report = new AnalysisReport(report.getId(), report.getServerInfo(), filteredLogEntries);
      report.collectFingerprint(visitor);
    }

    return report;
  }
}
