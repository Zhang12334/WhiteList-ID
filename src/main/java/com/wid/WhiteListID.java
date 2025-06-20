package com.wid;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

public class WhiteListID extends JavaPlugin implements Listener {

    private Set<String> whiteList;
    private String storageType;
    private LanguageManager languageManager;
    private StorageManager storageManager;
    private CommandHandler commandHandler;
    private boolean prefixEnable;
    private String prefix;

    @Override
    public void onEnable() {
        // bstats
        int pluginId = 23704;
        Metrics metrics = new Metrics(this, pluginId);

        // 保存默认配置文件
        this.saveDefaultConfig();
        whiteList = new HashSet<>();
        // 初始化 LanguageManager
        languageManager = new LanguageManager(getConfig());

        // 检查 MeowLibs 是否已加载
        if (!Bukkit.getPluginManager().isPluginEnabled("MeowLibs")) {
            getLogger().warning(languageManager.getMessage("CanNotFoundMeowLibs"));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // 初始化存储管理器
        storageType = getConfig().getString("storage", "json");
        storageManager = new StorageManager(this, storageType, getConfig(), languageManager, whiteList);

        // 加载前缀
        loadPrefix();

        // 初始化命令处理器
        commandHandler = new CommandHandler(this, storageManager, languageManager, whiteList);
        this.getCommand("wid").setExecutor(commandHandler);

        // 注册事件监听器
        Bukkit.getPluginManager().registerEvents(this, this);

        // 加载白名单
        storageManager.loadWhiteList();

        // 翻译者
        getLogger().info(languageManager.getMessage("TranslationContributors"));

        // 启动消息
        getLogger().info(languageManager.getMessage("startup"));
        String currentVersion = getDescription().getVersion();
        getLogger().info(languageManager.getMessage("nowusingversion") + " v" + currentVersion);
        getLogger().info(languageManager.getMessage("checkingupdate"));

        // 创建 CheckUpdate 实例
        CheckUpdate updateChecker = new CheckUpdate(
            getLogger(), // log记录器
            languageManager, // 语言管理器
            getDescription() // 插件版本信息
        );

        // 异步检查更新
        new BukkitRunnable() {
            @Override
            public void run() {
                updateChecker.checkUpdate();
            }
        }.runTaskAsynchronously(this);
    }

    public void loadPrefix() { 
        // 读取前缀配置
        prefixEnable = getConfig().getBoolean("prefix_settings.enable", false);
        prefix = getConfig().getString("prefix_settings.prefix", "be_");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> suggestions = new ArrayList<>();
        if (args.length == 1) {
            suggestions.add("add");
            suggestions.add("remove");
            suggestions.add("reload");
            suggestions.add("convert");
            suggestions.add("list");
        }
        return suggestions;
    }

    @Override
    public void onDisable() {
        // 已移除卸载时保存，因为操作均为热更新
        getLogger().info(languageManager.getMessage("shutdown"));
    }

    // 玩家预登录事件
    @EventHandler
    public void onPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
        // 获取ID
        final String playerName = event.getName();
        // 标记符
        boolean allowed = false;
        // 热加载白名单
        storageManager.loadWhiteList();
        
        // 判断流程
        if (whiteList.contains(playerName)) {
            // 白名单包含此ID，放行
            allowed = true;
        } else if (prefixEnable && playerName.startsWith(prefix)) {
            // 已启用前缀功能且此玩家ID以前缀开头
            // 切分ID
            String realName = playerName.substring(prefix.length());
            // 判断切分后ID是否位于白名单
            if (whiteList.contains(realName)) {
                // 放行
                allowed = true;
            }
        }

        // 放行判断
        if (!allowed) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_WHITELIST, languageManager.getMessage("notWhitelisted"));
        } else {
            event.allow();
        }
    }
}
