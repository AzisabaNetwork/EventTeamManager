package net.azisaba.lgw.eventteammanager.team;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import lombok.RequiredArgsConstructor;
import net.azisaba.lgw.eventteammanager.sql.TeamTableAdapter;

@RequiredArgsConstructor
public class TeamContainer {

  private final TeamTableAdapter teamTableAdapter;
  private final HashMap<String, CustomTeam> teamNameToCustomTeam = new HashMap<>();

  public boolean load() {
    List<CustomTeam> teams = teamTableAdapter.getAllTeams();
    if (teams == null) {
      return false;
    }

    for (CustomTeam team : teams) {
      teamNameToCustomTeam.put(team.getName(), team);
    }

    return true;
  }

  public void addTeam(CustomTeam team) {
    teamNameToCustomTeam.put(team.getName(), team);
  }

  public CustomTeam getTeam(String name) {
    return teamNameToCustomTeam.get(name);
  }

  public CustomTeam getTeamByIndex(int index) {
    for (CustomTeam team : teamNameToCustomTeam.values()) {
      if (team.getIndex() == index) {
        return team;
      }
    }
    return null;
  }

  public List<CustomTeam> getAllTeams() {
    return new ArrayList<>(teamNameToCustomTeam.values());
  }
}
