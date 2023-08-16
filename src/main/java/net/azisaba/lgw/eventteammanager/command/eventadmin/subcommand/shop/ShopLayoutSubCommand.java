package net.azisaba.lgw.eventteammanager.command.eventadmin.subcommand.shop;

import lombok.RequiredArgsConstructor;
import net.azisaba.lgw.core.utils.Chat;
import net.azisaba.lgw.eventteammanager.EventTeamManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
public class ShopLayoutSubCommand implements CommandExecutor {

  private final EventTeamManager plugin;

  @Override
  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    if (!(sender instanceof Player)) {
      sender.sendMessage(Chat.f("&cこのコマンドはプレイヤーのみ実行可能です。"));
      return true;
    }

    Player p = (Player) sender;
    p.openInventory(plugin.getEventShopGUIBuilder().getEditInventory());
    return true;
  }
}