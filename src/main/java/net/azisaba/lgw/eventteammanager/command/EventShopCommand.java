package net.azisaba.lgw.eventteammanager.command;

import lombok.RequiredArgsConstructor;
import net.azisaba.lgw.core.utils.Chat;
import net.azisaba.lgw.eventteammanager.EventTeamManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
public class EventShopCommand implements CommandExecutor {

  private final EventTeamManager plugin;

  @Override
  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    if (!(sender instanceof Player)) {
      sender.sendMessage("このコマンドはプレイヤーのみ実行可能です。");
      return true;
    }

    if (plugin.getEventShopItemContainer().getItems().size() == 0) {
      sender.sendMessage(Chat.f("&e現在イベントショップは開店していません"));
      return true;
    }

    Player p = (Player) sender;
    p.openInventory(plugin.getEventShopGUIBuilder().get());
    return true;
  }
}
