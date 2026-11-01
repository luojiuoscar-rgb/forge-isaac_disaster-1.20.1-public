# ISAAC DISASTER
*made by LuojiuOscar*

## INTRODUCTION
此模组目标在于还原以撒中的各类道具。[帮助文档](docs/documentation.md)

目前内容包括：

- 245 个道具：202 个被动道具、43 个主动道具
- 46 个饰品
- 39 种卡牌，以及 4 种随机卡背
- 44 种药丸效果（普通与巨型药丸的效果不重复计数）
- 还有更多！

模组的开发正在缓慢进行中，欢迎大家关注我的b站账号[洛玖Oscar](https://space.bilibili.com/250685960)！

---

## INTRODUCTION
The goal of this mod is to recreate various items from The Binding of Isaac.

Current content includes:

- 245 items: 202 passive and 43 active
- 46 trinkets
- 39 cards, plus 4 random-card backs
- 44 distinct pill effects, counting normal and horse variants together
- And more!

Development of the mod is progressing slowly. Welcome to follow my Bilibili account[洛玖Oscar](https://space.bilibili.com/250685960)!

---

## LICENSE / 许可证

本项目中由版权持有人有权并同意授权的原创源代码采用 [MIT License](LICENSE)。
第三方代码保留各自的许可；贴图、音效、模型、动画、文档、数据文件等非代码资源
不包含在本项目的 MIT 授权范围内，另有明确许可的除外。

《以撒的结合》相关图片下载自英文 Wiki，音效通过录制游戏播放内容后提取或编辑
取得。这些来源说明不代表已经获得原权利人的使用或再分发授权。本项目是非官方
同人项目，不声称获得原游戏权利人的背

Original source code released by authorized copyright holders is licensed under
the [MIT License](LICENSE). Third-party code retains its own license terms.
Non-code resources, including textures, audio, models, animations, documentation
and data files, are excluded from this project's MIT grant unless separately and
explicitly licensed.

Game-related images were downloaded from the English Binding of Isaac Wiki;
audio was recorded from game playback and then extracted or edited. These source
descriptions do not establish right-holder permission to use or redistribute
the materials. This is an unofficial fan project; no official endorsement is
claimed.

See [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md) for the license scope and
resource provenance. MixinExtras is maintained by LlamaLad7 and distributed under
MIT; its full license is included in [LICENSES/MixinExtras.txt](LICENSES/MixinExtras.txt).

## NOTES
<details>
<summary>click to expand</summary>

 - data/forge/loot_modifiers/global_loot_modifiers.json is overwritten by custom provider: ForgeGlobalLootModifiersProvider. 
This will ensure that all loot modifiers are in the correct order.
 - Removal of an **ITEM** from corresponding item pool is processed manually. 
Ensures in the future item could be generated from pool without removing it from pool.
**TRINKETS** will not be removed from any pool in any circumstance.
 - **ITEM** displayed on any blocks will only be grabbed when player's hand is empty.

#### BLOCK ENTITIES
 - Give pedestal with loots: ```/give @s isaac_disaster:pedestal{BlockEntityTag:{itemLootTable:"isaac_disaster:pools/item/passive_items",isDecoration:0b}}```
 - Debug stick could link multiple pedestals.
 - Players(not creative & spectator) within **5** blocks from a pedestal will activate it and spawns item based on its lootTables.
 - Locked_chest/old_chest: 20% to spawn an item; If it spawns any item then the original drop in it will be cleared. (tag: itemLootTable)
 - A chest without any itemLootTable will spawn item from the default pool, but they do not have default LootTable.
 - Positions of blocks containing items should be stored into IsaacItemBlockData. When the item disappears it should also be removed.

#### ITEM POOLS & LOOT TABLES
 - Generating more than 1 item from item pool may cause unpredictable error. Item display will always choose the first item in the container.
 - Only pools under  *data/isaac_disaster/loot_tables/pools/item/* folder will be affected by other items.
 - Items generated from pedestal & chests will be marked as removal.
 - Chest Loots are located under *resource/data/isaac_disaster/loot_tables/chests/isaac/* folder.

</details>

# CREDITS
I would like to thank the following contributors for their invaluable help and support:

- [@半只橘猫](https://github.com/cutvmh307): Provided preset loot tables for item pools and item documentation for datapack version.



