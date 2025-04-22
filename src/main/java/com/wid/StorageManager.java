package com.wid;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.JSONArray;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Set;

public class StorageManager {

    private final JavaPlugin plugin;
    private final String storageType;
    private final FileConfiguration config;
    private final LanguageManager languageManager;
    private final Set<String> whiteList;

    public StorageManager(JavaPlugin plugin, String storageType, FileConfiguration config, LanguageManager languageManager, Set<String> whiteList) {
        this.plugin = plugin;
        this.storageType = storageType;
        this.config = config;
        this.languageManager = languageManager;
        this.whiteList = whiteList;
    }

    public void saveWhiteList() {
        if (storageType.equalsIgnoreCase("json")) {
            saveToJSON();
        } else if (storageType.equalsIgnoreCase("mysql")) {
            saveToMySQL();
        }
    }

    public void loadWhiteList() {
        whiteList.clear(); // 清空旧数据
        if (storageType.equalsIgnoreCase("json")) {
            loadFromJSON();
        } else if (storageType.equalsIgnoreCase("mysql")) {
            loadFromMySQL();
        }
    }

    public void loadFromJSON() {
        File file = new File(plugin.getDataFolder(), "whitelist.json");
        if (!file.exists()) {
            return;
        }

        try {
            String content = new String(Files.readAllBytes(Paths.get(file.getPath())));
            JSONArray jsonArray = new JSONArray(content);

            for (int i = 0; i < jsonArray.length(); i++) {
                whiteList.add(jsonArray.getString(i));
            }
            plugin.getLogger().info(languageManager.getMessage("loadedFromJson"));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveToJSON() {
        File file = new File(plugin.getDataFolder(), "whitelist.json");

        try (FileWriter writer = new FileWriter(file)) {
            JSONArray jsonArray = new JSONArray();

            for (String entry : whiteList) {
                jsonArray.put(entry);
            }

            writer.write(jsonArray.toString(4));
            plugin.getLogger().info(languageManager.getMessage("savedToJson"));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadFromMySQL() {
        String url = "jdbc:mysql://" + config.getString("mysql.host") + ":" + config.getInt("mysql.port") + "/" + config.getString("mysql.database");
        String user = config.getString("mysql.username");
        String password = config.getString("mysql.password");

        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement()) {

            String sql = "CREATE TABLE IF NOT EXISTS whitelist (id INT AUTO_INCREMENT, player_name VARCHAR(255), PRIMARY KEY (id))";
            stmt.executeUpdate(sql);

            ResultSet rs = stmt.executeQuery("SELECT player_name FROM whitelist");
            while (rs.next()) {
                whiteList.add(rs.getString("player_name"));
            }
            plugin.getLogger().info(languageManager.getMessage("loadedFromMySQL"));

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void saveToMySQL() {
        String url = "jdbc:mysql://" + config.getString("mysql.host") + ":" + config.getInt("mysql.port") + "/" + config.getString("mysql.database") + "?autoReconnect=true";
        String user = config.getString("mysql.username");
        String password = config.getString("mysql.password");

        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement()) {

            stmt.executeUpdate("TRUNCATE TABLE whitelist");

            for (String playerName : whiteList) {
                String sql = "INSERT INTO whitelist (player_name) VALUES ('" + playerName + "')";
                stmt.executeUpdate(sql);
            }
            plugin.getLogger().info(languageManager.getMessage("savedToMySQL"));

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String getStorageType() {
        return storageType;
    }
}
