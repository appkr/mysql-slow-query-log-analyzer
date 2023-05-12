package dev.appkr.demo.adapter.out;

import dev.appkr.tools.AnalysisReportRepository;
import dev.appkr.tools.model.AnalysisReport;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class AnalysisReportJpaRepository implements AnalysisReportRepository {

  final EntityManager em;

  @Override
  public Optional<AnalysisReport> findByMd5hash(String md5hash) {
    AnalysisReport report = null;
    try {
// TODO @appkr org.hibernate.query.sqm.UnknownEntityException: Could not resolve root entity 'AnalysisReport'
//      report = em
//          .createQuery("SELECT r FROM AnalysisReport r WHERE r.md5hash = :md5Hash", AnalysisReport.class)
//          .setParameter("md5Hash", md5hash)
//          .getSingleResult();
      report = (AnalysisReport)(em
          .createNativeQuery("SELECT * FROM Z_ANALYSISREPORTS WHERE md5hash = :md5hash", AnalysisReport.class)
          .setParameter("md5hash", md5hash)
          .getSingleResult());
    } catch (NoResultException ignored) {}
    
    return Optional.ofNullable(report);
  }

  @Override
  @Transactional
  public AnalysisReport save(AnalysisReport entity) {
// TODO @appkr Unable to locate persister: dev.appkr.tools.model.AnalysisReport
    em.persist(entity);
    em.flush();

    return entity;
  }
}
