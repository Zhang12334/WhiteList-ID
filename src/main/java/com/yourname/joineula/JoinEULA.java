package com.yourname.joineula;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class JoinEULA extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
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
            meta.addPage("欢迎来到服务器！\n\n请阅读以下协议:\n\n1. 不得使用任何作弊插件。\n2. 尊重其他玩家。\n3. 不得发布任何不当言论。\n\n是否同意？");

            book.setItemMeta(meta);
            player.openBook(book);
        }
    }
}
