package dev.appkr.tools.core;

import dev.appkr.tools.core.model.AnalysisReport;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import org.springframework.util.DigestUtils;

public class CacheableLogAnalyzer implements LogAnalyzer {

  final LogAnalyzer delegate;
  final AnalysisReportRepository repository;

  public CacheableLogAnalyzer(LogAnalyzer delegate, AnalysisReportRepository repository) {
    this.delegate = delegate;
    this.repository = repository;
  }

  public AnalysisReport analyze(Path path) throws IOException {
    final String md5hash = generateMd5HashFrom(path);
    final Optional<AnalysisReport> reportOptional = repository.findById(md5hash);

    AnalysisReport report = null;
    if (reportOptional.isPresent()) {
      report = reportOptional.get();
    } else {
      report = delegate.analyze(path);
      report.setId(md5hash);
      repository.save(report);
    }

    return report;
  }

  String generateMd5HashFrom(Path path) {
    if (path == null) {
      return null;
    }

    try {
      return DigestUtils.md5DigestAsHex(Files.readAllBytes(path));
    } catch (IOException e) {
      return null;
    }
  }
}
