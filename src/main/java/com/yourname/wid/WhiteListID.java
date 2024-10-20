package com.yourname.wid;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
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

public class WhiteListID extends JavaPlugin implements CommandExecutor {

    private Set<String> whiteList;  // 使用玩家ID名称存储
    private String storageType;

    @Override
    public void onEnable() {
        this.saveDefaultConfig();  // 保存默认配置文件
        whiteList = new HashSet<>();
        this.getCommand("wid").setExecutor(this);
        
        // 读取存储类型
        storageType = getConfig().getString("storage", "json");
        
        if (storageType.equalsIgnoreCase("json")) {
            loadFromJSON();
        } else if (storageType.equalsIgnoreCase("mysql")) {
            loadFromMySQL();
        }

        getLogger().info("WhiteList-ID 插件已启用，使用存储类型: " + storageType);
    }

    @Override
    public void onDisable() {
        if (storageType.equalsIgnoreCase("json")) {
            saveToJSON();
        } else if (storageType.equalsIgnoreCase("mysql")) {
            saveToMySQL();
        }

        getLogger().info("WhiteList-ID 插件已禁用");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 2) {
            sender.sendMessage(ChatColor.RED + "用法: /wid <add|remove> <player>");
            return false;
        }

        String action = args[0];
        String playerName = args[1];

        if (action.equalsIgnoreCase("add")) {
            if (!sender.hasPermission("wid.add")) {
                sender.sendMessage(ChatColor.RED + "你没有权限使用此命令");
                return false;
            }

            if (whiteList.contains(playerName)) {
                sender.sendMessage(ChatColor.YELLOW + "玩家 " + playerName + " 已在白名单中");
            } else {
                whiteList.add(playerName);
                sender.sendMessage(ChatColor.GREEN + "玩家 " + playerName + " 已添加到白名单");
            }

        } else if (action.equalsIgnoreCase("remove")) {
            if (!sender.hasPermission("wid.remove")) {
                sender.sendMessage(ChatColor.RED + "你没有权限使用此命令");
                return false;
            }

            if (whiteList.contains(playerName)) {
                whiteList.remove(playerName);
                sender.sendMessage(ChatColor.GREEN + "玩家 " + playerName + " 已从白名单中移除");
            } else {
                sender.sendMessage(ChatColor.YELLOW + "玩家 " + playerName + " 不在白名单中");
            }
        } else {
            sender.sendMessage(ChatColor.RED + "未知操作: " + action);
            return false;
        }

        return true;
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
            getLogger().info("成功从 JSON 加载白名单");

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
            getLogger().info("白名单已保存至 JSON");

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
            getLogger().info("成功从 MySQL 加载白名单");

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
            getLogger().info("白名单已保存至 MySQL");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
