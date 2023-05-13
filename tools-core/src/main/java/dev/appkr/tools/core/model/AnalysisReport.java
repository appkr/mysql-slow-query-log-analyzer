package dev.appkr.tools.core.model;

import java.io.Serializable;
import java.time.Duration;
import java.util.*;
import java.util.function.Predicate;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import org.springframework.data.domain.Sort;

@Data
public class AnalysisReport implements Serializable {

  private static final long serialVersionUID = 1L;

  String id;

  @Setter(AccessLevel.NONE)
  List<SlowQueryLog> logEntries = new ArrayList<>();

  LongSummaryStatistics summary;

  public AnalysisReport(List<SlowQueryLog> logEntries) {
    setLogEntries(logEntries);
  }

  public AnalysisReport(String id, List<SlowQueryLog> logEntries) {
    this.id = id;
    setLogEntries(logEntries);
  }

  public void setLogEntries(List<SlowQueryLog> logEntries) {
    if (logEntries.isEmpty()) {
      this.summary = new LongSummaryStatistics(0, 0, 0, 0);
    } else {
      this.summary = logEntries.stream()
          .mapToLong(entry -> entry.getQueryTime().toMillis())
          .summaryStatistics();
    }

    this.logEntries = logEntries;
  }

  public static Predicate<SlowQueryLog> filterSpecBy(Integer filterMillis, String filterQuery) {
    Predicate<SlowQueryLog> filterSpec = entry -> true;
    if (filterMillis != null && filterMillis > 0) {
      filterSpec = filterSpec.and(entry -> entry.getQueryTime().compareTo(Duration.ofMillis(filterMillis)) > 0);
    }

    if (filterQuery != null && !filterQuery.isBlank()) {
      final String allLower = filterQuery.toUpperCase();
      final String allUpper = filterQuery.toLowerCase();
      final String regex = String.format("^(%s|%s)\\s*.+", allLower, allUpper);

      filterSpec = filterSpec.and(entry -> entry.getSql().matches(regex));
    }

    return filterSpec;
  }

  public static Comparator<SlowQueryLog> sortSpecBy(Sort sort) {
    if (sort == null || sort.isEmpty()) {
      return Comparator.comparing(SlowQueryLog::getTime);
    }

    final List<Comparator<SlowQueryLog>> comparators = sort.stream()
        .map(order -> {
          final String property = order.getProperty();
          Comparator<SlowQueryLog> comparator = switch (property) {
            case "time" -> Comparator.comparing(SlowQueryLog::getTime);
            case "queryTime" -> Comparator.comparing(SlowQueryLog::getQueryTime);
            case "lockTime" -> Comparator.comparing(SlowQueryLog::getLockTime);
            case "rowsSent" -> Comparator.comparing(SlowQueryLog::getRowsSent);
            case "rowsExamined" -> Comparator.comparing(SlowQueryLog::getRowsExamined);
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
