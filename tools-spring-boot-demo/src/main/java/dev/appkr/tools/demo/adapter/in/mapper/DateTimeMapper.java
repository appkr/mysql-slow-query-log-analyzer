package dev.appkr.tools.demo.adapter.in.mapper;

import static dev.appkr.tools.demo.config.Constants.SEOUL;

import java.time.Instant;
import java.time.OffsetDateTime;
import org.springframework.stereotype.Component;

@Component
public class DateTimeMapper {

  public Instant toInstant(OffsetDateTime dateTime) {
    if (dateTime == null) {
      return null;
    }
    return dateTime.toInstant();
  }

  public OffsetDateTime toOffsetDateTime(Instant dateTime) {
    if (dateTime == null) {
      return null;
    }
    return OffsetDateTime.ofInstant(dateTime, SEOUL);
  }
}
