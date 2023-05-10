package dev.appkr.tool.adapter.in.rest.mapper;

import java.util.List;

public interface DtoMapper<D, E> {

  D toDto(E entity);
  List <D> toDto(List<E> entityList);
}
