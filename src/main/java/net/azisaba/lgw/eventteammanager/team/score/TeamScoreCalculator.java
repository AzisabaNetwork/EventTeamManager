package net.azisaba.lgw.eventteammanager.team.score;

import lombok.RequiredArgsConstructor;
import net.azisaba.lgw.eventteammanager.EventTeamManager;
import net.azisaba.lgw.eventteammanager.team.CustomTeam;

@RequiredArgsConstructor
public class TeamScoreCalculator {

  private final EventTeamManager plugin;

  public double calculateScore(CustomTeam team) {
    long totalPoint = plugin.getPlayerTableAdapter().getTotalPoints(team);
    int playerCount = plugin.getPlayerTableAdapter().getPlayerCount(team);

    if (playerCount == 0) {
      return 0;
    }

    return (double) totalPoint / playerCount;
  }
}
