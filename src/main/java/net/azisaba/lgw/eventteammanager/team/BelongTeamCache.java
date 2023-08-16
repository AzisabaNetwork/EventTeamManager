package net.azisaba.lgw.eventteammanager.team;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import net.azisaba.lgw.eventteammanager.EventTeamManager;

@RequiredArgsConstructor
public class BelongTeamCache {

  private final EventTeamManager plugin;

  private final HashMap<UUID, CustomTeam> belong = new HashMap<>();
  private final List<UUID> notBelong = new ArrayList<>();

  public boolean hasCache(UUID uuid) {
    return belong.containsKey(uuid) || notBelong.contains(uuid);
  }

  public void setBelong(UUID uuid, CustomTeam team) {
    notBelong.remove(uuid);
    belong.put(uuid, team);
  }

  public CustomTeam getBelong(UUID uuid) {
    if (notBelong.contains(uuid)) {
      return null;
    }

    if (belong.containsKey(uuid)) {
      return belong.get(uuid);
    }

    int teamIndex = plugin.getPlayerTableAdapter().getTeamIndex(uuid);
    if (teamIndex < 0) {
      notBelong.add(uuid);
      return null;
    }
    
    CustomTeam team = plugin.getTeamContainer().getTeamByIndex(teamIndex);

    if (team == null) {
      notBelong.add(uuid);
      return null;
    }

    belong.put(uuid, team);
    return team;
  }
}
