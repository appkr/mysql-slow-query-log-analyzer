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
    if (entity == null) {
      return null;
    }

    return new SlowQueryLogEntry()
        .time(dateTimeMapper.toOffsetDateTime(entity.getTime()))
        .user(entity.getUser())
        .host(entity.getHost())
        .id(entity.getQueryId())
        .queryTime(entity.getQueryTime().toString())
        .lockTime(entity.getLockTime().toString())
        .rowsSent(entity.getRowsSent())
        .rowsExamined(entity.getRowsExamined())
        .sql(entity.getSql())
        .executionPlans(executionPlanEntryMapper.toDto(entity.getExecutionPlans()));
  }
}
