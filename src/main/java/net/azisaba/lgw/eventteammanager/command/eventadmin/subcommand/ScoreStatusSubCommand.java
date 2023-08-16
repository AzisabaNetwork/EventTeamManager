package net.azisaba.lgw.eventteammanager.command.eventadmin.subcommand;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import lombok.RequiredArgsConstructor;
import net.azisaba.lgw.core.utils.Chat;
import net.azisaba.lgw.eventteammanager.EventTeamManager;
import net.azisaba.lgw.eventteammanager.team.CustomTeam;
import org.apache.logging.log4j.util.Strings;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
public class ScoreStatusSubCommand implements CommandExecutor {

  private final EventTeamManager plugin;

  private final ReentrantLock lock = new ReentrantLock();

  private String cache;
  private long cacheExpireAt;

  @Override
  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    lock.lock();
    try {
      if (System.currentTimeMillis() < cacheExpireAt) {
        sender.sendMessage(cache);
        return true;
      }
    } finally {
      lock.unlock();
    }

    sender.sendMessage(Chat.f("&eデータを確認中..."));

    Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
      HashMap<CustomTeam, Double> map = new HashMap<>();

      for (CustomTeam team : plugin.getTeamContainer().getAllTeams()) {
        map.put(team, plugin.getTeamScoreCalculator().calculateScore(team));
      }

      Player p = null;
      if (sender instanceof Player) {
        p = (Player) sender;
      }

      List<String> lines = plugin.getEventStatusDisplay().getScoreFormatter().chat(p, map);

      lock.lock();
      try {
        cache = Strings.join(lines, '\n');
        cacheExpireAt = System.currentTimeMillis() + 1000 * 30;
      } finally {
        lock.unlock();
      }

      sender.sendMessage(cache);
    });

    return true;
  }
}
