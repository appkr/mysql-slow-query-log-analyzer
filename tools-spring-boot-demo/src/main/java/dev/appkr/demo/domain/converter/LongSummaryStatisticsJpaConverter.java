package dev.appkr.demo.domain.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.LongSummaryStatistics;

@Converter(autoApply = true)
public class LongSummaryStatisticsJpaConverter implements AttributeConverter<LongSummaryStatistics, String> {

  @Override
  public String convertToDatabaseColumn(LongSummaryStatistics attribute) {
    return null;
  }

  @Override
  public LongSummaryStatistics convertToEntityAttribute(String dbData) {
    return null;
  }
}
