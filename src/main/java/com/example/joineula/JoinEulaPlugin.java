package main.java.com.example.joineula;

import org.bukkit.plugin.java.JavaPlugin;

public class JoinEulaPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        // 注册事件
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(), this);
        getServer().getPluginManager().registerEvents(new BookAgreementListener(), this);
    }

    @Override
    public void onDisable() {
        // 插件禁用时执行的逻辑
    }
}
