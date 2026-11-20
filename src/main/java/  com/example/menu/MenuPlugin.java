package com.example.menu;

import org.bukkit.plugin.java.JavaPlugin;

public class MenuPlugin extends JavaPlugin {
    private XrayManager xray;

    @Override
    public void onEnable() {
        xray = new XrayManager(this);
        getCommand("menu").setExecutor(new MenuCommand(xray));
        getServer().getPluginManager().registerEvents(new MenuListener(xray), this);
        getServer().getPluginManager().registerEvents(xray, this);
        xray.startTask();
    }

    @Override
    public void onDisable() {
        if (xray != null) xray.disableAll();
    }
}
