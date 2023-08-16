package net.azisaba.lgw.eventteammanager.listener;

import lombok.RequiredArgsConstructor;
import net.azisaba.lgw.eventteammanager.EventTeamManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

@RequiredArgsConstructor
public class PlayerNameUpdateListener implements Listener {

  private final EventTeamManager plugin;

  @EventHandler
  public void onJoin(PlayerJoinEvent e) {
    Player p = e.getPlayer();

    Bukkit.getScheduler().runTaskAsynchronously(plugin,
        () -> plugin.getPlayerTableAdapter().updatePlayerName(p.getUniqueId(), p.getName())
    );
  }

}
