package main.java.com.example.joineula;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.entity.Player;

public class PlayerJoinListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // 检查玩家是否首次加入
        if (!player.hasPlayedBefore()) {
            // 显示协议书
            AgreementBook.showAgreementBook(player);
        }
    }
}
