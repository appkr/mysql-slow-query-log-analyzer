package dev.appkr.tools.core;

import dev.appkr.tools.core.model.AnalysisReport;
import java.util.List;
import java.util.Optional;

public interface AnalysisReportRepository {

  Optional<AnalysisReport> findById(String md5hash);

  List<AnalysisReport> findAll();

  AnalysisReport save(AnalysisReport entity);
}
