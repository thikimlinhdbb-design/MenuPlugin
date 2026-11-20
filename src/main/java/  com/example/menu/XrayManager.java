package com.example.menu;

import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.*;

public class XrayManager implements Listener {

    private static final Set<Material> ORES = Set.of(
        Material.DIAMOND_ORE, Material.DEEPSLATE_DIAMOND_ORE,
        Material.EMERALD_ORE, Material.DEEPSLATE_EMERALD_ORE,
        Material.GOLD_ORE, Material.DEEPSLATE_GOLD_ORE, Material.NETHER_GOLD_ORE,
        Material.IRON_ORE, Material.DEEPSLATE_IRON_ORE,
        Material.LAPIS_ORE, Material.DEEPSLATE_LAPIS_ORE,
        Material.REDSTONE_ORE, Material.DEEPSLATE_REDSTONE_ORE,
        Material.COPPER_ORE, Material.DEEPSLATE_COPPER_ORE,
        Material.COAL_ORE, Material.DEEPSLATE_COAL_ORE,
        Material.NETHER_QUARTZ_ORE, Material.ANCIENT_DEBRIS
    );

    private static final int CHUNK_RADIUS = 2;
    private static final int Y_RANGE = 40;
    private static final int MAX_MARKERS = 400;

    private final JavaPlugin plugin;
    private final Set<UUID> enabled = new HashSet<>();
    private final Map<UUID, List<ItemDisplay>> markers = new HashMap<>();
    private final Map<UUID, Location> lastScan = new HashMap<>();

    public XrayManager(JavaPlugin plugin) { this.plugin = plugin; }

    public boolean toggle(Player p) {
        if (enabled.remove(p.getUniqueId())) {
            clear(p);
            p.sendMessage("§c[X-Ray] Đã tắt.");
            return false;
        }
        enabled.add(p.getUniqueId());
        p.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION,
                Integer.MAX_VALUE, 0, false, false, false));
        refresh(p);
        p.sendMessage("§a[X-Ray] Đã bật.");
        return true;
    }

    public void disableAll() {
        for (UUID id : new ArrayList<>(enabled)) {
            Player p = Bukkit.getPlayer(id);
            clear(id);
            if (p != null) p.removePotionEffect(PotionEffectType.NIGHT_VISION);
        }
        enabled.clear();
    }

    public void startTask() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (UUID id : new ArrayList<>(enabled)) {
                Player p = Bukkit.getPlayer(id);
                if (p == null || !p.isOnline()) { clear(id); enabled.remove(id); continue; }
                if (!p.hasPotionEffect(PotionEffectType.NIGHT_VISION))
                    p.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION,
                            Integer.MAX_VALUE, 0, false, false, false));
                Location last = lastScan.get(id);
                if (last == null || last.getWorld() != p.getWorld()
                        || last.distanceSquared(p.getLocation()) > 64) refresh(p);
            }
        }, 40L, 40L);
    }

    private void refresh(Player p) {
        clear(p);
        World w = p.getWorld();
        int pcx = p.getLocation().getBlockX() >> 4;
        int pcz = p.getLocation().getBlockZ() >> 4;
        int py = p.getLocation().getBlockY();
        int minY = Math.max(w.getMinHeight(), py - Y_RANGE);
        int maxY = Math.min(w.getMaxHeight() - 1, py + Y_RANGE);

        List<ItemDisplay> list = new ArrayList<>();
        int count = 0;
        outer:
        for (int cx = pcx - CHUNK_RADIUS; cx <= pcx + CHUNK_RADIUS; cx++) {
            for (int cz = pcz - CHUNK_RADIUS; cz <= pcz + CHUNK_RADIUS; cz++) {
                if (!w.isChunkLoaded(cx, cz)) continue;
                ChunkSnapshot s = w.getChunkAt(cx, cz).getChunkSnapshot(false, false, false);
                for (int x = 0; x < 16; x++) {
                    for (int z = 0; z < 16; z++) {
                        for (int y = minY; y <= maxY; y++) {
                            Material t = s.getBlockType(x, y, z);
                            if (!ORES.contains(t)) continue;
                            Location loc = new Location(w,
                                    (cx << 4) + x + 0.5, y + 0.5, (cz << 4) + z + 0.5);
                            list.add(spawnMarker(p, loc, displayOf(t)));
                            if (++count >= MAX_MARKERS) break outer;
                        }
                    }
                }
            }
        }
        markers.put(p.getUniqueId(), list);
        lastScan.put(p.getUniqueId(), p.getLocation().clone());
    }

    private ItemDisplay spawnMarker(Player owner, Location loc, Material icon) {
        ItemDisplay d = loc.getWorld().spawn(loc, ItemDisplay.class, display -> {
            display.setItemStack(new ItemStack(icon));
            display.setBillboard(Display.Billboard.CENTER);
            display.setGlowing(true);
            display.setPersistent(false);
            display.setInvulnerable(true);
            display.setGravity(false);
            display.setTransformation(new Transformation(
                    new Vector3f(), new Quaternionf(),
                    new Vector3f(0.45f, 0.45f, 0.45f), new Quaternionf()));
        });
        for (Player other : Bukkit.getOnlinePlayers())
            if (!other.equals(owner)) other.hideEntity(plugin, d);
        return d;
    }

    private void clear(UUID id) {
        List<ItemDisplay> list = markers.remove(id);
        if (list != null) for (ItemDisplay d : list) if (!d.isDead()) d.remove();
        lastScan.remove(id);
    }
    private void clear(Player p) { clear(p.getUniqueId()); }

    private static Material displayOf(Material ore) {
        return switch (ore) {
            case DIAMOND_ORE, DEEPSLATE_DIAMOND_ORE -> Material.DIAMOND;
            case EMERALD_ORE, DEEPSLATE_EMERALD_ORE -> Material.EMERALD;
            case GOLD_ORE, DEEPSLATE_GOLD_ORE, NETHER_GOLD_ORE -> Material.GOLD_INGOT;
            case IRON_ORE, DEEPSLATE_IRON_ORE -> Material.IRON_INGOT;
            case LAPIS_ORE, DEEPSLATE_LAPIS_ORE -> Material.LAPIS_LAZULI;
            case REDSTONE_ORE, DEEPSLATE_REDSTONE_ORE -> Material.REDSTONE;
            case COPPER_ORE, DEEPSLATE_COPPER_ORE -> Material.COPPER_INGOT;
            case COAL_ORE, DEEPSLATE_COAL_ORE -> Material.COAL;
            case NETHER_QUARTZ_ORE -> Material.QUARTZ;
            case ANCIENT_DEBRIS -> Material.NETHERITE_SCRAP;
            default -> Material.STONE;
        };
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        UUID id = e.getPlayer().getUniqueId();
        enabled.remove(id);
        clear(id);
    }
                       }
