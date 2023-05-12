package dev.appkr.tools.model;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.Type;

/**
 * See https://dev.mysql.com/doc/refman/8.0/en/slow-query-log.html
 */
@Embeddable
@NoArgsConstructor
@Data
@Slf4j
public class SlowQueryLog implements Serializable {

  private static final long serialVersionUID = 1L;

  // # Time: 2023-04-16T12:02:32.382948Z
  static final Pattern TIME_LINE = Pattern.compile("^# Time:\\s*(?<datetime>[a-zA-Z0-9+-:./]+)$");

  // # User@Host: root[root] @  [172.18.0.1]  Id:  3464
  static final Pattern USER_LINE = Pattern
      .compile("^# User@Host:\\s*(?<user>[a-zA-Z0-9\\[\\]]+)\\s*@\\s*(?<host>[a-zA-Z0-9.\\[\\]]+)\\s*Id:\\s*(?<id>[0-9]+)$");

  // # Query_time: 1.077059  Lock_time: 0.005489 Rows_sent: 8  Rows_examined: 54728
  static final Pattern STAT_LINE = Pattern
      .compile("^# Query_time:\\s*(?<queryTime>[0-9.]+)\\s*Lock_time:\\s*(?<lockTime>[0-9.]+)\\s*" +
               "Rows_sent:\\s*(?<rowsSent>[0-9]+)\\s*Rows_examined:\\s*(?<rowsExamined>[0-9]+)$");

  // [], # 제외한 키보드에 있는 모든 특수 문자, 문자, 숫자
  static final Pattern SQL_LINE = Pattern
      .compile("(?<sql>^(SELECT|UPDATE|DELETE|INSERT|REPLACE)\\s*[\\p{L}\\p{Nd}`~!@$%^&*()_\\-=+\\|{}:;'\\\"<>?,./\\s\\n]+)$",
          Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);

  @Type(JsonType.class)
  LogEntry logEntry = new LogEntry();

  public SlowQueryLog(String aLogParagraph) {
    Stream.of(aLogParagraph.split("\\n"))
        .forEach(line -> {
          final Matcher timeLineMatcher = TIME_LINE.matcher(line);
          final Matcher userLineMatcher = USER_LINE.matcher(line);
          final Matcher statLineMatcher = STAT_LINE.matcher(line);
          try {
            if (timeLineMatcher.matches()) {
              logEntry.setTime(parseDateTimeFrom(timeLineMatcher.group("datetime").trim()));
            } else if(userLineMatcher.matches()) {
              logEntry.setUser(userLineMatcher.group("user").trim().split("\\[")[0]);
              logEntry.setHost(userLineMatcher.group("host").trim().replaceAll("\\[\\]", ""));
              logEntry.setId(userLineMatcher.group("id").trim());
            } else if (statLineMatcher.matches()) {
              logEntry.setQueryTime(parseDurationFrom(statLineMatcher.group("queryTime").trim()));
              logEntry.setLockTime(parseDurationFrom(statLineMatcher.group("lockTime").trim()));
              logEntry.setRowsSent(parseIntegerFrom(statLineMatcher.group("rowsSent").trim()));
              logEntry.setRowsExamined(parseIntegerFrom(statLineMatcher.group("rowsExamined").trim()));
            }
          } catch (Exception e) {
            log.warn("로그 분석 실패: log={}", aLogParagraph);
          }
        });

    try {
      final Matcher sqlLineMatcher = SQL_LINE.matcher(aLogParagraph);
      if (sqlLineMatcher.find()) {
        final String rawSql = sqlLineMatcher.group("sql").trim();
        final String sanitizedSql = Stream.of(rawSql.split("\\n"))
            .map(line -> line.replaceAll("\\s*--\\s*.*", "")
                .replaceAll("\\s*/\\*.*\\*/", "")
                .trim()
            )
            .collect(Collectors.joining(" "))
            .replaceAll("\\s{2,}", " ");
        logEntry.setSql(sanitizedSql);
      }
    } catch (Exception e) {
      log.warn("로그 분석 실패: log={}", aLogParagraph);
    }
  }

  public void accept(ExplainVisitor visitor) {
    visitor.visit(this.getLogEntry());
  }

  Instant parseDateTimeFrom(String dateTimeString) {
    try {
      return Instant.parse(dateTimeString);
    } catch (Exception ignored) {}
    return Instant.EPOCH;
  }

  Duration parseDurationFrom(String secondString) {
    try {
      final double queryTime = Double.parseDouble(secondString);
      return Duration.ofMillis((long)(queryTime * 1_000));
    } catch (NumberFormatException ignored) {}
    return Duration.ZERO;
  }

  Integer parseIntegerFrom(String numberString) {
    try {
      return Integer.parseInt(numberString);
    } catch (NumberFormatException ignored) {}
    return 0;
  }

  @Data
  public static class LogEntry implements Serializable {

    private static final long serialVersionUID = 1L;

    Instant time;

    String user;

    String host;

    String id;

    /**
     * The statement execution time in seconds
     */
    Duration queryTime;

    /**
     * The time to acquire locks in seconds.
     */
    Duration lockTime;

    /**
     * The number of rows sent to the client
     */
    Integer rowsSent;

    /**
     * The number of rows examined by the server layer (not counting any processing internal to storage engines)
     */
    Integer rowsExamined;

    String sql;

    List<ExecutionPlan> executionPlans = new ArrayList<>();
  }
}
