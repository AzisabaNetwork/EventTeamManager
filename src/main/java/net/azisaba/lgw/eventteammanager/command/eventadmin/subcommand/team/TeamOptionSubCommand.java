package net.azisaba.lgw.eventteammanager.command.eventadmin.subcommand.team;

import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import net.azisaba.lgw.core.utils.Chat;
import net.azisaba.lgw.eventteammanager.EventTeamManager;
import net.azisaba.lgw.eventteammanager.team.CustomTeam;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

@RequiredArgsConstructor
public class TeamOptionSubCommand implements CommandExecutor {

  private static final List<String> HELP = Arrays.asList(
      Chat.f("&c/<label> <arg0> <arg1> <TeamName> setcolor <color>")
  );
  private final EventTeamManager plugin;

  @Override
  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    if (args.length <= 3) {
      for (String line : HELP) {
        sender.sendMessage(
            line.replace("<label>", label)
                .replace("<arg0>", args[0])
                .replace("<arg1>", args[1])
        );
      }
      return true;
    }

    String teamName = args[2];
    String action = args[3];

    CustomTeam team = plugin.getTeamContainer().getTeam(teamName);

    if (team == null) {
      sender.sendMessage(Chat.f("&e{0}&cという名前のチームが存在しません。", teamName));
      return true;
    }

    if (action.equalsIgnoreCase("setcolor") || action.equalsIgnoreCase("color")) {
      if (args.length < 5) {
        sender.sendMessage(Chat.f("&c色を指定してください"));
        return true;
      }

      String colorString = args[4];
      ChatColor color;
      try {
        color = ChatColor.valueOf(colorString.toUpperCase());
      } catch (IllegalArgumentException e) {
        color = ChatColor.getByChar(colorString.replace("&", ""));
      }

      if (color == null) {
        sender.sendMessage(
            Chat.f("&e{0}&cという色が見つかりませんでした。カラーコードかChatColor名で指定してください。", colorString));
        return true;
      }

      if (!color.isColor()) {
        sender.sendMessage(Chat.f("&e{0}&cは色ではなくフォーマット文字であるため使用できません。", color.name()));
        return true;
      }

      if (!plugin.getTeamTableAdapter().setTeamColor(team.getIndex(), color)) {
        sender.sendMessage(Chat.f("&cチームの色の変更に失敗しました。"));
        return true;
      }

      team.setColor(color);
      sender.sendMessage(Chat.f("&aチームの色を {0}{1} &aに変更しました。", color.toString(), color.name()));

      plugin.getEventStatusDisplay().removeAllHologram();
      plugin.getEventStatusDisplay().updateForAllOnlinePlayers();
    }
    return true;
  }
}
