package com.meow.wid;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.Set;
import java.util.ArrayList;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import net.fabricmc.api.ModInitializer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.command.CommandSource;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FWhiteListID implements ModInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger(FWhiteListID.class);
    private Set<String> whiteList;
    private String storageType;
    private String debugmode;

    // 一堆存储消息的变量
    private String startupMessage;
    private String storageTypeMessage;
    private String disableMessage;
    private String notWhitelistedMessage;
    private String useExampleMessage;
    private String unknownOptionMessage;
    private String noPermissionMessage;
    private String playerMessage;
    private String playerAlreadyExistMessage;
    private String playerAddedMessage;
    private String playerRemovedFromWhitelistMessage;
    private String playerNotInWhitelistMessage;
    private String loadedJsonMessage;
    private String savedJsonMessage;
    private String loadedMysqlMessage;
    private String savedMysqlMessage;
    private String nowLanguageMessage;
    private String translatorMessage;
    private String reloadMessage;
    private String reloadLanguage;
    private String reloadWhitelist;

    @Override
    public void onInitialize() {
        whiteList = new HashSet<>();
        loadConfig();

        // 创建 lang 文件夹
        File langFolder = new File(getDataFolder(), "lang");
        if (!langFolder.exists()) {
            langFolder.mkdirs();
        }

        // 检查语言文件
        String language = "zh_cn"; // 直接从配置中读取
        File languageFile = new File(langFolder, language + ".json");

        // 如果指定的语言文件不存在，则尝试从 JAR 中复制
        if (!languageFile.exists()) {
            copyLanguageFile(languageFile, language);
        }

        loadLanguageFile(language);

        // 读取存储类型
        if (storageType.equalsIgnoreCase("json")) {
            loadFromJSON();
        } else if (storageType.equalsIgnoreCase("mysql")) {
            loadFromMySQL();
        }

        LOGGER.info(startupMessage);
        LOGGER.info(storageTypeMessage + " " + storageType);
    }

    private void loadConfig() {
        // 读取配置文件，设置 storageType 和 debugmode
        storageType = "json"; // 示例赋值
        debugmode = "enable"; // 示例赋值
    }

    private File getDataFolder() {
        // 返回插件数据存储的文件夹
        return new File(FabricLoader.getInstance().getConfigDirectory(), "WhiteList-ID");
    }

    private void copyLanguageFile(File languageFile, String language) {
        InputStream langInput = getClass().getResourceAsStream("/lang/" + language + ".json");
        if (langInput != null) {
            try {
                Files.copy(langInput, languageFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                LOGGER.info("Language file " + language + ".json copied");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            langInput = getClass().getResourceAsStream("/lang/zh_cn.json");
            if (langInput != null) {
                try {
                    Files.copy(langInput, languageFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    LOGGER.info("Default language file zh_cn.json copied");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void loadLanguageFile(String language) {
        try (InputStream inputStream = new FileInputStream(new File(getDataFolder(), "lang/" + language + ".json"))) {
            JSONParser parser = new JSONParser();
            JSONObject jsonObject = (JSONObject) parser.parse(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            
            // 直接存储消息内容
            JSONObject messagesObject = (JSONObject) jsonObject.get("messages");
            startupMessage = (String) messagesObject.get("startup");
            storageTypeMessage = (String) messagesObject.get("storagetype");
            disableMessage = (String) messagesObject.get("disable");
            notWhitelistedMessage = (String) messagesObject.get("not_whitelisted");
            useExampleMessage = (String) messagesObject.get("use_example");
            unknownOptionMessage = (String) messagesObject.get("unknown_option");
            noPermissionMessage = (String) messagesObject.get("no_permission");
            playerMessage = (String) messagesObject.get("player");
            playerAlreadyExistMessage = (String) messagesObject.get("player_already_exist");
            playerAddedMessage = (String) messagesObject.get("player_added");
            playerRemovedFromWhitelistMessage = (String) messagesObject.get("player_removed_from_whitelist");
            playerNotInWhitelistMessage = (String) messagesObject.get("player_not_in_whitelist");
            loadedJsonMessage = (String) messagesObject.get("loaded_json");
            savedJsonMessage = (String) messagesObject.get("saved_json");
            loadedMysqlMessage = (String) messagesObject.get("loaded_mysql");
            savedMysqlMessage = (String) messagesObject.get("saved_mysql");
            nowLanguageMessage = (String) messagesObject.get("now_language");
            translatorMessage = (String) messagesObject.get("translator");
            reloadMessage = (String) messagesObject.get("reload");
            reloadLanguage = (String) messagesObject.get("reload_language");
            reloadWhitelist = (String) messagesObject.get("reload_whitelist");                        
            
            // 当前使用语言
            LOGGER.info(nowLanguageMessage);
            // 翻译贡献者
            LOGGER.info(translatorMessage);
            // 调试模式
            if (debugmode.equals("enable")) {
                // debug！
                LOGGER.info("———————Debug———————");
                LOGGER.info(nowLanguageMessage);
                LOGGER.info("startup: " + startupMessage);
                LOGGER.info("storagetype: " + storageTypeMessage);
                LOGGER.info("disable: " + disableMessage);
                LOGGER.info("not_whitelisted: " + notWhitelistedMessage);
                LOGGER.info("use_example: " + useExampleMessage);
                LOGGER.info("unknown_option: " + unknownOptionMessage);
                LOGGER.info("no_permission: " + noPermissionMessage);
                LOGGER.info("player: " + playerMessage);
                LOGGER.info("player_already_exist: " + playerAlreadyExistMessage);
                LOGGER.info("player_added: " + playerAddedMessage);
                LOGGER.info("player_removed_from_whitelist: " + playerRemovedFromWhitelistMessage);
                LOGGER.info("player_not_in_whitelist: " + playerNotInWhitelistMessage);
                LOGGER.info("loaded_json: " + loadedJsonMessage);
                LOGGER.info("saved_json: " + savedJsonMessage);
                LOGGER.info("loaded_mysql: " + loadedMysqlMessage);
                LOGGER.info("saved_mysql: " + savedMysqlMessage);
                LOGGER.info("now_language: " + nowLanguageMessage);
                LOGGER.info("translator: " + translatorMessage);
                LOGGER.info("reload: " + reloadMessage);
                LOGGER.info("reload_language: " + reloadLanguage);
                LOGGER.info("reload_whitelist: " + reloadWhitelist);                                 
                LOGGER.info("———————Debug———————");
            }
        } catch (IOException | ParseException e) {
            LOGGER.warning("Can not found language file, using zh_cn.json as default");
            loadLanguageFile("zh_cn");
        }
    }

    private void onPlayerJoin(ServerPlayerEntity player) {
        String playerName = player.getName().getString();
        if (!whiteList.contains(playerName)) {
            player.networkHandler.disconnect(Text.of(notWhitelistedMessage));
        }
    }

    private int handleAddCommand(CommandSource source, String playerName) {
        if (!source.hasPermissionLevel(2)) {
            source.sendFeedback(Text.of(noPermissionMessage), false);
            return 0;
        }

        if (whiteList.contains(playerName)) {
            source.sendFeedback(Text.of(playerMessage + " " + playerName + " " + playerAlreadyExistMessage), false);
        } else {
            whiteList.add(playerName);
            saveWhiteList();
            source.sendFeedback(Text.of(playerMessage + " " + playerName + " " + playerAddedMessage), false);
        }
        return 1;
    }

    private int handleRemoveCommand(CommandSource source, String playerName) {
        if (!source.hasPermissionLevel(2)) {
            source.sendFeedback(Text.of(noPermissionMessage), false);
            return 0;
        }

        if (whiteList.contains(playerName)) {
            whiteList.remove(playerName);
            saveWhiteList();
            source.sendFeedback(Text.of(playerMessage + " " + playerName + " " + playerRemovedFromWhitelistMessage), false);
        } else {
            source.sendFeedback(Text.of(playerMessage + " " + playerName + " " + playerNotInWhitelistMessage), false);
        }
        return 1;
    }

    private int handleReloadCommand(CommandSource source) {
        loadLanguageFile("zh_cn");
        loadFromJSON();
        source.sendFeedback(Text.of(reloadWhitelist), false);
        return 1;
    }

    private void saveWhiteList() {
        if (storageType.equalsIgnoreCase("json")) {
            saveToJSON();
        } else {
            saveToMySQL();
        }
    }


    private void loadFromJSON() {
        File file = new File(getDataFolder(), "whitelist.json");
        if (!file.exists()) {
            return;
        }

        try {
            JSONParser parser = new JSONParser();
            JSONArray jsonArray = (JSONArray) parser.parse(new FileReader(file));

            for (Object obj : jsonArray) {
                whiteList.add((String) obj);
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

    private void saveToJSON() {
        File file = new File(getDataFolder(), "whitelist.json");

        try (FileWriter writer = new FileWriter(file)) {
            JSONArray jsonArray = new JSONArray();
            jsonArray.addAll(new ArrayList<>(whiteList));
            writer.write(jsonArray.toJSONString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 加载 MySQL 存储
    private void loadFromMySQL() {
        String url = "jdbc:mysql://" + getConfig().getString("mysql.host") + ":" + getConfig().getInt("mysql.port") + "/" + getConfig().getString("mysql.database");
        String user = getConfig().getString("mysql.username");
        String password = getConfig().getString("mysql.password");

        try (Connection conn = DriverManager.getConnection(url, user, password);
            Statement stmt = conn.createStatement()) {

            String sql = "CREATE TABLE IF NOT EXISTS whitelist (id INT AUTO_INCREMENT, player_name VARCHAR(255), PRIMARY KEY (id))";
            stmt.executeUpdate(sql);

            ResultSet rs = stmt.executeQuery("SELECT player_name FROM whitelist");
            while (rs.next()) {
                whiteList.add(rs.getString("player_name"));
            }
            getLogger().info(loadedMysqlMessage + ": " + whiteList.size() + " players loaded from MySQL.");

        } catch (SQLException e) {
            getLogger().error("Failed to load whitelist from MySQL", e);
        }
    }

    // 保存至 MySQL
    private void saveToMySQL() {
        String url = "jdbc:mysql://" + getConfig().getString("mysql.host") + ":" + getConfig().getInt("mysql.port") + "/" + getConfig().getString("mysql.database");
        String user = getConfig().getString("mysql.username");
        String password = getConfig().getString("mysql.password");

        try (Connection conn = DriverManager.getConnection(url, user, password);
            PreparedStatement pstmt = conn.prepareStatement("INSERT INTO whitelist (player_name) VALUES (?)")) {

            // 清空表内容
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("TRUNCATE TABLE whitelist");
            }

            // 插入白名单数据
            for (String playerName : whiteList) {
                pstmt.setString(1, playerName);
                pstmt.executeUpdate();
            }
            getLogger().info(savedMysqlMessage);

        } catch (SQLException e) {
            getLogger().error("Failed to save whitelist to MySQL", e);
        }
    }
}
