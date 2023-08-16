package net.azisaba.lgw.eventteammanager.listener;

import lombok.RequiredArgsConstructor;
import net.azisaba.lgw.core.events.MatchFinishedEvent;
import net.azisaba.lgw.core.util.BattleTeam;
import net.azisaba.lgw.eventteammanager.EventTeamManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

@RequiredArgsConstructor
public class VictoryPointListener implements Listener {

  private final EventTeamManager plugin;

  @EventHandler
  public void onVictory(MatchFinishedEvent e) {
    int points = plugin.getConfigLoader().getVictoryPoint();

    for (BattleTeam victoryTeam : e.getWinners()) {
      for (Player player : e.getTeamPlayers(victoryTeam)) {
        plugin.getPointCache().addPoints(player.getUniqueId(), points);
      }
    }
  }
}
