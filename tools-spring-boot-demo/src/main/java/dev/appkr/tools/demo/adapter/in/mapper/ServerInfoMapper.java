package dev.appkr.tools.demo.adapter.in.mapper;

import dev.appkr.tools.demo.rest.ServerInfo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
public class ServerInfoMapper {

  public ServerInfo toDto(dev.appkr.tools.core.model.ServerInfo entity) {
    if (entity == null) {
      return null;
    }

    final ServerInfo dto = new ServerInfo();
    BeanUtils.copyProperties(entity, dto);

    return dto;
  }
}
