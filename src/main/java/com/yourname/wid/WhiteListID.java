package com.yourname.wid;

import java.util.Collection;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader; 
import java.sql.*;
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
import org.yaml.snakeyaml.Yaml;


public class WhiteListID extends JavaPlugin implements CommandExecutor, Listener {

    private Set<String> whiteList;  // 使用玩家ID名称存储
    private String storageType;
    private Map<String, String> messages;  // 存储语言消息

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

        getLogger().info(getMessage("messages.startup"));
        getLogger().info(getMessage("messages.storagetype") + " " + storageType); // 添加空格以便格式良好
    }

    private void copyLanguageFile(File languageFile, String language) {
        InputStream langInput = getResource("lang/" + language + ".json");
        if (langInput != null) {
            try (FileWriter writer = new FileWriter(languageFile);
                 InputStreamReader isr = new InputStreamReader(langInput)) { // 添加 InputStreamReader
                char[] buffer = new char[1024];
                int length;
                while ((length = isr.read(buffer)) > 0) {
                    writer.write(buffer, 0, length);
                }
                getLogger().info(language + " 语言文件已复制到 lang 文件夹");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            // 如果指定的语言文件在 JAR 中也不存在，则复制默认的 zh_cn.json
            langInput = getResource("lang/zh_cn.json");
            if (langInput != null) {
                try (FileWriter writer = new FileWriter(languageFile);
                     InputStreamReader isr = new InputStreamReader(langInput)) { // 添加 InputStreamReader
                    char[] buffer = new char[1024];
                    int length;
                    while ((length = isr.read(buffer)) > 0) {
                        writer.write(buffer, 0, length);
                    }
                    getLogger().info("默认语言文件 zh_cn.json 已复制到 lang 文件夹");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                getLogger().warning("未找到语言文件 zh_cn.json，无法复制！");
            }
        }
    }

    private void loadLanguageFile(String language) {
        try (InputStream inputStream = new FileInputStream(new File(getDataFolder(), "lang/" + language + ".json"))) {
            JSONParser parser = new JSONParser();
            JSONObject jsonObject = (JSONObject) parser.parse(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            messages = (Map<String, String>) jsonObject.get("messages");
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

        getLogger().info(getMessage("messages.disable"));
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String playerName = player.getName();

        // 检查玩家是否在白名单中
        if (!whiteList.contains(playerName)) {
            Bukkit.getScheduler().runTask(this, () -> {
                player.kickPlayer(getMessage("messages.not_whitelisted"));
            });
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 2) {
            sender.sendMessage(ChatColor.RED + getMessage("messages.use_example", "/wid <add|remove> <playername>"));
            return true;
        }

        String action = args[0];
        String playerName = args[1];

        if (action.equalsIgnoreCase("add")) {
            return handleAddCommand(sender, playerName);
        } else if (action.equalsIgnoreCase("remove")) {
            return handleRemoveCommand(sender, playerName);
        } else {
            sender.sendMessage(ChatColor.RED + getMessage("messages.unknown_option", action));
            return false;
        }
    }

    private boolean handleAddCommand(CommandSender sender, String playerName) {
        if (!sender.hasPermission("wid.add")) {
            sender.sendMessage(ChatColor.RED + getMessage("messages.no_permission"));
            return false;
        }

        if (whiteList.contains(playerName)) {
            sender.sendMessage(ChatColor.YELLOW + getMessage("messages.player", playerName) + getMessage("messages.player_already_exist"));
        } else {
            whiteList.add(playerName);
            sender.sendMessage(ChatColor.GREEN + getMessage("messages.player", playerName) + getMessage("messages.player_added"));
            saveWhiteList(); // 添加后保存
        }

        return true;
    }

    private boolean handleRemoveCommand(CommandSender sender, String playerName) {
        if (!sender.hasPermission("wid.remove")) {
            sender.sendMessage(ChatColor.RED + getMessage("messages.no_permission"));
            return false;
        }

        if (whiteList.contains(playerName)) {
            whiteList.remove(playerName);  // 从白名单中移除
            sender.sendMessage(ChatColor.GREEN + getMessage("messages.player", playerName) + getMessage("messages.player_removed_from_whitelist"));
            saveWhiteList(); // 移除后保存
        } else {
            sender.sendMessage(ChatColor.YELLOW + getMessage("messages.player", playerName) + getMessage("messages.player_not_in_whitelist"));
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
            getLogger().info(getMessage("messages.loaded_json"));

        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

    // 保存至 JSON
    private void saveToJSON() {
        File file = new File(getDataFolder(), "whitelist.json");

        try (FileWriter writer = new FileWriter(file)) {
            JSONArray jsonArray = new JSONArray();
            jsonArray.addAll((Collection<?>) whiteList); // 更新这一行以消除警告
            writer.write(jsonArray.toJSONString());
            getLogger().info(getMessage("messages.saved_json"));

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
            getLogger().info(getMessage("messages.loaded_mysql"));

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
            getLogger().info(getMessage("messages.saved_mysql"));

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String getMessage(String key, String... args) {
        String message = messages.getOrDefault(key, key); // 获取对应的消息
        for (int i = 0; i < args.length; i++) {
            message = message.replace("%" + i + "%", args[i]); // 替换参数
        }
        return message;
    }
}
