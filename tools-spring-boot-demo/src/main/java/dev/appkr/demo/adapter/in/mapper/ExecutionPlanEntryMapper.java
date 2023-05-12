package dev.appkr.demo.adapter.in.mapper;

import dev.appkr.demo.rest.ExecutionPlanEntry;
import dev.appkr.tools.model.ExecutionPlan;
import org.springframework.stereotype.Component;

@Component
public class ExecutionPlanEntryMapper implements DtoMapper<ExecutionPlan, ExecutionPlanEntry> {

  @Override
  public ExecutionPlanEntry toDto(ExecutionPlan entity) {
    if (entity == null) {
      return null;
    }

    return new ExecutionPlanEntry()
        .id(entity.getId())
        .selectType(entity.getSelectType())
        .table(entity.getTable())
        .partitions(entity.getPartitions())
        .type(entity.getType())
        .possibleKeys(entity.getPossibleKeys())
        .key(entity.getKey())
        .keyLen(entity.getKeyLen())
        .ref(entity.getRef())
        .rows(entity.getRows())
        .filtered(entity.getFiltered())
        .extra(entity.getExtra());
  }
}
