package com.yourname.joineula;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class JoinEULA extends JavaPlugin implements Listener {

    private String eulaContent;

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        loadEULAContent();
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!player.hasPlayedBefore()) {
            openAgreementUI(player);
        }
    }

    private void openAgreementUI(Player player) {
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();
        if (meta != null) {
            meta.setTitle(ChatColor.GOLD + "Server EULA");
            meta.setAuthor("Server Admin");
            meta.addPage(eulaContent);

            book.setItemMeta(meta);
            player.openBook(book); // 模拟打开书
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
            // 创建 EULA 文件并写入默认内容
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
}
