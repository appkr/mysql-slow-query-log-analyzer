package dev.appkr.tools.application.port.out;

import dev.appkr.tools.domain.AnalysisReport;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnalysisReportJpaRepository extends JpaRepository<AnalysisReport, String> {

  Optional<AnalysisReport> findByMd5hash(String md5hash);
}
