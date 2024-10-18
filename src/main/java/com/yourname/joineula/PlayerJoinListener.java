package com.yourname.joineula;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.WrittenBookMeta;

public class PlayerJoinListener implements Listener {
    private final Main plugin;

    public PlayerJoinListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // 检查玩家是否已经同意过协议
        if (!player.hasPermission("joineula.agree")) {
            // 发送书本界面
            openAgreementBook(player);
        }
    }

    private void openAgreementBook(Player player) {
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        WrittenBookMeta meta = (WrittenBookMeta) book.getItemMeta();
        
        if (meta != null) {
            meta.setTitle("游玩协议");
            meta.setAuthor("服务器管理团队");
            meta.addPage("请阅读以下游玩协议：\n\n1. 您同意遵守所有服务器规则。\n2. 不允许使用任何作弊或不当手段。\n\n请确认您是否同意上述协议。");
            book.setItemMeta(meta);
            
            player.getInventory().addItem(book);
            player.sendMessage("请查看您的背包中的书以同意游玩协议。");
        }
    }
}
