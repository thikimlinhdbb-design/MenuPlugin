package com.example.menu;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.inventory.InventoryClickEvent;

public class MenuListener implements Listener {

    private final XrayManager xray;
    public MenuListener(XrayManager xray) { this.xray = xray; }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getInventory().getHolder() instanceof MenuHolder)) return;
        e.setCancelled(true);
        if (!(e.getWhoClicked() instanceof Player p)) return;
        if (e.getClickedInventory() == null) return;

        switch (e.getRawSlot()) {
            case MenuCommand.SLOT_SHOP -> { p.closeInventory(); p.performCommand("shop"); }
            case MenuCommand.SLOT_WARP -> { p.closeInventory(); p.performCommand("warps"); }
            case MenuCommand.SLOT_XRAY -> {
                boolean now = xray.toggle(p);
                e.getInventory().setItem(MenuCommand.SLOT_XRAY, MenuCommand.xrayItem(now));
                p.playSound(p.getLocation(),
                        now ? Sound.BLOCK_BEACON_ACTIVATE : Sound.BLOCK_BEACON_DEACTIVATE, 1f, 1f);
            }
            default -> {}
        }
    }
}
