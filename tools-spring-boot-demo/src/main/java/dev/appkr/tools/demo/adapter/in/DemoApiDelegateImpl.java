package dev.appkr.tools.demo.adapter.in;

import dev.appkr.tools.core.FilterableLogAnalyzer;
import dev.appkr.tools.core.model.AnalysisReport;
import dev.appkr.tools.core.model.LogFilter;
import dev.appkr.tools.demo.adapter.in.mapper.SlowQueryLogAnalysisReportMapper;
import dev.appkr.tools.demo.domain.FieldError;
import dev.appkr.tools.demo.port.in.exception.ConstraintViolationProblem;
import dev.appkr.tools.demo.rest.DemoApiDelegate;
import dev.appkr.tools.demo.rest.SlowQueryLogAnalysisReport;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
@RequiredArgsConstructor
@Slf4j
public class DemoApiDelegateImpl implements DemoApiDelegate {

  final FilterableLogAnalyzer analyzer;
  final SlowQueryLogAnalysisReportMapper mapper;

  @Override
  public ResponseEntity<SlowQueryLogAnalysisReport> analyzeSlowQueryLog(MultipartFile logFile,
      Integer filterMillis, String filterQuery, String sort) {
    if (filterMillis == null) {
      filterMillis = 0;
    }
    final Path path = validateAndGet(logFile);
    final LogFilter filter = LogFilter.builder()
        .slowerThanMillis(filterMillis)
        .queryType(filterQuery)
        .sort(sort)
        .build();

    AnalysisReport report;
    try {
      report = analyzer.analyze(path, filter);

      if (path != null) {
        Files.deleteIfExists(path);
      }
    } catch (Exception e) {
      log.error("Something wrong :(", e);
      throw new ConstraintViolationProblem(e.getMessage());
    }

    final SlowQueryLogAnalysisReport dto = mapper.toDto(report);

    return ResponseEntity.ok(dto);
  }

  Path validateAndGet(MultipartFile mFile) {
    if (mFile == null) {
      throw new ConstraintViolationProblem("Bad request",
          List.of(new FieldError("AnalyzeSlowQueryLogCommand.logFile", "A file is required")));
    }

    Path path;
    try {
      path = Files.createTempFile("dev.appkr.tools", "_" + mFile.getOriginalFilename());
      mFile.transferTo(path);
    } catch (IOException ignored) {
      return null;
    }

    return path;
  }
}
