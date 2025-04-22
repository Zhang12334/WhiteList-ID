package com.wid;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Set;

public class CommandHandler implements CommandExecutor {

    private final JavaPlugin plugin;
    private final StorageManager storageManager;
    private final LanguageManager languageManager;
    private final Set<String> whiteList;

    public CommandHandler(JavaPlugin plugin, StorageManager storageManager, LanguageManager languageManager, Set<String> whiteList) {
        this.plugin = plugin;
        this.storageManager = storageManager;
        this.languageManager = languageManager;
        this.whiteList = whiteList;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            return handleReloadCommand(sender);
        } else if (args.length == 1 && args[0].equalsIgnoreCase("convert")) {
            return convertwhitelist(sender);
        } else if (args.length == 1 && args[0].equalsIgnoreCase("list")) {
            return query_list(sender);
        }

        if (args.length != 2) {
            sender.sendMessage(ChatColor.RED + String.format(languageManager.getMessage("usage") + " /wid <add|remove> <playername> or /wid reload or /wid convert"));
            return true;
        }

        String action = args[0];
        String playerName = args[1];

        if (action.equalsIgnoreCase("add")) {
            return handleAddCommand(sender, playerName);
        } else if (action.equalsIgnoreCase("remove")) {
            return handleRemoveCommand(sender, playerName);
        } else {
            sender.sendMessage(ChatColor.RED + String.format(languageManager.getMessage("unknownOption") + " %s", action));
            return false;
        }
    }

    private boolean query_list(CommandSender sender) {
        if (!sender.hasPermission("wid.list")) {
            sender.sendMessage(ChatColor.RED + languageManager.getMessage("nopermission"));
            return false;
        }

        storageManager.loadWhiteList();

        if (whiteList.isEmpty()) {
            sender.sendMessage(ChatColor.YELLOW + languageManager.getMessage("whitelistNull"));
            return true;
        }

        String whiteListString = String.join(", ", whiteList);

        if (sender instanceof Player) {
            sender.sendMessage(ChatColor.GREEN + languageManager.getMessage("whitelistLists") + " " + whiteListString);
        } else {
            plugin.getLogger().info(languageManager.getMessage("whitelistLists") + " " + whiteListString);
        }

        return true;
    }

    private boolean convertwhitelist(CommandSender sender) {
        if (!sender.hasPermission("wid.convert")) {
            sender.sendMessage(ChatColor.RED + languageManager.getMessage("nopermission"));
            return false;
        }
        if (storageManager.getStorageType().equalsIgnoreCase("json")) {
            // 如果当前是 Json ，则从 MySQL 中读取然后存到 Json 中
            storageManager.loadFromMySQL();
            storageManager.saveToJSON();
            sender.sendMessage(ChatColor.GREEN + languageManager.getMessage("storageConvertSuccess") + " MySQL --> Json");
        } else if (storageManager.getStorageType().equalsIgnoreCase("mysql")) {
            // 反之，Json 读然后存到 MySQL 中
            storageManager.loadFromJSON();
            storageManager.saveToMySQL();
            sender.sendMessage(ChatColor.GREEN + languageManager.getMessage("storageConvertSuccess") + " Json --> MySQL");
        } else {
            sender.sendMessage(ChatColor.RED + languageManager.getMessage("unknownStorageType") + storageManager.getStorageType());
            return false;
        }
        return true;
    }

    private boolean handleReloadCommand(CommandSender sender) {
        if (!sender.hasPermission("wid.reload")) {
            sender.sendMessage(ChatColor.RED + languageManager.getMessage("nopermission"));
            return false;
        }

        plugin.reloadConfig();

        plugin.getLogger().info(languageManager.getMessage("reloaded"));

        whiteList.clear();
        storageManager.loadWhiteList();
        plugin.getLogger().info(languageManager.getMessage("whitelistReloaded"));
        sender.sendMessage(ChatColor.GREEN + languageManager.getMessage("reloaded"));
        return true;
    }

    private boolean handleAddCommand(CommandSender sender, String playerName) {
        if (!sender.hasPermission("wid.add")) {
            sender.sendMessage(ChatColor.RED + languageManager.getMessage("nopermission"));
            return false;
        }

        storageManager.loadWhiteList();

        if (whiteList.contains(playerName)) {// 已经存在
            sender.sendMessage(ChatColor.YELLOW + String.format(languageManager.getMessage("playerAlreadyExist"), playerName));
        } else {
            whiteList.add(playerName);
            sender.sendMessage(ChatColor.GREEN + String.format(languageManager.getMessage("playerAdded"), playerName));
            storageManager.saveWhiteList(); // 添加后保存
        }

        return true;
    }

    private boolean handleRemoveCommand(CommandSender sender, String playerName) {
        if (!sender.hasPermission("wid.remove")) {
            sender.sendMessage(ChatColor.RED + languageManager.getMessage("nopermission"));
            return false;
        }

        storageManager.loadWhiteList();

        if (whiteList.contains(playerName)) {
            whiteList.remove(playerName);  // 从白名单中移除
            sender.sendMessage(ChatColor.GREEN + String.format(languageManager.getMessage("playerRemoved"), playerName));
            storageManager.saveWhiteList(); // 移除后保存
        } else {
            sender.sendMessage(ChatColor.YELLOW + String.format(languageManager.getMessage("playerNotExist"), playerName));
        }

        return true;
    }
}
