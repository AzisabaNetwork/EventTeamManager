package net.azisaba.lgw.eventteammanager.config;

import java.io.File;
import javax.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import net.azisaba.lgw.eventteammanager.EventTeamManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;

@RequiredArgsConstructor
public class HologramConfig {

  private final EventTeamManager plugin;

  private Location eventStatusHologramLocation;

  public void load() {
    YamlConfiguration conf = YamlConfiguration.loadConfiguration(getFile());

    if (!conf.isSet("location.world") ||
        !conf.isSet("location.x") ||
        !conf.isSet("location.y") ||
        !conf.isSet("location.z")
    ) {
      eventStatusHologramLocation = null;
      return;
    }

    World world = Bukkit.getWorld(conf.getString("location.world"));
    double x = conf.getDouble("location.x");
    double y = conf.getDouble("location.y");
    double z = conf.getDouble("location.z");

    if (world == null) {
      eventStatusHologramLocation = null;
      return;
    }

    eventStatusHologramLocation = new Location(world, x, y, z);
  }

  public boolean save(@Nullable Location loc) {
    this.eventStatusHologramLocation = loc;

    YamlConfiguration conf = new YamlConfiguration();

    if (loc != null) {
      conf.set("location.world", loc.getWorld().getName());
      conf.set("location.x", loc.getX());
      conf.set("location.y", loc.getY());
      conf.set("location.z", loc.getZ());
    }

    try {
      conf.save(getFile());
      return true;
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }

  public Location getEventStatusHologramLocation() {
    if (eventStatusHologramLocation == null) {
      return null;
    }
    return eventStatusHologramLocation.clone();
  }

  private File getFile() {
    return new File(plugin.getDataFolder(), "hologram.yml");
  }
}
