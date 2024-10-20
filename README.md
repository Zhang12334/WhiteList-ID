# WhiteList-ID

一个使用 ID 进行辨别的 Minecraft Spigot 白名单插件
A Minecraft Spigot whitelist plugin that identifies players by ID

支持 MySQL/Json 存储  
Supports MySQL/JSON storage

## 命令 / Commands

- 添加白名单：/wid add <playername>  
  Add to whitelist: /wid add <playername>
  
- 移除白名单：/wid remove <playername>  
  Remove from whitelist: /wid remove <playername>
  
- 重载配置：/wid reload  
  Reload configuration: /wid reload

执行命令需要 OP 权限，或拥有权限节点：  
Requires OP permission or the following permission nodes:

- 添加白名单权限节点：wid.add  
  Permission node for adding: wid.add
  
- 移除白名单权限节点：wid.remove  
  Permission node for removing: wid.remove
  
- 重载权限节点：wid.reload  
  Permission node for reloading: wid.reload

## 兼容性测试 / Compatibility Testing

|        | 1.16 | 1.17 | 1.18 | 1.19 | 1.20 | 1.20.1 | 1.21 |
|--------|------|------|------|------|------|------|------|
| Paper  | ❓   | ❓   | ❓   | ❓   | ❓   | ✅  | ❓   |
| Purpur | ❓   | ❓   | ❓   | ❓   | ❓   | ✅   | ❓   |
| Spigot | ❓   | ❓   | ❓   | ❓   | ❓   | ✅   | ❓   |
| Bukkit | ❓   | ❓   | ❓   | ❓   | ❓   |  ❓  | ❓   |
| Mohist | ❓   | ❓   | ❓   | ❓   | ❓   | ❓   | ❓   |
| ArcLight | ❓   | ❓   | ❓   | ❓   | ❓   | ❓   | ❓   |
| Cardboard | ❓   | ❓   | ❓   | ❓   | ❓   | ✅   | ❓   |
| Banner  | ❓   | ❓   | ❓   | ❓   | ❓   | ✅   | ❓   |

欢迎辅助开发者完善兼容性测试列表，您的名字将会被列入感谢列表！

Welcome to assist developers in improving the compatibility testing list, and your name will be included in the thanks list!

## 功能列表 / Feature List

| 功能 / Feature                          | 实现状态 / Implementation Status |
|-----------------------------------------|-----------------------------------|
| 添加白名单 / Add to whitelist          | ✅                                |
| 移除白名单 / Remove from whitelist     | ✅                                |
| 重载配置 / Reload configuration         | ✅                                |
| MySQL 存储 / Support MySQL storage      | ✅                                |
| JSON 存储 / Support JSON storage        | ✅                                |
| 白名单实时重载 / Player join event      | ✅                                |
| 三方语言文件支持 / Support for third-party language files | ✅                                |
| 多语言支持 / Multi-language support     | ✅                                |
| 调试模式（用于显示语言文件内容） / Debug mode (for displaying language file contents) | ✅                                |


## 支持语言列表 / Supported Languages

| 语言 / Language   | 支持状态 / Support Status |
|--------------------|---------------------------|
| 中文（简体）/ Simplified Chinese | ✅ 支持 / Supported        |
| 中文（繁体）/ Traditional Chinese | ❌ 不支持 / Not Supported |
| 英文 / English      | ✅ 支持 / Supported        |
| 法语 / French       | ❌ 不支持 / Not Supported |
| 德语 / German       | ❌ 不支持 / Not Supported |
| 意大利语 / Italian  | ❌ 不支持 / Not Supported |
| 日语 / Japanese     | ❌ 不支持 / Not Supported |

欢迎联系开发者提交其他语言的翻译，您的名字将会被列入感谢列表！

Feel free to contact the developer to submit translations in other languages, and your name will be included in the thanks list!

## 致谢 / Acknowledgments

| 名称 / Name | 贡献 / Contribution |
|-------------|---------------------|
| Zhang1233   | 开发 / Development   |

本插件部分英文内容由 ChatGPT 辅助翻译

This plugin contains some English content translated with the assistance of ChatGPT.