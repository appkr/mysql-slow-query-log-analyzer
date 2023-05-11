package dev.appkr.tools.adapter.in.rest;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import dev.appkr.tools.adapter.in.rest.mapper.SlowQueryLogAnalysisReportMapper;
import dev.appkr.tools.application.FilterableLogAnalyzer;
import dev.appkr.tools.application.port.in.exception.ConstraintViolationProblem;
import dev.appkr.tools.domain.AnalysisReport;
import dev.appkr.tools.domain.FieldError;
import dev.appkr.tools.domain.SlowQueryLog;
import dev.appkr.tools.rest.SlowQueryLogAnalysisReport;
import dev.appkr.tools.rest.ToolsApiDelegate;
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
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
@RequiredArgsConstructor
public class ToolsApiDelegateImpl implements ToolsApiDelegate {

  static final Pattern FILTER_QUERY_SPEC = Pattern.compile("^(SELECT|UPDATE|DELETE|INSERT|REPLACE)$");
  static final Pattern SORT_EXPRESSION
      = Pattern.compile("^(?<property>[\\p{L}][\\p{L}\\p{Nd}]*)(?::)?(?<direction>asc|desc)?$");

  final FilterableLogAnalyzer analyzer;
  final SlowQueryLogAnalysisReportMapper mapper;

  @Override
  public ResponseEntity<SlowQueryLogAnalysisReport> analyzeSlowQueryLog(String reportId,
      MultipartFile logFile, Integer filterMillis, String filterQuery, String sort) {
    final Sort sortSpec = validateAndGet(reportId, logFile, filterMillis, filterQuery, sort);
    if (filterMillis == null) {
      filterMillis = 0;
    }

    AnalysisReport report;
    try {
      final Path path = convertFrom(logFile);
      if (reportId == null && path == null) {
        throw new RuntimeException("빈 파일을 제출했습니다");
      }

      report = analyzer.analyze(reportId, path, filterMillis, filterQuery, sortSpec);

      if (path != null) {
        Files.deleteIfExists(path);
      }
    } catch (Exception e) {
      throw new ConstraintViolationProblem(e.getMessage());
    }

    final SlowQueryLogAnalysisReport dto = mapper.toDto(report);

    return ResponseEntity.ok(dto);
  }

  Path convertFrom(MultipartFile mFile) {
    if (mFile == null || mFile.isEmpty()) {
      return null;
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

  Sort validateAndGet(String reportId, MultipartFile logFile, Integer filterMillis, String filterQuery, String sort) {
    final List<FieldError> errors = new ArrayList<>();
    if (isEmpty(reportId) && (logFile == null || logFile.isEmpty())) {
      errors.add(new FieldError("AnalyzeSlowQueryLogCommand.requiredFields",
          "reportId, logFile 둘 중 하나는 제출해야합니다"));
    }

    if (filterMillis != null && filterMillis < 0) {
      errors.add(new FieldError("AnalyzeSlowQueryLogCommand.filterMillis", "음수는 허용하지 않습니다"));
    }

    if (isNotEmpty(filterQuery)) {
      if (!FILTER_QUERY_SPEC.matcher(filterQuery).matches()) {
        errors.add(new FieldError("AnalyzeSlowQueryLogCommand.filterQuery", "처리할 수 없는 필터입니다"));
      }
    }

    Sort sortSpec = null;
    if (isEmpty(sort)) {
      sortSpec = Sort.unsorted();
    } else {
      final List<String> acceptableProperties = Stream.of(SlowQueryLog.class.getDeclaredFields())
          .map(Field::getName)
          .toList();

      final String[] parts = sort.split(",");
      final List<Sort.Order> orders = Stream.of(parts)
          .map(part -> {
            final Matcher matcher = SORT_EXPRESSION.matcher(part);
            if (!matcher.matches() || !acceptableProperties.contains(matcher.group("property"))) {
              errors.add(new FieldError("AnalyzeSlowQueryLogCommand.sort", "처리할 수 없는 값입니다"));
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

    return sortSpec;
  }
}
