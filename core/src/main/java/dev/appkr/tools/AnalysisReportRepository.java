package dev.appkr.tools;

import dev.appkr.tools.model.AnalysisReport;
import java.util.Optional;

public interface AnalysisReportRepository {

  Optional<AnalysisReport> findByMd5hash(String md5hash);

  AnalysisReport save(AnalysisReport entity);
}
