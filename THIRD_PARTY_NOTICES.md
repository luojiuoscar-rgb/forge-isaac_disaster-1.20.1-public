# License scope and third-party notices / 许可范围与第三方声明

## Original source code / 原创源代码

The MIT license in `LICENSE` applies only to original source code authored for
Isaac Disaster by copyright holders who have authorized its release under MIT.
It does not relicense third-party code or non-code resources. Existing third-party
copyright and license notices remain applicable wherever those materials occur.

Textures, audio, models, animations, documentation, data files and other non-code
resources are outside this project's MIT grant unless separately and explicitly
licensed. Their inclusion in this repository or a mod JAR does not grant rights
to use, modify or redistribute them.

`LICENSE` 中的 MIT 许可证仅适用于版权持有人有权并同意以 MIT 授权的 Isaac
Disaster 原创源代码，不对第三方代码或非代码资源重新授权。第三方材料原有的
版权与许可声明继续适用。

贴图、音效、模型、动画、文档、数据文件及其他非代码资源不属于本项目的 MIT
授权范围，另有明确许可的除外。资源出现在仓库或模组 JAR 中，不代表获得其
使用、修改或再分发许可。

## The Binding of Isaac resources / 《以撒的结合》资源

Isaac Disaster is an unofficial fan project inspired by The Binding of Isaac.
No endorsement by the original game's creators or rights holders is claimed.
Rights in the original game and its artwork, recordings and other protected
materials remain with their respective rights holders.

The project maintainer reports that images were downloaded from the English
Binding of Isaac Wiki and audio was recorded from game playback and then
extracted or edited. Relevant resource locations include:

- `src/main/resources/assets/isaac_disaster/textures/`
- `src/main/resources/assets/isaac_disaster/sounds/`
- Game-derived materials in `blockbench/` and other resource directories.

These are directory-level source descriptions, not a verified per-file inventory.
The exact Wiki host, page URLs, download dates and individual file licenses have
not yet been recorded. No right-holder permission to redistribute these game
resources is documented here. Downloading an image from a Wiki or recording game
audio does not itself establish permission to relicense the underlying material.
This notice does not assert that a Wiki's general text license covers game
images or that this use qualifies as fair use.

The project does not grant any license to these third-party game resources.
Attribution and exclusion from MIT do not substitute for any required permission.
Any applicable right-holder permissions or file-specific licenses must be checked
before relying on them for reuse or distribution.

本项目是受《以撒的结合》启发的非官方同人模组，不声称获得原游戏作者或权利人
的背书。原游戏及其美术、录音等受保护内容的权利归各自权利人所有。

根据项目维护者提供的信息，图片下载自《以撒》英文 Wiki，音效通过录制游戏播放
内容后提取或编辑取得。以上目录说明尚未构成逐文件核验清单；具体 Wiki 站点、
页面链接、下载日期和文件许可尚未登记，本文也未记录权利人的素材再分发授权。
Wiki 的文字许可不应被默认视为游戏图片的许可，自行录制游戏声音也不代表取得
原声音的再授权权利。本文不主张上述使用已构成合理使用。

本项目不对这些第三方游戏素材授予许可。署名和排除 MIT 仅用于说明权利边界，
不能代替所需授权；再次使用或分发前仍需核实适用的许可或权利人授权。

## MixinExtras

- Project: MixinExtras
- Author and copyright holder: LlamaLad7
- Copyright notice: `Copyright (c) 2022-present LlamaLad7`
- Version: 0.5.5 (see `mixinextras_version` in `gradle.properties`)
- Embedded artifact: `io.github.llamalad7:mixinextras-forge:0.5.5`
- Upstream: https://github.com/LlamaLad7/MixinExtras
- License: MIT; full text reproduced in `LICENSES/MixinExtras.txt`.

MixinExtras is an independently maintained library with Forge integration.
This project embeds the Forge artifact through Jar-in-Jar and preserves its
`LICENSE_MixinExtras` entry inside the embedded dependency. The repository copy
of its license is also included in the mod JAR as `LICENSES/MixinExtras.txt`.
When updating MixinExtras, review its upstream license and update this entry
and the license copy if necessary.

MixinExtras 由 LlamaLad7 独立维护，提供 Forge 集成。本项目通过 Jar-in-Jar 嵌入
该依赖，并保留内嵌 JAR 中的 `LICENSE_MixinExtras`；另在仓库和模组 JAR 中提供
`LICENSES/MixinExtras.txt` 完整许可文本。升级依赖时应重新核对其许可与本声明。

## Other third-party materials / 其他第三方材料

Minecraft, Minecraft Forge, JEI, Curios, Gradle Wrapper and other third-party
components retain their own copyright and license terms. Dependency declarations
do not make those components part of this project's MIT grant. Preserve their
applicable notices when redistributing their code or binaries. This document is
not an exhaustive per-file license audit.

Minecraft、Minecraft Forge、JEI、Curios、Gradle Wrapper 等第三方组件继续适用
各自的版权与许可条件，不因依赖声明而纳入本项目的 MIT 授权。再分发其代码或
二进制文件时应保留适用声明。本文不是完整的逐文件许可证审计。
