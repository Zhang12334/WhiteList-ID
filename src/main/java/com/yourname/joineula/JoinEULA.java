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
import java.util.HashSet;
import java.util.Set;

public class JoinEULA extends JavaPlugin implements Listener {

    private String eulaContent; // EULA 内容
    private Set<String> agreedPlayers; // 同意 EULA 的玩家名字列表
    private Gson gson; // Gson 实例
    private double teleportRange; // 玩家移动范围

    @Override
    public void onEnable() {
        loadConfig(); // 加载配置
        Bukkit.getPluginManager().registerEvents(this, this);
        gson = new Gson();
        loadEULAContent();
        loadAgreedPlayers(); // 加载同意 EULA 的玩家
    }

    private void loadConfig() {
        saveDefaultConfig(); // 保存默认配置文件
        FileConfiguration config = getConfig(); // 获取配置
        teleportRange = config.getDouble("teleport-range", 2.0); // 默认范围为2.0
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("JoinEULA") && args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            if (sender.hasPermission("joineula.reload")) {
                loadConfig(); // 重新加载配置
                loadEULAContent(); // 重新加载 EULA 内容
                loadAgreedPlayers(); // 重新加载已同意的玩家
                sender.sendMessage(ChatColor.GREEN + "EULA 插件配置已重新加载。");
                return true;
            } else {
                sender.sendMessage(ChatColor.RED + "您没有权限执行此命令。");
                return true;
            }
        }
        return false;
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
        //获取玩家
        Player player = event.getPlayer();
        //已同意的玩家列表获取不到此玩家名
        if (!agreedPlayers.contains(player.getName())) {
            Location to = event.getTo();
            Location spawnLocation = player.getWorld().getSpawnLocation(); // 获取出生点位置
            loadConfig();
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
