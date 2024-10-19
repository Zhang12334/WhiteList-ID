package com.yourname.joineula;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

public class JoinEULA extends JavaPlugin implements Listener {

    private String eulaContent; // EULA 内容
    private Set<String> agreedPlayers; // 同意 EULA 的玩家名字列表
    private Gson gson; // Gson 实例
    private double teleportRange; // TP 范围 K

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        gson = new Gson();
        loadConfig(); // 加载配置
        loadEULAContent();
        loadAgreedPlayers(); // 加载同意 EULA 的玩家
    }

    private void loadConfig() {
        File configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            try {
                saveResource("config.yml", false); // 从资源文件夹复制配置文件
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        teleportRange = config.getDouble("teleport-range", 2.0); // 默认范围为2.0
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
            if (!isInSpawnRange(player)) {
                teleportToSpawn(player); // 传送到主世界出生点
                player.sendMessage(ChatColor.YELLOW + "请阅读并签署 EULA 协议！");
                giveUnsignedBook(player); // 给玩家未签名的书
            }
        }
    }

    private boolean isInSpawnRange(Player player) {
        Location spawnLocation = player.getWorld().getSpawnLocation();
        return player.getLocation().distance(spawnLocation) <= teleportRange;
    }

    private void teleportToSpawn(Player player) {
        Location spawnLocation = player.getWorld().getSpawnLocation();
        player.teleport(spawnLocation); // 传送到主世界出生点
    }

    private void giveUnsignedBook(Player player) {
        // 检查玩家的物品栏是否已存在未署名的书
        boolean hasBook = false;
        for (ItemStack item : player.getInventory()) {
            if (item != null && item.getType() == Material.WRITTEN_BOOK) {
                hasBook = true;
                break;
            }
        }
        
        if (!hasBook) {
            // 创建未署名的书
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
        
        // 检查掉落的物品是否是用户协议书
        if (droppedItem.getType() == Material.WRITTEN_BOOK) {
            // 如果玩家没有下蹲
            if (!player.isSneaking()) {
                event.getItemDrop().remove(); // 删除掉落的书
                giveUnsignedBook(player); // 给玩家新的未签名的书
            } else {
                // 如果玩家下蹲，记录同意
                event.getItemDrop().remove(); // 删除掉落的书
                playerAgrees(player); // 记录同意
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        ItemStack[] inventory = player.getInventory().getContents();
        
        // 遍历玩家的物品栏
        for (int i = 0; i < inventory.length; i++) {
            ItemStack item = inventory[i];
            // 检查玩家是否持有用户协议书
            if (item != null && item.getType() == Material.WRITTEN_BOOK) {
                inventory[i] = null; // 书移除走
            }
        }
        player.getInventory().setContents(inventory); // 更新物品栏
    }

    @EventHandler
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        // 检查玩家是否同意了 EULA
        if (!agreedPlayers.contains(player.getName())) {
            // 检查命令是否是 /reg 或 /login
            String command = event.getMessage().toLowerCase();
            if (!command.equals("/reg") && !command.equals("/login")) {
                event.setCancelled(true); // 取消命令
                player.sendMessage(ChatColor.RED + "您必须同意 EULA 才能使用其他指令。");
            }
        }
    }

    private void loadEULAContent() {
        Path path = Paths.get(getDataFolder().toString(), "text.txt");
        if (!Files.exists(path)) {
            try {
                Files.createDirectories(path.getParent());
                Files.createFile(path);
                // 在文件中写入默认 EULA 内容
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
        File file = new File(getDataFolder(), "agreedPlayers.json");
        if (!file.exists()) {
            try {
                file.createNewFile();
                agreedPlayers = new HashSet<>();
                saveAgreedPlayers();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try (FileReader reader = new FileReader(file)) {
                agreedPlayers = gson.fromJson(reader, new TypeToken<Set<String>>() {}.getType());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void saveAgreedPlayers() {
        File file = new File(getDataFolder(), "agreedPlayers.json");
        try (FileWriter writer = new FileWriter(file)) {
            gson.toJson(agreedPlayers, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void playerAgrees(Player player) {
        agreedPlayers.add(player.getName()); // 使用玩家名字
        saveAgreedPlayers();
        player.sendMessage(ChatColor.GREEN + "您已同意 EULA！");
    }
}
