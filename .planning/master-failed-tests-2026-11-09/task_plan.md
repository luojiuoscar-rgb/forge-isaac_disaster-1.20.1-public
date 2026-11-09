# Remove obsolete and prose tests
Follow-up explicitly authorized on master by user; same investigation.
1. [complete] Inventory JUnit and Forge GameTest prose assertions.
2. [complete] Remove four failing test methods, all prose-only methods and mixed prose assertions; retain gameplay checks.
3. [complete] Java17 gradlew test compileGameTestJava --offline --no-daemon: exit0, BUILD SUCCESSFUL; 204 JUnit suites, 975 tests, 0 failures/errors/skips.
4. [complete] Add no-prose-test rule to AGENTS.md:21; verify source scan and diff.
Only tests, AGENTS.md and task records changed. No production code or language resources changed. GameTest runtime and client checks not run.
