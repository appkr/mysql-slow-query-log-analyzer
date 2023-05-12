package dev.appkr.demo.adapter.in.mapper;

import java.util.ArrayList;
import java.util.List;

public interface DtoMapper<E, D> {

  D toDto(E entity);

  default List<D> toDto(Iterable<E> entityList) {
    if (entityList == null) {
      return null;
    }

    final List<D> dtoList = new ArrayList<>();
    for (E entity : entityList) {
      dtoList.add(toDto(entity));
    }

    return dtoList;
  }
}
