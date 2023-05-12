package dev.appkr.demo.adapter.in.rest.mapper;

import static dev.appkr.demo.config.Constants.SEOUL;

import dev.appkr.demo.domain.Carbon;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
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
    return OffsetDateTime.ofInstant(dateTime, ZoneId.systemDefault());
  }

  public OffsetDateTime toOffsetDateTime(Carbon dateTime) {
    if (dateTime == null) {
      return null;
    }

    return OffsetDateTime.ofInstant(dateTime.toInstant(), SEOUL);
  }
}
