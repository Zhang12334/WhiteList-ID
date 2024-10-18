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
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

public class JoinEULA extends JavaPlugin implements Listener {

    private String eulaContent; // EULA 内容
    private Set<String> agreedPlayers; // 同意 EULA 的玩家 ID 列表
    private Gson gson; // Gson 实例

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        gson = new Gson();
        loadEULAContent();
        loadAgreedPlayers(); // 加载同意 EULA 的玩家
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!player.hasPlayedBefore()) {
            player.sendMessage(ChatColor.YELLOW + "请阅读并签署 EULA 协议！");
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!agreedPlayers.contains(player.getUniqueId().toString())) {
            teleportToSpawn(player); // 传送到主世界出生点
            giveUnsignedBook(player); // 给玩家未签名的书
        }
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

            player.sendMessage(ChatColor.GREEN + "请阅读 EULA 后通过下蹲同意来进入服务器");
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        // 检查玩家是否在蹲下状态并持有未署名的书
        if (event.getItem() != null && event.getItem().getType() == Material.WRITTEN_BOOK &&
            event.getItem().getItemMeta() != null && event.getItem().getItemMeta().getDisplayName().contains("Server EULA")) {
            if (player.isSneaking()) {
                playerAgrees(player); // 记录同意
                // 收回书
                player.getInventory().remove(event.getItem());
                player.sendMessage(ChatColor.GREEN + "感谢您同意 EULA！");
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
                    writer.write("欢迎来到服务器！\n\n请阅读以下协议:\n\n1. 不得使用任何作弊插件。\n2. 尊重其他玩家。\n3. 不得发布任何不当言论。\n\n是否同意？");
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
        agreedPlayers.add(player.getUniqueId().toString());
        saveAgreedPlayers();
    }
}
