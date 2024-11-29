package com.meow.wid;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.Map;
import java.util.Set;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class WhiteListID extends JavaPlugin implements CommandExecutor, Listener {

    private Set<String> whiteList;  // 使用玩家ID名称存储
    private String storageType;
    private String debugmode;

    // 一堆存储消息的变量
    private String startupMessage;
    private String storageTypeMessage;
    private String disableMessage;
    private String notWhitelistedMessage;
    private String useExampleMessage;
    private String unknownOptionMessage;
    private String noPermissionMessage;
    private String playerMessage;
    private String playerAlreadyExistMessage;
    private String playerAddedMessage;
    private String playerRemovedFromWhitelistMessage;
    private String playerNotInWhitelistMessage;
    private String loadedJsonMessage;
    private String savedJsonMessage;
    private String loadedMysqlMessage;
    private String savedMysqlMessage;
    private String nowLanguageMessage;
    private String translatorMessage;
    private String reloadMessage;
    private String reloadLanguage;
    private String reloadWhitelist;        
    private String checkingupdateMessage;
    private String updateavailableMessage;
    private String checkfailedMessage;
    private String updateurlMessage;
    private String usingversionMessage;
    private String nowusinglatestversionMessage;
    private String oldversionmaycauseproblemMessage;
    private String convertsuccessMessage;

    @Override
    public void onEnable() {
        // bstats
        int pluginId = 23704;
        Metrics metrics = new Metrics(this, pluginId);
        // 保存默认配置文件
        this.saveDefaultConfig();
        whiteList = new HashSet<>();
        this.getCommand("wid").setExecutor(this);
        // 注册事件监听
        Bukkit.getPluginManager().registerEvents(this, this);
        // 读取debugmode值
        debugmode = getConfig().getString("debugmode", "disable");
        // 检查语言文件
        String language = getConfig().getString("language", "zh_cn");
        // 读取语言文件
        loadLanguageFile(language);
        // 读取存储类型
        storageType = getConfig().getString("storage", "json");
        // 加载存储
        loadWhiteList();
        // 输出插件版本号及其他信息
        String version = getDescription().getVersion();
        getLogger().info(usingversionMessage + " " + version); //当前使用版本
        getLogger().info(startupMessage); // 启动消息
        getLogger().info(storageTypeMessage + " " + storageType); // 添加空格，瞅着好看
        getLogger().info(checkingupdateMessage); // 检查更新消息
        new BukkitRunnable() {
            @Override
            public void run() {
                check_update();
            }
        }.runTaskAsynchronously(this);
    }

    private void check_update() {
        // 获取当前版本号
        String currentVersion = getDescription().getVersion();
        // github加速地址，挨个尝试
        String[] githubUrls = {
            "https://ghp.ci/",
            "https://raw.fastgit.org/",
            ""
        };
        // 获取 github release 最新版本号作为最新版本
        // 仓库地址：https://github.com/Zhang12334/WhiteList-ID
        String latestVersionUrl = "https://github.com/Zhang12334/WhiteList-ID/releases/latest";
        // 获取版本号
        try {
            String latestVersion = null;
            for (String url : githubUrls) {
                HttpURLConnection connection = (HttpURLConnection) new URL(url + latestVersionUrl).openConnection();
                connection.setInstanceFollowRedirects(false); // 不自动跟随重定向
                int responseCode = connection.getResponseCode();
                if (responseCode == 302) { // 如果 302 了
                    String redirectUrl = connection.getHeaderField("Location");
                    if (redirectUrl != null && redirectUrl.contains("tag/")) {
                        // 从重定向URL中提取版本号
                        latestVersion = extractVersionFromUrl(redirectUrl);
                        break; // 找到版本号后退出循环
                    }
                }
                connection.disconnect();
                if (latestVersion != null) {
                    break; // 找到版本号后退出循环
                }
            }
            if (latestVersion == null) {
                getLogger().warning(checkfailedMessage);
                return;
            }
            // 比较版本号
            if (isVersionGreater(latestVersion, currentVersion)) {
                // 如果有新版本，则提示新版本
                getLogger().warning(updateavailableMessage + " " + latestVersion);
                // 提示下载地址（latest release地址）
                getLogger().warning(updateurlMessage + " https://github.com/Zhang12334/WhiteList-ID/releases/latest");
                getLogger().warning(oldversionmaycauseproblemMessage);
            } else {
                getLogger().info(nowusinglatestversionMessage);
            }
        } catch (Exception e) {
            getLogger().warning(checkfailedMessage);
        }
    }

    // 版本比较
    private boolean isVersionGreater(String version1, String version2) {
        String[] v1Parts = version1.split("\\.");
        String[] v2Parts = version2.split("\\.");
        for (int i = 0; i < Math.max(v1Parts.length, v2Parts.length); i++) {
            int v1Part = i < v1Parts.length ? Integer.parseInt(v1Parts[i]) : 0;
            int v2Part = i < v2Parts.length ? Integer.parseInt(v2Parts[i]) : 0;
            if (v1Part > v2Part) {
                return true;
            } else if (v1Part < v2Part) {
                return false;
            }
        }
        return false;
    }
    
    private String extractVersionFromUrl(String url) {
        // 解析 302 URL 中的版本号
        int tagIndex = url.indexOf("tag/");
        if (tagIndex != -1) {
            int endIndex = url.indexOf('/', tagIndex + 4);
            if (endIndex == -1) {
                endIndex = url.length();
            }
            return url.substring(tagIndex + 4, endIndex);
        }
        return null;
    }

    private void copyLanguageFile(File languageFile, String language) {
        InputStream langInput = getResource("lang/" + language + ".json");
        if (langInput != null) {
            try {
                Files.copy(langInput, languageFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                getLogger().info("[Chinese] 语言文件 " + language + ".json 已复制到 lang 文件夹");
                getLogger().info("[English] Language file " + language + ".json has been copied to the lang folder");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            // 复制默认的 zh_cn.json
            langInput = getResource("lang/zh_cn.json");
            if (langInput != null) {
                try {
                    Files.copy(langInput, languageFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    getLogger().info("[Chinese] 默认语言文件 zh_cn.json 已复制到 lang 文件夹");
                    getLogger().info("[English] Default language file zh_cn.json has been copied to the lang folder");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void loadLanguageFile(String language) {
        try {
            loadLanguageFileInternal(language);
        } catch (IOException | ParseException e) {
            getLogger().warning("[Language File Warn-Chinese] 未找到语言文件，使用默认语言 zh_cn.json");
            getLogger().warning("[Language File Warn-English] The language file was not found, using the default language zh_cn.json");

            try {
                loadLanguageFileInternal("zh_cn");
            } catch (IOException | ParseException ex) {
                getLogger().severe("[Language File Error-Chinese] 加载默认语言文件 zh_cn.json 失败，插件将被卸载");
                getLogger().severe("[Language File Error-Chinese] 请勿更改插件本体文件！若未更改请附带错误日志前往 Github 提交 Issue!");
                getLogger().severe("[Language File Error-English] Failed to load the default language file zh_cn.json, the plugin will be disabled");
                getLogger().severe("[Language File Error-English] Do not modify the plugin's core files! If you have not made any changes, please attach the error log and submit an issue on Github!");
                // 卸载插件
                unloadPlugin();
            }
        }
    }

    private void unloadPlugin() {
        // 禁用插件
        getServer().getPluginManager().disablePlugin(this);
    }

    private void loadLanguageFileInternal(String language) throws IOException, ParseException {
        try (InputStream inputStream = getClass().getResourceAsStream("/lang/" + language + ".json")) {
            if (inputStream == null) {
                throw new IOException("Language file not found: " + language + ".json");
            }
            JSONParser parser = new JSONParser();
            JSONObject jsonObject = (JSONObject) parser.parse(new InputStreamReader(inputStream, StandardCharsets.UTF_8));

            // 存储消息内容
            JSONObject messagesObject = (JSONObject) jsonObject.get("messages");
            startupMessage = (String) messagesObject.get("startup");
            usingversionMessage = (String) messagesObject.get("nowversion");
            updateavailableMessage = (String) messagesObject.get("updateavailable");
            updateurlMessage = (String) messagesObject.get("updateurl");
            checkingupdateMessage = (String) messagesObject.get("checkingupdate");
            checkfailedMessage = (String) messagesObject.get("checkupdatefailed");
            nowusinglatestversionMessage = (String) messagesObject.get("nowusinglatestversion");
            oldversionmaycauseproblemMessage = (String) messagesObject.get("oldversionmaycauseproblem");
            storageTypeMessage = (String) messagesObject.get("storagetype");
            disableMessage = (String) messagesObject.get("disable");
            notWhitelistedMessage = (String) messagesObject.get("not_whitelisted");
            useExampleMessage = (String) messagesObject.get("use_example");
            unknownOptionMessage = (String) messagesObject.get("unknown_option");
            noPermissionMessage = (String) messagesObject.get("no_permission");
            playerMessage = (String) messagesObject.get("player");
            playerAlreadyExistMessage = (String) messagesObject.get("player_already_exist");
            playerAddedMessage = (String) messagesObject.get("player_added");
            playerRemovedFromWhitelistMessage = (String) messagesObject.get("player_removed_from_whitelist");
            playerNotInWhitelistMessage = (String) messagesObject.get("player_not_in_whitelist");
            loadedJsonMessage = (String) messagesObject.get("loaded_json");
            savedJsonMessage = (String) messagesObject.get("saved_json");
            loadedMysqlMessage = (String) messagesObject.get("loaded_mysql");
            savedMysqlMessage = (String) messagesObject.get("saved_mysql");
            nowLanguageMessage = (String) messagesObject.get("now_language");
            translatorMessage = (String) messagesObject.get("translator");
            reloadMessage = (String) messagesObject.get("reload");
            reloadLanguage = (String) messagesObject.get("reload_language");
            reloadWhitelist = (String) messagesObject.get("reload_whitelist");
            convertsuccessMessage = (String) messagesObject.get("convert_success");

            // 当前使用语言
            getLogger().info(nowLanguageMessage);
            // 翻译贡献者
            getLogger().info(translatorMessage);
            // 调试模式
            if ("enable".equals(debugmode)) {
                // debug！
                getLogger().info("———————Language Debug mode———————");
                getLogger().info(nowLanguageMessage);
                getLogger().info("startup: " + startupMessage);
                getLogger().info("usingversion: " + usingversionMessage);
                getLogger().info("checkingupdate: " + checkingupdateMessage);
                getLogger().info("checkupdatefailed: " + checkfailedMessage);
                getLogger().info("nowusinglatestversion: " + nowusinglatestversionMessage);
                getLogger().info("oldversionmaycauseproblem: " + oldversionmaycauseproblemMessage);
                getLogger().info("updateavailable: " + updateavailableMessage);
                getLogger().info("updateurl: " + updateurlMessage);
                getLogger().info("storagetype: " + storageTypeMessage);
                getLogger().info("disable: " + disableMessage);
                getLogger().info("not_whitelisted: " + notWhitelistedMessage);
                getLogger().info("use_example: " + useExampleMessage);
                getLogger().info("unknown_option: " + unknownOptionMessage);
                getLogger().info("no_permission: " + noPermissionMessage);
                getLogger().info("player: " + playerMessage);
                getLogger().info("player_already_exist: " + playerAlreadyExistMessage);
                getLogger().info("player_added: " + playerAddedMessage);
                getLogger().info("player_removed_from_whitelist: " + playerRemovedFromWhitelistMessage);
                getLogger().info("player_not_in_whitelist: " + playerNotInWhitelistMessage);
                getLogger().info("loaded_json: " + loadedJsonMessage);
                getLogger().info("saved_json: " + savedJsonMessage);
                getLogger().info("loaded_mysql: " + loadedMysqlMessage);
                getLogger().info("saved_mysql: " + savedMysqlMessage);
                getLogger().info("now_language: " + nowLanguageMessage);
                getLogger().info("translator: " + translatorMessage);
                getLogger().info("reload: " + reloadMessage);
                getLogger().info("reload_language: " + reloadLanguage);
                getLogger().info("reload_whitelist: " + reloadWhitelist);
                getLogger().info("convert_success: " + convertsuccessMessage);
                getLogger().info("———————Language Debug mode———————");
            }
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> suggestions = new ArrayList<>();
        if (args.length == 1) {
            suggestions.add("add");   
            suggestions.add("remove");            
            suggestions.add("reload");
            suggestions.add("convert");
        }
        return suggestions;
    }

    @Override
    public void onDisable() {
        if (storageType.equalsIgnoreCase("json")) {
            saveToJSON();
        } else if (storageType.equalsIgnoreCase("mysql")) {
            saveToMySQL();
        }

        getLogger().info(disableMessage);
    }

    @EventHandler
    public void onPlayerPreLogin(PlayerPreLoginEvent event) {
        Player player = event.getPlayer();
        String playerName = player.getName();

        // 检查玩家是否在白名单中
        if (!whiteList.contains(playerName)) {
            event.disallow(PlayerPreLoginEvent.PreLoginEventResult.KICK_OTHER, notWhitelistedMessage);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            return handleReloadCommand(sender);
        } else if (args.length == 1 && args[0].equalsIgnoreCase("convert")) {
            return convertwhitelist(sender);
        } 

        if (args.length != 2) {
            sender.sendMessage(ChatColor.RED + String.format(useExampleMessage + " /wid <add|remove> <playername> or /wid reload or /wid convert"));
            return true;
        }

        String action = args[0];
        String playerName = args[1];

        if (action.equalsIgnoreCase("add")) {
            return handleAddCommand(sender, playerName);
        } else if (action.equalsIgnoreCase("remove")) {
            return handleRemoveCommand(sender, playerName);
        } else {
            sender.sendMessage(ChatColor.RED + String.format(unknownOptionMessage + " %s", action));
            return false;
        }
    }

    private boolean convertwhitelist(CommandSender sender) {
        if(!sender.hasPermission("wid.convert")) {
            sender.sendMessage(ChatColor.RED + noPermissionMessage);
            return false;
        }
        if (storageType.equalsIgnoreCase("json")) {
            // 如果当前是 Json ，则从 MySQL 中读取然后存到 Json 中
            loadFromMySQL();
            saveToJSON();
            sender.sendMessage(ChatColor.GREEN + convertsuccessMessage + " MySQL --> Json");
        } else if (storageType.equalsIgnoreCase("mysql")) {
            // 反之，Json 读然后存到 MySQL 中
            loadFromJSON();
            saveToMySQL();
            sender.sendMessage(ChatColor.GREEN + convertsuccessMessage + " Json --> MySQL");
        } else {
            sender.sendMessage(ChatColor.RED + "未知的存储类型: " + storageType);
            return false;
        }
        return true;
    }

    private boolean handleReloadCommand(CommandSender sender) {
        if (!sender.hasPermission("wid.reload")) {
            sender.sendMessage(ChatColor.RED + noPermissionMessage);
            return false;
        }

        // 重载配置文件
        reloadConfig();

        // 重载debugmode配置值
        debugmode = getConfig().getString("debugmode", "disable");

        // 重载语言文件
        String language = getConfig().getString("language", "zh_cn");
        loadLanguageFile(language);
        getLogger().info(reloadLanguage);
        
        // 清除缓存
        whiteList.clear();
        // 重新加载WhiteList
        storageType = getConfig().getString("storage", "json");
        if (storageType.equalsIgnoreCase("json")) {
            loadFromJSON();
        } else if (storageType.equalsIgnoreCase("mysql")) {
            loadFromMySQL();
        }
        getLogger().info(reloadWhitelist);
        sender.sendMessage(ChatColor.GREEN + reloadMessage);
        return true;
    }

    private boolean handleAddCommand(CommandSender sender, String playerName) {
        if (!sender.hasPermission("wid.add")) {
            sender.sendMessage(ChatColor.RED + noPermissionMessage);
            return false;
        }

        if (whiteList.contains(playerName)) {// 已经存在
            sender.sendMessage(ChatColor.YELLOW + playerMessage + " " + playerName + " " + playerAlreadyExistMessage);
        } else {
            whiteList.add(playerName);
            sender.sendMessage(ChatColor.GREEN + playerMessage + " " + playerName + " " + playerAddedMessage);
            saveWhiteList(); // 添加后保存
        }

        return true;
    }

    private boolean handleRemoveCommand(CommandSender sender, String playerName) {
        if (!sender.hasPermission("wid.remove")) {
            sender.sendMessage(ChatColor.RED + noPermissionMessage);
            return false;
        }

        if (whiteList.contains(playerName)) {
            whiteList.remove(playerName);  // 从白名单中移除
            sender.sendMessage(ChatColor.GREEN + playerMessage + " " + playerName + " " + playerRemovedFromWhitelistMessage);
            saveWhiteList(); // 移除后保存
        } else {
            sender.sendMessage(ChatColor.YELLOW + playerMessage + " " + playerName + " " + playerNotInWhitelistMessage);
        }

        return true;
    }

    // 保存白名单
    private void saveWhiteList() {
        if (storageType.equalsIgnoreCase("json")) {
            saveToJSON();
        } else if (storageType.equalsIgnoreCase("mysql")) {
            saveToMySQL();
        }
    }

    private void loadWhiteList() {
        storageType = getConfig().getString("storage", "json");
        if (storageType.equalsIgnoreCase("json")) {
            loadFromJSON();
        } else if (storageType.equalsIgnoreCase("mysql")) {
            loadFromMySQL();
        }
    }

    // 加载 JSON 存储
    private void loadFromJSON() {
        File file = new File(getDataFolder(), "whitelist.json");
        if (!file.exists()) {
            return;
        }

        try {
            JSONParser parser = new JSONParser();
            JSONArray jsonArray = (JSONArray) parser.parse(new FileReader(file));

            for (Object obj : jsonArray) {
                whiteList.add((String) obj);
            }
            getLogger().info(loadedJsonMessage);

        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

    // 保存至 JSON
    private void saveToJSON() {
        File file = new File(getDataFolder(), "whitelist.json");

        try (FileWriter writer = new FileWriter(file)) {
            JSONArray jsonArray = new JSONArray();
            jsonArray.addAll(new ArrayList<>(whiteList));
            writer.write(jsonArray.toJSONString());
            getLogger().info(savedJsonMessage);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 加载 MySQL 存储
    private void loadFromMySQL() {
        String url = "jdbc:mysql://" + getConfig().getString("mysql.host") + ":" + getConfig().getInt("mysql.port") + "/" + getConfig().getString("mysql.database");
        String user = getConfig().getString("mysql.username");
        String password = getConfig().getString("mysql.password");

        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement()) {

            String sql = "CREATE TABLE IF NOT EXISTS whitelist (id INT AUTO_INCREMENT, player_name VARCHAR(255), PRIMARY KEY (id))";
            stmt.executeUpdate(sql);

            ResultSet rs = stmt.executeQuery("SELECT player_name FROM whitelist");
            while (rs.next()) {
                whiteList.add(rs.getString("player_name"));
            }
            getLogger().info(loadedMysqlMessage);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 保存至 MySQL
    private void saveToMySQL() {
        String url = "jdbc:mysql://" + getConfig().getString("mysql.host") + ":" + getConfig().getInt("mysql.port") + "/" + getConfig().getString("mysql.database") + "?autoReconnect=true";
        String user = getConfig().getString("mysql.username");
        String password = getConfig().getString("mysql.password");

        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement()) {

            // 清空表内容
            stmt.executeUpdate("TRUNCATE TABLE whitelist");

            // 插入白名单数据
            for (String playerName : whiteList) {
                String sql = "INSERT INTO whitelist (player_name) VALUES ('" + playerName + "')";
                stmt.executeUpdate(sql);
            }
            getLogger().info(savedMysqlMessage);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}