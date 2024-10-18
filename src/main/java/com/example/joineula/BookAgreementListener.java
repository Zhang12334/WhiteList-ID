package main.java.com.example.joineula;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.entity.Player;

public class BookAgreementListener implements Listener {

    @EventHandler
    public void onPlayerSignBook(PlayerEditBookEvent event) {
        Player player = event.getPlayer();

        if (event.isSigning()) {
            // 玩家签署了协议
            player.sendMessage("感谢您同意协议，祝您游戏愉快！");
        } else {
            // 玩家未签署协议，踢出服务器
            player.kickPlayer("您必须同意协议才能继续游玩本服务器。");
        }
    }
}
