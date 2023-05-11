package dev.appkr.tools.domain;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.Duration;
import java.util.*;
import java.util.function.Predicate;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Sort;

@Entity
@Table(name = "Z_SLOW_QUERY_LOG_REPORTS")
@Getter
@Setter
public class AnalysisReport implements Serializable {

  private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  Long id;

  String md5hash;

  @OneToMany(mappedBy = "report", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
  List<SlowQueryLog> logEntries = new ArrayList<>();

  @Transient
  LongSummaryStatistics summary;

  protected AnalysisReport() {
  }

  public AnalysisReport(String md5hash, List<SlowQueryLog> logEntries) {
    this.md5hash = md5hash;
    this.logEntries = logEntries.stream()
        .map(e -> {
          e.setReport(this);
          return e;
        })
        .toList();

    summarize();
  }

  public static Predicate<SlowQueryLog> filterSpecBy(Integer filterMillis, String filterQuery) {
    Predicate<SlowQueryLog> filterSpec = entry -> true;
    if (isNotEmpty(filterMillis) && filterMillis > 0) {
      filterSpec = filterSpec.and(entry -> entry.getQueryTime().compareTo(Duration.ofMillis(filterMillis)) > 0);
    }

    if (isNotEmpty(filterQuery)) {
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

  void summarize() {
    if (this.logEntries.isEmpty()) {
      this.summary = new LongSummaryStatistics(0, 0, 0, 0);
    } else {
      this.summary = this.logEntries.stream()
          .mapToLong(entry -> entry.getQueryTime().toMillis())
          .summaryStatistics();
    }
  }
}
