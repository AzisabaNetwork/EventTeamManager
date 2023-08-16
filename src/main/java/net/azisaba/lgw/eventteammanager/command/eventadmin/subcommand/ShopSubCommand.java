package net.azisaba.lgw.eventteammanager.command.eventadmin.subcommand;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import net.azisaba.lgw.core.utils.Chat;
import net.azisaba.lgw.eventteammanager.EventTeamManager;
import net.azisaba.lgw.eventteammanager.command.eventadmin.subcommand.shop.ShopAddSubCommand;
import net.azisaba.lgw.eventteammanager.command.eventadmin.subcommand.shop.ShopLayoutSubCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ShopSubCommand implements CommandExecutor {

  private final Map<String, CommandExecutor> subCommands = new HashMap<>();

  public ShopSubCommand(EventTeamManager plugin) {
    subCommands.put("add", new ShopAddSubCommand(plugin));
    subCommands.put("layout", new ShopLayoutSubCommand(plugin));
  }

  @Override
  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    if (args.length == 1) {
      sendUsage(sender, label, args);
      return true;
    }

    String subCommandName = args[1].toLowerCase(Locale.ROOT);

    // aliases
    if (subCommandName.equalsIgnoreCase("create")) {
      subCommandName = "add";
    }

    CommandExecutor subCommand = subCommands.get(subCommandName);
    if (subCommand == null) {
      sendUsage(sender, label, args);
      return true;
    }

    subCommand.onCommand(sender, cmd, label, args);
    return true;
  }

  public void sendUsage(CommandSender sender, String label, String[] args) {
    sender.sendMessage(Chat.f("&c/{0} {1} create <必要ポイント>", label, args[0]));
    sender.sendMessage(Chat.f("&c/{0} {1} layout", label, args[0]));
  }
}
