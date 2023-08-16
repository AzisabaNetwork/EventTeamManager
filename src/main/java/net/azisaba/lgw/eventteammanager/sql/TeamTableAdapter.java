package net.azisaba.lgw.eventteammanager.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import net.azisaba.lgw.eventteammanager.team.CustomTeam;
import org.bukkit.ChatColor;

public class TeamTableAdapter {

  private static final String TABLE_NAME = "teams";

  private final MySQLConnector connector;

  public TeamTableAdapter(MySQLConnector connector) {
    this.connector = connector;
  }

  public boolean createTable() {
    try (Connection con = connector.getHikariDataSource().getConnection()) {
      con.createStatement().executeUpdate(
          "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ("
              + "team_index TINYINT NOT NULL,"
              + "team_name VARCHAR(32) NOT NULL,"
              + "team_color VARCHAR(16) DEFAULT 'WHITE' NOT NULL,"
              + "PRIMARY KEY (team_index)"
              + ");"
      );
      return true;
    } catch (SQLException e) {
      e.printStackTrace();
      return false;
    }
  }

  public CustomTeam createTeam(String name) {
    try (Connection con = connector.getHikariDataSource().getConnection()) {
      ResultSet result = con.createStatement()
          .executeQuery("SELECT `team_index` FROM " + TABLE_NAME + " ORDER BY team_index ASC;");

      int preferredIndex = 0;
      while (result.next()) {
        int index = result.getInt("team_index");
        if (index != preferredIndex) {
          break;
        }
        preferredIndex++;
      }

      PreparedStatement stm = con.prepareStatement(
          "INSERT INTO " + TABLE_NAME + " (team_index, team_name) VALUES (?, ?);");

      stm.setInt(1, preferredIndex);
      stm.setString(2, name);

      if (stm.executeUpdate() == 1) {
        return new CustomTeam(name, preferredIndex, ChatColor.WHITE);
      } else {
        return null;
      }
    } catch (SQLException e) {
      e.printStackTrace();
      return null;
    }
  }

  public boolean deleteTeam(int index) {
    try (Connection con = connector.getHikariDataSource().getConnection()) {
      PreparedStatement stm = con.prepareStatement(
          "DELETE FROM " + TABLE_NAME + " WHERE team_index = ?;");

      stm.setInt(1, index);

      return stm.executeUpdate() == 1;
    } catch (SQLException e) {
      e.printStackTrace();
      return false;
    }
  }

  public List<CustomTeam> getAllTeams() {
    try (Connection con = connector.getHikariDataSource().getConnection()) {
      ResultSet result = con.createStatement().executeQuery("SELECT * FROM " + TABLE_NAME + ";");

      List<CustomTeam> customTeams = new ArrayList<>();
      while (result.next()) {
        ChatColor color;
        try {
          color = ChatColor.valueOf(result.getString("team_color"));
        } catch (IllegalArgumentException | SQLException e) {
          color = ChatColor.WHITE;
        }

        customTeams.add(
            new CustomTeam(
                result.getString("team_name"),
                result.getInt("team_index"),
                color
            )
        );
      }

      return customTeams;
    } catch (SQLException e) {
      e.printStackTrace();
      return null;
    }
  }

  public boolean setTeamColor(int teamIndex, ChatColor color) {
    try (Connection con = connector.getHikariDataSource().getConnection()) {
      PreparedStatement stm = con.prepareStatement(
          "UPDATE " + TABLE_NAME + " SET team_color = ? WHERE team_index = ?;");

      stm.setString(1, color.name());
      stm.setInt(2, teamIndex);

      return stm.executeUpdate() == 1;
    } catch (SQLException e) {
      e.printStackTrace();
      return false;
    }
  }

  public boolean flushAllData() {
    try (Connection con = connector.getHikariDataSource().getConnection()) {
      con.createStatement().executeUpdate("TRUNCATE TABLE " + TABLE_NAME + ";");
      return true;
    } catch (SQLException e) {
      e.printStackTrace();
      return false;
    }
  }
}