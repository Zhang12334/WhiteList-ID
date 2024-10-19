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
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

public class JoinEULA extends JavaPlugin implements Listener {

    private String eulaContent; // EULA 内容
    private Set<String> agreedPlayers; // 同意 EULA 的玩家名字列表
    private Gson gson; // Gson 实例
    private double teleportRange; // 玩家移动范围
    private Connection connection; // 数据库连接

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        gson = new Gson();
        loadConfigDefaults(); // 确保配置项的默认值
        setupDatabase(); // 设置数据库连接
        loadEULAContent();
        loadAgreedPlayers(); // 加载同意 EULA 的玩家
    }

    private void loadConfigDefaults() {
        FileConfiguration config = getConfig();
        if (!config.contains("version")) {
            config.set("version", "1.0");
        }
        if (!config.contains("storage-type")) {
            config.set("storage-type", "json");
        }
        if (!config.contains("teleport-range")) {
            config.set("teleport-range", 2.0);
        }
        saveConfig();
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
                getLogger().info("EULA 协议表已创建或已存在。");
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
                    sender.sendMessage(ChatColor.GREEN + "EULA 插件配置已重新加载。");
                    return true;
                } else {
                    sender.sendMessage(ChatColor.RED + "您没有权限执行此命令。");
                    return true;
                }
            } else if (args[0].equalsIgnoreCase("remove") && args.length > 1) {
                if (sender.hasPermission("joineula.remove")) {
                    String playerName = args[1];
                    removePlayerAgreement(playerName);
                    sender.sendMessage(ChatColor.GREEN + "已删除 " + playerName + " 的 EULA 同意记录。");
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
                Statement statement = connection.createStatement();
                String sql = "DELETE FROM eula_agreements WHERE player_name = '" + playerName + "'";
                statement.executeUpdate(sql);
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
        Player player = event.getPlayer();
        if (!player.hasPlayedBefore()) {
            teleportToSpawn(player); // 传送到主世界出生点
            player.sendMessage(ChatColor.YELLOW + "请阅读并签署 EULA 协议！");
            giveUnsignedBook(player); // 给玩家未签名的书
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
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
        player.teleport(spawnLocation); // 传送到主世界出生点
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
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String command = event.getMessage().toLowerCase(); // 获取命令并转为小写

        // 检查玩家是否已同意 EULA
        if (!agreedPlayers.contains(player.getName())) {
            // 如果命令不是 /reg 或 /login，禁止执行
            if (!command.startsWith("/reg") && !command.startsWith("/login")) {
                player.sendMessage(ChatColor.RED + "您必须先同意 EULA 才能执行此命令。");
                event.setCancelled(true); // 取消命令执行
            }
        }
    }

    private void loadEULAContent() {
        Path path = Paths.get(getDataFolder().toString(), "text.txt");
        if (!Files.exists(path)) {
            try {
                Files.createDirectories(path.getParent());
                try (BufferedWriter writer = Files.newBufferedWriter(path)) {
                    writer.write("欢迎来到本服务器！\n\n请阅读以下协议:\n\n1. 不得有作弊行为\n2. 尊重其他玩家\n3. 不得发布任何不当言论\n\n是否同意？");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            eulaContent = new String(Files.readAllBytes(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadAgreedPlayers() {
        FileConfiguration config = getConfig();
        String storageType = config.getString("storage-type", "json");

        if ("mysql".equalsIgnoreCase(storageType)) {
            loadAgreedPlayersFromDatabase();
        } else {
            loadAgreedPlayersFromJson();
        }
    }

    private void loadAgreedPlayersFromDatabase() {
        // 从数据库加载已同意的玩家
        agreedPlayers = new HashSet<>();
        if (connection != null) {
            try {
                Statement statement = connection.createStatement();
                var resultSet = statement.executeQuery("SELECT player_name FROM eula_agreements");
                while (resultSet.next()) {
                    agreedPlayers.add(resultSet.getString("player_name"));
                }
            } catch (SQLException e) {
                getLogger().severe("加载同意玩家时出错: " + e.getMessage());
            }
        }
    }

    private void loadAgreedPlayersFromJson() {
        File file = new File(getDataFolder(), "agreedPlayers.json");
        if (!file.exists()) {
            agreedPlayers = new HashSet<>(); // 初始化同意玩家列表
            saveAgreedPlayers(); // 保存初始化文件
        } else {
            try (FileReader reader = new FileReader(file)) {
                agreedPlayers = gson.fromJson(reader, new TypeToken<Set<String>>() {}.getType());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void saveAgreedPlayers() {
        FileConfiguration config = getConfig();
        String storageType = config.getString("storage-type", "json");

        if ("mysql".equalsIgnoreCase(storageType)) {
            saveAgreedPlayersToDatabase();
        } else {
            saveAgreedPlayersToJson();
        }
    }

    private void saveAgreedPlayersToDatabase() {
        if (connection != null) {
            try {
                Statement statement = connection.createStatement();
                for (String playerName : agreedPlayers) {
                    String sql = "INSERT INTO eula_agreements (player_name) VALUES ('" + playerName + "')" +
                                 " ON DUPLICATE KEY UPDATE player_name = player_name"; // 防止重复插入
                    statement.executeUpdate(sql);
                }
            } catch (SQLException e) {
                getLogger().severe("保存同意玩家时出错: " + e.getMessage());
            }
        }
    }

    private void saveAgreedPlayersToJson() {
        File file = new File(getDataFolder(), "agreedPlayers.json");
        try (FileWriter writer = new FileWriter(file)) {
            gson.toJson(agreedPlayers, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void playerAgrees(Player player) {
        agreedPlayers.add(player.getName());
        saveAgreedPlayers();
        player.sendMessage(ChatColor.GREEN + "您已同意 EULA！");
    }
}
