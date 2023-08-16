package net.azisaba.lgw.eventteammanager.shop;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import lombok.RequiredArgsConstructor;
import net.azisaba.lgw.core.utils.Chat;
import net.azisaba.lgw.eventteammanager.EventTeamManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

@RequiredArgsConstructor
public class EventShopGUIBuilder {

  private final EventTeamManager plugin;

  private HashMap<Integer, ItemStack> slots = new HashMap<>();

  private Inventory inventoryCache = null;

  public Inventory get() {
    if (inventoryCache != null) {
      return inventoryCache;
    }

    if (slots.isEmpty()) {
      List<ItemStack> items = plugin.getEventShopItemContainer().getItems();
      for (int i = 0, size = items.size(); i < size; i++) {
        slots.put(i, items.get(i));
      }
    }

    Inventory inv = Bukkit.createInventory(null, 9 * 6, getTitle());
    for (int i = 0; i < 54; i++) {
      if (slots.containsKey(i)) {
        inv.setItem(i, slots.get(i));
      }
    }

    inventoryCache = inv;
    return inv;
  }

  public Inventory getEditInventory() {
    if (slots.isEmpty()) {
      List<ItemStack> items = plugin.getEventShopItemContainer().getItems();
      for (int i = 0, size = items.size(); i < size; i++) {
        slots.put(i, items.get(i));
      }
    }

    Inventory inv = Bukkit.createInventory(null, 9 * 6, getEditInventoryTitle());
    for (int i = 0; i < 54; i++) {
      if (slots.containsKey(i)) {
        inv.setItem(i, slots.get(i));
      }
    }

    return inv;
  }

  public String getTitle() {
    return Chat.f("&6Event Shop &7- {0}", plugin.getName());
  }

  public String getEditInventoryTitle() {
    return Chat.f("&aShop Layout &7- {0}", plugin.getName());
  }

  public void reset() {
    slots.clear();
    inventoryCache = null;
  }

  public List<ItemStack> applyLayout(Inventory inv) {
    List<ItemStack> shopItems = new ArrayList<>();
    for (ItemStack item : plugin.getEventShopItemContainer().getItems()) {
      shopItems.add(item.clone());
    }

    for (int i = 0; i < 54; i++) {
      if (inv.getItem(i) == null || inv.getItem(i).getType() == Material.AIR) {
        continue;
      }

      ItemStack item = inv.getItem(i);
      shopItems.remove(item);
      slots.put(i, item);
    }

    return shopItems;
  }

  public void save() {
    YamlConfiguration conf = new YamlConfiguration();

    for (int i = 0; i < 54; i++) {
      if (slots.containsKey(i)) {
        conf.set(i + "", slots.get(i));
      }
    }

    try {
      conf.save(getFile());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void load() {
    YamlConfiguration conf = YamlConfiguration.loadConfiguration(getFile());

    for (String key : conf.getKeys(false)) {
      slots.put(Integer.parseInt(key), conf.getItemStack(key));
    }
  }

  private File getFile() {
    return new File(plugin.getDataFolder(), "shopLayout.yml");
  }
}
