# Findings
Baseline: HEAD 5926081c, master equals fetched origin/master, no pre-existing dirty changes.
Original JUnit report: 208 suites, 987 tests, 4 failures, 0 skipped.
1. HeadlessBabyLoreTest.java:24 expects 持续留下伤害血迹; en_us.json:794 says 持续留下血迹. Both introduced in 9e03a6d2 (2026-11-04). Dynamic damage formatting assertions already pass.
2. DepressionLoreTest.java:31-33 loops six lines; resource and Depression.getDesc define exactly five. Missing sixth line causes NPE in the test, not in gameplay. Introduced together in 5bca92fb (2026-11-04).
3. BulletColorSelectionTest.java:115-116 expects Uranus=(100,30), actual preset=(0,90).
4. UranusTest.java:66-72 assumes Uranus overrides Ipecac. Actual same tier0 and priorities90 vs110 correctly select Ipecac. Both color failures trace to 9e761785 (2026-10-31).
Important provenance: .planning/bullet-color/progress.md final line explicitly records that the USER manually changed priority values, agent only sorted preserving user values, and tests were NOT rerun afterward. Previous all-passing results refer to the pre-adjustment numbers. Earlier design text specifying Uranus tier100 is superseded by this later note. Thus both color test assertions are stale relative to the final user-adjusted configuration, rather than evidence the selector algorithm is broken.
Recommended follow-up: keep current implementation; align wording and five-line lore assertions, rename six-line test, align Uranus preset expectation and Ipecac winner while retaining selector behavior tests. Investigation only; no source/test modifications made.

Fresh full rerun: gradlew.bat test --offline --no-daemon --rerun-tasks --console=plain, Microsoft Java17.0.11. compileJava and compileTestJava completed; test reported exactly the same four failures out of987, exit1, BUILD FAILED in1m25s. Log: junit-rerun.log. No GameTest/client validation performed.

## Authorized cleanup and verification
User requested deletion of the four failures and every prose test, then addition of a no-prose-test rule to AGENTS.md.
Removed 12 JUnit methods (10 prose, 2 obsolete color expectations) and 8 prose-only GameTest methods. Removed prose assertions from mixed functional GameTests while preserving their gameplay/metadata checks. Remaining ID-only classes renamed DepressionItemIdTest and ShardOfGlassItemIdTest; remaining behavioral trinket coverage renamed TrinketLootGameTests.
Static scan over src/test and src/gameTest found no remaining getDesc/getSynergyDesc/getExtraDesc/translatable/dynamicDescription/dynamicNumber/language-resource/lore assertions. Custom names used as saved item payload markers and NBT string checks are behavioral persistence tests and remain.
AGENTS.md:21 now explicitly forbids prose/name/description/translation/line-count/format tests.
Fresh verification: Microsoft Java17.0.11, gradlew.bat test compileGameTestJava --offline --no-daemon --console=plain; exit0, BUILD SUCCESSFUL in59s. Fresh XML:204 suites,975 tests,0 failures,0 errors,0 skipped. Forge GameTest compilation passed; GameTest runtime and client not run. No changes under src/main or src/generated.
