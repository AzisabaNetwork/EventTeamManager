package net.azisaba.lgw.eventteammanager;

import lombok.Getter;
import net.azisaba.lgw.eventteammanager.command.EventShopCommand;
import net.azisaba.lgw.eventteammanager.command.event.EventCommand;
import net.azisaba.lgw.eventteammanager.command.eventadmin.EventAdminCommand;
import net.azisaba.lgw.eventteammanager.config.ConfigLoader;
import net.azisaba.lgw.eventteammanager.config.HologramConfig;
import net.azisaba.lgw.eventteammanager.listener.EventShopEditListener;
import net.azisaba.lgw.eventteammanager.listener.EventShopListener;
import net.azisaba.lgw.eventteammanager.listener.KillPointListener;
import net.azisaba.lgw.eventteammanager.listener.VictoryPointListener;
import net.azisaba.lgw.eventteammanager.listener.hologram.ScoreStatusDisplayListener;
import net.azisaba.lgw.eventteammanager.shop.EventShopGUIBuilder;
import net.azisaba.lgw.eventteammanager.shop.EventShopItemContainer;
import net.azisaba.lgw.eventteammanager.shop.PurchaseConfirmGUIBuilder;
import net.azisaba.lgw.eventteammanager.sql.MySQLConnector;
import net.azisaba.lgw.eventteammanager.sql.PlayerTableAdapter;
import net.azisaba.lgw.eventteammanager.sql.TeamTableAdapter;
import net.azisaba.lgw.eventteammanager.team.BelongTeamCache;
import net.azisaba.lgw.eventteammanager.team.TeamContainer;
import net.azisaba.lgw.eventteammanager.team.score.TeamScoreCalculator;
import net.azisaba.lgw.eventteammanager.team.score.display.EventStatusDisplay;
import net.azisaba.lgw.eventteammanager.util.PointCache;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

@Getter
public class EventTeamManager extends JavaPlugin {

  private ConfigLoader configLoader;
  private HologramConfig hologramConfig;

  private MySQLConnector mysqlConnector;
  private PlayerTableAdapter playerTableAdapter;
  private TeamTableAdapter teamTableAdapter;

  private EventShopItemContainer eventShopItemContainer;
  private EventShopGUIBuilder eventShopGUIBuilder;
  private PurchaseConfirmGUIBuilder purchaseConfirmGUIBuilder;

  private BelongTeamCache belongTeamCache;
  private TeamScoreCalculator teamScoreCalculator;
  private EventStatusDisplay eventStatusDisplay;

  private TeamContainer teamContainer;
  private PointCache pointCache;

  private BukkitTask sqlUpdateTask;

  @Override
  public void onEnable() {
    configLoader = new ConfigLoader(this);
    configLoader.load();
    hologramConfig = new HologramConfig(this);
    hologramConfig.load();

    mysqlConnector = new MySQLConnector(configLoader.getMySQLConnectionData());
    mysqlConnector.connect();

    playerTableAdapter = new PlayerTableAdapter(mysqlConnector);
    teamTableAdapter = new TeamTableAdapter(mysqlConnector);

    if (!playerTableAdapter.createTable() || !teamTableAdapter.createTable()) {
      Bukkit.getLogger().severe("Failed to create MySQL table. Disabling plugin...");
      Bukkit.getPluginManager().disablePlugin(this);
      return;
    }

    eventShopItemContainer = new EventShopItemContainer(this);
    eventShopItemContainer.load();
    eventShopGUIBuilder = new EventShopGUIBuilder(this);
    eventShopGUIBuilder.load();
    purchaseConfirmGUIBuilder = new PurchaseConfirmGUIBuilder(this);

    belongTeamCache = new BelongTeamCache(this);
    teamScoreCalculator = new TeamScoreCalculator(this);
    eventStatusDisplay = new EventStatusDisplay(this);

    teamContainer = new TeamContainer(teamTableAdapter);
    teamContainer.load();

    pointCache = new PointCache(this, playerTableAdapter);
    pointCache.runTask();

    Bukkit.getPluginManager().registerEvents(new KillPointListener(this), this);
    Bukkit.getPluginManager().registerEvents(new VictoryPointListener(this), this);
    Bukkit.getPluginManager().registerEvents(new EventShopListener(this), this);
    Bukkit.getPluginManager().registerEvents(new EventShopEditListener(this), this);

    Bukkit.getPluginManager().registerEvents(new ScoreStatusDisplayListener(this), this);

    Bukkit.getPluginCommand("eventadmin").setExecutor(new EventAdminCommand(this));
    Bukkit.getPluginCommand("eventshop").setExecutor(new EventShopCommand(this));
    Bukkit.getPluginCommand("event").setExecutor(new EventCommand(this));

    this.sqlUpdateTask = Bukkit.getScheduler().runTaskTimerAsynchronously(this,
        () -> getPointCache().executeSQLUpdate(),
        20L * 30L, 20L * 30L
    );

    Location holoLoc = hologramConfig.getEventStatusHologramLocation();
    if (holoLoc != null) {
      eventStatusDisplay.updateForAllOnlinePlayers();
    }

    Bukkit.getLogger().info(getName() + " enabled.");
  }

  @Override
  public void onDisable() {
    if (eventStatusDisplay != null) {
      eventStatusDisplay.removeAllHologram();
    }

    if (sqlUpdateTask != null) {
      sqlUpdateTask.cancel();
    }
    if (pointCache != null) {
      pointCache.executeSQLUpdate();
    }

    if (mysqlConnector != null) {
      mysqlConnector.close();
    }

    if (eventShopItemContainer != null) {
      eventShopItemContainer.save();
    }
    if (eventShopGUIBuilder != null) {
      eventShopGUIBuilder.save();
    }
    Bukkit.getLogger().info(getName() + " disabled.");
  }
}
