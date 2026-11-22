# nuts-runtime — Assessment and Fix Plan (2026-11-21)

## Scope

- **Module**: `core/nuts-runtime` (`net.thevpc.nuts:nuts-runtime:1.2.0.0`)
- **Repo**: `/home/vpc/xprojects/nuts-world/nuts`
- **Baseline**: existing `core/nuts-runtime/ASSESSMENT-AND-FIX-PLAN.md` (2026-11-20, final verification 2026-11-21) plus current source scan.
- **Method**: reviewed existing assessment, module POM/module-info/service registrations, git working tree state, TODO/FIXME/BUG markers in `core/nuts-runtime/src/main/java`, and key hotspots cited by the assessment and current TODO scan. No runtime source files were modified.

## Executive summary

`nuts-runtime` is the Nuts execution engine (boot/workspace lifecycle, repositories, dependency solving, execution, IO/paths, security, text/formatting, extensions). Most historical P0-P3 findings in the existing assessment are marked DONE, with CI-equivalent gate reported green on 2026-11-21.

**Current state is not “done” yet.** Remaining work is concentrated in:

1. **ARCH-08 god-class decomposition (IN PROGRESS)** — owner-approved, API-safe facade slices; option-builder slice is partially present in the working tree and must be completed/validated before continuing.
2. **Residual TODO/BUG markers (36 in main Java)** — several are harmless notes, but some indicate unfinished behavior, unclear security posture, or silent failure modes.
3. **Deferred perf/API items** — PERF-04 (stateful HTTP client sharing) and PERF-09 (shared process pipe executor) require lifecycle/API design; ARCH-10 full per-workspace scoping also deferred.
4. **Structural debt guardrails** — 49 grandfathered bidirectional package cycles, constructor-injected components not JPMS-providable, Windows optional deps still compile-scope, integration-test-only confidence model.

## Current baseline evidence

| Item | Observed |
|---|---|
| Build/packaging | Maven jar, Java 8 main bytecode, Java 9 multi-release `module-info` moved to `META-INF/versions/9` by antrun; `Multi-Release: true`. |
| Dependencies | `nuts` API, `nuts-boot`, `jansi:2.3.4`, `mslinks:1.0.5`. Windows deps still compile-scope though wrappers were made reflective (ARCH-03 partial packaging semantics remain). |
| Tests | No `core/nuts-runtime/src/test`; regression coverage lives in `test/nuts-runtime-test` (JUnit 5). Existing assessment reports CI-equivalent `mvn -B -ntp install -Dexcluded.groups=local-only` BUILD SUCCESS on 2026-11-21. |
| Service registry | `META-INF/services/net.thevpc.nuts.spi.NComponent` + JPMS provides/uses/exports maintained; integrity/cycle/stale-assets guard tests exist per assessment. |
| Working tree | Dirty ARCH-08 slice work: modified `DefaultNBootOptionsBuilder.java`, `DefaultNWorkspaceOptionsBuilder.java`; added state-holder classes and characterization tests/resources; untracked `.fix_builders.py`, `.fix_imports.py`, `.refactor_builders.py`, `.verify_api.py` at repo root (ad-hoc scripts, not runtime code). |

## Findings and fix plan

### P1 — Complete in-progress ARCH-08 safely

| ID | Status | Evidence | Risk | Fix | Verification |
|---|---|---|---|---|---|
| RT-ARCH-08A | In progress | Dirty option-builder split files under `core/nuts-runtime/src/main/java/net/thevpc/nuts/runtime/standalone/` and new characterization test `test/nuts-runtime-test/src/test/java/net/thevpc/nuts/core/test/OptionBuilderCharacterizationTest.java` | Refactor can silently change builder defaults/copy/reset/build semantics; API drift; unreviewed generated helper scripts in repo root. | Finish option-builder slice as API-safe facade extraction: keep public/protected signatures, preserve defaults/copyFrom/copy/reset/build behavior, move only internal state/logic. Remove or relocate ad-hoc `.py` refactor scripts from repo root before merge. | `mvn -B -ntp -pl test/nuts-runtime-test -am test -Dtest=OptionBuilderCharacterizationTest`; then CI-equivalent `mvn -B -ntp install -Dexcluded.groups=local-only`. Compare builder API baseline resources. |
| RT-ARCH-08B | Planned | Assessment ARCH-08 remaining targets: `DefaultNSession`, `DefaultNWorkspaceConfigModel`, `DefaultNWorkspace` | Large god classes are change focal points; future fixes have high regression risk. | Continue slice-by-slice: add characterization/behavior tests first, extract cohesive facades/helpers behind unchanged APIs, one domain at a time. Do not start a slice without characterization coverage. | Per-slice characterization tests + full excluded-groups CI run; no public API signature changes unless explicitly approved. |

**ARCH-08 order recommendation:** stabilize current option-builder slice → `DefaultNSession` → `DefaultNWorkspaceConfigModel` → `DefaultNWorkspace` (largest/riskiest last).

### P2 — Fix concrete residual correctness/TODO issues

| ID | Evidence | Issue | Fix | Verification |
|---|---|---|---|---|
| RT-BUG-01 | `standalone/reflect/NReflectPropertyFiller.java` `_getMethods/_getFields` | Opaque `IllegalArgumentException("TODO")` on unsupported/unresolved type lookup. | **DONE 2026-11-21**: replaced with descriptive exceptions including the offending type; minor raw-cast cleanup. | `mvn -q -pl core/nuts-runtime -am -DskipTests compile` passes. Regression test still recommended for generic/parameterized shapes. |
| RT-SEC-01 | `workspace/cmd/install/InstallHelper.java` `fireEventBeforeInstall` | Dead fire-and-forget recommendation thread, discarded result, swallowed exceptions, security TODO. | **DONE 2026-11-21 (mitigation)**: removed dead background computation; method is now an explicit no-op with comment. Reintroduce only with a real consumer, lifecycle, and offline/secure guards. | Compile passes; safer/no unintended background work. Add offline/secure install side-effect test if recommendations are reintroduced. |
| RT-COR-01 | `workspace/cmd/push/DefaultNPush.java` | Ambiguous `//TODO : CHECK ME, why offline?` around fetch mode selection. | **DONE 2026-11-21 (clarified)**: removed misleading TODO; existing logic already uses `LOCAL` when offline and `REMOTE` otherwise. | Compile passes. Add push offline/online integration test for full confidence. |
| RT-COR-02 | `format/json/DefaultJsonElementFormat.java:385,408` `TODO FIXE ME LATER`; `format/tson/parser/custom/TsonCustomParser.java:357,373` `TODO: handle error` | Parser/format error handling gaps can cause poor diagnostics or incorrect parsing on malformed input. | Identify malformed-input cases, return/throw structured parse errors with location, add parser regression tests. | Parser tests for malformed JSON/TSON inputs; no silent acceptance. |
| RT-COR-03 | `workspace/cmd/fetch/DefaultNFetch.java:194`, `workspace/cmd/search/DefaultNSearch.java:104`, `AbstractNSearch.java:1201` `TODO fix me` | Command behavior has known unresolved edge cases. | Reproduce each TODO case, convert to issue-specific tests, fix with minimal behavior change. | Command-level regression tests in `nuts-runtime-test`. |
| RT-COR-04 | `repository/impl/folder/NFolderRepositoryBase.java:316` logs `[BUG] unsupported Hash Type %s` | Unsupported hash type path may be noisy or indicate incomplete hash support. | Confirm expected hash types; either support missing type, or produce actionable validation error and test. | Unit/integration test for unsupported/supported hash types. |

### P2 — Performance and scalability follow-ups

| ID | Status | Evidence | Issue | Fix | Verification |
|---|---|---|---|---|---|
| RT-PERF-01 | Deferred (API) | Assessment PERF-04 | `URLPath` resolves HTTP client per stream; stateful client sharing needs API/lifecycle change. PERF-03 keep-alive mitigates main cost. | Design a workspace-scoped/request-scoped HTTP client contract that can be reused by URLPath/extension discovery without breaking SPI. Implement behind compatible API or in next major/minor API window. | Request-count/TLS-handshake benchmark before/after; no behavior change for auth/proxy/redirect. |
| RT-PERF-02 | Deferred (API) | Assessment PERF-09 | `ProcessBuilder2` creates per-process pipe executors; sharing needs lifecycle changes. | Introduce bounded workspace/session executor for pipe pumping with explicit shutdown/lifecycle hooks; keep per-process futures/cancellation semantics. | JFR thread/allocation profile during process-heavy commands; stress test no deadlocks/leaks. |
| RT-PERF-03 | Planned | 36 TODO markers; historical perf fixes already present | Risk of perf regressions in hot paths (descriptor resolution, parsing, formatting, HTTP, process). | Add lightweight perf guardrails for hot operations already fixed by PERF-01..23: benchmark scripts/JMH or repeatable timing tests in CI-optional profile. | Baseline p50/p95 for fetch/install/run and selected microbenchmarks; compare on changes touching hot paths. |
| RT-PERF-04 | Planned | `DefaultNHttpClient.java:563` TODO “smart copy input source!” | Potential avoidable copy/allocation in HTTP response handling. | Profile before changing; if hot, replace with streaming/buffer-size-aware copy preserving lazy-body semantics. | Allocation/profile comparison on large downloads; existing HTTP tests pass. |

### P3 — Structural/packaging debt

| ID | Status | Evidence | Issue | Fix | Verification |
|---|---|---|---|---|---|
| RT-ARCH-01 | Guarded/deferred | Assessment ARCH-09 | 49 bidirectional package cycles grandfathered. | Keep cycle guard test; when touching a cycle edge, reduce coupling opportunistically by moving interfaces downwards or extracting neutral packages. Do not add new cycles. | `PackageCycleGuardTest` green; cycle count decreases over time. |
| RT-ARCH-02 | Deferred | Assessment ARCH-10 | Static caches have reset hooks but are not fully workspace-scoped. | Move mutable static caches behind workspace/session services where feasible; keep reset hooks until then. | Two-workspace-in-one-JVM lifecycle tests; no cross-workspace leakage. |
| RT-ARCH-03 | Planned | `pom.xml` deps `jansi`, `mslinks` compile-scope; properties comment out optional/os | Windows-only deps are unconditional; packaging/license/size impact. | Make optional/platform-specific where compatible, or isolate into runtime-optional artifacts; keep reflective wrappers and JPMS/module-path behavior. | Build/test on Linux/Windows profiles; `jar --describe-module`; no CNFE on non-Windows. |
| RT-ARCH-04 | Planned | 4 constructor-injected components classpath-only per assessment | JPMS ServiceLoader parity gap. | Document clearly; if possible provide JPMS-compatible factory/provides alternative without changing behavior. | `JpmsSmokeTest`/module-path smoke includes component availability checks. |
| RT-ARCH-05 | Planned | No in-module unit tests (ARCH-14 decision) | Fast feedback for runtime internals is limited. | Keep black-box suite as source of truth, but add narrowly scoped unit tests for pure/isolatable logic when refactoring (parsers, utils, formatters) if maintainers agree; otherwise expand characterization tests in `nuts-runtime-test`. | Test runtime for touched modules decreases; regressions caught before integration suite. |

## Suggested execution order

1. **Stabilize ARCH-08 option-builder slice** - finish/validate dirty work, remove ad-hoc scripts, run characterization + CI-equivalent tests.
2. **Triage residual TODO/BUG markers** - convert RT-BUG/COR items into small tested fixes; remove stale TODOs only after evidence.
3. **Performance guardrails** - record baseline for fetch/install/run and HTTP/process hot paths before PERF-04/09 API work.
4. **Design deferred API perf items** - PERF-04/PERF-09/ARCH-10 scoping with explicit lifecycle/API proposal.
5. **Opportunistic structural cleanup** - reduce cycles and packaging debt while touching related code, never without tests.

## Verification checklist

```bash
# Working tree / scope
git -C /home/vpc/xprojects/nuts-world/nuts status --short

# TODO/BUG markers in runtime main sources
grep -RInE 'TODO|FIXME|HACK|XXX|\[BUG\]' core/nuts-runtime/src/main/java | wc -l

# Characterization test for current ARCH-08 slice
mvn -B -ntp -pl test/nuts-runtime-test -am -Dtest=OptionBuilderCharacterizationTest test

# CI-equivalent gate (as used by assessment)
mvn -B -ntp install -Dexcluded.groups=local-only

# JPMS descriptor sanity
jar --describe-module --release 9 core/nuts-runtime/target/nuts-runtime-*.jar
```

## Notes / risks

- The existing `ASSESSMENT-AND-FIX-PLAN.md` is comprehensive and should remain the historical register; this file is a current, action-oriented delta plan.
- Do not start broad god-class splits without characterization tests and explicit API-safety review.
- PERF-04, PERF-09, and full ARCH-10 scoping are intentionally deferred because they require lifecycle/API changes; track them as design items, not silent refactors.
- Untracked `.py` files at repo root appear to be refactoring helpers; they should not be shipped as part of runtime fixes.
