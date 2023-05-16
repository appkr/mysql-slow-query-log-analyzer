package dev.appkr.tools.demo.adapter.in.mapper;

import dev.appkr.tools.core.model.TokenizedQuery;
import dev.appkr.tools.demo.rest.QueryStat;
import org.springframework.stereotype.Component;

@Component
public class QueryStatMapper implements DtoMapper<TokenizedQuery, QueryStat> {

  @Override
  public QueryStat toDto(TokenizedQuery entity) {
    if (entity == null) {
      return null;
    }

    return new QueryStat()
        .key(entity.getKey())
        .fingerprint(entity.getFingerprint())
        .calls(entity.getCalls())
        .cumQueryTime(entity.getCumQueryTime().toString())
        .cumLockTime(entity.getCumLockTime().toString())
        .cumRowsExamined(entity.getCumRowsExamined())
        .cumRowsSent(entity.getCumRowsSent())
        .minTime(entity.getMinTime().toString())
        .maxTime(entity.getMaxTime().toString())
        .avgTime(entity.getAvgTime().toString())
        .p50Time(entity.getP50Time().toString())
        .p95Time(entity.getP95Time().toString())
        .stddevTime(entity.getStddevTime().toString());
  }
}
