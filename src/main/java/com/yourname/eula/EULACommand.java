package com.yourname.eula;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;

public class EULACommand implements CommandExecutor {
    private final JavaPlugin plugin;
    private final Set<String> agreedPlayers = new HashSet<>();

    public EULACommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            if (agreedPlayers.contains(player.getName())) {
                player.sendMessage("§aYou have already agreed to the EULA.");
                return true;
            }

            agreedPlayers.add(player.getName());
            player.sendMessage("§aYou have agreed to the server EULA.");
            Bukkit.getLogger().info(player.getName() + " has agreed to the EULA.");
        } else {
            sender.sendMessage("This command can only be used by players.");
        }
        return true;
    }
}
