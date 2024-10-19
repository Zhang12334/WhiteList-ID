# Join EULA

进服EULA，强制要求玩家同意EULA后才可进服

支持群组服，支持MySQL/Json存储

两种服务模式

服务类型为verify时负责对玩家进行是否同意的验证，同意则写入mysql中

为request时仅发送请求查询玩家列表，若不在其中则踢出

可配置同意EULA前允许的移动范围

支持移除EULA同意，op权限

/joineula remove <playername>