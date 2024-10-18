package com.example.joineula;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

public class AgreementBook {

    public static void showAgreementBook(Player player) {
        ItemStack book = new ItemStack(Material.WRITABLE_BOOK);  // 创建一本书
        BookMeta meta = (BookMeta) book.getItemMeta();

        meta.setTitle("服务器游玩协议");
        meta.setAuthor("服务器管理员");
        meta.addPage(
            "欢迎来到服务器！\n\n" +
            "请仔细阅读以下协议条款，并选择是否同意。\n\n" +
            "1. 不得破坏其他玩家建筑。\n" +
            "2. 不得作弊或使用外挂。\n\n" +
            "签署此书即表示同意这些条款。"
        );

        book.setItemMeta(meta);

        // 显示给玩家
        player.openBook(book);
    }
}
