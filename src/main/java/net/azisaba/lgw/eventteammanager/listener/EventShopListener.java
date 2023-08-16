package net.azisaba.lgw.eventteammanager.listener;

import java.util.List;
import lombok.RequiredArgsConstructor;
import net.azisaba.lgw.core.utils.Chat;
import net.azisaba.lgw.eventteammanager.EventTeamManager;
import net.azisaba.lgw.eventteammanager.shop.PurchaseConfirmInventoryItemType;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

@RequiredArgsConstructor
public class EventShopListener implements Listener {

  private final EventTeamManager plugin;

  @EventHandler
  public void onClickInventory(InventoryClickEvent e) {
    if (e.getWhoClicked() == null || !(e.getWhoClicked() instanceof Player)) {
      return;
    }

    Player p = (Player) e.getWhoClicked();
    if (p.getOpenInventory().getTopInventory() == null) {
      return;
    }

    String title = p.getOpenInventory().getTopInventory().getTitle();
    if (title == null || !title.equals(plugin.getEventShopGUIBuilder().getTitle())) {
      return;
    }

    e.setCancelled(true);
    ItemStack clickedItem = e.getCurrentItem();

    if (clickedItem == null) {
      return;
    }

    boolean clickedSellingItem = false;
    for (ItemStack item : plugin.getEventShopItemContainer().getItems()) {
      if (item.equals(clickedItem)) {
        clickedSellingItem = true;
        break;
      }
    }

    if (!clickedSellingItem) {
      return;
    }

    int price = plugin.getEventShopItemContainer().getPrice(clickedItem);
    if (price < 0) {
      return;
    }

    Inventory inv = plugin.getPurchaseConfirmGUIBuilder().create(clickedItem.clone(), price);
    p.openInventory(inv);
  }

  @EventHandler
  public void onClickPurchaseConfirmInventory(InventoryClickEvent e) {
    if (e.getWhoClicked() == null || !(e.getWhoClicked() instanceof Player)) {
      return;
    }

    Player p = (Player) e.getWhoClicked();
    if (p.getOpenInventory().getTopInventory() == null) {
      return;
    }

    Inventory inv = p.getOpenInventory().getTopInventory();
    String title = inv.getTitle();
    if (title == null || !title.equals(plugin.getPurchaseConfirmGUIBuilder().getTitle())) {
      return;
    }

    e.setCancelled(true);
    ItemStack clickedItem = e.getCurrentItem();

    if (clickedItem == null) {
      return;
    }

    PurchaseConfirmInventoryItemType type = plugin.getPurchaseConfirmGUIBuilder()
        .getItemType(clickedItem);

    if (type == null) {
      return;
    }

    if (type == PurchaseConfirmInventoryItemType.SOFT_ADD_AMOUNT) {
      ItemStack newSign = addWrittenAmountOfSign(inv.getItem(22), 1);
      inv.setItem(22, newSign);
    } else if (type == PurchaseConfirmInventoryItemType.HARD_ADD_AMOUNT) {
      ItemStack newSign = addWrittenAmountOfSign(inv.getItem(22), 10);
      inv.setItem(22, newSign);
    } else if (type == PurchaseConfirmInventoryItemType.SOFT_REMOVE_AMOUNT) {
      ItemStack newSign = addWrittenAmountOfSign(inv.getItem(22), -1);
      inv.setItem(22, newSign);
    } else if (type == PurchaseConfirmInventoryItemType.HARD_REMOVE_AMOUNT) {
      ItemStack newSign = addWrittenAmountOfSign(inv.getItem(22), -10);
      inv.setItem(22, newSign);
    } else if (type == PurchaseConfirmInventoryItemType.CANCEL) {
      p.closeInventory();
    } else if (type == PurchaseConfirmInventoryItemType.CONFIRM) {
      ItemStack item = inv.getItem(22);
      if (item == null) {
        p.closeInventory();
        p.sendMessage(Chat.f("&cエラーが発生したため購入処理をキャンセルしました。"));
        return;
      }

      ItemMeta meta = item.getItemMeta();
      List<String> lore = meta.getLore();

      int price = plugin.getPurchaseConfirmGUIBuilder().parsePriceFromLore(lore);
      int amount = plugin.getPurchaseConfirmGUIBuilder().parseAmountFromLore(lore);

      ItemStack targetItem = inv.getItem(13);
      if (countMinimumEmptySlotRequirements(targetItem, amount) > countEmptySlots(p)) {
        p.closeInventory();
        p.sendMessage(Chat.f("&cインベントリに十分な空きが無いため購入処理をキャンセルしました"));
        return;
      }

      boolean success = plugin.getPlayerTableAdapter()
          .deductPoints(p.getUniqueId(), price * amount);

      if (!success) {
        p.closeInventory();
        p.sendMessage(Chat.f("&cポイントが足りないため購入できませんでした"));
        return;
      }

      for (int i = 0; i < amount; i++) {
        p.getInventory().addItem(targetItem.clone());
      }

      p.closeInventory();
      p.sendMessage(Chat.f("&a購入処理が完了しました。"));

      plugin.getEventStatusDisplay().updateAsync(p);
    }
  }

  private ItemStack addWrittenAmountOfSign(ItemStack item, int increaseAmount) {
    ItemStack cloned = item.clone();
    ItemMeta meta = cloned.getItemMeta();
    List<String> lore = meta.getLore();

    int price = plugin.getPurchaseConfirmGUIBuilder().parsePriceFromLore(lore);
    int currentAmount = plugin.getPurchaseConfirmGUIBuilder().parseAmountFromLore(lore);
    int newAmount = currentAmount + increaseAmount;
    if (newAmount < 1) {
      newAmount = 1;
    } else if (newAmount > 100) {
      newAmount = 100;
    }

    lore = plugin.getPurchaseConfirmGUIBuilder().createSignLore(price, newAmount, item.getAmount());

    meta.setLore(lore);
    cloned.setItemMeta(meta);
    return cloned;
  }

  private int countEmptySlots(Player p) {
    int count = 0;
    for (int i = 0; i < 9 * 4; i++) {
      ItemStack item = p.getInventory().getItem(0);
      if (item == null || item.getType() == Material.AIR) {
        count++;
      }
    }
    return count;
  }

  private int countMinimumEmptySlotRequirements(ItemStack item, int amount) {
    return (item.getAmount() * amount) / item.getMaxStackSize();
  }
}
