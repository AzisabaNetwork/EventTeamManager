package net.azisaba.lgw.eventteammanager.command.eventadmin.subcommand;

import lombok.RequiredArgsConstructor;
import net.azisaba.lgw.core.utils.Chat;
import net.azisaba.lgw.eventteammanager.EventTeamManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
public class HologramSubCommand implements CommandExecutor {

  private final EventTeamManager plugin;

  @Override
  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    if (!(sender instanceof Player)) {
      sender.sendMessage(Chat.f("&cこのコマンドはプレイヤーのみ実行可能です"));
      return true;
    }

    if (args.length <= 1) {
      sendUsage(sender, label, args);
      return true;
    }

    if (args[1].equalsIgnoreCase("set") || args[1].equalsIgnoreCase("here")) {
      Player p = (Player) sender;
      plugin.getHologramConfig().save(p.getLocation());
      p.sendMessage(Chat.f("&aホログラムの位置を設定しました"));

      plugin.getEventStatusDisplay().removeAllHologram();
      plugin.getEventStatusDisplay().updateForAllOnlinePlayers();
    } else if (args[1].equalsIgnoreCase("delete") || args[1].equalsIgnoreCase("remove")) {
      plugin.getHologramConfig().save(null);
      plugin.getEventStatusDisplay().removeAllHologram();
      sender.sendMessage(Chat.f("&aホログラムを削除しました"));
    } else {
      sendUsage(sender, label, args);
    }

    return true;
  }

  private void sendUsage(CommandSender sender, String label, String[] args) {

  }
}
