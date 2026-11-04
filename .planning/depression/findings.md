# 发现

- 当前分支feature/feature-c，开始时工作区干净。原模型为单网格，91顶点、91法线、136面，64×64 PNG、无动画。
- OBJ坐标已为格，直接居中后缩放0.6；三角形重复末顶点适配四顶点绘制，保留每顶点法线。
- BABIES与ITEM_GIVERS目前各64只，用户确认合并；跟班需求与掉落进度独立持久化，不需要旧组别名。
- 共享水迹按来源与目标去重，最新液体结算；专属回调内检查hurt返回值后抽取1%，不放宽共享目标规则。
- water与holy_light均已在bypasses_cooldown标签内，连续结算不需修改受伤规则。
- 规划已确认：抑郁症不加入连体，BFFS不增圣光伤害，圣光读取触发时面板。
- 中英文Wiki正文访问受限；原版EID/Guru频率冲突，使用用户批准数值。图标仅允许英文Wiki32×32 PNG。
- 图标最终来源为英文Wiki原始媒体：https://static.wikia.nocookie.net/bindingofisaacre_gamepedia/images/8/85/Collectible_Depression_icon.png/revision/latest?format=original 。验证PNG、32×32、221字节，SHA256=b2daea60adbb920bbc76b27abc116e22c2aeddc82aa3dd05c8b0a8e9d5986772。
- 英文Wiki媒体服务器默认返回WebP，因此显式请求format=original；未转码、未使用替代素材。此前Wiki.gg TLS/403及Fandom正文入口403已解决为媒体文件直接获取。
- 共享配额会保留既有同优先级分配；后续需求不会抢占已分配位置。新增混合类型测试覆盖等待补出和掉落库存/生产倒计时不重置。
- 运行时新增类限于实体、能力、产水效果、专属水迹及模型/渲染数据；不新增Runtime/State/Manager、触发类型或协议字段。
