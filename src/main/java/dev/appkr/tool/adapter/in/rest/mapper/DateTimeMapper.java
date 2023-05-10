package dev.appkr.tool.adapter.in.rest.mapper;

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
}
