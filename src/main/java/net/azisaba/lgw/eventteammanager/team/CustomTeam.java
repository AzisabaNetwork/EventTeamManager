package net.azisaba.lgw.eventteammanager.team;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.bukkit.ChatColor;

@Data
@AllArgsConstructor
public class CustomTeam {

  private String name;
  private int index;
  private ChatColor color;

}
