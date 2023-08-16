package net.azisaba.lgw.eventteammanager.team.score.display;

import com.google.common.base.Strings;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import net.azisaba.lgw.core.utils.Chat;
import net.azisaba.lgw.eventteammanager.EventTeamManager;
import net.azisaba.lgw.eventteammanager.team.CustomTeam;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
public class ScoreFormatter {

  private final EventTeamManager plugin;

  public List<String> chat(Player p, Map<CustomTeam, Double> scoreMap) {
    double max = -1;
    for (double v : scoreMap.values()) {
      if (v > max) {
        max = v;
      }
    }

    List<Entry<CustomTeam, Double>> list = new ArrayList<>(scoreMap.entrySet());
    list.sort(Entry.comparingByValue(Comparator.reverseOrder()));

    List<String> lines = new ArrayList<>();

    lines.add(Chat.f("&7============= &eチームスコア &7============="));

    for (Entry<CustomTeam, Double> entry : list) {
      CustomTeam team = entry.getKey();
      double value = entry.getValue();

      int barLength = (int) (value / max * 50);
      int grayBarLength = 50 - barLength;

      lines.add(
          Chat.f(
              "&r[{0}{1}&7{2}&r] &7- {0}{3} &7- &e{4} pt",
              team.getColor().toString(),
              Strings.repeat("|", barLength),
              Strings.repeat("|", grayBarLength),
              team.getName(),
              String.format("%.1f", scoreMap.get(team))
          )
      );
    }

    lines.add(Chat.f("&7===================================="));

    return lines;
  }

  public CompletableFuture<List<String>> hologram(Player p, Map<CustomTeam, Double> scoreMap) {
    CompletableFuture<List<String>> future = new CompletableFuture<>();

    if (scoreMap.size() == 0) {
      future.complete(Collections.singletonList(Chat.f("&6現在対抗イベントは開催されていません")));
      return future;
    }

    Bukkit.getScheduler().runTaskAsynchronously(plugin,
        () -> generateLinesAndCompleteWithFuture(future, p, scoreMap));

    return future;
  }

  private void generateLinesAndCompleteWithFuture(
      CompletableFuture<List<String>> future,
      Player p,
      Map<CustomTeam, Double> scoreMap
  ) {
    double total = 0;
    for (double v : scoreMap.values()) {
      total += v;
    }

    List<Entry<CustomTeam, Double>> list = new ArrayList<>(scoreMap.entrySet());
    list.sort(Entry.comparingByValue(Comparator.reverseOrder()));

    CustomTeam belongTeam = plugin.getBelongTeamCache().getBelong(p.getUniqueId());
    List<String> lines = new ArrayList<>();

    lines.add(Chat.f("&7======= &eイベントボード &7======="));
    lines.add(" ");

    StringBuilder teamName = new StringBuilder();
    StringBuilder valueLine = new StringBuilder();
    StringBuilder bar = new StringBuilder();

    for (Entry<CustomTeam, Double> entry : list) {
      CustomTeam team = entry.getKey();
      double value = entry.getValue();

      teamName.append(Chat.f("{0}{1}", team.getColor().toString(), team.getName()));
      teamName.append(Chat.f("&r : "));

      valueLine
          .append(Chat.f("&e{0}{1} pt", team.getColor().toString(), String.format("%.1f", value)))
          .append(Chat.f("&r : "));

      if (total > 0) {
        int barLength = (int) (value / total * 50);
        bar.append(Chat.f("{0}{1}", team.getColor().toString(), Strings.repeat("|", barLength)));
      }
    }

    if (total <= 0) {
      bar.append(Chat.f("&r{0}", Strings.repeat("|", 50)));
    }

    lines.add(teamName.substring(0, teamName.length() - 5));
    lines.add(valueLine.substring(0, valueLine.length() - 5));
    lines.add(bar.toString());

    lines.add(" ");

    if (belongTeam != null) {
      int earnedPoint = plugin.getPlayerTableAdapter().getPoint(p.getUniqueId());
      int availablePoint = plugin.getPlayerTableAdapter().getAvailablePoint(p.getUniqueId());

      lines.add(Chat.f("&eあなたの所属チーム: {0}{1}", belongTeam.getColor(), belongTeam.getName()));
      lines.add(Chat.f("&eあなたの獲得ポイント: &6{0} pt", earnedPoint));
      lines.add(Chat.f("&eあなたの使用可能ポイント: &c{0} pt", availablePoint));
    } else {
      lines.add(Chat.f("&7あなたはチームに参加していません！"));
      lines.add(Chat.f("&6/event join <チーム名>"));
      lines.add(Chat.f("&7でチームに参加しよう！"));
    }

    lines.add(" ");

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    lines.add(Chat.f("&7{0} ({1}) {0}", Strings.repeat("=", 3), sdf.format(new Date())));

    future.complete(lines);
  }
}
