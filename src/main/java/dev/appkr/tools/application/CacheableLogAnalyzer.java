package dev.appkr.tools.application;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

import dev.appkr.tools.application.port.out.AnalysisReportJpaRepository;
import dev.appkr.tools.domain.AnalysisReport;
import dev.appkr.tools.domain.SlowQueryLog;
import jakarta.persistence.EntityNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.transaction.annotation.Transactional;

public class CacheableLogAnalyzer {

  final SlowQueryLogAnalyzer delegate;
  final AnalysisReportJpaRepository repository;

  public CacheableLogAnalyzer(SlowQueryLogAnalyzer delegate, AnalysisReportJpaRepository repository) {
    this.delegate = delegate;
    this.repository = repository;
  }

  @Transactional
  public AnalysisReport analyze(String reportId, Path path) throws IOException {
    Optional<AnalysisReport> reportOptional = Optional.empty();
    if (isNotEmpty(reportId)) {
      reportOptional = repository.findByMd5hash(reportId);
      if (reportOptional.isEmpty()) {
        throw new EntityNotFoundException("캐시에서 분석 결과를 찾을 수 없습니다: repordId=" + reportId);
      }
    }

    reportId = generateMd5HashFrom(path);
    if (reportOptional.isEmpty() && path != null) {
      reportOptional = repository.findByMd5hash(reportId);
    }

    AnalysisReport report = null;
    if (reportOptional.isPresent()) {
      report = reportOptional.get();
    } else {
      final List<SlowQueryLog> logEntries = delegate.analyze(path);
      if (!logEntries.isEmpty()) {
        report = repository.save(new AnalysisReport(reportId, logEntries));
      }
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
