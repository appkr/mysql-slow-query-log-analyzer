package dev.appkr.tools.core.model;

import java.io.Serializable;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import org.springframework.data.domain.Sort;

@Data
public class AnalysisReport implements Serializable {

  private static final long serialVersionUID = 1L;

  String id;

  ServerInfo serverInfo;

  @Setter(AccessLevel.NONE)
  List<SlowQueryLog> logEntries = new ArrayList<>();

  final ConcurrentHashMap<String, TokenizedQuery> tokenizedQueries = new ConcurrentHashMap<>();

  final LongSummaryStatistics summary = new LongSummaryStatistics(0, 0, 0, 0);

  public AnalysisReport(String id, ServerInfo serverInfo, List<SlowQueryLog> logEntries) {
    this(serverInfo, logEntries);
    this.id = id;
  }

  public AnalysisReport(ServerInfo serverInfo, List<SlowQueryLog> logEntries) {
    this(logEntries);
    this.serverInfo = serverInfo;
  }

  public AnalysisReport(List<SlowQueryLog> logEntries) {
    setLogEntries(logEntries);
  }

  void setLogEntries(List<SlowQueryLog> logEntries) {
    this.logEntries = logEntries;
    if (!this.logEntries.isEmpty()) {
      final LongSummaryStatistics stats = this.logEntries.stream()
          .mapToLong(entry -> entry.getQueryTime().toMillis())
          .summaryStatistics();
      this.summary.combine(stats);
    }
  }

  public void collectFingerprint(FingerprintVisitor visitor) {
    if (!logEntries.isEmpty()) {
      this.logEntries
          .forEach(entry -> {
            final TokenizedQuery.Tuple tuple = entry.accept(visitor);
            if (tokenizedQueries.containsKey(tuple.getKey())) {
              final TokenizedQuery tokenizedQuery = tokenizedQueries.get(tuple.getKey());
              tokenizedQuery.addAndCombine(tuple);
            } else {
              tokenizedQueries.put(tuple.getKey(), new TokenizedQuery(tuple));
            }
          });
    }
  }

  public static Predicate<SlowQueryLog> getFilterSpec(Integer slowerThanMillis, String queryType) {
    Predicate<SlowQueryLog> filterSpec = entry -> true;
    if (slowerThanMillis != null && slowerThanMillis > 0) {
      filterSpec = filterSpec
          .and(entry -> entry.getQueryTime().compareTo(Duration.ofMillis(slowerThanMillis)) > 0);
    }

    if (queryType != null && !queryType.isBlank()) {
      final String allLower = queryType.toUpperCase();
      final String allUpper = queryType.toLowerCase();
      final String regex = String.format("^(%s|%s)\\s*.+", allLower, allUpper);

      filterSpec = filterSpec.and(entry -> entry.getSql().matches(regex));
    }

    return filterSpec;
  }

  public static Comparator<SlowQueryLog> getSortSpec(Sort sort) {
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
            default -> throw new IllegalArgumentException("Indigestable sort property: property=" + property);
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
