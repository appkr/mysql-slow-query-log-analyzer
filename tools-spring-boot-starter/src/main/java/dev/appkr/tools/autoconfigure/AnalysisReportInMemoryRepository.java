package dev.appkr.tools.autoconfigure;

import dev.appkr.tools.core.AnalysisReportRepository;
import dev.appkr.tools.core.model.AnalysisReport;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class AnalysisReportInMemoryRepository implements AnalysisReportRepository {

  final ConcurrentHashMap<String, AnalysisReport> storage = new ConcurrentHashMap<>();

  @Override
  public Optional<AnalysisReport> findById(String id) {
    return Optional.ofNullable(storage.getOrDefault(id, null));
  }

  @Override
  public List<AnalysisReport> findAll() {
    return new ArrayList<>(storage.values());
  }

  @Override
  public AnalysisReport save(AnalysisReport entity) {
    storage.putIfAbsent(entity.getId(), entity);
    return entity;
  }
}
