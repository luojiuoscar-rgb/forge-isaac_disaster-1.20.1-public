# 当前进度

- 在 feature/feature-c 完成玻璃碎片；保留改动，不提交。
- 按用户最新更正，复用 ON_HURT / LivingHurtEvent.amount；已删除额外结算触发类型及分发代码。护甲、抗性与吸收不改变掉心的计算基数。
- 周期固定扣血只发布一次 self_damage 通知，掉心统一由 ON_HURT 处理；已移除重复手动判定。通知取消不回滚固定扣血，但会取消该次掉心。
- 生命以每2 tick实际采样为准，不使用 Hurt 金额推算减伤后生命；采样间隔内治疗可被伤害掩盖。
- 用户调整过的当前说明文本保留，仅更新旧翻译断言。
- 技能规则：每个效果模块的精确 TriggerType 必须确认；现有类型完全无法实现时才能提议新增并等待明确批准。源技能、项目和用户安装副本6文件完整哈希一致；quick_validate通过（UTF-8）。
- 本轮验证：原工程 Forge 映射 JAR 被占用；在 build/shard-on-hurt-project 中以同一源码和复制的构建配置隔离验证，JUnit 10/10、实际 GameTest 21/21（玻璃碎片13、贫血8），Java17，Gradle退出0。
- 前轮一次 runData 成功。本轮没有资源／注册改动，不重复运行；未执行独立 compile/build。
- 客户端画面、多人实测及真实存档重载未执行；ExtraData 序列化和复制已测试。

- 喷泉调整完成：面板弹速×0.5、仰角60°～85°、每3 tick一发；回血与血迹采样仍每2 tick。新增独立发射倒计时，随结束清理。
- 方向测试先在旧实现复现失败；修改后 JUnit 10/10、实际 GameTest 22/22（玻璃碎片14、贫血8）通过，包括真实周期队列410 tick节奏检查。使用已有独立验证目录与Java17，日志 build/shard-fountain-validation.log；客户端喷泉观感未实测。

- 补充修改：衰弱粒子色复用原版虚弱颜色；流血血泪静音。用户当前固定弹速0.4已保留。定向JUnit 10/10、实际GameTest 22/22通过，日志 build/shard-silent-frailty-validation.log；未实测客户端画面／音频，未提交。
