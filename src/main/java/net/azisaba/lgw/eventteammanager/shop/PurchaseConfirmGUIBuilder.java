package net.azisaba.lgw.eventteammanager.shop;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import net.azisaba.lgw.core.utils.Chat;
import net.azisaba.lgw.eventteammanager.EventTeamManager;
import net.azisaba.lgw.eventteammanager.util.ItemHelper;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

@RequiredArgsConstructor
public class PurchaseConfirmGUIBuilder {

  private final EventTeamManager plugin;

  private boolean itemStackCacheInitialized = false;
  private final Map<PurchaseConfirmInventoryItemType, ItemStack> cachedItems = new HashMap<>();

  public Inventory create(ItemStack item, int price) {
    initializeCachedItems();

    Inventory inv = Bukkit.createInventory(null, 9 * 6, getTitle());

    for (int i = 0; i < 54; i++) {
      inv.setItem(i, cachedItems.get(PurchaseConfirmInventoryItemType.BLACK_GLASS_PANE));
    }

    List<String> lore = createSignLore(price, 1, item.getAmount());
    ItemStack sign = ItemHelper.setLore(ItemHelper.create(Material.SIGN, Chat.f("&r購入情報")),
        lore);

    inv.setItem(13, item);
    inv.setItem(20, cachedItems.get(PurchaseConfirmInventoryItemType.HARD_REMOVE_AMOUNT));
    inv.setItem(21, cachedItems.get(PurchaseConfirmInventoryItemType.SOFT_REMOVE_AMOUNT));
    inv.setItem(22, sign);
    inv.setItem(23, cachedItems.get(PurchaseConfirmInventoryItemType.SOFT_ADD_AMOUNT));
    inv.setItem(24, cachedItems.get(PurchaseConfirmInventoryItemType.HARD_ADD_AMOUNT));

    inv.setItem(39, cachedItems.get(PurchaseConfirmInventoryItemType.CANCEL));
    inv.setItem(41, cachedItems.get(PurchaseConfirmInventoryItemType.CONFIRM));

    return inv;
  }

  public String getTitle() {
    return Chat.f("&6&l購入確認 &7 - {0}", plugin.getName());
  }

  public PurchaseConfirmInventoryItemType getItemType(ItemStack item) {
    initializeCachedItems();

    for (PurchaseConfirmInventoryItemType type : PurchaseConfirmInventoryItemType.values()) {
      if (cachedItems.get(type).isSimilar(item)) {
        return type;
      }
    }

    return null;
  }

  public List<String> createSignLore(int price, int amount, int stackSize) {
    String countNoun = stackSize == 1 ? "個" : "組";

    return Arrays.asList(
        Chat.f("&r1{0}あたり: &e{1}pt", countNoun, price),
        Chat.f("&r購入{0}数: &e{1}", countNoun, amount),
        Chat.f("&r必要ポイント: &e{0}pt", price * amount)
    );
  }

  public int parsePriceFromLore(List<String> lore) {
    String line = lore.get(0);
    String priceStr = ChatColor.stripColor(line.split(":")[1]).replace("pt", "").trim();
    try {
      return Integer.parseInt(priceStr);
    } catch (NumberFormatException e) {
      return -1;
    }
  }

  public int parseAmountFromLore(List<String> lore) {
    String line = lore.get(1);
    String amountStr = ChatColor.stripColor(line.split(":")[1]).trim();
    try {
      return Integer.parseInt(amountStr);
    } catch (NumberFormatException e) {
      return -1;
    }
  }

  private void initializeCachedItems() {
    if (itemStackCacheInitialized) {
      return;
    }

    cachedItems.put(PurchaseConfirmInventoryItemType.BLACK_GLASS_PANE,
        ItemHelper.createItem(Material.STAINED_GLASS_PANE, 15, " "));
    cachedItems.put(PurchaseConfirmInventoryItemType.SOFT_REMOVE_AMOUNT,
        ItemHelper.createItem(Material.STAINED_GLASS_PANE, 1, Chat.f("&c-1")));
    cachedItems.put(PurchaseConfirmInventoryItemType.HARD_REMOVE_AMOUNT,
        ItemHelper.createItem(Material.STAINED_GLASS_PANE, 14, Chat.f("&c-10")));
    cachedItems.put(PurchaseConfirmInventoryItemType.SOFT_ADD_AMOUNT,
        ItemHelper.createItem(Material.STAINED_GLASS_PANE, 5, Chat.f("&a+1")));
    cachedItems.put(PurchaseConfirmInventoryItemType.HARD_ADD_AMOUNT,
        ItemHelper.createItem(Material.STAINED_GLASS_PANE, 13, Chat.f("&a+10")));
    cachedItems.put(PurchaseConfirmInventoryItemType.CONFIRM,
        ItemHelper.createItem(Material.STAINED_CLAY, 5, Chat.f("&a購入する")));
    cachedItems.put(PurchaseConfirmInventoryItemType.CANCEL,
        ItemHelper.createItem(Material.STAINED_CLAY, 14, Chat.f("&cキャンセル")));
    itemStackCacheInitialized = true;
  }
}
