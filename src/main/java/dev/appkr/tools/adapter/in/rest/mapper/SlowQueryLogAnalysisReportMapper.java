package dev.appkr.tools.adapter.in.rest.mapper;

import dev.appkr.tools.domain.AnalysisReport;
import dev.appkr.tools.rest.SlowQueryLogAnalysisReport;
import dev.appkr.tools.rest.SummaryReport;
import java.time.Duration;
import java.util.LongSummaryStatistics;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SlowQueryLogAnalysisReportMapper {

  final SlowQueryLogEntryMapper slowQueryLogEntryMapper;

  public SlowQueryLogAnalysisReport toDto(AnalysisReport entity) {
    if (entity == null) {
      return null;
    }

    return new SlowQueryLogAnalysisReport()
        .summary(toDto(entity.getSummary()))
        .data(slowQueryLogEntryMapper.toDto(entity.getLogEntries()));
  }

  public SummaryReport toDto(LongSummaryStatistics entity) {
    if (entity == null) {
      return null;
    }

    return new SummaryReport()
        .count(entity.getCount())
        .min(Duration.ofMillis(entity.getMin()).toString())
        .average(Duration.ofMillis((long)Math.ceil(entity.getAverage())).toString())
        .max(Duration.ofMillis(entity.getMax()).toString());
  }
}
