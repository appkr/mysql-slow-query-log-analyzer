package dev.appkr.tool.application.port.out.spec;

import dev.appkr.tool.application.port.in.SearchCriteria;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public class SpecificationBuilder<T> {

  private List<SearchCriteria> params = new ArrayList<>();

  public SpecificationBuilder<T> with(String key, SearchCriteria.Operator operator, Object value) {
    params.add(new SearchCriteria(key, operator, value));
    return this;
  }

  @SuppressWarnings("unchecked")
  public Specification<T> build() {
    if (params.size() == 0) {
      return null;
    }

    Specification result = new GenericSpecification(params.get(0));

    for (int i = 1; i < params.size(); i++) {
      result = Specification.where(result).and(new GenericSpecification(params.get(i)));
    }

    return result;
  }
}
