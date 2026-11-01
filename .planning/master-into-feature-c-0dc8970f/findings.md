# Findings

Feature C has 17 unique commits, master 25. Feature C adds ground effects/tracking, holy light, Uranus, D10, jump and dual-parameter bullet colors. Master contains player knife/weapon and baby familiars with a renamed familiar base. Preserve both branches and only modify feature C worktree. No commit or push.

All 18 Git conflicts resolved. Keep C generic EFFECT_ATTACHMENT_PREPARE, dual-channel colors and knockback capture; keep master knife state/input and ATTACK_FIRED Lead Pencil counting. Mulligan attachment receives master's source IDs through the generic event. Scheduler retains C append/cancel helpers and all 3 master cancellation/overlap tests. Master ItemId order is canonical; append 8 C items. Protocol 21 distinguishes combined packet formats. Include water/holy/knife damage tags and both branches' locale/loot entries. Updated two new SwarmItem tests to current generic trigger name.

First compilation found two API mismatches: PlayerMomsKnifeEntity still used getColorRl, and GHOST_TEAR used the retired 3-argument BulletColor constructor. Adapted to getColorId and builder with white/0.6 alpha/neutral (0,0) priority. Recompile next.

Production compilation passes after color migration. Test compilation required adapting AttackColorContextTest's AttackType stub to the onFired callback signature. GameTest compilation exposed two old getColorRl calls in AutoAttackBaby/Conjoined tests; changed to getColorId. Re-running final GameTest compilation and JUnit.

Resource verification passed: 13 changed JSON files, 1311 locale entries match key-wise three-way merge, both conflicting loot pools preserve parent entries without duplicates, master 254 item ordinals unchanged with 8 C items appended.

JUnit completed: 965 tests, 2 failures. UranusTest expects Uranus to beat Ipecac; BulletColorSelectionTest expects URANUS (100,30), but current feature C preset is (0,90). Verified BulletColor, BulletColorPrio, BulletColorSelection and both failing tests have zero diff against original feature C HEAD. These are pre-existing expectation/implementation mismatches; preserve current production semantics and report the failures, rather than choosing a new color hierarchy within merge scope. All compilation tasks pass. Forge datagen pending.

Forge runData passed (registry loading and generated resources) on Java 17.0.11. Final limitations: GameTest runtime and client visuals not run; 2 existing color-priority test failures intentionally left for explicit policy choice. No commit/push; only feature C worktree modified.
