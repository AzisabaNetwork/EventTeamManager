package net.azisaba.lgw.eventteammanager.command.eventadmin.subcommand;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import net.azisaba.lgw.core.utils.Chat;
import net.azisaba.lgw.eventteammanager.EventTeamManager;
import net.azisaba.lgw.eventteammanager.command.eventadmin.subcommand.team.TeamCreateSubCommand;
import net.azisaba.lgw.eventteammanager.command.eventadmin.subcommand.team.TeamJoinSubCommand;
import net.azisaba.lgw.eventteammanager.command.eventadmin.subcommand.team.TeamOptionSubCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class TeamSubCommand implements CommandExecutor {

  private final Map<String, CommandExecutor> subCommands = new HashMap<>();

  public TeamSubCommand(EventTeamManager plugin) {
    subCommands.put("join", new TeamJoinSubCommand(plugin));
    subCommands.put("create", new TeamCreateSubCommand(plugin));
    subCommands.put("option", new TeamOptionSubCommand(plugin));
  }

  @Override
  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    if (args.length == 1) {
      sendUsage(sender, label, args);
      return true;
    }

    String subCommandName = args[1].toLowerCase(Locale.ROOT);

    // aliases
    if (subCommandName.equalsIgnoreCase("add")) {
      subCommandName = "create";
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
    sender.sendMessage(Chat.f("&c/{0} {1} join <TeamName>, [Players...]", label, args[0]));
    sender.sendMessage(Chat.f("&c/{0} {1} create <TeamName>", label, args[0]));
    sender.sendMessage(Chat.f("&c/{0} {1} option <...>", label, args[0]));
  }
}
