package dev.appkr.tools.domain;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 * See https://dev.mysql.com/doc/refman/8.0/en/slow-query-log.html
 */
@Entity
@Table(name = "Z_SLOW_QUERY_LOGS")
@Getter
@Setter
@Slf4j
@ToString(exclude = "report")
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

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  Long id;

  @Column(name = "log_time")
  Carbon time;

  @Column(name = "query_user")
  String user;

  @Column(name = "query_host")
  String host;

  @Column(name = "query_id")
  String queryId;

  /**
   * The statement execution time in seconds
   */
  @Column(name = "query_time")
  Duration queryTime;

  /**
   * The time to acquire locks in seconds.
   */
  @Column(name = "lock_time")
  Duration lockTime;

  /**
   * The number of rows sent to the client
   */
  @Column(name = "rows_sent")
  Integer rowsSent;

  /**
   * The number of rows examined by the server layer (not counting any processing internal to storage engines)
   */
  @Column(name = "rows_examined")
  Integer rowsExamined;

  @Column(name = "qeury_executed")
  String sql;

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "Z_QUERY_EXECUTION_PLANS", joinColumns=@JoinColumn(name="log_id"))
  @OrderBy("id")
  List<ExecutionPlan> executionPlans = new ArrayList<>();

  @ManyToOne
  @JoinColumn(name = "report_id")
  AnalysisReport report;

  protected SlowQueryLog() {
  }

  public SlowQueryLog(String aLogString) {
    Stream.of(aLogString.split("\\n"))
        .forEach(line -> {
          final Matcher timeLineMatcher = TIME_LINE.matcher(line);
          final Matcher userLineMatcher = USER_LINE.matcher(line);
          final Matcher statLineMatcher = STAT_LINE.matcher(line);
          try {
            if (timeLineMatcher.matches()) {
              this.time = parseDateTimeFrom(timeLineMatcher.group("datetime").trim());
            } else if(userLineMatcher.matches()) {
              this.user = userLineMatcher.group("user").trim().split("\\[")[0];
              this.host = userLineMatcher.group("host").trim().replaceAll("\\[\\]", "");
              this.queryId = userLineMatcher.group("id").trim();
            } else if (statLineMatcher.matches()) {
              this.queryTime = parseDurationFrom(statLineMatcher.group("queryTime").trim());
              this.lockTime = parseDurationFrom(statLineMatcher.group("lockTime").trim());
              this.rowsSent = parseIntegerFrom(statLineMatcher.group("rowsSent").trim());
              this.rowsExamined = parseIntegerFrom(statLineMatcher.group("rowsExamined").trim());
            }
          } catch (Exception e) {
            log.warn("로그 분석 실패: log={}", aLogString);
          }
        });

    try {
      final Matcher sqlLineMatcher = SQL_LINE.matcher(aLogString);
      if (sqlLineMatcher.find()) {
        final String rawSql = sqlLineMatcher.group("sql").trim();
        this.sql = Stream.of(rawSql.split("\\n"))
            .map(line -> line.replaceAll("\\s*--\\s*.*", "")
                .replaceAll("\\s*/\\*.*\\*/", "")
                .trim()
            )
            .collect(Collectors.joining(" "))
            .replaceAll("\\s{2,}", " ");
      }
    } catch (Exception e) {
      log.warn("로그 분석 실패: log={}", aLogString);
    }
  }

  public void accept(ExplainVisitor visitor) {
    visitor.visit(this);
  }

  Carbon parseDateTimeFrom(String dateTimeString) {
    try {
      return Carbon.parse(dateTimeString);
    } catch (Exception ignored) {}
    return Carbon.EPOCH;
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
}
