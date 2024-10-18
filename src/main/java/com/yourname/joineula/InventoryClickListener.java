package com.yourname.joineula;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class InventoryClickListener implements Listener {
    private final Main plugin;

    public InventoryClickListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getCurrentItem() != null && event.getCurrentItem().getType() == Material.WRITTEN_BOOK) {
            Player player = (Player) event.getWhoClicked();
            event.setCancelled(true); // 取消点击事件

            // 检查是否点击了书
            if (event.getSlot() == 0) { // 假设协议在第一个槽
                // 同意协议
                player.sendMessage("您已同意游玩协议！");
                player.addAttachment(plugin, "joineula.agree", true); // 授予权限
                player.closeInventory();
            } else {
                // 拒绝协议
                player.kickPlayer("您未同意游玩协议！");
            }
        }
    }
}
