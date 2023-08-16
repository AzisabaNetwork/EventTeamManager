package net.azisaba.lgw.eventteammanager.listener;

import lombok.RequiredArgsConstructor;
import net.azisaba.lgw.eventteammanager.EventTeamManager;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

@RequiredArgsConstructor
public class KillPointListener implements Listener {

  private final EventTeamManager plugin;

  @EventHandler
  public void onKill(PlayerDeathEvent e) {
    Player killer = e.getEntity().getKiller();

    if (killer == null) {
      return;
    }

    World world = killer.getWorld();

    if (!plugin.getConfigLoader().getKillCountWorlds().contains(world.getName())) {
      return;
    }

    plugin.getPointCache().addPoints(killer.getUniqueId(), 1);
  }
}
