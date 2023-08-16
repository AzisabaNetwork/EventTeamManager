package net.azisaba.lgw.eventteammanager.command.eventadmin.subcommand.team;

import lombok.RequiredArgsConstructor;
import net.azisaba.lgw.core.utils.Chat;
import net.azisaba.lgw.eventteammanager.EventTeamManager;
import net.azisaba.lgw.eventteammanager.team.CustomTeam;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
public class TeamJoinSubCommand implements CommandExecutor {

  private final EventTeamManager plugin;

  @Override
  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    if (args.length <= 3) {
      sendUsage(sender, label, args);
      return true;
    }

    String teamName = args[2];
    CustomTeam team = plugin.getTeamContainer().getTeam(teamName);

    if (team == null) {
      sender.sendMessage(Chat.f("&cそのチームは存在しません。"));
      return true;
    }

    for (int i = 3; i < args.length; i++) {
      String playerName = args[i];
      Player p = Bukkit.getPlayerExact(playerName);

      if (p == null) {
        sender.sendMessage(Chat.f("&cプレイヤー {0} はオフラインのため追加できません。", playerName));
        continue;
      }

      if (plugin.getPlayerTableAdapter().joinTeam(p.getUniqueId(), p.getName(), team)) {
        plugin.getBelongTeamCache().setBelong(p.getUniqueId(), team);
        sender.sendMessage(Chat.f("&aプレイヤー {0} をチームに追加しました。", playerName));
      } else {
        sender.sendMessage(Chat.f("&cエラーによりプレイヤー {0} をチームに追加できませんでした。", playerName));
      }
    }
    return true;
  }

  private void sendUsage(CommandSender sender, String label, String[] args) {
    sender.sendMessage(Chat.f("&c/{0} {1} {2} <TeamName> <Players...>", label, args[0], args[1]));
  }
}
