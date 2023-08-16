package net.azisaba.lgw.eventteammanager.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * @author siloneco forked from amata1219 version: 1.0.0
 */
public class ItemHelper {

  public static ItemStack create(Material type) {
    ItemStack item = new ItemStack(type);
    return item;
  }

  public static ItemStack create(Material type, String title, String... lore) {
    ItemStack item = new ItemStack(type);
    ItemMeta meta = item.getItemMeta();
    meta.setDisplayName(title);
    if (lore.length > 0) {
      meta.setLore(Arrays.asList(lore));
    }
    item.setItemMeta(meta);
    return item;
  }

  public static ItemStack createItem(Material material, int data, String displayName,
      String... lore) {
    ItemStack item = getItemStackWithoutWarning(material, data);
    ItemMeta meta = item.getItemMeta();

    meta.setDisplayName(displayName != null ? displayName : "");

    if (lore == null || lore.length == 0) {
      meta.setLore(new ArrayList<String>());
    } else {
      meta.setLore(Arrays.asList(lore));
    }

    item.setItemMeta(meta);
    return item;
  }

  public static void addHideEnchant(ItemStack item) {
    ItemMeta meta = item.getItemMeta();
    meta.addEnchant(Enchantment.DURABILITY, 1, true);
    meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
    item.setItemMeta(meta);
  }

  public static void addLore(ItemStack item, String msg) {
    ItemMeta meta = item.getItemMeta();
    List<String> lore = new ArrayList<>(meta.getLore());
    lore.add(msg);
    meta.setLore(lore);
    item.setItemMeta(meta);
  }

  public static ItemStack setLore(ItemStack item, List<String> lore) {
    ItemMeta meta = item.getItemMeta();
    meta.setLore(lore);
    item.setItemMeta(meta);
    return item;
  }

  private static ItemStack getItemStackWithoutWarning(Material material, int data) {
    try {
      return ItemStack.class.getConstructor(Material.class, int.class, short.class)
          .newInstance(material, 1,
              (short) data);
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }
}