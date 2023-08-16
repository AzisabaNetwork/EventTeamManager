package net.azisaba.lgw.eventteammanager.shop;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import net.azisaba.lgw.eventteammanager.EventTeamManager;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

@RequiredArgsConstructor
public class EventShopItemContainer {

  private final EventTeamManager plugin;

  private final List<ItemStack> items = new ArrayList<>();
  private final Map<ItemStack, Integer> prices = new HashMap<>();
  private List<ItemStack> unmodifiableItems = Collections.emptyList();

  public void load() {
    YamlConfiguration conf = YamlConfiguration.loadConfiguration(getFile());

    for (int i = 0; i < 54; i++) {
      if (conf.getConfigurationSection(i + "") == null) {
        break;
      }

      ItemStack item = conf.getItemStack(i + ".item");
      int price = conf.getInt(i + ".price");

      addItem(item, price);
    }
  }

  public void save() {
    YamlConfiguration conf = new YamlConfiguration();

    for (int i = 0; i < items.size(); i++) {
      conf.set(i + ".item", items.get(i));
      conf.set(i + ".price", prices.get(items.get(i)));
    }

    try {
      conf.save(getFile());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void addItem(ItemStack item, int price) {
    if (hasItem(item)) {
      return;
    }

    items.add(item);
    unmodifiableItems = Collections.unmodifiableList(items);
    prices.put(item, price);

    if (plugin.getEventShopGUIBuilder() != null) {
      plugin.getEventShopGUIBuilder().reset();
    }
  }

  public void removeItem(ItemStack item) {
    for (int i = 0; i < items.size(); i++) {
      if (items.get(i).equals(item)) {
        prices.remove(items.get(i));
        items.remove(i);
        unmodifiableItems = Collections.unmodifiableList(items);
        return;
      }
    }
  }

  public int getPrice(ItemStack item) {
    return prices.getOrDefault(item, -1);
  }

  public List<ItemStack> getItems() {
    return unmodifiableItems;
  }

  public void clear() {
    items.clear();
    unmodifiableItems = Collections.emptyList();
    prices.clear();

    plugin.getEventShopGUIBuilder().reset();
  }

  private boolean hasItem(ItemStack item) {
    for (ItemStack i : items) {
      if (i.equals(item)) {
        return true;
      }
    }
    return false;
  }

  private File getFile() {
    return new File(plugin.getDataFolder(), "shopItem.yml");
  }
}
