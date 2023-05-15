package dev.appkr.tools.core.model;

import static java.util.stream.Collectors.joining;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import lombok.Getter;
import org.springframework.data.domain.Sort;

@Getter
public class LogFilter {

  public static final Pattern PATTERN_QUERY_TYPE = Pattern.compile("^(SELECT|UPDATE|DELETE|INSERT|REPLACE)$");
  public static final Pattern PATTERN_SORT
      = Pattern.compile("^(?<property>[\\p{L}][\\p{L}\\p{Nd}]*)(?::)?(?<direction>asc|desc)?$");

  Integer slowerThanMillis;
  String queryType;
  Sort sort = Sort.unsorted();

  private LogFilter(Integer slowerThanMillis, String queryType, Sort sort) {
    this.slowerThanMillis = slowerThanMillis;
    this.queryType = queryType;
    this.sort = sort;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    Integer slowerThanMillis;
    String queryType;
    String sortRaw;
    Sort sort;

    public Builder slowerThanMillis(Integer slowerThanMillis) {
      this.slowerThanMillis = slowerThanMillis;
      return this;
    }

    public Builder queryType(String queryType) {
      this.queryType = queryType;
      return this;
    }

    public Builder sort(String sortRaw) {
      this.sortRaw = sortRaw;
      return this;
    }

    public LogFilter build() {
      final List<String> errors = new ArrayList<>();
      if (slowerThanMillis != null && slowerThanMillis < 0) {
        errors.add("Value must be positive: slowerThanMillis=" + slowerThanMillis);
      }

      if (queryType != null && !queryType.isBlank()) {
        if (!PATTERN_QUERY_TYPE.matcher(queryType).matches()) {
          errors.add("Indigestable value: queryType=" + queryType);
        }
      }

      if (sortRaw != null && !sortRaw.isBlank()) {
        final List<String> acceptableProperties = Stream.of(SlowQueryLog.class.getDeclaredFields())
            .map(Field::getName)
            .toList();

        final String[] parts = sortRaw.split(",");
        final List<Sort.Order> orders = Stream.of(parts)
            .map(part -> {
              final Matcher matcher = PATTERN_SORT.matcher(part);
              if (!matcher.matches() || !acceptableProperties.contains(matcher.group("property"))) {
                errors.add("Indigestable value: sort=" + sortRaw);
              }

              Sort.Direction direction = Sort.Direction.ASC;
              String property = matcher.group("property");
              if (matcher.group("direction") != null && matcher.group("direction").equals("desc")) {
                direction = Sort.Direction.DESC;
              }

              return new Sort.Order(direction, property);
            })
            .toList();

        this.sort = Sort.by(orders);
      }

      if (!errors.isEmpty()) {
        throw new IllegalArgumentException(errors.stream().collect(joining("; ")));
      }

      return new LogFilter(slowerThanMillis, queryType, sort);
    }
  }
}
