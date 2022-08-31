# EasyGrasscutters（蓝图游戏设计插件）

![image-20220831193232720](https://raw.githubusercontent.com/liujiaqi7998/GraphicBed/main/img/202208311932848.png)

你想有一个完善的剧情的体验吗？你想自己创建剧情吗？你想在游戏内实现小游戏吗？

EasyGrasscutters满足你！

通过使用Node-Red软件，创作你自己的蓝图！

这个插件拥有超过19（未来更新还会添加）个节点包括：9个执行功能节点，5个事件监听器和其他各种功能节点。

可以实现：添加剧情、完成剧情、添加实体、传送等功能。

也可以实现监听：剧情状态、玩家是否在指定位置、玩家是否杀死怪物等状态。

你可以使用工具箱里面的节点创作，实现你的剧情线和小游戏。

你甚至可以添加其他模块，与我的世界，智能家居，等其他领域联动！

详细说明请看[node-red-easy-grasscutters](https://github.com/liujiaqi7998/node-red-easy-grasscutters)文档

**说明**：

本插件为我一人耗时打造，可能在某些说明或功能上处理的不尽人意，你可以[提交Issues](https://github.com/liujiaqi7998/node-red-easy-grasscutters/issues)
来取得我的帮助（如果我看到的话一定会帮你），当然，如果你喜欢这个项目还请给我一个Star

我平时维护时间较少，还请看到项目有能力的大佬帮助维护一下，我会及时合并请求

I'd really like to internationalize this project, but it's thought it's a lot of work and needs a good translation, I'd
appreciate it if you could help translate

## 版本说明

### v1.0.0

* 实现基本的节点，可以实现命令调试。（适配 Grasscutters 1.2.3）

### 使用方法

1. 将本插件放在plugin文件夹

2. 启动服务器，看到 服务器地址:wss://xxxx

3. 记下地址配置node-red，请参考[node-red-easy-grasscutters](https://github.com/liujiaqi7998/node-red-easy-grasscutters)文档

## 注意事项

本插件遵循 [GPL-3.0 license](https://github.com/liujiaqi7998/EasyGrasscutters/blob/master/LICENSE) 协议，禁止对原插件商用，二次修改需要添加说明并开源！
