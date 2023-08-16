package net.azisaba.lgw.eventteammanager.sql;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MySQLConnectionData {

  private String hostname;
  private int port;
  private String database;
  private String username;
  private String password;

}
