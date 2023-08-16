package net.azisaba.lgw.eventteammanager.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import net.azisaba.lgw.eventteammanager.team.CustomTeam;

@RequiredArgsConstructor
public class PlayerTableAdapter {

  private static final String TABLE_NAME = "players";

  private final MySQLConnector connector;

  public boolean createTable() {
    try (Connection con = connector.getHikariDataSource().getConnection()) {
      con.createStatement().executeUpdate(
          "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ("
              + "uuid VARCHAR(36) NOT NULL,"
              + "name VARCHAR(16) NOT NULL,"
              + "team_index TINYINT NOT NULL,"
              + "points INT UNSIGNED DEFAULT 0,"
              + "paid_points INT UNSIGNED DEFAULT 0,"
              + "PRIMARY KEY (uuid)"
              + ");"
      );
      return true;
    } catch (SQLException e) {
      e.printStackTrace();
      return false;
    }
  }

  public boolean joinTeam(UUID uuid, String mcid, CustomTeam team) {
    try (Connection con = connector.getHikariDataSource().getConnection()) {
      PreparedStatement stm = con.prepareStatement(
          "INSERT INTO " + TABLE_NAME + " (uuid, name, team_index) VALUES (?, ?, ?);");

      stm.setString(1, uuid.toString());
      stm.setString(2, mcid);
      stm.setInt(3, team.getIndex());

      return stm.executeUpdate() >= 1;
    } catch (SQLException e) {
      e.printStackTrace();
      return false;
    }
  }

  public boolean leaveTeam(UUID uuid) {
    try (Connection con = connector.getHikariDataSource().getConnection()) {
      PreparedStatement stm = con.prepareStatement(
          "DELETE FROM " + TABLE_NAME + " WHERE uuid = ?;");

      stm.setString(1, uuid.toString());

      return stm.executeUpdate() >= 1;
    } catch (SQLException e) {
      e.printStackTrace();
      return false;
    }
  }

  public void updatePlayerName(UUID uuid, String name) {
    try (Connection con = connector.getHikariDataSource().getConnection()) {
      PreparedStatement stm = con.prepareStatement(
          "UPDATE " + TABLE_NAME + " SET name = ? WHERE uuid = ?;");

      stm.setString(1, name);
      stm.setString(2, uuid.toString());

      stm.executeUpdate();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public int getTeamIndex(UUID uuid) {
    try (Connection con = connector.getHikariDataSource().getConnection()) {
      PreparedStatement stm = con.prepareStatement(
          "SELECT team_index FROM " + TABLE_NAME + " WHERE uuid = ?;");

      stm.setString(1, uuid.toString());

      ResultSet result = stm.executeQuery();
      if (!result.next()) {
        return -1;
      }

      return result.getInt("team_index");
    } catch (SQLException e) {
      e.printStackTrace();
      return -1;
    }
  }

  public int getPoint(UUID uuid) {
    try (Connection con = connector.getHikariDataSource().getConnection()) {
      PreparedStatement stm = con.prepareStatement(
          "SELECT points FROM " + TABLE_NAME + " WHERE uuid = ?;");

      stm.setString(1, uuid.toString());

      ResultSet result = stm.executeQuery();
      if (!result.next()) {
        return -1;
      }

      return result.getInt("points");
    } catch (SQLException e) {
      e.printStackTrace();
      return -1;
    }
  }

  public int getAvailablePoint(UUID uuid) {
    try (Connection con = connector.getHikariDataSource().getConnection()) {
      PreparedStatement stm = con.prepareStatement(
          "SELECT points - paid_points as available_points FROM " + TABLE_NAME
              + " WHERE uuid = ?;");

      stm.setString(1, uuid.toString());

      ResultSet result = stm.executeQuery();
      if (!result.next()) {
        return -1;
      }

      return result.getInt("available_points");
    } catch (SQLException e) {
      e.printStackTrace();
      return -1;
    }
  }

  public boolean addPoints(Map<UUID, Integer> pointMap) {
    if (pointMap.isEmpty()) {
      return true;
    }

    try (Connection con = connector.getHikariDataSource().getConnection()) {
      PreparedStatement stm = con.prepareStatement(
          "UPDATE " + TABLE_NAME + " SET points = points + ? WHERE uuid = ?;");

      for (UUID uuid : pointMap.keySet()) {
        stm.setInt(1, pointMap.get(uuid));
        stm.setString(2, uuid.toString());
        stm.addBatch();
      }

      return stm.executeBatch().length >= 1;
    } catch (SQLException e) {
      e.printStackTrace();
      return false;
    }
  }

  public boolean deductPoints(UUID uuid, int point) {
    try (Connection con = connector.getHikariDataSource().getConnection()) {
      PreparedStatement stm = con.prepareStatement(
          "UPDATE " + TABLE_NAME
              + " SET paid_points = paid_points + ? WHERE uuid = ? and points >= paid_points + ?;");

      stm.setInt(1, point);
      stm.setString(2, uuid.toString());
      stm.setInt(3, point);

      return stm.executeUpdate() >= 1;
    } catch (SQLException e) {
      e.printStackTrace();
      return false;
    }
  }

  public long getTotalPoints(CustomTeam team) {
    try (Connection con = connector.getHikariDataSource().getConnection()) {
      PreparedStatement stm = con.prepareStatement(
          "select sum(`points`) as `total_points` from " + TABLE_NAME + " where `team_index` = ?;");

      stm.setInt(1, team.getIndex());

      ResultSet result = stm.executeQuery();
      if (!result.next()) {
        return -1;
      }

      return result.getLong("total_points");
    } catch (SQLException e) {
      e.printStackTrace();
      return -1;
    }
  }

  public int getPlayerCount(CustomTeam team) {
    try (Connection con = connector.getHikariDataSource().getConnection()) {
      PreparedStatement stm = con.prepareStatement(
          "select count(`uuid`) as `player_count` from " + TABLE_NAME + " where `team_index` = ?;");

      stm.setInt(1, team.getIndex());

      ResultSet result = stm.executeQuery();
      if (!result.next()) {
        return -1;
      }

      return result.getInt("player_count");
    } catch (SQLException e) {
      e.printStackTrace();
      return -1;
    }
  }

  public List<Entry<String, Integer>> getTopPlayers(CustomTeam team, int limit) {
    try (Connection con = connector.getHikariDataSource().getConnection()) {
      PreparedStatement stm = con.prepareStatement(
          "select `name`, `points` from " + TABLE_NAME
              + " where `team_index` = ? order by `points` desc limit ?;");

      stm.setInt(1, team.getIndex());
      stm.setInt(2, limit);

      ResultSet result = stm.executeQuery();

      List<Entry<String, Integer>> list = new ArrayList<>();
      while (result.next()) {
        list.add(new AbstractMap.SimpleEntry<>(result.getString("name"), result.getInt("points")));
      }

      return list;
    } catch (SQLException e) {
      e.printStackTrace();
      return null;
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
