# 发现与决定

- 当前分支 feature/feature-c，实施前工作区干净；用户明确跳过同步、不提交。
- ActiveItem每格100耐久，默认每4 tick恢复4点，2格约10秒；沿用5 tick使用冷却。
- 使用书籍主动框架与类级CompositeTrigger/EMTPY；独立IAbilityEffect不依赖原始使用事件。
- IsaacFlightController.stopThrust处理推进/姿势/重力，需同时暂停enabled并发送已有状态，避免按住推进键覆盖跃起。
- 原版实体速度包可同步替换速度，无需新协议。
- 语言实际位于assets/isaac_disaster/lang/en_us.json，内容含中文；不存在zh_cn.json。
- 规划时Wiki页面读取遭遇SSL连接失败/超时，本轮web工具未返回可用正文；不宣称正文核验成功，道具池为用户确认的项目规则。
- 英文Wiki素材下载成功，来源为static.wikia.nocookie.net/bindingofisaacre_gamepedia/images/d/d1/Collectible_How_to_Jump_icon.png/revision/latest?format=original；Pillow确认PNG、32×32，已目视确认书籍图标。
- curl默认沙箱出现SEC_E_NO_CREDENTIALS，正常权限流程后使用--ssl-no-revoke成功；没有关闭服务器证书校验。
- 用户最新确认取消首次落地免伤：删除LANDINGS、LandingProtection及全部事件监听，普通跃起不再主动重置fallDistance。特殊飞行退出仍调用现有飞行接口，不修改共享飞行逻辑。

## 最终普通说明
1. 朝视线方向跳跃

保留用户已手动精简的单句普通说明与移除Shift详情的修改，不恢复旧免伤文案。
