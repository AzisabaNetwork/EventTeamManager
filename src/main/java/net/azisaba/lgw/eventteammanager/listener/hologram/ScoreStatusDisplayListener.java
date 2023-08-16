package net.azisaba.lgw.eventteammanager.listener.hologram;

import lombok.RequiredArgsConstructor;
import net.azisaba.lgw.eventteammanager.EventTeamManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;

@RequiredArgsConstructor
public class ScoreStatusDisplayListener implements Listener {

  private final EventTeamManager plugin;

  @EventHandler
  public void onChangeWorld(PlayerChangedWorldEvent e) {
    Player p = e.getPlayer();
    Location loc = plugin.getHologramConfig().getEventStatusHologramLocation();

    if (loc == null || p.getWorld() != loc.getWorld()) {
      return;
    }

    plugin.getEventStatusDisplay().updateAsync(p);
  }

  @EventHandler
  public void onJoin(PlayerJoinEvent e) {
    Player p = e.getPlayer();
    Location loc = plugin.getHologramConfig().getEventStatusHologramLocation();

    if (loc == null || p.getWorld() != loc.getWorld()) {
      return;
    }

    plugin.getEventStatusDisplay().updateAsync(p);
  }
}
