package dev.appkr.tools.domain;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.Duration;
import java.util.*;
import java.util.function.Predicate;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Sort;

@Entity
@Table(name = "Z_ANALYSISREPORTS")
@Getter
@Setter
@EqualsAndHashCode(of = {"id"})
public class AnalysisReport implements Serializable {

  private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  Long id;

  String md5hash;

  @Setter(AccessLevel.NONE)
  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "Z_SLOWQUERYLOGS", joinColumns=@JoinColumn(name="report_id"))
  List<SlowQueryLog> logEntries = new ArrayList<>();

  @Transient
  LongSummaryStatistics summary;

  protected AnalysisReport() {
  }

  public AnalysisReport(List<SlowQueryLog> logEntries) {
    setLogEntries(logEntries);
  }

  public AnalysisReport(String md5hash, List<SlowQueryLog> logEntries) {
    this.md5hash = md5hash;
    setLogEntries(logEntries);
  }

  public void setLogEntries(List<SlowQueryLog> logEntries) {
    if (logEntries.isEmpty()) {
      this.summary = new LongSummaryStatistics(0, 0, 0, 0);
    } else {
      this.summary = logEntries.stream()
          .mapToLong(entry -> entry.getLogEntry().getQueryTime().toMillis())
          .summaryStatistics();
    }

    this.logEntries = logEntries;
  }

  public static Predicate<SlowQueryLog> filterSpecBy(Integer filterMillis, String filterQuery) {
    Predicate<SlowQueryLog> filterSpec = entry -> true;
    if (isNotEmpty(filterMillis) && filterMillis > 0) {
      filterSpec = filterSpec.and(entry -> entry.getLogEntry().getQueryTime().compareTo(Duration.ofMillis(filterMillis)) > 0);
    }

    if (isNotEmpty(filterQuery)) {
      final String allLower = filterQuery.toUpperCase();
      final String allUpper = filterQuery.toLowerCase();
      final String regex = String.format("^(%s|%s)\\s*.+", allLower, allUpper);

      filterSpec = filterSpec.and(entry -> entry.getLogEntry().getSql().matches(regex));
    }

    return filterSpec;
  }

  public static Comparator<SlowQueryLog> sortSpecBy(Sort sort) {
    if (sort == null || sort.isEmpty()) {
      return Comparator.comparing(SlowQueryLog::getLogEntry, Comparator.comparing(SlowQueryLog.LogEntry::getTime));
    }

    final List<Comparator<SlowQueryLog>> comparators = sort.stream()
        .map(order -> {
          final String property = order.getProperty();
          Comparator<SlowQueryLog> comparator = switch (property) {
            case "time" -> Comparator.comparing(SlowQueryLog::getLogEntry, Comparator.comparing(SlowQueryLog.LogEntry::getTime));
            case "queryTime" -> Comparator.comparing(SlowQueryLog::getLogEntry, Comparator.comparing(SlowQueryLog.LogEntry::getQueryTime));
            case "lockTime" -> Comparator.comparing(SlowQueryLog::getLogEntry, Comparator.comparing(SlowQueryLog.LogEntry::getLockTime));
            case "rowsSent" -> Comparator.comparing(SlowQueryLog::getLogEntry, Comparator.comparing(SlowQueryLog.LogEntry::getRowsSent));
            case "rowsExamined" -> Comparator.comparing(SlowQueryLog::getLogEntry, Comparator.comparing(SlowQueryLog.LogEntry::getRowsExamined));
            default -> throw new IllegalArgumentException("정렬할 수 없는 property입니다: property=" + property);
          };

          final Sort.Direction direction = order.getDirection();
          if (direction.isDescending()) {
            comparator = comparator.reversed();
          }

          return comparator;
        })
        .toList();

    final Iterator<Comparator<SlowQueryLog>> comparatorIter = comparators.iterator();
    Comparator<SlowQueryLog> comparator = comparatorIter.next();
    while (comparatorIter.hasNext()) {
      comparator = comparator.thenComparing(comparatorIter.next());
    }

    return comparator;
  }
}
