package dev.appkr.tools.adapter.in.rest.mapper;

import static dev.appkr.tools.config.Constants.SEOUL;

import dev.appkr.tools.domain.Carbon;
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
