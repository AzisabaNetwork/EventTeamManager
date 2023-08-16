package net.azisaba.lgw.eventteammanager.config;

import java.util.Collections;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.azisaba.lgw.eventteammanager.EventTeamManager;
import net.azisaba.lgw.eventteammanager.sql.MySQLConnectionData;

@Getter
@RequiredArgsConstructor
public class ConfigLoader {

  private final EventTeamManager plugin;

  private MySQLConnectionData mySQLConnectionData;
  private List<String> killCountWorlds;

  private int victoryPoint;

  public void load() {
    plugin.saveDefaultConfig();
    plugin.reloadConfig();

    String mysqlHost;
    String mysqlUser;
    int mysqlPort;
    String mysqlPassword;
    String database;

    try {
      mysqlHost = plugin.getConfig().getString("mysql.host");
      mysqlUser = plugin.getConfig().getString("mysql.user");
      mysqlPort = plugin.getConfig().getInt("mysql.port", 3306);
      mysqlPassword = plugin.getConfig().getString("mysql.password");
      database = plugin.getConfig().getString("mysql.database");

      assert mysqlHost != null && mysqlUser != null && mysqlPassword != null && database != null;
    } catch (Exception e) {
      plugin.getLogger()
          .severe("Failed to load MySQL connection data. Please check your config.yml.");
      plugin.getLogger().severe("Disabling plugin...");
      plugin.getServer().getPluginManager().disablePlugin(plugin);
      return;
    }

    mySQLConnectionData = new MySQLConnectionData(
        mysqlHost,
        mysqlPort,
        database,
        mysqlUser,
        mysqlPassword
    );

    killCountWorlds = plugin.getConfig().getStringList("kill-count-worlds");
    if (killCountWorlds == null) {
      killCountWorlds = Collections.emptyList();
    } else {
      killCountWorlds = Collections.unmodifiableList(killCountWorlds);
    }

    victoryPoint = plugin.getConfig().getInt("victory-point", 80);
  }
}
