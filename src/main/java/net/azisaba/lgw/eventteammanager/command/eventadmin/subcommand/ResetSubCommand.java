package net.azisaba.lgw.eventteammanager.command.eventadmin.subcommand;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import net.azisaba.lgw.core.utils.Chat;
import net.azisaba.lgw.eventteammanager.EventTeamManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
public class ResetSubCommand implements CommandExecutor {

  private final EventTeamManager plugin;

  private final HashMap<UUID, Long> confirming = new HashMap<>();

  @Override
  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    if (!(sender instanceof Player)) {
      executeFlush(sender);
      return true;
    }

    Player p = (Player) sender;
    if (confirming.getOrDefault(p.getUniqueId(), 0L) > System.currentTimeMillis()) {
      executeFlush(sender);
      return true;
    }

    confirming.put(p.getUniqueId(), System.currentTimeMillis() + 10000L);
    sender.sendMessage(Chat.f(
        "&c警告: &eこのコマンドは現在登録されているイベントデータを完全に消去するものです！"
            + "この操作は取り消せません。本当に実行する場合は10秒以内にもう一度コマンドを実行してください"));
    return true;
  }

  private void executeFlush(CommandSender sender) {
    sender.sendMessage(Chat.f("&eイベントデータをリセットしています..."));

    try {
      boolean flushedTeamTable = plugin.getTeamTableAdapter().flushAllData();
      boolean flushedPlayerTable = plugin.getPlayerTableAdapter().flushAllData();

      plugin.getEventStatusDisplay().removeAllHologram();

      List<String> relatedInventoryTitles = Arrays.asList(
          plugin.getEventShopGUIBuilder().getTitle(),
          plugin.getPurchaseConfirmGUIBuilder().getTitle(),
          plugin.getEventShopGUIBuilder().getEditInventoryTitle()
      );

      for (Player p : Bukkit.getOnlinePlayers()) {
        if (relatedInventoryTitles.contains(p.getOpenInventory().getTitle())) {
          p.closeInventory();
        }
      }

      plugin.getHologramConfig().save(null);
      plugin.getPointCache().clear();

      plugin.getEventShopItemContainer().clear();
      Bukkit.getScheduler().runTaskAsynchronously(
          plugin,
          () -> plugin.getEventShopItemContainer().save()
      );

      plugin.getEventShopGUIBuilder().reset();
      Bukkit.getScheduler().runTaskAsynchronously(
          plugin,
          () -> plugin.getEventShopGUIBuilder().save()
      );

      if (flushedTeamTable && flushedPlayerTable) {
        sender.sendMessage(Chat.f("&aイベントデータをリセットしました"));
      } else {
        sender.sendMessage(Chat.f("&cイベントデータのリセットに失敗しました"));
      }
    } catch (Exception e) {
      e.printStackTrace();
      sender.sendMessage(Chat.f("&cイベントデータのリセットに失敗しました"));
    }
  }
}