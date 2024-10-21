# WhiteList-ID

一个使用 ID 进行辨别的 Minecraft Spigot 白名单插件

A Minecraft Spigot whitelist plugin that identifies players by ID

支持 MySQL/Json 存储

Supports MySQL/JSON storage

搭配群组服食用效果极佳!

Works exceptionally well when used with a server network!
## 下载 / Download
本插件为开源项目并免费提供下载使用！

您可以自行编译开发中的源代码或下载 Release 版本使用，出现问题可以提出 Issue！

同时您也可以在爱发电平台上赞助我，并通过加入QQ交流群以获得及时、迅速的技术支持与安装指导！赞助链接：https://afdian.com/a/NachoNeko_/

This plugin is an open-source project and is available for free download and use!

You can compile the source code being developed yourself or download the Release version. If you encounter any issues, feel free to raise an Issue!

You can also support me on the Afdian platform and join the QQ group for timely and efficient technical support and installation guidance. Link (Simplified Chinese only): https://afdian.com/a/NachoNeko_/

## 赞助价格表 / Pricing Table

- ¥25 元：获取插件技术支持 
  ¥25 CNY: Get technical support for the plugin.

- ¥200 元：获取插件技术支持 + 一次定制功能的机会
  ¥200 CNY: Get technical support + one opportunity for a custom feature.

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

| 语言 / Language   | 文件名称 / File name |支持状态 / Support Status |
|--------------------|---------------------------|---------------------------|
| 简体中文 / Simplified Chinese | zh_cn | ✅ 支持 / Supported        |
| 喵喵中文 / Simplified Chinese | zh_meow | ✅ 支持 / Supported        |
| 繁体中文 / Traditional Chinese | zh_tc | ❌ 不支持 / Not Supported |
| 英文 / English      | en | ✅ 支持 / Supported        |
| 法语 / French       | fr | ❌ 不支持 / Not Supported |
| 德语 / German       | de | ❌ 不支持 / Not Supported |
| 意大利语 / Italian  | it | ❌ 不支持 / Not Supported |
| 日语 / Japanese     | ja_jp | ❌ 不支持 / Not Supported |

欢迎联系开发者提交其他语言的翻译，您的名字将会被列入感谢列表！

Feel free to contact the developer to submit translations in other languages, and your name will be included in the thanks list!

## 致谢 / Acknowledgments

| 名称 / Name | 贡献 / Contribution |
|-------------|---------------------|
| Zhang1233   | 开发 / Development   |

本插件部分英文内容由 ChatGPT 辅助翻译

This plugin contains some English content translated with the assistance of ChatGPT.

## 许可 / License

本插件使用 GNU GPL 许可证

This plugin is licensed under the GNU GPL license.