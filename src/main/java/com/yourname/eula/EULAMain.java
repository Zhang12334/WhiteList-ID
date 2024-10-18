package com.yourname.eula;

import org.bukkit.plugin.java.JavaPlugin;

public class EULAMain extends JavaPlugin {

    @Override
    public void onEnable() {
        getLogger().info("EULA Plugin has been enabled.");
        getCommand("eula").setExecutor(new EULACommand(this));
    }

    @Override
    public void onDisable() {
        getLogger().info("EULA Plugin has been disabled.");
    }
}
