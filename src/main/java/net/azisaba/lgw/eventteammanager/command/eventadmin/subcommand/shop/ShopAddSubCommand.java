package net.azisaba.lgw.eventteammanager.command.eventadmin.subcommand.shop;

import lombok.RequiredArgsConstructor;
import net.azisaba.lgw.core.utils.Chat;
import net.azisaba.lgw.eventteammanager.EventTeamManager;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@RequiredArgsConstructor
public class ShopAddSubCommand implements CommandExecutor {

  private final EventTeamManager plugin;

  @Override
  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    if (!(sender instanceof Player)) {
      sender.sendMessage(Chat.f("&cこのコマンドはプレイヤーのみ実行可能です。"));
      return true;
    }

    if (args.length < 3) {
      sender.sendMessage(Chat.f("&c/{0} {1} {2} <必要ポイント>", label, args[0], args[1]));
      return true;
    }

    Player p = (Player) sender;
    ItemStack item = p.getInventory().getItemInMainHand();

    if (item == null || item.getType() == Material.AIR) {
      sender.sendMessage(Chat.f("&c手にアイテムを持って実行してください"));
      return true;
    }

    int price;
    try {
      price = Integer.parseInt(args[2]);

      if (price < 0) {
        sender.sendMessage(Chat.f("&c必要ポイントは0以上である必要があります。"));
        return true;
      }
    } catch (Exception e) {
      sender.sendMessage(Chat.f("&c必要ポイントは整数を入力してください。"));
      return true;
    }

    plugin.getEventShopItemContainer().addItem(item.clone(), price);
    sender.sendMessage(Chat.f("&aアイテムを追加しました"));
    return true;
  }
}
