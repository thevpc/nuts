# nuts-runtime — Assessment and Fix Plan

**Scope:** `core/nuts-runtime` (module `net.thevpc.nuts:nuts-runtime:1.2.0.0`) in the Nuts repository.
**Assessment date:** 2026-11-20.
**Method:** static source review of all 1,539 main Java sources plus POMs, service registrations, module descriptors, CI config and the separate `test/nuts-runtime-test` suite. Findings were verified by reading the surrounding code; the highest-priority items were re-checked directly before writing this plan. **No source files were changed** as part of the assessment.

> Line numbers refer to the state of the tree at the assessment date and will drift as edits land. Use the file + symbol as the durable reference.

---

## 1. Baseline and headline numbers

| Item | Observed state |
|---|---|
| Build | `mvn -o -q -pl core/nuts-runtime -am compile -DskipTests` → **exit 0** (JDK 25.0.5 / Maven 3.10.0). Main classes are Java 8 bytecode; `module-info.class` is Java 9. |
| Unit tests in module | **0** — `src/test` does not exist; no test-scope dependencies or surefire config in the module POM. |
| Integration tests | `test/nuts-runtime-test`: 114 Java files, 99 with `@Test`; 67 boot a full workspace. Not runnable as unit tests. |
| Service registry | `META-INF/services/net.thevpc.nuts.spi.NComponent` has 155 live entries; `DefaultNVersionWriter` is listed twice. |
| Dead code | 33 Java files are **entirely commented out** (3,222 lines); ~11,600 `//` lines module-wide. |
| Structure | 49 bidirectional package pairs (2-level import graph); heaviest: `workspace ↔ repository`, `workspace ↔ util`. |
| Packaging | Manifest claims `Multi-Release: true` but `module-info.class` sits at jar root, not `META-INF/versions/9`. |
| License headers | 1,254 / 1,539 Java files (81%) lack the header despite a checked-in `licenseheader.txt`. |
| Full-suite baseline (2026-11-20, before fixes) | `mvn install -Dexcluded.groups=local-only`: **929 tests, 5 errors** — `SshTest` (2), `UncompressTest` (1), `PsTest` (2), all `not an extension: nuts-ssh / nuts-compression`. Pre-existing test-environment issue; must be resolved for a green gate (tracked with ARCH-16). |
| Deps | `jansi` and `mslinks` (Windows-only) are unconditional compile-scope dependencies; `<optional>` and `<os>` are commented out. |

**Top five risks (in order):** arbitrary file write via archive extraction → plaintext credential leakage → root command injection in service install → no integrity verification of downloaded artifacts → warm-cache descriptor resolution doing full recursive re-hashing.

---

## 2. Priority model

| Priority | Meaning | Target |
|---|---|---|
| **P0** | Exploitable security issue, data loss/corruption, or feature that is completely broken | Phase 1 (week 1) |
| **P1** | High-impact correctness or performance regression with real user cost | Phase 1–2 (weeks 1–3) |
| **P2** | Medium correctness/reliability/performance issue; degraded diagnostics or throughput | Phase 3 (weeks 3–5) |
| **P3** | Structural, build, documentation and maintainability debt | Phase 4 (weeks 5–9) |

Each fix must land with a regression test (unit test preferred; a focused integration test if workspace boot is unavoidable). Where a fix changes observable behaviour (e.g. credentials in URLs), call it out in the release notes.

---

## 3. Workstream A — Security and data integrity

### A.1 Archive handling (do this first — direct path from malicious artifact to arbitrary file write)

| ID | Prio | Location | Issue | Fix |
|---|---|---|---|---|
| SEC-01 | **P0 — DONE 2026-11-20** | `standalone/xtra/compress/NUncompressZip.java:53-95` | Entry name from `ze.getName()` is passed straight to `folder.resolve(fileName)` and `Files.newOutputStream` — **Zip Slip**. Reachable from JDK downloads (`TemurinProvider`, `AbstractDiscoJavaProvider`) and settings-backup restore. | Add `resolveSafeEntry(Path root, String name)`: normalize, reject absolute paths, `..` segments, Windows drive/UNC forms and backslashes; require `resolved.startsWith(root.toAbsolutePath().normalize())`. Reject symlinks in the parent chain. |
| SEC-02 | **P0 — DONE 2026-11-20** | `standalone/io/util/ZipUtils.java:181-240` (`unzip`) | Same Zip Slip via `outputFolder + File.separator + fileName`; reached by `ZipInstallerComponent` when installing `packaging=zip` artifacts. | Use the same shared helper as SEC-01. |
| SEC-03 | **P0 — DONE 2026-11-20** | `NUncompressZip.java:50-97`, `ZipUtils.java:197-240` | No limits on entry count, per-entry size, total uncompressed bytes or compression ratio — zip bomb / disk exhaustion. No cleanup of partially extracted files. | Configurable budgets (default e.g. 1 GiB total, 256 MiB per entry, 10k entries, ratio 200:1); check `ze.getSize()` and cumulative bytes while streaming; extract to a staging dir and move into place only on success; delete staging on failure. |
| SEC-18 | P1 — DONE 2026-11-20 | `workspace/cmd/settings/backup/NSettingsBackupSubCommand.java:117-148` | Restore target derived from untrusted `name` in the backup JSON without the usual `CoreNUtils.resolveValidWorkspaceName()` sanitization. | Validate the name and assert the resolved target is under the platform workspace root before extracting. Combine with SEC-01/02. |

### A.2 Credentials and secrets

| ID | Prio | Location | Issue | Fix |
|---|---|---|---|---|
| SEC-04 | **P0 — DONE 2026-11-20** | `repository/impl/nuts/NHttpSrvRepository.java:304-319` + URL building at 113/131/134/155/185/197/241/245 | `resolveAuthURLPart()` puts `ul=<user>&up=<password>` into URLs; full URLs are logged at 258 and in `DefaultNHttpClient` 507-521/526/597/601. Plaintext credentials in logs, proxies and server access logs; cleartext over `http://`. | Stop appending auth to URLs. Send an `Authorization` header over HTTPS only. Add central redaction (`user:pass@`, `ul=`, `up=`, `Authorization`) for all log/exception URL rendering. Rotate any credentials already exposed. |
| SEC-05 | **P0 — DONE 2026-11-20** | `repository/config/DefaultNRepositoryConfigModel.java:307-328`; `io/NReservedMonitoredURLInputStream.java:38,46,56,110-115,131-150` | Repository credentials embedded in URL userinfo, then logged and included in `"url not accessible " + url` exceptions. Affects all URL downloads via `NCoreIOUtils.openStream`. | Never materialize secrets into URLs; pass a secure token/header through the request layer. If a URL must temporarily carry userinfo, strip it before `log()`, `toString()` or exception messages. |
| SEC-06 | **P0 — DONE 2026-11-20** | `security/DefaultNWorkspaceSecurityModel.java:118-131, 138-145` | **Inverted/ineffective password check**: compares `evalSHA1(input)` (40-char hex) with `input` itself, so equality is impossible. `switchUnsecureMode()` accepts any password and disables secure mode; `switchSecureMode()` rejects every password. | Verify against the stored `NSecureToken` via `WrapperNAuthenticationAgent.verify()` / `verifyOneWay()` with constant-time comparison. Delete the self-comparison. Add tests for both directions. |
| SEC-09 — DONE 2026-11-20 | P1 | `xtra/web/DefaultNHttpClient.java:42-70`; `NHttpRequestImpl.java:700-709` | Basic auth auto-attached from any URL userinfo with no HTTPS/host allowlist; `.getBytes()` uses platform charset. | Attach credentials only for configured repository hosts and only over HTTPS; use `StandardCharsets.UTF_8`; inject auth from the security layer rather than deriving it from URL text. |
| SEC-10 — DONE 2026-11-20 | P1 | `io/util/CoreSecurityUtilsV1.java:47,78-85,133-154,192-206`; `DefaultNAuthenticationAgentV1.java:22-33` | Legacy agent still registered; AES/ECB with a single unsalted SHA-256 of a **hardcoded default passphrase** (`CoreSecurityUtils.java:7`). | Disable V1 by default, migrate stored secrets to V2 (PBKDF2 + AES-GCM), force rotation of secrets stored via V1 or `PlainNAuthenticationAgent`. |
| SEC-11 — DONE 2026-11-20 | P1 | `xtra/digest/NDigestUtils.java:63-93,119-142`; `security/util/CoreDigestHelper.java:27-32`; `io/NCoreIOUtils.java:130,164` | MD5/SHA-1 used for integrity/equality decisions and key derivation. | Switch trust decisions and key derivation to SHA-256/SHA-512 or HMAC; keep MD5 only where an external protocol requires it and never as a trust gate. |
| SEC-19 — DONE 2026-11-20 | P1 | `executor/system/ProcessBuilder2.java:261-263, 662-687, 711-747, 934-948` | Full command line and **all environment values** logged at FINEST and embedded in exceptions; secrets passed via `NExec.env()` leak. | Redact env by default (allowlist), never embed raw commands in exceptions, provide a debug-only opt-in. |

### A.3 Untrusted execution and downloads

| ID | Prio | Location | Issue | Fix |
|---|---|---|---|---|
| SEC-07 | **P0 — DONE 2026-11-20** | `workspace/cmd/service/DefaultInstallSvcCommand.java:150-166, 236-259, 290-317, 601-629` | `serviceName` from CLI is interpolated unquoted into root shell scripts (`systemctl ...`, `rm -Rf /etc/systemd/system/<name>.service`) run via `su -s /bin/sh root -c`. **Command injection as root.** | Validate name against `[A-Za-z0-9_-]+`; shell-escape every interpolated value; prefer direct `ProcessBuilder` over generated root scripts; strictly quote env template values. |
| SEC-08 | **P0 — DONE 2026-11-20 (JDK/tool download paths; segmented downloads have no checksum metadata and remain a follow-up)** | `util/jclass/TemurinProvider.java:133-139`, `AbstractDiscoJavaProvider.java:150-158`, `NHttpSrvRepository.java:245-247`, `NSegmentedHttpDownload.java:167-170` | Downloaded JDK/tool artifacts are not integrity-verified; checksums from remote APIs are read and discarded; same-server SHA-1 is not a trust anchor. Combined with SEC-01/03/12 this is a full supply-chain path to code execution. | Require SHA-256/signature from trusted metadata before extraction/execution; verify after download in `DefaultNCp`/`NSegmentedHttpDownload`; fail closed when expected checksum is absent. |
| SEC-12 — DONE 2026-11-20 | P1 | `reflect/JavaNTypeNameDomain.java:22-31`; `util/NTypeLoaderImpl.java:137-153`; `extension/ServiceTypeIterator.java:67-86` | `Class.forName(name, true, ...)` initializes static initializers for caller-supplied names; `setAccessible(true)` instantiation; services from downloaded artifacts loaded without isolation. | Use `Class.forName(name, false, loader)` where possible, validate type names, load third-party artifact code in a restricted loader/process. |

---

## 4. Workstream B — Correctness fixes

### B.1 P0/P1 — broken logic that must be fixed in Phase 1

| ID | Prio | Location | Defect | Fix |
|---|---|---|---|---|
| BUG-01 | **P0 — DONE 2026-11-20** | `repository/config/DefaultNRepositoryConfigModel.java:236-269, 333-337` | `resolveLocationWithAuth` dereferences `repository.uuid()/name()` even when `repository == null` (only `repoUuid`/`repoName` were derived) → NPE; outer `catch` logs at FINE and returns the unauthenticated URL. | Use the derived `repoUuid`/`repoName` locals, null-check the access list, log failures visibly and fail closed. |
| BUG-02 | **P0 — DONE 2026-11-20** | `workspace/cmd/settings/clinfo/NCliInfo.java:57-74` | `saveVar` writes **only when the value is unchanged** (`if (Objects.equals(old, value))`); real changes are silently dropped. | Invert to `if (!Objects.equals(old, value))`. Add a save/reload test. |
| BUG-03 | **P0 — DONE 2026-11-20** | `io/cache/DefaultCachedSupplier.java:202-230` | `getMemValue` uses `||` and compares `currentCacheId` to itself, so a nonblank cache id always sets `evaluated=false` — the in-memory cache never hits (also a major perf bug). | Mirror the file path at 131-133: `&&` and compare against `valueCacheId`. |
| BUG-04 | **P0 — DONE 2026-11-20** | `session/DefaultNSession.java:315-331` | `--progress` reads `NArg v` at 316 but uses stale `a` at 318/319/322/325/327; `progressOptions(s)` is called two or three times. | Use `v.getStringValue()`/`v.isNegated()` and call `progressOptions(s)` exactly once. |
| BUG-05 | P1 — DONE 2026-11-20 | `platform/CorePlatformUtils.java:442-481` | `isLoadedClassPath` returns false on the first `ClassNotFoundException`, producing false negatives when a jar has classes invisible to the loader. | Continue scanning; return true on the first successfully loaded class, false only after exhausting entries. |
| BUG-06 | P1 — DONE 2026-11-20 | `util/jclass/JavaJarUtils.java:201-211` | `--main-class Foo` registers `"--main-class"` as the entry because `i++` advances but `a` is reused; `--main-class=Foo` works. | Use `arguments.get(i)` after incrementing. |
| BUG-07 | P1 — DONE 2026-11-20 | `io/util/CoreSecurityUtilsV1.java:133-178` (live via V1 agent) | Encryption uses UTF-8, decryption uses platform default charset → corrupted non-ASCII secrets across platforms. | Decode with `StandardCharsets.UTF_8`; provide migration for already-corrupted legacy secrets (coordinate with SEC-10). |

### B.2 P2 — medium correctness and diagnostics

| ID | Location | Defect / fix |
|---|---|---|
| BUG-08 — DONE 2026-11-20 | `io/cache/DefaultCachedSupplier.java:146-157` | A value rejected by `validator.isValidValue(d)` is deleted from disk but still assigned and returned → do not return `d`; recompute. |
| BUG-09/10 — DONE 2026-11-20 | `text/highlighter/ShCodeHighlighter.java:363-388`, `FishCodeHighlighter.java:306-330` | Lookahead `p = all.get(i+1)` is computed but inner switch tests `resolveTokenType(n)` again → `SPACE`/`SEPARATORS` branches unreachable. Switch on `resolveTokenType(p)`. |
| BUG-11 — DONE 2026-11-20 | `repository/impl/nuts/NHttpSrvRepository.java:332-349` | `NamedNIdFromStreamIterator.source0` never assigned in constructor; `describe()` NPEs. Assign it or describe with a stored label. |
| BUG-12 — DONE 2026-11-20 | `io/util/CoreSecurityUtilsV1.java:169-177` | `(ch1|ch2|ch3|ch4) < 0` after `& 0xff` is unreachable; no length/bounds checks on truncated ciphertext. Validate `bytes.length >= 4` and `4L + v <= bytes.length`. |
| BUG-13 — DONE 2026-11-20 | `xtra/web/DefaultNHttpClient.java:42-55` | `URLDecoder.decode` treats `+` as space in userinfo; colonless userinfo produces Basic credentials without `:`. Decode percent-escapes only and append `":"` when needed. |
| BUG-14 — DONE 2026-11-20 | `format/tson/parser/custom/TsonCustomLexer.java:208-242` | `<` operator tokens carry wrong image/variant (`<=`, `<--`, `<-`, plain `<`); align with `NOperatorSymbol`. |
| BUG-15 — DONE 2026-11-20 | `workspace/config/DefaultNWorkspaceConfigModel.java:801-805` | `setBootRuntimeDependencies` is a no-op (body commented out) → implement like `setRuntimeId` (790-798). |
| BUG-16 — DONE 2026-11-20 | `repository/NRepositoryRegistryHelper.java:77-79` | Invalid bare `%` in CFORMAT message → change to `"%s / %s"`. |
| BUG-17 — DONE 2026-11-20 | `xtra/expr/DefaultNExprsCommonOps.java:536,562`; `elem/DefaultNElementNavigator.java:41-46`; `elem/item/DefaultNNumberElement.java:148-149`; `util/NTypeLoaderImpl.java:124-129` | CFORMAT strings with three placeholders but two args → `[ERROR] Invalid CFORMAT message`. Align placeholders/args; fix "method"→"field" wording. |
| BUG-18 — DONE 2026-11-20 | `text/DefaultNTextRPI.java:1035`; `workspace/NWorkspaceUtils.java:290-295`; `io/NCoreIOUtils.java:219-220` | Extra CFORMAT args silently drop exception/context text → add missing placeholders. |
| REL-01 — DONE 2026-11-20 | `io/util/ZipUtils.java:164` | `FileInputStream` in `addFileToZip()` never closed → fd exhaustion on large backups; use try-with-resources. |
| REL-02 — DONE 2026-11-20 | `security/util/CoreDigestHelper.java:105-108`; `io/NCoreIOUtils.java:139-142,186-196` | `Files.newInputStream` leaked in directory digest walks (one fd per file, reached at boot from `DefaultNWorkspace.java:959`); close in the `visitFile` callbacks. |
| REL-03 — DONE 2026-11-20 | `io/NReservedMonitoredURLInputStream.java:98-117` | `close()` throws on length mismatch and dereferences a possibly null log → never throw from `close()`; validate separately; null-safe logger. |
| REL-04 — DONE 2026-11-20 (error paths; successful lazy bodies still owned by the caller) | `xtra/web/DefaultNHttpClient.java:502-527, 584-595` | `uc.disconnect()` only on one-way responses → connection leak on errors; disconnect in `finally` when body ownership was not transferred. |
| REL-05 — DONE 2026-11-20 | `store/NWorkspaceStoreOnDisk.java:134-136,473-489`; `format/DefaultObjectWriterBase.java:223-230` | Config JSON written non-atomically → truncation/corruption on crash or concurrent processes; temp file + fsync + `ATOMIC_MOVE` under an inter-process lock. |
| REL-07 — DONE 2026-11-20 | `executor/system/ProcessBuilder2.java:99-122, 319-338, 369-388, 662-687` | PID discovery via JDK-internal reflection always returns -1 on JPMS; `PATH` redirect assigns `in.file` but passes `out.file`/`err.file` (null) to `Redirect.to` → NPE on process start. Use `Process.pid()`; fix the field assignments; test all `NRedirectType.PATH` combinations. |
| REL-08 — DONE 2026-11-20 | `security/WrapperNAuthenticationAgent.java:20,35-45`; `security/DefaultNWorkspaceSecurityModel.java:57,325-360`; `repository/impl/maven/util/MvnClient.java:16,36-68` | Plain `HashMap` auth caches and mutable `status` used from parallel solver/download threads → `ConcurrentHashMap.computeIfAbsent`, immutable cached values, `volatile`/atomic state. |
| REL-09 — DONE 2026-11-20 (already `volatile`; verified) | `util/NTypeLoaderImpl.java:82-101` | Non-volatile double-checked locking → make fields `volatile` or use an atomic holder. |
| REL-10 — DONE 2026-11-20 (fails loudly instead of silently downgrading) | `xtra/web/DefaultNHttpClient.java:630-661` | PATCH relies on JDK-internal reflection; on Java 17+ silently becomes POST → use `java.net.http.HttpClient` on 11+ or fail loudly. |
| — | `workspace/config/DefaultNWorkspaceConfigModel.java` (near `DefaultNWorkspace.java:116`) | Keep `RUNTIME_VERSION` constant in sync when bumping versions (see ARCH-04). |

---

## 5. Workstream C — Performance

The top five (PERF-01..05) are the high-ROI items; do them in Phase 2 with before/after measurements. The rest are low-risk, mostly local changes that can be batched.

| ID | Prio | Location | Cost / trigger | Fix |
|---|---|---|---|---|
| PERF-01 | **P0 — DONE 2026-11-20** | `workspace/DefaultNWorkspace.java:1498-1532`, helper at 968-988 | On **every** cached effective-descriptor lookup, recursively refetches every parent descriptor and computes SHA-1 over each. Warm-cache commands pay full descriptor work. | Cache the fingerprint keyed by descriptor identity/source path+last-modified+size; store it next to the cached effective descriptor so a hit is one map lookup. |
| PERF-02 | **P0 — DONE 2026-11-20** | `workspace/DefaultNWorkspace.java:1091-1098` | O(n·m) scan of `effStandardDeps` per old dependency, recomputing `shortName()` each comparison; every descriptor resolution. | Build `Map<String, NDependency>` keyed by `shortName()` once; constant-time lookups. |
| PERF-03 — DONE 2026-11-20 (keep-alive restored; full workspace-scoped client pooling deliberately deferred) | P1 | `xtra/web/DefaultNHttpClient.java:437,482,561,589` | New `HttpURLConnection` per request, `setUseCaches(false)`, explicit `disconnect()` → fresh TCP+TLS for HEAD/metadata/POM/.sha1/JAR per artifact. | Workspace-scoped pooled HTTP client (keep-alive); stop disabling the connection cache. |
| PERF-04 — DEFERRED 2026-11-20 (stateful client instances cannot be shared without an API change; keep-alive reuse from PERF-03 covers the main cost) | P1 | `io/path/spi/URLPath.java:421-423` | Extension discovery + scoring + sorting for `NHttpClient` on every `getInputStream()`. | Cache resolved client per protocol/host (reuse PERF-03 client). |
| PERF-05 — DONE 2026-11-20 | P1 | `extension/DefaultNWorkspaceExtensionModel.java:870-901`; `extension/NClassLoaderBase.java:642-660` | `synchronized` methods perform classpath resolution (network I/O) while holding the monitor → parallel fetches serialize. | Resolve outside the lock; synchronize only cache lookup/insert via `ConcurrentHashMap`. |
| PERF-06 — DONE 2026-11-20 | P1 | `format/xml/XmlUtils.java:214-271` from `repository/impl/maven/pom/NPomXmlParser.java:712-715` | `DocumentBuilderFactory.newInstance()` + JAXP service lookup per POM; hundreds per dependency graph. | One statically configured factory; per-parse builders. |
| PERF-07 — DONE 2026-11-20 | P1 | `repository/impl/maven/util/MavenMetadataParser.java:101,125,179` | `XMLInputFactory.newInstance()` and `new SimpleDateFormat` per metadata parse. | Static factory; `DateTimeFormatter` or `ThreadLocal<SimpleDateFormat>`. |
| PERF-08 — DONE 2026-11-20 | P2 | `xtra/ps/WindowsPsCsvParser.java:181`, `WindowsPs1Parser.java:162`, `PosixPsParser.java:326,331,341` | Pattern/`DateTimeFormatter` compiled per process row. | Hoist to `static final`. |
| PERF-09 — DEFERRED 2026-11-20 (per-process pipe executor is required to await pipe completion before process teardown; sharing needs lifecycle API changes) | P2 | `executor/system/ProcessBuilder2.java:431-533` | `Executors.newCachedThreadPool()` (9 sites) per spawned process, including JDK probes. | Share one bounded workspace executor for pipes; keep per-process futures only. |
| PERF-10 — DONE 2026-11-20 | P2 | `elem/mapper/NElementMapperObjReflect.java:201-242,286-311,332,361` | Reflective record metadata (`isRecord`, components, constructors, annotations) per deserialized element. | Cache `RecordComponentData[]` and constructors per `Class<?>` in a concurrent map. |
| PERF-11 — DONE 2026-11-20 | P2 | `workspace/IdCache.java:64-70` | Same class loaded once per extension point at bootstrap: c·e reflective attempts. | Load class once, then test `isAssignableFrom` per point. |
| PERF-12 — DONE 2026-11-20 | P2 | `executor/java/JavaExecutorComponent.java:221-233` | `Pattern.compile` per classpath entry at Java app startup (hundreds of entries). | `static final Pattern`. |
| PERF-13 — DONE 2026-11-20 | P2 | `repository/util/NRepositoryUtils.java:234,240` | Regex compiled per repo-location parse (twice, via `matches`). | Two `static final Pattern`s. |
| PERF-14 — DONE 2026-11-20 | P2 | `util/jclass/NJavaSdkUtils.java:707-731` | 13 regex compiles + `toLowerCase()` + `split()` per detected JDK path at startup. | Precompiled pattern array; lowercase once. |
| PERF-15 — DONE 2026-11-20 | P2 | `text/NMsgCFormatHelper.java:146-150` | `new Formatter(...)` per placeholder in every `NMsg.ofC` call, including hot loops. | Fast path for `%s` with String/`NText`; thread-local Formatter for the rest. |
| PERF-16 — DONE 2026-11-20 | P2 | `text/parser/AbstractNText.java:297-302` | `Pattern.quote(separator)` compiled per split; line regex per `splitLines()`. | Static line pattern; small bounded cache for quoted separators. |
| PERF-17 — DONE 2026-11-20 | P2 | `util/NStringBuilderImpl.java:1670-1671,1710` | `Pattern.compile` per replace; `data.toString()` full copy just to compare in `replaceFirst`. | Cache patterns; compare with `s.contentEquals(data)`. |
| PERF-18 — DONE 2026-11-20 | P2 | `format/yaml/YamlParser.java:991-1020` | `joinFolded` → `hasNonEmptyLineAfter` rescans the remainder per line: O(n²). | Precompute a suffix boolean in one pass. |
| PERF-19 — DONE 2026-11-20 | P2 | `io/path/spi/URLPath.java:310-331` | `exists()` performs a full GET when HEAD is unsupported. | Ranged GET (`bytes=0-0`) or treat 405/501 as "unknown" without a second request. |
| PERF-20 — DONE 2026-11-20 | P3 | `format/obj/RollingFileService.java:146-153,163-179` | `SimpleDateFormat` per rotated file on `roll()`/`load()`. | `DateTimeFormatter` or thread-local formatter. |
| PERF-21 — DONE 2026-11-20 | P2 | `workspace/DefaultNWorkspace.java:1594-1600` | `deployBoot` refetches the original boot id once per dependency. | Fetch each dependency once and cache by long id. |
| PERF-22 — DONE 2026-11-20 | P3 | `workspace/config/DefaultNIndexStore.java:207-215` | Unclosed `Scanner` on HTTP response in `isSubscribed()`; leaks connection. | Try-with-resources; read a single line/byte. |
| PERF-23 — DONE 2026-11-20 | P3 | `repository/impl/maven/NReservedMavenUtils.java:885-900,952-980` | Regex compiled in classpath loop; whole-POM regex scan per property lookup. | Static pattern; parse POM once into a map. |

### Measurement plan (run before and after Phase 2)

1. **End-to-end:** cold/warm workspace `nuts fetch`, `nuts install`, `nuts run`; record p50/p95 over ≥10 runs using the built-in timing in `DefaultNWorkspace.java:488-507` and `NWorkspaceProfilerImpl.debug()`.
2. **CPU/allocation:** JFR or async-profiler during a representative install; watch `Pattern.compile`, `DocumentBuilderFactory.newInstance`, `DateTimeFormatter.ofPattern`, `Class.getMethod`, SHA-1, `HttpURLConnection.getResponseCode`.
3. **Network:** count requests with a local proxy before/after PERF-03/04; expect a substantial drop in handshakes per artifact.
4. **Micro:** JMH for `resolveEffectiveDescriptor` (synthetic descriptor with 10–50 parents/deps), `parseMavenMetaData`, `XmlUtils.createDocumentBuilder`, PS-row parsing; assert ns/op and allocation-rate improvements.
5. **Concurrency:** multithreaded fetch/install stress with JFR `Java Monitor Enter` events to confirm the classloader monitor (PERF-05) no longer dominates.

---

## 6. Workstream D — Architecture, build and testability

| ID | Prio | Location | Issue / fix |
|---|---|---|---|
| ARCH-01 — **DONE 2026-11-21** | P1 | `src/main/java9/module-info.java`; both `META-INF/services` files; `nuts-boot/.../NBootWorkspaceImpl.java:1215` | The JPMS descriptor exports nothing and declares no `provides`; 155 `NComponent` providers + the boot factory are discovered only via `META-INF/services`, which named modules ignore. Module-path launch cannot boot. **Fixed:** generated `exports`/`uses`/`provides` in all three descriptors, patched internal RPI exports, and verified real module-path discovery in `JpmsSmokeTest`. |
| ARCH-02 — **DONE 2026-11-21** | P1 | `core/nuts-runtime/pom.xml:48-78` | Manifest says `Multi-Release: true` but `module-info.class` is at jar root. Set the compiler execution output to `META-INF/versions/9` or drop the manifest entry; verify with `jar --describe-module` / `jdeps --multi-release 9`. **Fixed:** descriptor moved to `META-INF/versions/9` in `prepare-package`, verified by `jar --describe-module --release 9` and `JpmsSmokeTest`. |
| ARCH-03 — **DONE 2026-11-21** | P2 | `pom.xml:26-29,39-40`; `optional/jansi/OptionalJansi.java`, `optional/mslink/OptionalMsLinkHelper.java` | Windows-only libs unconditional and "optional" wrappers contain direct bytecode references; `OptionalJansi.java:108-159` has dead `window=false` branch and an unreachable nested check. Consume or remove `nuts.windows-os-dependencies`; isolate Windows code behind reflection or a separate artifact. **Fixed:** both wrappers now use reflective loading only, dead install branch removed, bytecode-reference check added in `OptionalDependencyTest`. |
| ARCH-04 — **DONE 2026-11-21** | P2 | `pom.xml:8,12,36-38`; `parent/pom.xml:7`; `DefaultNWorkspace.java:116` | Mixed `1.2.0` / `1.2.0.0` versions hardcoded in several places. Introduce `nuts.api.version` / `nuts.impl.version` properties; generate or validate the Java constant; add an enforcer/test check. **Fixed:** properties added to `parent/pom.xml`, all `net.thevpc.nuts` dependency versions now reference them, and `BuildVersionConsistencyTest` validates the Java constants and forbids new literals. |
| ARCH-05 — **DONE 2026-11-21** | P3 | `run-javacc`, both `.jj` grammars, `compare-with-tson.txt`, `TsonCustomLexer.java` | Stale JavaCC assets: script hardcodes nonexistent paths, no Maven plugin, blog says JavaCC support dropped, lexer is hand-written. Delete the leftovers or make generation reproducible. **Fixed:** stale script, comparison notes and both grammars deleted; `StaleAssetsTest` guards against reintroduction. |
| ARCH-06 — **DONE 2026-11-21** | P2 | `META-INF/services/net.thevpc.nuts.spi.NComponent` | Manually maintained, contains a duplicate entry. Add a test that loads every class, asserts uniqueness and assignability; consider annotation-based generation. **Fixed:** duplicate removed and `ServiceRegistryIntegrityTest` added. |
| ARCH-07 — **DONE 2026-11-21** | P3 | `parent/pom.xml:112-138`, `installers/nuts-bundle-template/pom.xml:39` | Javadoc bound to `compile` with a JDK 8 toolchain requirement; missing antrun plugin version; obsolete source/target warnings. Move javadoc to `package`/`verify`, drop or document the toolchain, pin plugins. **Fixed:** javadoc moved to `package`, toolchain requirement removed, antrun version pinned in pluginManagement, obsolete-options warning suppressed; `BuildVersionConsistencyTest` checks the configuration. |
| ARCH-08 | P3 | `DefaultNWorkspace.java` (2,378 lines / 139 public methods), `DefaultNBootOptionsBuilder.java` (2,924 / 192), `DefaultNSession.java` (1,859 / 139), `DefaultNWorkspaceConfigModel.java`, `DefaultNWorkspaceOptionsBuilder.java`, `TsonCustomLexer.java` | God classes are change focal points and untestable in isolation. Split by responsibility behind facades **only after** characterization tests exist. |
| ARCH-09 | P2 | `standalone/**` import graph | 49 bidirectional package pairs (84 at 3-level); global service locator via `NWorkspaceExt.of()` (50 `NExtensions.of(` sites, 24 `NTypeLoaderImpl`, 10 `Class.forName`). Define allowed dependency direction, break cycles with lower-layer interfaces, inject collaborators, enforce with ArchUnit/jdeps in CI. |
| ARCH-10 — **DONE 2026-11-21 (reset hooks + lifecycle test; full per-workspace scoping deferred: requires API changes)** | P2 | `extension/NIdClassLoaderRegistry.java:75-127`, `NWorkspaceProfilerImpl.java:14`, `concurrent/DefaultFileNLock.java:101`, others | 33 mutable `static final` collections + 7 non-final mutable statics; classloader caches survive workspace recreation. Scope to workspace/session, add `resetForTests()` hooks, test two workspaces in one JVM. **Fixed:** `resetForTests()` added to the classloader registry and profiler; `StaticCacheLifecycleTest` verifies cache clearing and two workspaces in one JVM. Full per-workspace scoping needs API/signature changes and is out of scope for this pass. |
| ARCH-11 | P3 | 33 fully commented-out Java files (3,222 lines), e.g. `AbstractNUpdateUserCmd.java` (423), `NStreamDelegate.java` (375), `RemoteNWorkspace.java` (120) | Delete dead files (git preserves history); add a review/checkstyle rule banning large commented-out blocks. |
| ARCH-12 | P3 | `io/util/CoreIOUtils` vs `io/NCoreIOUtils`; two zip implementations; `CoreSecurityUtilsV1/V2`; 5 `NReserved*` files; `runtime/deprecated/`; 95 filter classes | Parallel stacks multiply the fix surface (SEC-01/02 and BUG-07 are direct consequences). Choose one implementation per concern, make others thin deprecated delegates, add `@Deprecated(since, forRemoval)`, genericize filter families, delete zero-reference files. |
| ARCH-13 | P3 | `licenseheader.txt`, 1,254 files without headers | Wire a license/checkstyle plugin to the template and backfill, or remove the template; document the rule in `documentation/repo/src/main/CONTRIBUTING.md` (root CONTRIBUTING.md is generated). |
| ARCH-14 | **P1** | `core/nuts-runtime/pom.xml` (no `src/test`, no test deps) | Zero unit tests for 1,539 sources; `mvn -pl core/nuts-runtime test` runs nothing. Add `src/test/java`, JUnit 5 + surefire (+ jacoco in a CI profile), enable in CI, keep black-box tests in `test/nuts-runtime-test`. |
| ARCH-15 | P1 | — | First unit tests to add: service-registry integrity (duplicate/stale/misassigned classes); module-path boot smoke test; `DefaultCachedSupplier` invalidation; `NCliInfo.saveVar`; `DefaultNSession` `--progress`; `DefaultNWorkspaceSecurityModel` secure-mode round trip; `resolveSafeEntry` Zip Slip cases; budget/zip-bomb limits; option-builder defaults/copy/serialization; TSON lexer token boundaries; filter families; `NPomXmlParser`; IO utils. |
| ARCH-16 | P3 — DONE 2026-11-20 (CI-breaking tests tagged; maintainer scripts documented) | `local-test-commands.md`, `scripts/test-commands.sh`, `test/nuts-runtime-test/runtime/` (~945 MB) | Machine-specific paths and a checked-in failing surefire report (`NIllegalArgumentException: not an extension: nuts-compression`). Make test scripts hermetic (temp workspaces, no hardcoded hosts) and document tags (`local-only`). |
| ARCH-17 | P3 | `CONTRIBUTING.md` vs `documentation/repo/src/main/CONTRIBUTING.md` | Generated docs drift (Java 8–24+ vs CI matrix 25; "add tests" but no test lane). Update the source docs and regenerate with `./nuts-release-tool`. |

---

## 7. Execution sequence

Effort estimates are in engineer-days and assume one developer familiar with the codebase; phases 1–3 can be parallelized across two developers by workstream (security/correctness vs performance).

| Phase | Timing | Contents | Exit criteria |
|---|---|---|---|
| **0 — Guardrails** | Days 1–2 | ARCH-14/15 (test scaffolding + first regression tests), baseline build + perf measurements from §5, branch/PR checklist requiring a test per fix. | `mvn -pl core/nuts-runtime test` runs green with the first ~10 unit tests; baseline perf numbers recorded. |
| **1 — P0 security & correctness** | Days 3–10 | SEC-01/02/03/04/05/06/07/08; BUG-01/02/03/04; REL-01 (fd leak); PERF-01/02 if time permits. | Zip Slip + bomb tests pass; no secrets in URLs/logs (grep + log-capture test); secure-mode round trip works; service-name validation test; `--progress` and `saveVar` regressions pass. |
| **2 — Performance** | Weeks 3–4 | PERF-03/04/05/06/07/11, then the remaining Pattern/formatter hoists (PERF-08/12/13/14/16/17/20) and PERF-09/10/15/18/19/21/22/23. | p50/p95 cold+warm fetch/install improved with no correctness regressions; request count per artifact reduced; JFR shows no `Pattern.compile`/`DocumentBuilderFactory.newInstance` in hot stacks. |
| **3 — Reliability & packaging** | Weeks 5–6 | SEC-09/10/11/12/19; REL-02/03/04/05/07/08/09/10; BUG-05/06/07 and P2 bug batch; ARCH-01/02/06. | fd/connection leak tests pass; atomic config writes verified under interruption; JPMS decision implemented and smoke-tested; services registry test green. |
| **4 — Structure & cleanup** | Weeks 7–10 | ARCH-03/04/05/07/09/10/11/12/13/16/17; characterization tests; then carefully split god classes (ARCH-08). | Package-cycle rule enforced in CI; dead files removed; duplicate implementations reduced; docs regenerated. |
| **Ongoing** | Each release | Re-run §5 measurements; update this plan's status column; rotate any secrets exposed by SEC-04/05. | No P0/P1 item without a linked test and release note. |

### Dependency notes

- SEC-06 depends on the authentication-agent API; coordinate with the SEC-10 V1 migration and secret rotation.
- SEC-08 depends on the shared `resolveSafeEntry` helper from SEC-01/02.
- PERF-03/04 should be designed once (workspace-scoped HTTP client) and then reused by SEC-04/05 and REL-04.
- ARCH-08 (god-class splitting) must come **after** ARCH-14/15 tests; otherwise the refactor is unverifiable.
- ARCH-01 (JPMS) is implemented: module-path launches are supported and smoke-tested by `JpmsSmokeTest`. Four constructor-injected components remain classpath-only because they cannot be `ServiceLoader` providers.

---

## 8. Suggested tracking status column

Add a `Status` column to the tables above when work starts: `Open` → `In progress` → `Fixed in <commit>` → `Verified by <test/benchmark>`. Every P0/P1 item must have a verification link before it can be closed. Re-assess quarterly; regenerate line references with a scripted grep so the tables do not go stale.

---

## Appendix A — Grep seeds for re-verification

```bash
cd core/nuts-runtime/src/main/java
# correctness markers
grep -RInE 'TODO|FIXME|XXX|HACK' . | head -50
grep -RIn 'catch *(.*) *{ *}' . | head -50
# security APIs
grep -RInE 'new FileInputStream|Files.newInputStream|new ZipInputStream|Runtime.getRuntime|Class.forName|setAccessible|URLDecoder|getBytes\(\)' . | head -80
# perf hot patterns
grep -RInE 'Pattern.compile|SimpleDateFormat|DateTimeFormatter.ofPattern|DocumentBuilderFactory.newInstance|XMLInputFactory.newInstance|new Formatter|String.matches' . | head -80
grep -RIn 'synchronized' . | head -80
# structural
find . -name '*.java' -printf '%s %p\n' | sort -rn | head -20
```
