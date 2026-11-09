# 关键依据

- 复用 Anemic 血迹与粒子、纯净 BulletState 创建、现有 DEATH 和模块移除回调。
- 按用户最新要求使用既有 ON_HURT，直接以 LivingHurtEvent 的 amount 计算掉心；环境和无攻击者伤害已经由该入口覆盖。
- H 使用当前 MAX_HEALTH 单位；掉心最高 25%，固定扣血每 400 tick，重复受伤不重置。
- 固定扣血先更新生命基线，再发布一次 self_damage LivingHurtEvent；本品通过同一个 ON_HURT 入口判定掉心，删除独立手动掉心，避免重复。取消通知会抑制掉心，但不回滚已完成扣血。
- 无额外兼容层；客户端与存档验收必须单独说明未执行范围。
- ON_HURT 处于护甲、抗性和吸收处理之前，不能用其 amount 推算最终剩余生命。初始化使用当前生命，后续每2 tick采样；采样间隔内治疗被伤害掩盖的限制保留。
- 已移除新增类型与额外分发入口，现有 ON_HURT、DEAL_DAMAGE 流程保持原样。
- 喷泉调整：血泪速度现由用户调整为固定0.4格/tick，仰角60°～85°，水平角仍均匀随机。周期模块每tick驱动，独立普通ExtraData发射倒计时保证每3 tick一发；生命采样、粒子和血迹仍每2 tick，固定扣血仍每400 tick。重复受伤不重置发射节奏，400 tick重置扣血时不重置喷血节奏。
- 图标：https://static.wikia.nocookie.net/bindingofisaacre_gamepedia/images/1/13/Collectible_Shard_of_Glass_icon.png/revision/latest?format=png 。实际 PNG 32×32，已目检为玻璃碎片图标。

- 最新外观与音效调整：FRAILTY 粒子色直接引用 MobEffects.WEAKNESS.getColor()；移除血泪发射的 makeSound 调用，泪弹运行时／碎裂网络路径也没有主动播放音效。
