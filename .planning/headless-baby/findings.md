# 发现

- 基线已有用户暂存的Blockbench模型和索引；实施前记录其索引及文件哈希，不改动这些资产。
- 当前最后ItemId为SHARD_OF_GLASS；新道具追加其后。
- 无头宝宝无需继承AbstractBabyFamiliarEntity的射击/瞄准同步；复用AbstractChainFamiliarEntity。
- BloodTrail原来忽略AMPLIFIER与TARGET_POSITION；贫血和玻璃碎片的周期上下文会携带模块份数。扩展接口时必须在这两个既有产血调用处显式归一倍率1，防止多份意外增伤。
- BFFS使用现有getEnhancement和getVisualScale；跟班血迹快照不随之后的增强变更。
- 模型8方块、6组、64×64 RGBA贴图、无动画；运行时保留波比弟弟0.6基础缩放及14.5/16垂直变换。
- 英文Wiki原版身份/品质/池已在规划核实；中文Wiki403。原版BFFS不增血迹伤害，本项目明确采用增伤。
- 道具图标来源：https://bindingofisaacrebirth.wiki.gg/images/Collectible_Headless_Baby_icon.png；PNG/32×32，SHA256为1b22e48b973d9bf8608533d2ccd83da455f5b8763bce67c8d0e32926804dda2d。
- 沙箱curl下载因系统凭据失败，Python请求403；经正常审批的curl下载成功，无替代素材。用户Gradle缓存只读，使用已安装Gradle8.8及工作区可写缓存离线验证。
- 共享跟班名册与需求计数分离：取消需求后由既有维护流程移除实体；验证立即失效行为时必须实际移除名册登记。玻璃碎片激活效果依赖真实hurt事件，定向周期回归直接使用其activate入口。
