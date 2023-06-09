package dev.appkr.tools.core.model;

import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Objects;

public class ExecutionPlanVisitor {

  final EntityManager em;

  public ExecutionPlanVisitor(EntityManager em) {
    this.em = em;
  }

  public void visit(SlowQueryLog visitable) {
    if (visitable.getSql() == null || visitable.getSql().isBlank()) {
      return;
    }

    @SuppressWarnings("unchecked")
    final List<Object[]> resultList = em
        .createNativeQuery(" EXPLAIN " + visitable.getSql())
        .getResultList();

    List<ExecutionPlan> executionPlans = resultList.stream()
        .map(row -> {
          final ExecutionPlan plan = new ExecutionPlan();
          plan.setId(Objects.toString(row[0], ""));
          plan.setSelectType(Objects.toString(row[1], ""));
          plan.setTable(Objects.toString(row[2], ""));
          plan.setPartitions(Objects.toString(row[3], ""));
          plan.setType(Objects.toString(row[4], ""));
          plan.setPossibleKeys(Objects.toString(row[5], ""));
          plan.setKey(Objects.toString(row[6], ""));
          plan.setKeyLen(Objects.toString(row[7], ""));
          plan.setRef(Objects.toString(row[8], ""));
          plan.setRows(Objects.toString(row[9], ""));
          plan.setFiltered(Objects.toString(row[10], ""));
          plan.setExtra(Objects.toString(row[11], ""));
          return plan;
        })
        .toList();

    visitable.setExecutionPlans(executionPlans);
  }
}
