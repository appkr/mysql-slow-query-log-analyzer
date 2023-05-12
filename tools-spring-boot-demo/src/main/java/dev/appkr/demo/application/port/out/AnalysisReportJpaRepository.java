package dev.appkr.demo.application.port.out;

import dev.appkr.demo.domain.AnalysisReport;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnalysisReportJpaRepository extends JpaRepository<AnalysisReport, String> {

  Optional<AnalysisReport> findByMd5hash(String md5hash);
}
