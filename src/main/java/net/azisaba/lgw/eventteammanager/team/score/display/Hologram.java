package net.azisaba.lgw.eventteammanager.team.score.display;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * @author siloneco version: 1.0.0
 */
public class Hologram {

  private static String version;
  private static int versionNumber;
  private static Class<?> armorStand, spawnPacket, world, craftWorld, craftPlayer, iChatBaseComponent,
      chatComponentText, entityLiving, entityPlayer, playerConnection, packetRaw, nbtTagCompound, packetDestroy,
      packetTeleport;

  private static boolean ready = false;

  private final List<String> messages = new ArrayList<>();
  private Location defaultLocation = null;
  private final HashMap<Player, Location> locMap = new HashMap<>();

  static {
    version =
        Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3] + ".";

    String verTemp = version.substring(3);
    versionNumber = Integer.parseInt(verTemp.substring(0, verTemp.indexOf("_")));

    try {
      armorStand = Class.forName("net.minecraft.server." + version + "EntityArmorStand");
      spawnPacket = Class.forName(
          "net.minecraft.server." + version + "PacketPlayOutSpawnEntityLiving");
      world = Class.forName("net.minecraft.server." + version + "World");
      craftWorld = Class.forName("org.bukkit.craftbukkit." + version + "CraftWorld");
      craftPlayer = Class.forName("org.bukkit.craftbukkit." + version + "entity.CraftPlayer");
      iChatBaseComponent = Class.forName("net.minecraft.server." + version + "IChatBaseComponent");
      chatComponentText = Class.forName("net.minecraft.server." + version + "ChatComponentText");
      entityLiving = Class.forName("net.minecraft.server." + version + "EntityLiving");
      entityPlayer = Class.forName("net.minecraft.server." + version + "EntityPlayer");
      playerConnection = Class.forName("net.minecraft.server." + version + "PlayerConnection");
      packetRaw = Class.forName("net.minecraft.server." + version + "Packet");
      packetDestroy = Class.forName(
          "net.minecraft.server." + version + "PacketPlayOutEntityDestroy");
      packetTeleport = Class.forName(
          "net.minecraft.server." + version + "PacketPlayOutEntityTeleport");
      Class<?> entity = Class.forName("net.minecraft.server." + version + "Entity");

      if (versionNumber <= 8) {
        nbtTagCompound = Class.forName("net.minecraft.server." + version + "NBTTagCompound");
      }

      HoloComponent.init(packetTeleport, armorStand, craftPlayer, entityPlayer, playerConnection,
          packetRaw,
          entity);

      ready = true;
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
  }

  public void addLine(String msg) {
    messages.add(msg);
  }

  public void addLines(String... msg) {
    messages.addAll(Arrays.asList(msg));
  }

  public void setLine(int num, String message) {
    messages.set(num, message);
  }

  public void setLocation(Location loc) {
    defaultLocation = loc.clone();
  }

  public Location getLocation() {
    return defaultLocation;
  }

  public void setSpace(double space) {
    this.space = space;
  }

  public double getSpace() {
    return space;
  }

  public List<String> getLines() {
    return messages;
  }

  public String getLine(int line) {
    if (messages.size() <= line) {
      return null;
    }

    return messages.get(line);
  }

  private double space = 0.3;
  private final HashMap<Player, HoloComponent> holoMap = new HashMap<>();

  public void display(Player... players) {
    for (Player p : players) {

      if (holoMap.containsKey(p)) {
        continue;
      }

      Location loc = defaultLocation.clone();
      if (locMap.containsKey(p)) {
        loc = locMap.get(p);
      }

      loc = loc.clone();

      try {
        Object w = craftWorld.cast(loc.getWorld());
        Method getHandleMethod = craftWorld.getMethod("getHandle");
        Object wServer = getHandleMethod.invoke(w);

        List<Object> armorStands = new ArrayList<>();

        Collections.reverse(messages);
        for (String msg : messages) {
          if (msg.trim().equals("")) {
            loc.add(0, space, 0);
            continue;
          }
          Object armorStandEntity = armorStand.getConstructor(world)
              .newInstance(world.cast(wServer));
          displayArmorStand(p, armorStandEntity, loc.add(0, space, 0), msg);
          armorStands.add(armorStandEntity);
        }

        Collections.reverse(armorStands);
        HoloComponent comp = new HoloComponent(p, armorStands, space);
        holoMap.put(p, comp);

        Collections.reverse(messages);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  public void teleport(Location loc, Player... players) {
    loc = loc.clone();
    for (Player p : players) {
      if (!holoMap.containsKey(p)) {
        continue;
      }

      HoloComponent comp = holoMap.get(p);
      comp.teleport(loc);
      locMap.put(p, loc);
    }
  }

  public void removeFrom(Player... players) {
    for (Player p : players) {

      if (!holoMap.containsKey(p)) {
        continue;
      }

      try {
        List<Integer> idList = holoMap.get(p).armorStandIDList();
        int[] ids = new int[idList.size()];

        int count = 0;
        for (Integer i : idList) {
          ids[count] = i.intValue();
          count++;
        }

        Object packet = packetDestroy.getConstructor(int[].class).newInstance(ids);
        sendPacket(p, packet);
      } catch (Exception e) {
        e.printStackTrace();
        continue;
      }

      holoMap.remove(p);
    }
  }

  public void removeAll() {
    for (Player p : holoMap.keySet()) {
      removeFrom(p);
    }
  }

  public void update(Player... players) {
    removeFrom(players);
    display(players);
  }

  private int displayArmorStand(Player p, Object armorStandEntity, Location loc, String msg) {
    try {
      Method setName;
      if (versionNumber >= 13) {
        setName = armorStand.getMethod("setCustomName", iChatBaseComponent);
      } else {
        setName = armorStand.getMethod("setCustomName", String.class);
      }
      Method setNameVisible = armorStand.getMethod("setCustomNameVisible", boolean.class);
      Method invisible = armorStand.getMethod("setInvisible", boolean.class);
      Method marker = null;
      if (versionNumber >= 9) {
        marker = armorStand.getMethod("setMarker", boolean.class);
      }
      Method small = armorStand.getMethod("setSmall", boolean.class);
      Method location = armorStand.getMethod("setLocation", double.class, double.class,
          double.class, float.class,
          float.class);

      if (versionNumber <= 8) {
        loc = loc.clone();
        loc.subtract(0, 1, 0);
        Object nbtTag = nbtTagCompound.getConstructor().newInstance();
        Method setBoolean = nbtTagCompound.getMethod("setBoolean", String.class, boolean.class);
        setBoolean.invoke(nbtTag, "Marker", true);
        setBoolean.invoke(nbtTag, "Small", true);
        Method setNBT = armorStand.getMethod("a", nbtTagCompound);
        setNBT.invoke(armorStandEntity, nbtTag);
      }
      if (versionNumber >= 13) {
        setName.invoke(armorStandEntity,
            chatComponentText.getConstructor(String.class).newInstance(msg));
      } else {
        setName.invoke(armorStandEntity, msg);
      }
      setNameVisible.invoke(armorStandEntity, true);
      invisible.invoke(armorStandEntity, true);
      if (versionNumber >= 9) {
        marker.invoke(armorStandEntity, true);
        small.invoke(armorStandEntity, true);
      }
      location.invoke(armorStandEntity, loc.getX(), loc.getY() - 0.7, loc.getZ(), 0f, 0f);

      Object packet = spawnPacket.getConstructor(entityLiving)
          .newInstance(entityLiving.cast(armorStandEntity));

      sendPacket(p, packet);

      return (int) armorStand.getMethod("getId").invoke(armorStandEntity);
    } catch (Exception e) {
      e.printStackTrace();
      return -1;
    }
  }

  private static void sendPacket(Player p, Object packet) throws Exception {
    Object handle = craftPlayer.getMethod("getHandle").invoke(craftPlayer.cast(p));
    Object connection = entityPlayer.getField("playerConnection").get(handle);
    playerConnection.getMethod("sendPacket", packetRaw).invoke(connection, packet);
  }

  public static Hologram create() {

    if (!ready) {
      throw new IllegalStateException("Not ready yet or failed to initialize.");
    }

    return new Hologram();
  }

  public static Hologram create(String message) {

    if (!ready) {
      throw new IllegalStateException("Not ready yet or failed to initialize.");
    }

    return new Hologram(message);
  }

  public static Hologram create(Location loc) {

    if (!ready) {
      throw new IllegalStateException("Not ready yet or failed to initialize.");
    }

    return new Hologram(loc);
  }

  public static Hologram create(String message, Location loc) {

    if (!ready) {
      throw new IllegalStateException("Not ready yet or failed to initialize.");
    }

    return new Hologram(message, loc);
  }

  private Hologram() {

  }

  private Hologram(String message) {
    messages.add(message);
  }

  private Hologram(Location loc) {
    defaultLocation = loc.clone();
  }

  private Hologram(String message, Location loc) {
    messages.add(message);
    defaultLocation = loc.clone();
  }
}

class HoloComponent {

  private List<Object> entityArmorStandList = new ArrayList<>();

  private static Class<?> packetTeleport, armorStand, craftPlayer, entityPlayer, playerConnection, packetRaw, entity;
  private final double space;
  private final Player player;

  public static void init(Class<?> packetTeleport, Class<?> armorStand, Class<?> craftPlayer,
      Class<?> entityPlayer,
      Class<?> playerConnection, Class<?> packetRaw, Class<?> entity) {
    HoloComponent.packetTeleport = packetTeleport;
    HoloComponent.armorStand = armorStand;
    HoloComponent.craftPlayer = craftPlayer;
    HoloComponent.entityPlayer = entityPlayer;
    HoloComponent.playerConnection = playerConnection;
    HoloComponent.packetRaw = packetRaw;
    HoloComponent.entity = entity;
  }

  public HoloComponent(Player p, List<Object> entities, double space) {
    player = p;
    entityArmorStandList = entities;
    this.space = space;
  }

  public void teleport(Location loc) {
    loc = loc.clone();
    Collections.reverse(entityArmorStandList);

    for (Object armorStandEntity : entityArmorStandList) {
      try {
        armorStand.getMethod("setLocation", double.class, double.class, double.class, float.class,
                float.class)
            .invoke(armorStandEntity, loc.getX(), loc.getY(), loc.getZ(), 0f, 0f);

        Object packet = packetTeleport.getConstructor(entity).newInstance(armorStandEntity);
        sendPacket(player, packet);
      } catch (Exception e) {
        e.printStackTrace();
      }

      loc.add(0, space, 0);
    }

    Collections.reverse(entityArmorStandList);
  }

  public List<Integer> armorStandIDList() {
    List<Integer> idList = new ArrayList<>();

    for (Object entityArmorStand : entityArmorStandList) {
      try {
        idList.add((int) armorStand.getMethod("getId").invoke(entityArmorStand));
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    return idList;
  }

  private static void sendPacket(Player p, Object packet) throws Exception {
    Object handle = craftPlayer.getMethod("getHandle").invoke(craftPlayer.cast(p));
    Object connection = entityPlayer.getField("playerConnection").get(handle);
    playerConnection.getMethod("sendPacket", packetRaw).invoke(connection, packet);
  }
}