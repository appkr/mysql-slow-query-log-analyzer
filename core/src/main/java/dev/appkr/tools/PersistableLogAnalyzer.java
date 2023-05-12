package dev.appkr.tools;

import dev.appkr.tools.model.AnalysisReport;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

public class PersistableLogAnalyzer implements LogAnalyzer {

  final LogAnalyzer delegate;
  final AnalysisReportRepository repository;

  public PersistableLogAnalyzer(LogAnalyzer delegate, AnalysisReportRepository repository) {
    this.delegate = delegate;
    this.repository = repository;
  }

  @Transactional
  public AnalysisReport analyze(Path path) throws IOException {
    final String md5hash = generateMd5HashFrom(path);
    final Optional<AnalysisReport> reportOptional = repository.findByMd5hash(md5hash);

    AnalysisReport report = null;
    if (reportOptional.isPresent()) {
      report = reportOptional.get();
    } else {
      report = delegate.analyze(path);
      report.setMd5hash(md5hash);
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
