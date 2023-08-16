package net.azisaba.lgw.eventteammanager.command.eventadmin.subcommand;

import com.google.common.base.Strings;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReentrantLock;
import lombok.RequiredArgsConstructor;
import net.azisaba.lgw.core.utils.Chat;
import net.azisaba.lgw.eventteammanager.EventTeamManager;
import net.azisaba.lgw.eventteammanager.team.CustomTeam;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

@RequiredArgsConstructor
public class TopSubCommand implements CommandExecutor {

  private final EventTeamManager plugin;

  private String cache;
  private long cacheExpireAt = 0;

  private final ReentrantLock lock = new ReentrantLock();

  @Override
  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    if (cacheExpireAt > System.currentTimeMillis()) {
      sender.sendMessage(cache);
      return true;
    }

    sender.sendMessage(Chat.f("&eデータを取得中..."));
    Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
      lock.lock();
      try {
        if (cacheExpireAt > System.currentTimeMillis()) {
          sender.sendMessage(cache);
          return;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

        StringBuilder result = new StringBuilder();

        result.append(
            Chat.f("&7{0} &e貢献度上位者 &8({1}) &7 {0}\n",
                Strings.repeat("=", 10),
                sdf.format(new Date())
            )
        );

        for (CustomTeam team : plugin.getTeamContainer().getAllTeams()) {
          List<Entry<String, Integer>> data = plugin.getPlayerTableAdapter()
              .getTopPlayers(team, 10);

          result.append(Chat.f("{0}{1}&r:\n", team.getColor(), team.getName()));
          if (data == null) {
            result.append(Chat.f("&r- &cデータの取得に失敗しました\n"));
            continue;
          }

          for (Entry<String, Integer> entry : data) {
            result.append(Chat.f("&r- {0}{1}&r: &e{2}&7pt\n",
                team.getColor(),
                entry.getKey(),
                entry.getValue())
            );
          }
        }

        result.append(ChatColor.GRAY).append(Strings.repeat("=", 40));

        cache = result.toString();
        cacheExpireAt = System.currentTimeMillis() + 10000L;

        sender.sendMessage(cache);
      } finally {
        lock.unlock();
      }
    });

    return true;
  }
}
