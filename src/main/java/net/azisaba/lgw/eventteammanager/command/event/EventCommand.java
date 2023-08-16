package net.azisaba.lgw.eventteammanager.command.event;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import lombok.RequiredArgsConstructor;
import net.azisaba.lgw.core.utils.Chat;
import net.azisaba.lgw.eventteammanager.EventTeamManager;
import net.azisaba.lgw.eventteammanager.team.CustomTeam;
import org.apache.logging.log4j.util.Strings;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
public class EventCommand implements CommandExecutor {

  private final EventTeamManager plugin;

  private final ReentrantLock lock = new ReentrantLock();

  private String statusCache;
  private long statusCacheExpireAt;

  @Override
  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    if (!(sender instanceof Player)) {
      sender.sendMessage(Chat.f("&cこのコマンドはプレイヤーのみ実行可能です。"));
      return true;
    }

    Player p = (Player) sender;

    if (args.length == 0) {
      sendUsage(sender, label);
      return true;
    }

    if (args[0].equalsIgnoreCase("join")) {
      if (plugin.getBelongTeamCache().getBelong(p.getUniqueId()) != null) {
        sender.sendMessage(Chat.f("&cあなたは既にチームに参加しています"));
        return true;
      }

      if (args.length < 2) {
        sender.sendMessage(Chat.f("&c参加したいチーム名を指定してください！", label));
        return true;
      }

      CustomTeam team = plugin.getTeamContainer().getTeam(args[1]);
      if (team == null) {
        sender.sendMessage(Chat.f("&e{0}&cという名前のチームは見つかりませんでした", args[1]));
        return true;
      }

      if (plugin.getPlayerTableAdapter().joinTeam(p.getUniqueId(), p.getName(), team)) {
        plugin.getBelongTeamCache().setBelong(p.getUniqueId(), team);
        sender.sendMessage(Chat.f("&aチームに参加しました"));

        Location holoLoc = plugin.getHologramConfig().getEventStatusHologramLocation();
        if (holoLoc == null || holoLoc.getWorld() != p.getWorld()) {
          return true;
        }

        plugin.getEventStatusDisplay().updateAsync(p);
      } else {
        sender.sendMessage(Chat.f("&cエラーによりチームに参加できませんでした"));
      }
    } else if (args[0].equalsIgnoreCase("status")) {
      lock.lock();
      try {
        if (System.currentTimeMillis() < statusCacheExpireAt) {
          sender.sendMessage(statusCache);
          return true;
        }
      } finally {
        lock.unlock();
      }

      sender.sendMessage(Chat.f("&eデータを取得中..."));

      Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
        lock.lock();
        try {
          if (System.currentTimeMillis() < statusCacheExpireAt) {
            sender.sendMessage(statusCache);
            return;
          }

          HashMap<CustomTeam, Double> map = new HashMap<>();

          for (CustomTeam team : plugin.getTeamContainer().getAllTeams()) {
            map.put(team, plugin.getTeamScoreCalculator().calculateScore(team));
          }

          List<String> lines = plugin.getEventStatusDisplay().getScoreFormatter().chat(p, map);

          statusCache = Strings.join(lines, '\n');
          statusCacheExpireAt = System.currentTimeMillis() + 1000 * 30;
        } finally {
          lock.unlock();
        }

        sender.sendMessage(statusCache);
      });
    } else if (args[0].equalsIgnoreCase("shop")) {
      p.openInventory(plugin.getEventShopGUIBuilder().get());
    } else {
      sendUsage(sender, label);
    }

    return true;
  }

  private void sendUsage(CommandSender sender, String label) {
    sender.sendMessage(Chat.f("&e/{0} join <チーム名> &7- &7チームに参加する", label));
    sender.sendMessage(Chat.f("&e/{0} shop &7- &7ショップを開く", label));
    sender.sendMessage(Chat.f("&e/{0} status &7- &7イベント状況を確認する", label));
  }
}