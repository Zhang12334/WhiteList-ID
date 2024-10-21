package com.meow.wid;

import java.util.Collection;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader; 
import java.nio.charset.StandardCharsets; // 确保导入这个类
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
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

    @Override
    public void onEnable() {
        this.saveDefaultConfig();  // 保存默认配置文件
        whiteList = new HashSet<>();
        this.getCommand("wid").setExecutor(this);
        Bukkit.getPluginManager().registerEvents(this, this); // 注册事件监听

        // 创建 lang 文件夹
        File langFolder = new File(getDataFolder(), "lang");
        if (!langFolder.exists()) {
            langFolder.mkdirs(); // 创建文件夹
        }

        debugmode = getConfig().getString("debugmode", "disable");

        // 检查语言文件
        String language = getConfig().getString("language", "zh_cn");
        File languageFile = new File(langFolder, language + ".json");

        // 如果指定的语言文件不存在，则尝试从 JAR 中复制
        if (!languageFile.exists()) {
            copyLanguageFile(languageFile, language);
        }

        loadLanguageFile(language);

        // 读取存储类型
        storageType = getConfig().getString("storage", "json");
        
        if (storageType.equalsIgnoreCase("json")) {
            loadFromJSON();
        } else if (storageType.equalsIgnoreCase("mysql")) {
            loadFromMySQL();
        }

        getLogger().info(startupMessage);
        getLogger().info(storageTypeMessage + " " + storageType); // 添加空格，瞅着好看
    }

    private void copyLanguageFile(File languageFile, String language) {
        InputStream langInput = getResource("lang/" + language + ".json");
        if (langInput != null) {
            try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(languageFile), StandardCharsets.UTF_16BE);
                InputStreamReader isr = new InputStreamReader(langInput, StandardCharsets.UTF_16BE)) { // 使用 UTF-8 读取
                char[] buffer = new char[1024];
                int length;
                while ((length = isr.read(buffer)) > 0) {
                    writer.write(buffer, 0, length);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            // 如果指定的语言文件在 JAR 中也不存在，则复制默认 zh_cn.json
            langInput = getResource("lang/zh_cn.json");
            if (langInput != null) {
                try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(languageFile), StandardCharsets.UTF_16BE);
                    InputStreamReader isr = new InputStreamReader(langInput, StandardCharsets.UTF_16BE)) { // 使用 UTF-8 读取
                    char[] buffer = new char[1024];
                    int length;
                    while ((length = isr.read(buffer)) > 0) {
                        writer.write(buffer, 0, length);
                    }
                    getLogger().info("默认语言文件 zh_cn.json 已复制到 lang 文件夹");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    private void loadLanguageFile(String language) {
        try (InputStream inputStream = new FileInputStream(new File(getDataFolder(), "lang/" + language + ".json"))) {
            JSONParser parser = new JSONParser();
            JSONObject jsonObject = (JSONObject) parser.parse(new InputStreamReader(inputStream, StandardCharsets.UTF_16BE));
            
            // 直接存储消息内容
            JSONObject messagesObject = (JSONObject) jsonObject.get("messages");
            startupMessage = (String) messagesObject.get("startup");
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
            // 当前使用语言
            getLogger().info(nowLanguageMessage);
            //翻译贡献者
            getLogger().info(translatorMessage);
            // 调试模式
            //一个判断搞半天，写C++写习惯了直接两个等于号扔上来了，我是傻逼
            if(debugmode.equals("enable")){
                // ↑tmd该死的判断，大难绷之if(debugmode == "enable")
                // debug！
                getLogger().info("———————Debug———————");
                getLogger().info(nowLanguageMessage);
                getLogger().info("startup: " + startupMessage);
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
                getLogger().info("———————Debug———————");
            }
        } catch (IOException | ParseException e) {
            getLogger().warning("未找到语言文件，使用默认语言 zh_cn.json");
            loadLanguageFile("zh_cn");
        }
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
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String playerName = player.getName();

        // 检查玩家是否在白名单中
        if (!whiteList.contains(playerName)) {
            Bukkit.getScheduler().runTask(this, () -> {
                player.kickPlayer(notWhitelistedMessage);
            });
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            return handleReloadCommand(sender);
        }

        if (args.length != 2) {
            sender.sendMessage(ChatColor.RED + String.format(useExampleMessage + " /wid <add|remove> <playername>"));
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
        File languageFile = new File(getDataFolder() + "/lang/", language + ".json");

        if (!languageFile.exists()) {
            copyLanguageFile(languageFile, language);
        }

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
        String url = "jdbc:mysql://" + getConfig().getString("mysql.host") + ":" + getConfig().getInt("mysql.port") + "/" + getConfig().getString("mysql.database");
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