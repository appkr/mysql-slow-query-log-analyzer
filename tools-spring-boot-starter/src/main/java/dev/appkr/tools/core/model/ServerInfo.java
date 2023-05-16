package dev.appkr.tools.core.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ServerInfo {

  String binary;
  String version;
  String versionDescription;
  String tcpPort;
  String unixSocket;
}
