package com.example.menu;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class MenuCommand implements CommandExecutor {

    public static final int SLOT_SHOP = 11;
    public static final int SLOT_WARP = 13;
    public static final int SLOT_XRAY = 15;

    private final XrayManager xray;

    public MenuCommand(XrayManager xray) { this.xray = xray; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage("Chỉ người chơi mới dùng được.");
            return true;
        }
        p.openInventory(buildMenu());
        return true;
    }

    public static Inventory buildMenu() {
        Inventory inv = Bukkit.createInventory(new MenuHolder(), 27,
                Component.text("§8» §6Menu chính §8«"));

        inv.setItem(SLOT_SHOP, item(Material.EMERALD, "§a§lCửa hàng",
                List.of("§7Mua bán vật phẩm")));
        inv.setItem(SLOT_WARP, item(Material.COMPASS, "§b§lDịch chuyển",
                List.of("§7Đi đến khu vực")));
        inv.setItem(SLOT_XRAY, xrayItem(false));

        ItemStack glass = item(Material.GRAY_STAINED_GLASS_PANE, " ", List.of());
        for (int i = 0; i < inv.getSize(); i++)
            if (inv.getItem(i) == null) inv.setItem(i, glass);

        return inv;
    }

    public static ItemStack xrayItem(boolean on) {
        return item(on ? Material.LIME_DYE : Material.GRAY_DYE,
                on ? "§a§lX-ray: §fBẬT" : "§c§lX-ray: §fTẮT",
                List.of("§7Hiện quặng xuyên tường", "§eNhấn để " + (on ? "§ctắt" : "§abật")));
    }

    public static ItemStack item(Material mat, String name, List<String> lore) {
        ItemStack it = new ItemStack(mat);
        ItemMeta meta = it.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(lore);
        it.setItemMeta(meta);
        return it;
    }
                        }
