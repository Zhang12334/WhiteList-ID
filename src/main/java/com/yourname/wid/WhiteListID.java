package com.yourname.wid;

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

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.util.HashSet;
import java.util.Set;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class WhiteListID extends JavaPlugin implements CommandExecutor, Listener {

    private Set<String> whiteList;  // 使用玩家ID名称存储
    private String storageType;

    @Override
    public void onEnable() {
        this.saveDefaultConfig();  // 保存默认配置文件
        whiteList = new HashSet<>();
        this.getCommand("wid").setExecutor(this);
        Bukkit.getPluginManager().registerEvents(this, this); // 注册事件监听

        String language = getConfig().getString("language", "zh_cn");
        loadLanguageFile(language);


        // 读取存储类型
        storageType = getConfig().getString("storage", "json");
        
        if (storageType.equalsIgnoreCase("json")) {
            loadFromJSON();
        } else if (storageType.equalsIgnoreCase("mysql")) {
            loadFromMySQL();
        }

        getLogger().info("messages.startup");
        getLogger().info("messages.storagetype" + storageType);
    }


    private void loadLanguageFile(String language) {
        Yaml yaml = new Yaml();
        try (InputStream inputStream = getResource("lang/" + language + ".yml")) {
            if (inputStream != null) {
                messages = yaml.load(inputStream);
            } else {
                getLogger().warning("语言文件未找到，使用默认语言 zh_cn.yml");
                loadLanguageFile("zh_cn");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
        if (storageType.equalsIgnoreCase("json")) {
            saveToJSON();
        } else if (storageType.equalsIgnoreCase("mysql")) {
            saveToMySQL();
        }

        getLogger().info("messages.disable");
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String playerName = player.getName();

        // 检查玩家是否在白名单中
        if (!whiteList.contains(playerName)) {
            Bukkit.getScheduler().runTask(this, () -> {
                player.kickPlayer("messages.not_whitelisted");
            });
        }
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 2) {
            sender.sendMessage(ChatColor.RED + "message.use_example" + "/wid <add|remove> <playername>");
            return true;
        }

        String action = args[0];
        String playerName = args[1];

        if (action.equalsIgnoreCase("add")) {
            return handleAddCommand(sender, playerName);
        } else if (action.equalsIgnoreCase("remove")) {
            return handleRemoveCommand(sender, playerName);
        } else {
            sender.sendMessage(ChatColor.RED + "message.unknown_option" + action);
            return false;
        }
    }

    private boolean handleAddCommand(CommandSender sender, String playerName) {
        if (!sender.hasPermission("wid.add")) {
            sender.sendMessage(ChatColor.RED + "messages.no_permission");
            return false;
        }

        if (whiteList.contains(playerName)) {
            sender.sendMessage(ChatColor.YELLOW + "messages.player" + playerName + "messages.player_already_exist");
        } else {
            whiteList.add(playerName);
            sender.sendMessage(ChatColor.GREEN + "messages.player" + playerName + "messages.player_added");
            saveWhiteList(); // 添加后保存
        }

        return true;
    }

    private boolean handleRemoveCommand(CommandSender sender, String playerName) {
        if (!sender.hasPermission("wid.remove")) {
            sender.sendMessage(ChatColor.RED + "messages.no_permission");
            return false;
        }

        if (whiteList.contains(playerName)) {
            whiteList.remove(playerName);  // 从白名单中移除
            sender.sendMessage(ChatColor.GREEN + "messages.player" + playerName + "messages.player_removed_from_whitelist");
            saveWhiteList(); // 移除后保存
        } else {
            sender.sendMessage(ChatColor.YELLOW + "messages.player" + playerName + "messages.player_not_in_whitelist");
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
            getLogger().info("messages.loaded_json");

        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

    // 保存至 JSON
    private void saveToJSON() {
        File file = new File(getDataFolder(), "whitelist.json");

        try (FileWriter writer = new FileWriter(file)) {
            JSONArray jsonArray = new JSONArray();
            jsonArray.addAll(whiteList);
            writer.write(jsonArray.toJSONString());
            getLogger().info("messages.saved_json");

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
            getLogger().info("messages.loaded_mysql");

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
            getLogger().info("messages.saved_mysql");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
