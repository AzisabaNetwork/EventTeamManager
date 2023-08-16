package net.azisaba.lgw.eventteammanager.team.score.display;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.ReentrantLock;
import lombok.Getter;
import net.azisaba.lgw.eventteammanager.EventTeamManager;
import net.azisaba.lgw.eventteammanager.team.CustomTeam;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class EventStatusDisplay {

  private final EventTeamManager plugin;

  @Getter
  private final ScoreFormatter scoreFormatter;

  private final HashMap<UUID, Hologram> hologramMap = new HashMap<>();
  private final ReentrantLock lock = new ReentrantLock();

  private Map<CustomTeam, Double> cache = null;
  private long cacheExpireAt = 0;

  public EventStatusDisplay(EventTeamManager plugin) {
    this.plugin = plugin;
    this.scoreFormatter = new ScoreFormatter(plugin);
  }

  public void updateAsync(Player p) {
    Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
      updateCache();
      if (p.isOnline()) {
        displayUsingCache(p);
      }
    });
  }

  public void updateForAllOnlinePlayers() {
    Location loc = plugin.getHologramConfig().getEventStatusHologramLocation();

    Bukkit.getOnlinePlayers().stream()
        .filter(p -> p.getWorld() == loc.getWorld())
        .forEach(this::updateAsync);
  }

  public void removeAllHologram() {
    lock.lock();
    try {
      hologramMap.values().forEach(Hologram::removeAll);
      hologramMap.clear();
    } finally {
      lock.unlock();
    }
  }

  private void updateCache() {
    lock.lock();
    try {
      HashMap<CustomTeam, Double> map = new HashMap<>();
      for (CustomTeam team : plugin.getTeamContainer().getAllTeams()) {
        map.put(team, plugin.getTeamScoreCalculator().calculateScore(team));
      }

      cache = map;
      cacheExpireAt = System.currentTimeMillis() + 1000 * 30;
    } finally {
      lock.unlock();
    }
  }

  private void displayUsingCache(Player p) {
    Location loc = plugin.getHologramConfig().getEventStatusHologramLocation();
    if (loc == null) {
      return;
    }

    Hologram hologram;

    lock.lock();
    try {
      hologram = hologramMap.getOrDefault(p.getUniqueId(), null);
      if (hologram == null) {
        hologram = Hologram.create(loc);
        hologramMap.put(p.getUniqueId(), hologram);
      }
    } finally {
      lock.unlock();
    }

    final Hologram finalHologram = hologram;

    CompletableFuture<List<String>> result = plugin.getEventStatusDisplay().getScoreFormatter()
        .hologram(p, cache);

    result.thenAccept(lines -> {
      int currentHologramSize = finalHologram.getLines().size();
      for (int i = 0, size = lines.size(); i < size; i++) {
        if (currentHologramSize <= i) {
          finalHologram.addLine(lines.get(i));
          continue;
        }
        finalHologram.setLine(i, lines.get(i));
      }

      Bukkit.getScheduler().runTask(plugin, () -> finalHologram.update(p));
    });
  }
}
