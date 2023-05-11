package dev.appkr.tools.application;

import dev.appkr.tools.application.port.in.LogAnalyzer;
import dev.appkr.tools.application.port.out.AnalysisReportJpaRepository;
import dev.appkr.tools.domain.AnalysisReport;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.transaction.annotation.Transactional;

public class PersistableLogAnalyzer implements LogAnalyzer {

  final LogAnalyzer delegate;
  final AnalysisReportJpaRepository repository;

  public PersistableLogAnalyzer(LogAnalyzer delegate, AnalysisReportJpaRepository repository) {
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
      return DigestUtils.md5Hex(Files.readAllBytes(path));
    } catch (IOException e) {
      return null;
    }
  }
}
