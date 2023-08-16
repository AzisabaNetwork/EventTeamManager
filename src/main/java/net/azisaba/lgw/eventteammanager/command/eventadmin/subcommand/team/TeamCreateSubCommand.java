package net.azisaba.lgw.eventteammanager.command.eventadmin.subcommand.team;

import lombok.RequiredArgsConstructor;
import net.azisaba.lgw.core.utils.Chat;
import net.azisaba.lgw.eventteammanager.EventTeamManager;
import net.azisaba.lgw.eventteammanager.team.CustomTeam;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

@RequiredArgsConstructor
public class TeamCreateSubCommand implements CommandExecutor {

  private final EventTeamManager plugin;

  @Override
  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    if (args.length < 3) {
      sender.sendMessage(Chat.f("&c/{0} {1} {2} <チーム名>", label, args[0], args[1]));
      return true;
    }
    String teamName = args[2];

    if (plugin.getTeamContainer().getTeam(teamName) != null) {
      sender.sendMessage(Chat.f("&cそのチームは既に存在します"));
      return true;
    }

    CustomTeam team = plugin.getTeamTableAdapter().createTeam(teamName);
    if (team == null) {
      sender.sendMessage(Chat.f("&cチームの作成に失敗しました"));
      return true;
    }

    plugin.getTeamContainer().addTeam(team);
    sender.sendMessage(Chat.f("&aチームの作成に成功しました"));

    plugin.getEventStatusDisplay().removeAllHologram();
    plugin.getEventStatusDisplay().updateForAllOnlinePlayers();
    return true;
  }
}
