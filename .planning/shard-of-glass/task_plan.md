# 玻璃碎片实施

已批准：feature/feature-c；跳过主分支同步；不提交。

- [x] 定向测试与既有 ON_HURT 节点（按用户最新更正）
- [x] 流血、固定扣血、纯净血泪与共享粒子
- [x] 注册、说明、宝箱房池与英文 Wiki 32×32 图标
- [x] JUnit、实际 GameTest、一次 runData、差异审查

遵守已批准方案：普通 ExtraData double；无新增 Runtime/State/Manager 或生命周期订阅；无额外 0.01 伤害；配置文本不动态高亮。

最新：喷泉调整后（弹速50%、仰角60°～85°、每3 tick一发）JUnit 10/10、实际 GameTest 22/22（本品14、贫血8）通过；前轮一次 runData 成功，本轮未改注册与资源。客户端画面、多人实际观察与真实存档重载未执行；仅已验证 ExtraData 序列化／复制。
