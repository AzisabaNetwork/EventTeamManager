package net.azisaba.lgw.eventteammanager.command.eventadmin;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import net.azisaba.lgw.core.utils.Chat;
import net.azisaba.lgw.eventteammanager.EventTeamManager;
import net.azisaba.lgw.eventteammanager.command.eventadmin.subcommand.HologramSubCommand;
import net.azisaba.lgw.eventteammanager.command.eventadmin.subcommand.ResetSubCommand;
import net.azisaba.lgw.eventteammanager.command.eventadmin.subcommand.ScoreStatusSubCommand;
import net.azisaba.lgw.eventteammanager.command.eventadmin.subcommand.ShopSubCommand;
import net.azisaba.lgw.eventteammanager.command.eventadmin.subcommand.TeamSubCommand;
import net.azisaba.lgw.eventteammanager.command.eventadmin.subcommand.TopSubCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class EventAdminCommand implements CommandExecutor {

  private final EventTeamManager plugin;
  private final Map<String, CommandExecutor> subCommands = new HashMap<>();

  public EventAdminCommand(EventTeamManager plugin) {
    this.plugin = plugin;

    subCommands.put("shop", new ShopSubCommand(plugin));
    subCommands.put("team", new TeamSubCommand(plugin));
    subCommands.put("top", new TopSubCommand(plugin));
    subCommands.put("hologram", new HologramSubCommand(plugin));
    subCommands.put("reset", new ResetSubCommand(plugin));
    subCommands.put("status", new ScoreStatusSubCommand(plugin));
  }

  @Override
  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    if (args.length == 0) {
      sendUsage(sender, label);
      return true;
    }

    String subCommandStr = args[0].toLowerCase(Locale.ROOT);

    if (subCommandStr.equalsIgnoreCase("holo")) {
      subCommandStr = "hologram";
    }

    CommandExecutor subCommand = subCommands.get(subCommandStr);
    if (subCommand == null) {
      sendUsage(sender, label);
      return true;
    }

    subCommand.onCommand(sender, cmd, label, args);
    return true;
  }

  private void sendUsage(CommandSender sender, String label) {
    sender.sendMessage(Chat.f("&c/{0} team <create/join/option>", label));
    sender.sendMessage(Chat.f("&c/{0} shop <add/layout>", label));
    sender.sendMessage(Chat.f("&c/{0} hologram <set/delete>", label));
    sender.sendMessage(Chat.f("&c/{0} top", label));
    sender.sendMessage(Chat.f("&c/{0} status", label));
    sender.sendMessage(Chat.f("&c/{0} reset", label));
  }
}