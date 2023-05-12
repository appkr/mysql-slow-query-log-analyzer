package dev.appkr.demo.adapter.in.rest;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import dev.appkr.demo.application.port.in.exception.ConstraintViolationProblem;
import dev.appkr.demo.domain.AnalysisReport;
import dev.appkr.demo.domain.FieldError;
import dev.appkr.demo.rest.DemoApiDelegate;
import dev.appkr.demo.rest.SlowQueryLogAnalysisReport;
import dev.appkr.demo.adapter.in.rest.mapper.SlowQueryLogAnalysisReportMapper;
import dev.appkr.demo.application.FilterableLogAnalyzer;
import dev.appkr.demo.domain.SlowQueryLog;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
@RequiredArgsConstructor
@Slf4j
public class DemoApiDelegateImpl implements DemoApiDelegate {

  static final Pattern FILTER_QUERY_SPEC = Pattern.compile("^(SELECT|UPDATE|DELETE|INSERT|REPLACE)$");
  static final Pattern SORT_EXPRESSION
      = Pattern.compile("^(?<property>[\\p{L}][\\p{L}\\p{Nd}]*)(?::)?(?<direction>asc|desc)?$");

  final FilterableLogAnalyzer analyzer;
  final SlowQueryLogAnalysisReportMapper mapper;

  @Override
  public ResponseEntity<SlowQueryLogAnalysisReport> analyzeSlowQueryLog(MultipartFile logFile,
      Integer filterMillis, String filterQuery, String sort) {
    if (filterMillis == null) {
      filterMillis = 0;
    }
    final Path path = validateAndGet(logFile);
    final Sort sortSpec = validateAndGet(filterMillis, filterQuery, sort);

    AnalysisReport report;
    try {
      report = analyzer.analyze(path, filterMillis, filterQuery, sortSpec);

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
      path = Files.createTempFile("dev.appkr.demo", "_" + mFile.getOriginalFilename());
      mFile.transferTo(path);
    } catch (IOException ignored) {
      return null;
    }

    return path;
  }

  Sort validateAndGet(Integer filterMillis, String filterQuery, String sort) {
    final List<FieldError> errors = new ArrayList<>();
    if (filterMillis != null && filterMillis < 0) {
      errors.add(new FieldError("AnalyzeSlowQueryLogCommand.filterMillis", "The value must be positive"));
    }

    if (isNotEmpty(filterQuery)) {
      if (!FILTER_QUERY_SPEC.matcher(filterQuery).matches()) {
        errors.add(new FieldError("AnalyzeSlowQueryLogCommand.filterQuery", "Unprocessable filter value"));
      }
    }

    Sort sortSpec = null;
    if (isEmpty(sort)) {
      sortSpec = Sort.unsorted();
    } else {
      final List<String> acceptableProperties = Stream.of(SlowQueryLog.LogEntry.class.getDeclaredFields())
          .map(Field::getName)
          .toList();

      final String[] parts = sort.split(",");
      final List<Sort.Order> orders = Stream.of(parts)
          .map(part -> {
            final Matcher matcher = SORT_EXPRESSION.matcher(part);
            if (!matcher.matches() || !acceptableProperties.contains(matcher.group("property"))) {
              errors.add(new FieldError("AnalyzeSlowQueryLogCommand.sort", "Unprocessable sort property"));
            }

            Sort.Direction direction = Sort.Direction.ASC;
            String property = matcher.group("property");
            if (matcher.group("direction") != null && matcher.group("direction").equals("desc")) {
              direction = Sort.Direction.DESC;
            }

            return new Sort.Order(direction, property);
          })
          .toList();

      sortSpec = Sort.by(orders);
    }

    if (!errors.isEmpty()) {
      throw new ConstraintViolationProblem("Bad request", errors);
    }

    return sortSpec;
  }
}
