package com.yourname.joineula;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class JoinEULA extends JavaPlugin implements Listener {

    private String eulaContent; // EULA 内容
    private Set<String> agreedPlayers; // 同意 EULA 的玩家名字列表
    private Gson gson; // Gson 实例
    private double teleportRange; // 玩家移动范围
    private Connection connection; // 数据库连接
    private List<String> allowedCommands; // 允许的指令列表

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        gson = new Gson();
        createDefaultConfig(); // 创建默认配置
        setupDatabase(); // 设置数据库连接
        loadEULAContent();
        loadAgreedPlayers(); // 加载同意 EULA 的玩家
        loadAllowedCommands(); // 加载允许的指令列表
    }

    private void createDefaultConfig() {
        // 如果 config.yml 不存在，创建一个默认的 config.yml
        File configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            // 创建目录
            getDataFolder().mkdirs();
            try {
                // 创建并写入默认配置
                FileWriter writer = new FileWriter(configFile);
                writer.write("# 配置文件版本，请勿更改\n");
                writer.write("version: 1.0\n");
                writer.write("# 存储类型，可选项：json / mysql\n");
                writer.write("storage-type: json\n");
                writer.write("# 服务类型，可选项：verify / request\n");
                writer.write("service-type: verify\n");
                writer.write("# 允许的指令列表，在玩家同意 EULA 前可以执行这些指令\n");
                writer.write("allowed-commands:\n");
                writer.write("  - reg\n"); // 示例指令
                writer.write("  - login\n"); // 示例指令
                writer.write("# TP玩家的范围，未同意EULA移动超过以出生点为中心此数为半径的范围后会被tp回出生点\n");
                writer.write("teleport-range: 2.0\n");
                writer.write("# MySQL 配置\n");
                writer.write("mysql:\n");
                writer.write("# 地址\n");
                writer.write("  host: localhost\n");
                writer.write("# 端口\n");
                writer.write("  port: 3306\n");
                writer.write("# 数据库名\n");
                writer.write("  database: yourdatabase\n");
                writer.write("# 用户名\n");
                writer.write("  username: yourusername\n");
                writer.write("# 密码\n");
                writer.write("  password: yourpassword\n");
                writer.close();
                getLogger().info("已创建默认的 config.yml 文件");
            } catch (IOException e) {
                getLogger().severe("创建 config.yml 文件时出错: " + e.getMessage());
            }
        } else {
            // 检查版本，如果是老版本则添加默认值
            FileConfiguration config = getConfig();
            if (!config.contains("version")) {
                config.set("version", "1.0");
            }
            if (!config.contains("storage-type")) {
                config.set("storage-type", "json");
            }
            if (!config.contains("service-type")) {
                config.set("service-type", "verify");
            }
            if (!config.contains("teleport-range")) {
                config.set("teleport-range", 2.0);
            }
            if (!config.contains("allowed-commands")) {
                config.set("allowed-commands", List.of("spawn", "help")); // 默认允许指令
            }
            saveConfig();
        }
    }

    private void loadAllowedCommands() {
        FileConfiguration config = getConfig();
        allowedCommands = config.getStringList("allowed-commands");
    }

    private void setupDatabase() {
        FileConfiguration config = getConfig();
        String storageType = config.getString("storage-type", "json");

        if ("mysql".equalsIgnoreCase(storageType)) {
            String host = config.getString("mysql.host", "localhost");
            int port = config.getInt("mysql.port", 3306);
            String database = config.getString("mysql.database", "yourdatabase");
            String user = config.getString("mysql.username", "yourusername");
            String password = config.getString("mysql.password", "yourpassword");

            String url = "jdbc:mysql://" + host + ":" + port + "/" + database;

            try {
                connection = DriverManager.getConnection(url, user, password);
                getLogger().info("MySQL 数据库连接成功。");
                createTable(); // 创建表
            } catch (SQLException e) {
                getLogger().severe("无法连接到 MySQL 数据库: " + e.getMessage());
            }
        }
    }

    private void createTable() {
        if (connection != null) {
            try {
                Statement statement = connection.createStatement();
                String sql = "CREATE TABLE IF NOT EXISTS eula_agreements (" +
                             "id INT AUTO_INCREMENT PRIMARY KEY, " +
                             "player_name VARCHAR(255) NOT NULL UNIQUE" +
                             ")";
                statement.executeUpdate(sql);
                getLogger().info("EULA 表已存在");
            } catch (SQLException e) {
                getLogger().severe("创建 EULA 表时出错: " + e.getMessage());
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("JoinEULA") && args.length > 0) {
            if (args[0].equalsIgnoreCase("reload")) {
                if (sender.hasPermission("joineula.reload")) {
                    loadEULAContent(); // 重新加载 EULA 内容
                    loadAgreedPlayers(); // 重新加载已同意的玩家
                    sender.sendMessage(ChatColor.GREEN + "EULA 插件配置已重新加载");
                    return true;
                } else {
                    sender.sendMessage(ChatColor.RED + "您没有权限执行此命令。");
                    return true;
                }
            } else if (args[0].equalsIgnoreCase("remove") && args.length > 1) {
                if (sender.hasPermission("joineula.remove")) {
                    String playerName = args[1];
                    removePlayerAgreement(playerName);
                    sender.sendMessage(ChatColor.GREEN + "已删除 " + playerName + " 的 EULA 同意记录");
                    return true;
                } else {
                    sender.sendMessage(ChatColor.RED + "您没有权限执行此命令。");
                    return true;
                }
            }
        }
        return false;
    }

    private void removePlayerAgreement(String playerName) {
        if (connection != null) {
            try {
                String sql = "DELETE FROM eula_agreements WHERE player_name = ?";
                try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                    preparedStatement.setString(1, playerName);
                    preparedStatement.executeUpdate();
                }
            } catch (SQLException e) {
                getLogger().severe("删除玩家协议时出错: " + e.getMessage());
            }
        } else {
            agreedPlayers.remove(playerName);
            saveAgreedPlayers();
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        loadAgreedPlayers();
        Player player = event.getPlayer();
        if (!agreedPlayers.contains(player.getName())) {
            teleportToSpawn(player); // 传送到主世界出生点
            player.sendMessage(ChatColor.YELLOW + "请阅读并签署 EULA 协议！");
            giveUnsignedBook(player); // 给玩家未签名的书
        }
    }

    public void onPlayerJoin(PlayerJoinEvent event) {
        loadAgreedPlayers();
        Player player = event.getPlayer();
        // 检查 service-type
        if ("request".equalsIgnoreCase(serviceType)) {
            if (!agreedPlayers.contains(player.getName())) {
                player.kickPlayer(ChatColor.RED + "您未同意EULA，无法进入服务器！");
                return; // 直接踢出玩家
            }
        }

        // 检查是否已同意 EULA
        if (!agreedPlayers.contains(player.getName())) {
            teleportToSpawn(player); // 传送到主世界出生点
            player.sendMessage(ChatColor.YELLOW + "请阅读并签署 EULA 协议！");
            giveUnsignedBook(player); // 给玩家未签名的书
        }
    }





    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        loadAgreedPlayers();
        Player player = event.getPlayer();
        if (!agreedPlayers.contains(player.getName())) {
            Location to = event.getTo();
            Location spawnLocation = player.getWorld().getSpawnLocation(); // 获取出生点位置
            teleportRange = getConfig().getDouble("teleport-range", 2.0); // 默认范围为2.0
            if (to != null && to.distance(spawnLocation) > teleportRange) {
                teleportToSpawn(player); // 传送到主世界出生点
                player.sendMessage(ChatColor.YELLOW + "请阅读并签署 EULA 协议！");
                giveUnsignedBook(player); // 给玩家未签名的书
            }
        }
    }

    private void teleportToSpawn(Player player) {
        Location spawnLocation = player.getWorld().getSpawnLocation();
        player.teleport(spawnLocation); // 传送玩家
    }

    private void giveUnsignedBook(Player player) {
        boolean hasBook = false;
        for (ItemStack item : player.getInventory()) {
            if (item != null && item.getType() == Material.WRITTEN_BOOK) {
                hasBook = true;
                break;
            }
        }
        
        if (!hasBook) {
            ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
            BookMeta meta = (BookMeta) book.getItemMeta();
            if (meta != null) {
                meta.setTitle(ChatColor.GOLD + "Server EULA");
                meta.setAuthor("Server Admin");
                meta.addPage(eulaContent); // 使用 EULA 内容
                book.setItemMeta(meta);
                player.getInventory().addItem(book); // 将书放入玩家的物品栏
            }
        }
        player.sendMessage(ChatColor.GREEN + "请下蹲并丢出本书 (Shift+Q) 来同意 EULA");
    }

    private void loadEULAContent() {
        // 从文件加载 EULA 内容
        try {
            Path eulaPath = Paths.get(getDataFolder().toString(), "eula.txt");
            eulaContent = Files.readString(eulaPath);
        } catch (IOException e) {
            getLogger().severe("加载 EULA 内容时出错: " + e.getMessage());
            eulaContent = "未找到EULA内容，请确保 eula.txt 文件存在";
        }
    }

    private void loadAgreedPlayers() {
        if (connection != null) {
            agreedPlayers = new HashSet<>();
            try {
                Statement statement = connection.createStatement();
                var resultSet = statement.executeQuery("SELECT player_name FROM eula_agreements");
                while (resultSet.next()) {
                    agreedPlayers.add(resultSet.getString("player_name"));
                }
            } catch (SQLException e) {
                getLogger().severe("加载同意的玩家时出错: " + e.getMessage());
            }
        } else {
            File file = new File(getDataFolder(), "agreedPlayers.json");
            if (file.exists()) {
                try (Reader reader = new FileReader(file)) {
                    agreedPlayers = gson.fromJson(reader, new TypeToken<Set<String>>() {}.getType());
                } catch (IOException e) {
                    getLogger().severe("加载已同意玩家文件时出错: " + e.getMessage());
                }
            } else {
                agreedPlayers = new HashSet<>();
            }
        }
    }

    private void saveAgreedPlayers() {
        if (connection != null) {
            // 使用 MySQL 保存同意的玩家
            try {
                for (String playerName : agreedPlayers) {
                    String sql = "INSERT INTO eula_agreements (player_name) VALUES (?) ON DUPLICATE KEY UPDATE player_name = player_name";
                    try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                        preparedStatement.setString(1, playerName);
                        preparedStatement.executeUpdate();
                    }
                }
                getLogger().info("已同意玩家信息已保存到 MySQL 数据库。");
            } catch (SQLException e) {
                getLogger().severe("保存已同意玩家到 MySQL 时出错: " + e.getMessage());
            }
        } else {
            // 如果没有数据库连接，保存到 JSON 文件
            File file = new File(getDataFolder(), "agreedPlayers.json");
            try (Writer writer = new FileWriter(file)) {
                gson.toJson(agreedPlayers, writer);
            } catch (IOException e) {
                getLogger().severe("保存已同意玩家文件时出错: " + e.getMessage());
            }
        }
    }

    @EventHandler
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) { 
        Player player = event.getPlayer();
        String command = event.getMessage(); // 保持原有大小写

        // 检查玩家是否已同意 EULA
        if (!agreedPlayers.contains(player.getName())) {
            // 检查是否是允许的指令
            for (String allowedCommand : allowedCommands) {
                if (command.toLowerCase().startsWith("/" + allowedCommand.toLowerCase())) {
                    return; // 如果是允许的指令，直接返回
                }
            }
            // 如果不在允许的指令中，则取消指令执行
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "您必须同意 EULA 才能执行该指令。");
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        ItemStack[] inventory = player.getInventory().getContents();
        
        for (int i = 0; i < inventory.length; i++) {
            ItemStack item = inventory[i];
            if (item != null && item.getType() == Material.WRITTEN_BOOK) {
                inventory[i] = null; // 书移除走
            }
        }
        player.getInventory().setContents(inventory); // 更新物品栏
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        ItemStack droppedItem = event.getItemDrop().getItemStack();
        
        if (droppedItem.getType() == Material.WRITTEN_BOOK) {
            if (!player.isSneaking()) {
                event.getItemDrop().remove(); // 删除掉落的书
                giveUnsignedBook(player); // 给玩家新的未签名的书
            } else {
                event.getItemDrop().remove(); // 删除掉落的书
                playerAgrees(player); // 记录同意
            }
        }
    }

    private void playerAgrees(Player player) {
        // 记录玩家同意的逻辑
        agreedPlayers.add(player.getName());
        saveAgreedPlayers(); // 保存同意记录
        player.sendMessage(ChatColor.GREEN + "您已成功同意 EULA！");
    }
}
