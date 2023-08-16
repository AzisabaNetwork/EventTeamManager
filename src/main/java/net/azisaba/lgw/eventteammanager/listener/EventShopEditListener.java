package net.azisaba.lgw.eventteammanager.listener;

import java.util.List;
import lombok.RequiredArgsConstructor;
import net.azisaba.lgw.core.utils.Chat;
import net.azisaba.lgw.eventteammanager.EventTeamManager;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

@RequiredArgsConstructor
public class EventShopEditListener implements Listener {

  private final EventTeamManager plugin;

  @EventHandler
  public void onCloseEditInventory(InventoryCloseEvent e) {
    Inventory inv = e.getInventory();

    if (inv.getTitle() == null ||
        !inv.getTitle().equals(plugin.getEventShopGUIBuilder().getEditInventoryTitle())) {
      return;
    }

    List<ItemStack> disappearedItems = plugin.getEventShopGUIBuilder().applyLayout(inv);
    if (disappearedItems.isEmpty()) {
      Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
        try {
          plugin.getEventShopGUIBuilder().save();
          e.getPlayer().sendMessage(Chat.f("&aレイアウトを保存しました"));
        } catch (Exception ex) {
          e.getPlayer().sendMessage(Chat.f("&cレイアウトの保存に失敗しました"));
          ex.printStackTrace();
        }
      });
      return;
    }

    for (ItemStack item : disappearedItems) {
      plugin.getEventShopItemContainer().removeItem(item);
    }

    e.getPlayer().sendMessage(Chat.f("&eレイアウトを保存しましたが、以下のアイテムがストアから削除されました"));

    for (ItemStack item : disappearedItems) {
      String name = item.getType().name();
      if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
        name = item.getItemMeta().getDisplayName();
      }
      e.getPlayer().sendMessage(Chat.f("&e- &f{0}", name));
    }
  }
}
