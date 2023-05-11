package dev.appkr.tools.adapter.in.rest.mapper;

import dev.appkr.tools.domain.SlowQueryLog;
import dev.appkr.tools.rest.SlowQueryLogEntry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SlowQueryLogEntryMapper implements DtoMapper<SlowQueryLog, SlowQueryLogEntry> {

  final DateTimeMapper dateTimeMapper;
  final ExecutionPlanEntryMapper executionPlanEntryMapper;

  @Override
  public SlowQueryLogEntry toDto(SlowQueryLog entity) {
    if (entity == null || entity.getLogEntry() == null) {
      return null;
    }

    final SlowQueryLog.LogEntry entry = entity.getLogEntry();
    return new SlowQueryLogEntry()
        .time(dateTimeMapper.toOffsetDateTime(entry.getTime()))
        .user(entry.getUser())
        .host(entry.getHost())
        .id(entry.getId())
        .queryTime(entry.getQueryTime().toString())
        .lockTime(entry.getLockTime().toString())
        .rowsSent(entry.getRowsSent())
        .rowsExamined(entry.getRowsExamined())
        .sql(entry.getSql())
        .executionPlans(executionPlanEntryMapper.toDto(entry.getExecutionPlans()));
  }
}
