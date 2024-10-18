package com.yourname.joineula;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location; // 添加这一行
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
            // 在玩家加入时提醒他们
            player.sendMessage(ChatColor.YELLOW + "请阅读并签署 EULA 协议！");
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!agreedPlayers.contains(player.getUniqueId().toString())) {
            teleportToSpawn(player); // 传送到主世界出生点
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        // 检查玩家是否拿到了书
        if (item != null && item.getType() == Material.WRITTEN_BOOK) {
            BookMeta meta = (BookMeta) item.getItemMeta();
            if (meta != null && meta.hasTitle() && meta.getTitle().equals(ChatColor.GOLD + "Server EULA")) {
                // 模拟签名过程
                if (meta.hasAuthor() && meta.getAuthor().equals("Server Admin")) {
                    player.sendMessage(ChatColor.GREEN + "您已同意 EULA！");
                    addPlayerToAgreedList(player); // 添加玩家到同意列表
                    return; // 退出，不再处理其他逻辑
                }
                player.sendMessage(ChatColor.RED + "请使用笔签署以同意协议。");
            }
        }

        // 如果玩家没有书，给他们一本 EULA 书
        if (event.getAction().toString().contains("RIGHT_CLICK")) {
            givePlayerEULABook(player);
        }
    }

    private void givePlayerEULABook(Player player) {
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();
        if (meta != null) {
            meta.setTitle(ChatColor.GOLD + "Server EULA");
            meta.setAuthor("Server Admin");
            meta.addPage(eulaContent);
            book.setItemMeta(meta);
            player.getInventory().addItem(book); // 给玩家一本 EULA 书
            player.sendMessage(ChatColor.YELLOW + "您获得了一本 EULA 书，请阅读并签署！");
        }
    }

    private void loadEULAContent() {
        Path path = Paths.get(getDataFolder().toString(), "text.txt");
        if (Files.exists(path)) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(Files.newInputStream(path)))) {
                StringBuilder contentBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    contentBuilder.append(line).append("\n");
                }
                eulaContent = contentBuilder.toString();
            } catch (IOException e) {
                getLogger().severe("Failed to load EULA content: " + e.getMessage());
                eulaContent = "EULA content could not be loaded.";
            }
        } else {
            createDefaultEULAFile(path);
            eulaContent = "EULA file created with default content.";
        }
    }

    private void createDefaultEULAFile(Path path) {
        try {
            Files.createDirectories(path.getParent()); // 创建目录
            try (BufferedWriter writer = Files.newBufferedWriter(path)) {
                writer.write("欢迎来到服务器！\n");
                writer.write("请阅读以下协议:\n");
                writer.write("1. 不得使用任何作弊插件。\n");
                writer.write("2. 尊重其他玩家。\n");
                writer.write("3. 不得发布任何不当言论。\n");
                writer.write("是否同意？\n");
            }
        } catch (IOException e) {
            getLogger().severe("Failed to create EULA file: " + e.getMessage());
        }
    }

    private void loadAgreedPlayers() {
        agreedPlayers = new HashSet<>();
        Path path = Paths.get(getDataFolder().toString(), "agreed_players.json");
        if (Files.exists(path)) {
            try (Reader reader = Files.newBufferedReader(path)) {
                Set<String> loadedPlayers = gson.fromJson(reader, new TypeToken<Set<String>>(){}.getType());
                if (loadedPlayers != null) {
                    agreedPlayers.addAll(loadedPlayers);
                }
            } catch (IOException e) {
                getLogger().severe("Failed to load agreed players: " + e.getMessage());
            }
        }
    }

    private void saveAgreedPlayers() {
        Path path = Paths.get(getDataFolder().toString(), "agreed_players.json");
        try (Writer writer = Files.newBufferedWriter(path)) {
            gson.toJson(agreedPlayers, writer);
        } catch (IOException e) {
            getLogger().severe("Failed to save agreed players: " + e.getMessage());
        }
    }

    private void addPlayerToAgreedList(Player player) {
        agreedPlayers.add(player.getUniqueId().toString());
        saveAgreedPlayers(); // 保存到文件
    }

    private void teleportToSpawn(Player player) {
        Location spawnLocation = player.getWorld().getSpawnLocation(); // 获取出生点
        player.teleport(spawnLocation); // 传送到出生点
    }
}
