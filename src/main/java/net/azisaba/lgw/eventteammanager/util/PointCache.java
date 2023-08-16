package net.azisaba.lgw.eventteammanager.util;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;
import lombok.RequiredArgsConstructor;
import net.azisaba.lgw.eventteammanager.EventTeamManager;
import net.azisaba.lgw.eventteammanager.sql.PlayerTableAdapter;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

@RequiredArgsConstructor
public class PointCache {

  private final EventTeamManager plugin;
  private final PlayerTableAdapter adapter;

  private final AtomicReference<BukkitTask> updateTask = new AtomicReference<>();

  private final ReentrantLock lock = new ReentrantLock();
  private final HashMap<UUID, Integer> pointMap = new HashMap<>();

  public void addPoints(UUID uuid, int point) {
    lock.lock();
    try {
      pointMap.compute(uuid, (key, value) -> value == null ? point : value + point);
    } finally {
      lock.unlock();
    }
  }

  public int getPointsBeforeAddToDatabase(UUID uuid) {
    lock.lock();
    try {
      return pointMap.getOrDefault(uuid, 0);
    } finally {
      lock.unlock();
    }
  }

  public void executeSQLUpdate() {
    lock.lock();
    try {
      adapter.addPoints(pointMap);
      pointMap.clear();
    } finally {
      lock.unlock();
    }
  }

  public void runTask() {
    updateTask.getAndUpdate(current -> {
      if (current != null) {
        current.cancel();
      }
      return Bukkit.getScheduler()
          .runTaskTimerAsynchronously(plugin, this::executeSQLUpdate, 20L, 20L);
    });
  }

  public void clear() {
    lock.lock();
    try {
      pointMap.clear();
    } finally {
      lock.unlock();
    }
  }
}
