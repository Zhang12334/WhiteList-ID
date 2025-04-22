package com.wid;

import org.bukkit.configuration.file.FileConfiguration;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class LanguageManager {
    private Map<String, String> messages = new HashMap<>();
    private FileConfiguration config;

    public LanguageManager(FileConfiguration config) {
        this.config = config;
        loadLanguage();
    }

    public void loadLanguage() {
        // 有效的语言列表
        Set<String> validLanguages = new HashSet<>(Arrays.asList("zh_hans", "zh_hant", "en_us", "ja_jp"));

        // 读取配置中的语言设置，默认为zh_hans
        String language = config.getString("language", "zh_hans");

        // 如果读取的语言不在有效列表中，则设为默认值
        if (!validLanguages.contains(language.toLowerCase())) {
            language = "zh_hans";
        }
        messages.clear();

        if ("zh_hans".equalsIgnoreCase(language)) {
            // 中文消息
            messages.put("TranslationContributors", "当前语言: 简体中文 (贡献者: Zhang1233)");
            messages.put("CanNotFoundMeowLibs", "未找到 MeowLibs, 请安装前置依赖 MeowLibs!");
            messages.put("startup", "MeowUID 已加载!");
            messages.put("shutdown", "MeowUID 已卸载!");
            messages.put("nowusingversion", "当前使用版本:");
            messages.put("checkingupdate", "正在检查更新...");
            messages.put("checkfailed", "检查更新失败，请检查你的网络状况!");
            messages.put("updateavailable", "发现新版本:");
            messages.put("updatemessage", "更新内容如下:");
            messages.put("updateurl", "新版本下载地址:");
            messages.put("oldversionmaycauseproblem", "旧版本可能会导致问题，请尽快更新!");
            messages.put("nowusinglatestversion", "您正在使用最新版本!");
            messages.put("reloaded", "配置文件已重载!");
            messages.put("nopermission", "你没有权限执行此命令!");
            messages.put("usage", "用法:");
            messages.put("notWhitelisted", "您不在本服白名单中，无法进入");
            messages.put("unknownOption", "未知操作:");
            messages.put("whitelistNull", "白名单为空");
            messages.put("whitelistLists", "当前位于白名单列表中的玩家:");
            messages.put("unknownStorageType", "未知的存储类型:");
            messages.put("storageConvertSuccess", "白名单转换成功:");
            messages.put("whitelistReloaded", "白名单已重载!");
            messages.put("playerAlreadyExist", "玩家 %s 已经在白名单中!");
            messages.put("playerAdded", "玩家 %s 已添加到白名单!");
            messages.put("playerNotExist", "玩家 %s 不在白名单中!");
            messages.put("playerRemoved", "玩家 %s 已从白名单中移除!");
            messages.put("loadedFromJson", "已从 JSON 文件加载白名单!");
            messages.put("savedToJson", "已将白名单保存到 JSON 文件!");
            messages.put("loadedFromMySQL", "已从 MySQL 数据库加载白名单!");
            messages.put("savedToMySQL", "已将白名单保存到 MySQL 数据库!");
        } else if ("zh_hant".equalsIgnoreCase(language)) {
            // 繁体中文消息
            messages.put("TranslationContributors", "當前语言: 繁體中文 (貢獻者: Zhang1233 & TongYi-Lingma LLM)");
            messages.put("CanNotFoundMeowLibs", "未找到 MeowLibs, 請安裝前置依賴 MeowLibs!");
            messages.put("startup", "MeowUID 已加载!");
            messages.put("shutdown", "MeowUID 已卸载!");
            messages.put("nowusingversion", "目前使用版本:");
            messages.put("checkingupdate", "正在檢查更新...");
            messages.put("checkfailed", "檢查更新失敗，請檢查你的網絡狀態!");
            messages.put("updateavailable", "發現新版本:");
            messages.put("updatemessage", "更新內容如下:");
            messages.put("updateurl", "新版本下載網址:");
            messages.put("oldversionmaycauseproblem", "舊版本可能會導致問題，請盡快更新!");
            messages.put("nowusinglatestversion", "您正在使用最新版本!");
            messages.put("reloaded", "配置文件已重载!");
            messages.put("nopermission", "你没有权限执行此命令!");
            messages.put("usage", "用法:");
            messages.put("notWhitelisted", "您不在本服白名單中，無法進入");
            messages.put("unknownOption", "未知操作:");
            messages.put("whitelistNull", "白名單為空");
            messages.put("whitelistLists", "當前位于白名單列表中的玩家:");
            messages.put("unknownStorageType", "未知的存储类型:");
            messages.put("storageConvertSuccess", "白名單轉換成功:");
            messages.put("whitelistReloaded", "白名單已重載!");
            messages.put("playerAlreadyExist", "玩家 %s 已经在白名單中!");
            messages.put("playerAdded", "玩家 %s 已添加到白名單!");
            messages.put("playerNotExist", "玩家 %s 不在白名單中!");
            messages.put("playerRemoved", "玩家 %s 已從白名單中移除!");
            messages.put("loadedFromJson", "已從 JSON 文件加载白名單!");
            messages.put("savedToJson", "已将白名單保存到 JSON 文件!");
            messages.put("loadedFromMySQL", "已從 MySQL 數據庫加載白名單!");
            messages.put("savedToMySQL", "已将白名單保存到 MySQL 數據庫!");
        } else if ("en_us".equalsIgnoreCase(language)) {
            // English messages
            messages.put("TranslationContributors", "Current language: English (Contributors: Zhang1233)");
            messages.put("CanNotFoundMeowLibs", "MeowLibs not found, please install the preceding dependency MeowLibs!");
            messages.put("startup", "MeowUID has been loaded!");
            messages.put("shutdown", "MeowUID has been unloaded!");
            messages.put("nowusingversion", "Currently using version:");
            messages.put("checkingupdate", "Checking for updates...");
            messages.put("checkfailed", "Update check failed, please check your network status!");
            messages.put("updateavailable", "New version available:");
            messages.put("updatemessage", "Update content:");
            messages.put("updateurl", "Download URL for new version:");
            messages.put("oldversionmaycauseproblem", "Old versions may cause problems, please update as soon as possible!");
            messages.put("nowusinglatestversion", "You are currently using the latest version!");
            messages.put("reloaded", "Config file has been reloaded!");
            messages.put("nopermission", "You do not have permission to execute this command!");
            messages.put("usage", "Usage: ");
            messages.put("notWhitelisted", "You are not on the server's whitelist. Access denied.");
            messages.put("unknownOption", "Unknown option: ");
            messages.put("whitelistNull", "The whitelist is currently empty.");
            messages.put("whitelistLists", "Players currently on the whitelist:");
            messages.put("unknownStorageType", "Unknown storage type: ");
            messages.put("storageConvertSuccess", "Whitelist converted successfully: ");
            messages.put("whitelistReloaded", "Whitelist has been reloaded!");
            messages.put("playerAlreadyExist", "Player %s is already on the whitelist!");
            messages.put("playerAdded", "Player %s has been added to the whitelist!");
            messages.put("playerNotExist", "Player %s is not on the whitelist!");
            messages.put("playerRemoved", "Player %s has been removed from the whitelist!");
            messages.put("loadedFromJson", "Whitelist loaded from JSON file!");
            messages.put("savedToJson", "Whitelist saved to JSON file!");
            messages.put("loadedFromMySQL", "Whitelist loaded from MySQL database!");
            messages.put("savedToMySQL", "Whitelist saved to MySQL database!");            
        } else if ("ja_jp".equalsIgnoreCase(language)) {
            // 日本語メッセージ
            messages.put("TranslationContributors", "現在の言語: 日本語 (貢献者: Zhang1233 & TongYi-Lingma LLM)");
            messages.put("CanNotFoundMeowLibs", "MeowLibsが見つかりません。プレフィックス依存をインストールしてください!");
            messages.put("startup", "MeowUIDが読み込まれました!");
            messages.put("shutdown", "MeowUIDがアンロードされました!");
            messages.put("nowusingversion", "現在使用中のバージョン:");
            messages.put("checkingupdate", "アップデートをチェック中...");
            messages.put("checkfailed", "アップデートチェックに失敗しました。ネットワークの状態を確認してください!");
            messages.put("updateavailable", "新しいバージョンが利用可能です:");
            messages.put("updatemessage", "アップデート内容:");
            messages.put("updateurl", "新しいバージョンのダウンロードURL:");
            messages.put("oldversionmaycauseproblem", "古いバージョンは問題を引き起こす可能性があります。できるだけ早くアップデートしてください!");
            messages.put("nowusinglatestversion", "現在最新バージョンを使用しています!");
            messages.put("reloaded", "設定ファイルがリロードされました!");
            messages.put("nopermission", "このコマンドの実行に権限がありません!");
            messages.put("usage", "使用法:");
            messages.put("notWhitelisted", "サーバーのホワイトリストに加入していません。アクセスが拒否されました。");
            messages.put("unknownOption", "不明なオプション:");
            messages.put("whitelistNull", "現在ホワイトリストには誰もいません。");
            messages.put("whitelistLists", "現在ホワイトリストに加入しているプレイヤー:");
            messages.put("unknownStorageType", "不明なストレージタイプ:");
            messages.put("storageConvertSuccess", "ホワイトリストが正常に変換されました:");
            messages.put("whitelistReloaded", "ホワイトリストがリロードされました!");
            messages.put("playerAlreadyExist", "プレイヤー%sはすでにホワイトリストに加入しています!");
            messages.put("playerAdded", "プレイヤー%sがホワイトリストに加入しました!");
            messages.put("playerNotExist", "プレイヤー%sはホワイトリストに加入していません!");
            messages.put("playerRemoved", "プレイヤー%sがホワイトリストから削除されました!");
            messages.put("loadedFromJson", "JSONファイルからホワイトリストが読み込まれました!");
            messages.put("savedToJson", "JSONファイルにホワイトリストが保存されました!");
            messages.put("loadedFromMySQL", "MySQLデータベースからホワイトリストが読み込まれました!");
            messages.put("savedToMySQL", "MySQLデータベースにホワイトリストが保存されました!");
        }
    }

    /**
     * 获取语言消息
     * @param key 消息键名
     * @return 对应的语言消息，如果不存在则返回键名
     */
    public String getMessage(String key) {
        return messages.getOrDefault(key, key);
    }
}
