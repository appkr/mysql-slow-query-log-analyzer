package dev.appkr.tools.core.model;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.LongSummaryStatistics;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@EqualsAndHashCode(of = {"key"})
@ToString(exclude = {"entries"})
public class TokenizedQuery {

  final List<Tuple> entries = new ArrayList<>();

  // Field referenced from https://github.com/devops-works/dw-query-digest
  String key;
  String fingerprint;
  Integer calls = 0;
  Duration cumQueryTime = Duration.ZERO;
  Duration cumLockTime = Duration.ZERO;
  Integer cumRowsExamined = 0;
  Integer cumRowsSent = 0;
  Duration minTime = Duration.ofDays(365);
  Duration maxTime = Duration.ZERO;
  Duration avgTime = Duration.ZERO;
  Duration p50Time = Duration.ZERO;
  Duration p95Time = Duration.ZERO;
  Duration stddevTime = Duration.ZERO;

  public TokenizedQuery(Tuple tuple) {
    addAndCombine(tuple);
  }

  public void addAndCombine(Tuple tuple) {
    this.entries.add(tuple);
    Collections.sort(this.entries);

    if (this.key == null) {
      this.key = tuple.getKey();
    }
    if (this.fingerprint == null) {
      this.fingerprint = tuple.getFingerprint();
    }

    this.calls += 1;
    this.cumQueryTime = this.cumQueryTime.plus(tuple.getQueryTime());
    this.cumLockTime = this.cumLockTime.plus(tuple.getLockTime());
    this.cumRowsExamined += tuple.getRowsExamined();
    this.cumRowsSent += tuple.getRowsSent();
    if (this.minTime.compareTo(tuple.getQueryTime()) > 0) {
      this.minTime = tuple.getQueryTime();
    }
    if (this.maxTime.compareTo(tuple.getQueryTime()) < 0) {
      this.maxTime = tuple.getQueryTime();
    }

    this.p50Time = percentile(50).getQueryTime();
    this.p95Time = percentile(95).getQueryTime();

    final LongSummaryStatistics stats = this.entries.stream()
        .mapToLong(t -> t.getQueryTime().toNanos())
        .summaryStatistics();
    this.avgTime = Duration.ofNanos((long)stats.getAverage());
    this.stddevTime = stddevFrom(stats);
  }

  Tuple percentile(Integer percentile) {
    if (percentile < 0 || percentile > 100) {
      return null;
    }
    final int index = (int) Math.ceil(percentile / 100.0 * this.entries.size());

    return this.entries.get(index - 1);
  }

  Duration stddevFrom(LongSummaryStatistics stats) {
    final long numerator = this.entries.stream()
        .mapToLong(t -> t.getQueryTime().toNanos())
        .reduce(0L, (a, b) -> a += Math.pow(b - stats.getAverage(), 2));
    return Duration.ofNanos((long)Math.sqrt(numerator / stats.getCount()));
  }

  @Getter
  @AllArgsConstructor
  @ToString
  public static class Tuple implements Comparable<Tuple> {

    String key;
    String fingerprint;
    Duration queryTime;
    Duration lockTime;
    Integer rowsExamined;
    Integer rowsSent;

    @Override
    public int compareTo(Tuple other) {
      return this.queryTime.compareTo(other.queryTime);
    }
  }
}
