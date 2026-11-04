# Blockbench 模型索引

本目录集中保存模型源文件及对应材质。表中的每个 `.bbmodel` 均含可独立读取的内嵌贴图；已有动画保留在模型源文件中。无需依赖临时目录加载模型。

外部 PNG 如存在，已核对与源文件内嵌贴图的像素一致。部分模型以源文件内嵌贴图交付；Java 文件为已有的导出参考，不代表已接入游戏。

## 已保存模型

| 模型 | 源文件 | 贴图尺寸 | 动画（秒） |
| --- | --- | --- | --- |
| blue_fly | [源文件](creature/blue_fly/blue_fly.bbmodel) | 16×16 | animation.blue_fly.hover (2) |
| blue_spider | [源文件](creature/blue_spider/blue_spider.bbmodel) | 16×16 | animation.blue_spider.idle (1.6), animation.blue_spider.move_forward (0.4), animation.blue_spider.move_left (0.4), animation.blue_spider.move_right (0.4) |
| blood_bomb | [源文件](entity/blood_bomb/blood_bomb.bbmodel) | 128×128 | 无 |
| bobby_bomb | [源文件](entity/bobby_bomb/bobby_bomb.bbmodel) | 128×64 | fuse (1) |
| bobs_bomb | [源文件](entity/bobs_bomb/bobs_bomb.bbmodel) | 128×64 | 无 |
| bomber_boy_bomb | [源文件](entity/bomber_boy_bomb/bomber_boy_bomb.bbmodel) | 128×64 | 无 |
| brimstone_bomb | [源文件](entity/brimstone_bomb/brimstone_bomb.bbmodel) | 256×128 | 无 |
| fast_bomb | [源文件](entity/fast_bomb/fast_bomb.bbmodel) | 128×64 | 无 |
| glitter_bomb | [源文件](entity/glitter_bomb/glitter_bomb.bbmodel) | 128×64 | 无 |
| golden_bomb | [源文件](entity/golden_bomb/golden_bomb.bbmodel) | 128×64 | 无 |
| isaac_bomb | [源文件](entity/isaac_bomb/isaac_bomb.bbmodel) | 128×64 | 无 |
| sad_bomb | [源文件](entity/sad_bomb/sad_bomb.bbmodel) | 128×64 | 无 |
| scatter_bomb | [源文件](entity/scatter_bomb/scatter_bomb.bbmodel) | 128×64 | 无 |
| sticky_bomb | [源文件](entity/sticky_bomb/sticky_bomb.bbmodel) | 128×64 | 无 |
| bomb_bag | [源文件](familiar/bomb_bag/bomb_bag.bbmodel) | 256×128 | 无 |
| brother_bobby | [源文件](familiar/brother_bobby/brother_bobby.bbmodel) | 64×64 | idle (1), attack (0.8) |
| clown_baby | [源文件](familiar/clown_baby/clown_baby.bbmodel) | 64×64 | idle (1), attack (0.8) |
| coin_bag | [源文件](familiar/coin_bag/coin_bag.bbmodel) | 256×128 | 无 |
| demon_baby | [源文件](familiar/demon_baby/demon_baby.bbmodel) | 64×64 | idle (1), attack (0.8) |
| depression | [源文件](familiar/depression/depression.bbmodel) | 64×64 | 无 |
| ghost_baby | [源文件](familiar/ghost_baby/ghost_baby.bbmodel) | 64×64 | idle (1), attack (0.8) |
| headless_baby | [源文件](familiar/headless_baby/headless_baby.bbmodel) | 64×64 | 无 |
| loki_baby | [源文件](familiar/loki_baby/loki_baby.bbmodel) | 64×64 | animation.idle (1), animation.attack (0.6) |
| mom_knife | [源文件](familiar/mom_knife/mom_knife.bbmodel) | 64×32, 16×16, 32×64 | 无 |
| mystery_bag | [源文件](familiar/mystery_bag/mystery_bag.bbmodel) | 256×128 | 无 |
| relic | [源文件](familiar/relic/relic.bbmodel) | 64×64 | 无 |
| robo_baby | [源文件](familiar/robo_baby/robo_baby.bbmodel) | 256×128 | idle (1) |
| robo_baby_2 | [源文件](familiar/robo_baby_2/robo_baby_2.bbmodel) | 128×64 | idle (1) |
| rotten_baby | [源文件](familiar/rotten_baby/rotten_baby.bbmodel) | 128×64 | idle (1), attack (0.8) |
| rune_bag | [源文件](familiar/rune_bag/rune_bag.bbmodel) | 256×128 | 无 |
| sister_maggy | [源文件](familiar/sister_maggy/sister_maggy.bbmodel) | 64×64 | idle (1), attack (0.8) |

共 31 个模型。

## 本次补齐

无头宝宝 `headless_baby`、符文袋 `rune_bag`，以及 `blood_bomb`、`bobs_bomb`、`bomber_boy_bomb`、`fast_bomb`、`glitter_bomb`、`sticky_bomb` 的正式源文件、PNG 和已有配套导出已补入对应目录。保留原始图层、UV 和动画状态。

本次保存不包含过程截图、试验版本、缓存或临时脚本。
