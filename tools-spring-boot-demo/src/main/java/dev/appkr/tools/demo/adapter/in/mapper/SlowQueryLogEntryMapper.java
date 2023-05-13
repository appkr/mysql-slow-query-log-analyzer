package dev.appkr.tools.demo.adapter.in.mapper;

import dev.appkr.tools.core.model.SlowQueryLog;
import dev.appkr.tools.demo.rest.SlowQueryLogEntry;
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
        .id(entity.getId())
        .queryTime(entity.getQueryTime().toString())
        .lockTime(entity.getLockTime().toString())
        .rowsSent(entity.getRowsSent())
        .rowsExamined(entity.getRowsExamined())
        .sql(entity.getSql())
        .executionPlans(executionPlanEntryMapper.toDto(entity.getExecutionPlans()));
  }
}
