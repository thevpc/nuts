# NTF / MTF Parser — Optimisation and Fix Plan

**Module:** `core/nuts-runtime` — package `net.thevpc.nuts.runtime.standalone.text.parser.v2`
**Assessment date:** 2026-11-24
**Status:** implemented — see §10 for the landed changes, deviations and measurements. The original plan text is kept unchanged below for reference.
**Verification class:** JDK 25 (`/usr/lib64/jvm/java-25-openjdk-25`), single machine, documentation corpus (83,739 chars). Ratios are stable across runs; absolute numbers move by a few percent.

> Line numbers refer to the tree at the assessment date and will drift. The durable reference is *file + method/symbol*.

---

## 1. Scope

In scope:

- `text/parser/v2/NTFParser2.java` — the default parser (`AbstractNTextNodeParserDefaults.createDefault()`)
- `text/parser/v2/MTFParser.java` — hybrid NTF + Markdown parser
- `collections/DefaultNCharQueue.java` — character queue, pattern matching, newline handling
- `text/AbstractNTextNodeParser.java` — `parseIncremental` entry points
- the `NText` construction path reached from the parsers (`NText.ofPlain` → `NTextRPI.of` → `NExtensions.of`)

Out of scope (separate workstreams, listed in §8):

- `NText` rendering / `NTextWriter` performance
- a full rewrite of the parser as a table-driven state machine
- JMH infrastructure in the build (the plan proposes a *separate* opt-in benchmark module, not a build change)

Non-goals: no public API change is required by any item in this plan. Every item is internal.

---

## 2. Architecture as found

```
NTextNodeParser
  └─ AbstractNTextNodeParser          // byte[]/char[]/String entry points, incremental feeding
       ├─ NTFParser2                  // default; StringBuilder buffer, recursive style stack
       └─ MTFParser                   // NTF + Markdown emphasis/backquote/source links
            └─ DefaultNCharQueue      // growable char[] window, peek/read, pattern probes
                 └─ NMultiPattern     // ordered pattern list, "best match" selection
```

Parsing is **incremental by design**: every probe can return `PARTIAL_MATCH`, which means "supply more input", and the parse suspends. That constraint is why a naive rewrite (match on the whole buffer at the end) is not acceptable — it would break streaming parsers and the terminal/text components that feed text chunk by chunk.

Two consequences drive the whole plan:

1. **Bulk reading must be incremental-safe.** Any fast path may only consume characters that are provably not the start of a construct, even when the input ends mid-token.
2. **Per-character work in the hot loop dominates everything else.** The parser spends its time in a `switch` over one character at a time with a `StringBuilder.append(char)` per plain character.

---

## 3. Baseline measurements

Corpus: 83,739 chars of real `.ntf` documentation; 404 `#`, 5,546 `` ` ``, 61 `[`. Timings are best-of-5 on warmed JVM.

| Scenario | Time | Throughput | Alloc / parse |
|---|---|---|---|
| `MTFParser` whole input (`readFully`) | **2.27 ms** | 37 MB/s | 2.2 MB (26× input) |
| `MTFParser`, 64-char chunks | 2.10 ms | 40 MB/s | — |
| `MTFParser`, 1-char chunks (worst case) | 6.4–7.0 ms | 12 MB/s | — |
| `NTFParser2`, 64-char chunks | **3.05 ms** | 27 MB/s | — |
| markup-free plain corpus, whole input | 1.27 ms | 66 MB/s | — |

Reference point: a plain `String.getBytes()` round trip over the same corpus is ~0.05 ms, so the parser is roughly 40–60× more expensive than a copy.

### JFR attribution (whole-input MTF parse)

| Share | Frame |
|---|---|
| **68 %** | `AbstractStringBuilder.append(char)` ← `MTFParser.readNode` (per-character `buffer.append(q.read())`) |
| 28 % | `NText.ofPlain` → `NTextRPI.of` → `NExtensions.of` → `createComponentOrNull` (char-by-char mode only) |
| 607 calls | `Pattern.compile` per MTF parse (`DefaultNCharQueue.pattern()` cache is commented out) |
| ~11 % alloc | bulk-patch variant allocated slightly *more* bytes than baseline (see O2 follow-up) |

So: one loop, one append, one regex compile per `#`. Everything else is noise at this corpus size.

---

## 4. Correctness defects found while benchmarking

These are not optimizations; they are bugs the optimisation work exposes. They should be fixed **before** the fast paths land, because the fast paths make it easy to cement the wrong behaviour.

### BUG-N1 (P0) — `NTFParser2.readFully()` returns only the first top-level node

`NTFParser2.read(boolean fully)` returns as soon as `stackedStyles` empties, instead of draining the queue.

- On the docs corpus it produced **35 of 83,739 characters** (first node only); the rest of the queue was silently dropped.
- When the queue is non-empty but the first `read()` yields `null`, `readFully()` can return `null` for non-empty input.

**Fix:** loop `read`-and-collect until the queue is exhausted, concatenating siblings at top level; keep `readFully()`'s contract as "the whole input as one `NText`". Add a regression test asserting `readFully().toString().length() == input.length()` for the corpus and for inputs with several top-level nodes.

### BUG-N2 (P1) — `AbstractNTextNodeParser.parseIncremental` round-trips through the platform charset

`parseIncremental(char[])` → `new String(buf)` → `parseIncremental(String)` → `buf.getBytes()` (default charset). Non-ASCII text is corrupted on any non-UTF-8 platform default, and every call copies the input three times.

**Fix:** feed the character path directly into the queue (`DefaultNCharQueue` is char-based already); use `StandardCharsets.UTF_8` for the byte→char decode. This also removes two full-buffer copies from the entry point — a free win for every incremental caller.

### BUG-N3 (P2) — `NTFParser2` TITLE mode ignores `fully`

`NTFParser2` newline handling calls `q.readNewLine(true)` in TITLE mode regardless of the caller's `fully` flag. In incremental mode a title line without a terminating newline is consumed as if complete.

**Fix:** pass `fully` through, mirroring the non-title branch.

### BUG-N4 (P2) — `DefaultNCharQueue.skipValue()` overruns the window

The partial-match loop bound is `i < to` while the compared index is `from + i`, so it can read past the live window (and past `content.length` if `to == content.length`). The correct bound is `to - from`. Currently dead code, but it is public API of the queue and will be used by O2's literal fast path.

**Fix:** `for (int i = 0; i < to - from; i++)`; add a unit test for partial-value matching across a sliding window.

---

## 5. Optimisation stages

Ordered by ROI. Each stage is independently shippable and independently verifiable. Numbers are **measured** on the scratch copies, not projected.

### O1 — Cache compiled patterns in `DefaultNCharQueue` (trivial, do first)

`DefaultNCharQueue.pattern(String)` compiles `"^" + pattern` on **every** probe; the memoised version is present but commented out. A whole-input MTF parse performs **607 compiles**.

```java
private Pattern pattern(String pattern) {
    return cachedPatterns.computeIfAbsent("^" + pattern, Pattern::compile);
}
```

- **Measured:** −13 % MTF whole-input.
- **Risk:** the map is unbounded. Bound it (the pattern set is effectively static and small — literals plus a handful of `#`/`` ` `` forms), or pre-seed it and make it immutable. If unbounded growth is a concern, cap at e.g. 256 entries and fall back to compiling.
- **Also:** hoist the constant patterns in `MTFParser.readSharp` (`"##:(?<n>…)[: ]"`, `"##\\{(?<n>…)[: ]"`, `SHARPS[level] + "($|[^#])"`) to `static final` where the level is fixed, and build the level-dependent one per level into a static array. `NTFParser2` builds a fresh `NMultiPattern` for every `#` — hoist it to a per-parser reused instance (reset per use) or a static one keyed by `fully`.

### O2 — Bulk plain-run scan (the main win)

**Idea:** in the `default:` branch of both parsers, when the current character is not a construct-introducing character, copy the maximal run of plain characters into the buffer in one call instead of one `append(char)` per character.

MTF specials: `#` `\` `` ` `` `*` `[` `}` `\u001E` `\r` `\n`
NTF2 specials: `}` `#` `\` `\u001E` `\r` `\n` `` ` ``

Shape (MTF `readNode` `default:` branch):

```java
default: {
    wasNewLine = false;
    if (skipTitleBlank) { /* unchanged single-char handling */ }
    int start = q.from();               // window accessor
    int end = start;
    char[] buf = q.content();           // window accessor
    while (end < q.to() && !isSpecial(buf[end])) end++;
    if (end == start) { buffer.append(q.read()); }
    else { buffer.append(buf, start, end - start); q.skip(end - start); }
}
```

The trailing character is **not** consumed, so a construct that starts at the end of the available input still yields `PARTIAL_MATCH` on the next probe — the incremental contract is preserved. This is the whole reason the fast path is safe: the special set is a *superset* of every construct prefix, and the first non-plain character is always handled by the existing `switch`.

- **Measured:** MTF whole 2.27 → 0.83–0.96 ms (**2.4–2.7×**); MTF 64-chunks 2.10 → 0.81 ms (**2.6×**); markup-free whole 1.27 → 0.30 ms (**4.2×**); NTF2 64-chunks 3.05 → 1.05 ms (**2.9×**); NTF2 plain whole 2.16 → 0.34 ms (**6.3×**).
- **Behaviour:** verified identical by differential fuzzing (§6.1).
- **Follow-up (O2b):** the patch's `append(char)` replacement increased allocation ~11 % because the run scan touches `q` accessors. Two mitigations to evaluate: expose a single package-private `appendPlainTo(StringBuilder)` on the queue (one call, no intermediate), and `q.readRun(char[] dst, int off, int len)` so the copy is `System.arraycopy` into the builder's own array. Measure allocation before claiming the regression is gone.

### O3 — `StringBuffer` → `StringBuilder` in `NTFParser2`

`NTFParser2` uses a `StringBuffer` (synchronised, per-append lock) while `read()` is already `synchronized (q)`. All appends happen under that monitor, so the per-append synchronisation is redundant.

- **Measured:** NTF2 64-chunks 1.05 → 0.98 ms (**~7 %** after O2). Smaller than O2 but free.
- **Risk:** confirm no other thread touches `buffer` outside the `q` monitor (grep for `buffer` — it is private and only used inside `read`/`consumeBuffer`/`resetBuffer`). If any path escapes, keep `StringBuffer`.

### O4 — Stop the per-node `NText.ofPlain` / `NExtensions.of` storm

In char-by-char incremental mode, 28 % of samples are in `NText.ofPlain` → `NTextRPI.of` → `NExtensions.of` → `createComponentOrNull`, i.e. the parser resolves the `NText` component **once per emitted node**, and flushes a node per `read()`.

Two changes:

1. **Cache the resolved RPI** in a static/instance field instead of resolving through the extension registry per node (`NTextRPI.of()` → memoised supplier).
2. **Coalesce flushes** in incremental mode: accumulate adjacent plain nodes and emit when a construct completes or when the caller asks, rather than one `NText` per `read()`.
3. **Reuse the buffer** rather than `new String(...)`-ing every node — `consumeBuffer` currently materialises a copy each time.

- **Expected:** large for char-by-char and for incremental consumers; negligible for whole-input. Measure with the 1-char-chunk scenario, where the baseline is 6.4–7.0 ms (12 MB/s) and there is no bulk-run scan to help.
- **Risk:** node identity changes if consumers rely on one node per chunk. Check `NTextNodeCollector` and the terminal/`NTextWriter` consumers before changing emission granularity; if that is unsafe, ship item 1 only (pure cache, no observable change).

### O5 — `MTFParser` reuse in emphasis parsing

`readEmphasis` constructs a new `MTFParser` per emphasis fragment (`parseFragment`), paying queue setup and pattern-cache warm-up each time. Reuse a single nested-parser instance (or a small pool) reset per fragment.

- **Expected:** visible on emphasis-heavy input; small on the docs corpus.
- **Risk:** the nested parser must be fully reset (buffer, styles, newline state). Test with inputs where an emphasis contains an unterminated construct.

### O6 — Micro-cleanups in the same files (batch, low risk)

- `readInlineCode` performs two `substring`s where one would do.
- `SHARPS[level] + "($|[^#])"` built per `#` — precompute per level.
- `peekString` instead of a regex for literal patterns (`"```"`, `"}##"`, `"\n"`-forms) — `skipValue`/`peekString` is already available (and fixed by BUG-N4).
- Avoid the double `NTextStyles.parse(...)` when a style sequence matches but fails to parse (append the raw string and continue, as today, but without re-parsing).

### O7 — Optional: queue growth policy

`DefaultNCharQueue.ensureAvailable` grows geometrically already; a variant with a larger initial window and `System.arraycopy` compaction was tried.

- **Measured:** marginal — chunk-64 and whole unchanged; char-by-char 7.0 → 6.8 ms. **Not the bottleneck.** Skip unless O4 lands and the profile says otherwise.

### Cumulative result (measured, A = O1, B = A+O2, C = B+O3)

| Scenario | Baseline | A (patterns) | B (+bulk) | C (+`StringBuilder`) | Speed-up C |
|---|---|---|---|---|---|
| MTF whole | 2.27 ms | 1.97 | 0.96 | 0.89 ms | **2.6×** |
| MTF 64-chunks | 2.10 ms | — | 0.81 | 0.81 ms | **2.6×** |
| NTF2 64-chunks | 3.05 ms | — | 1.05 | 0.98 ms | **3.1×** |
| MTF chunked throughput | 40 MB/s | — | — | **94 MB/s** | 2.4× |
| NTF2 chunked throughput | 27 MB/s | — | — | **91 MB/s** | 3.4× |

Resulting throughput (~90 MB/s on chunked input, 2.5–3× baseline) makes the parser comparable to a UTF-8 decode pass rather than dominating text handling.

---

## 6. Verification protocol

Nothing in §5 may be merged on benchmark numbers alone.

### 6.1 Differential fuzzing (mandatory gate for O1–O3)

The existing harness generates snippets and dumps the parse tree recursively as `type[filteredText]`, one line per node.

- Corpus: 6,000 generated snippets covering plain runs, nested `#`/`##` styles, composite `##{...}`, titles, inline code, backquote fences, emphasis, `[source]`, escapes, `\u001E`, CR/LF/CRLF, and truncated constructs at the buffer edge.
- Matrix: × {whole, chunk-1, chunk-3} × {MTFParser, NTFParser2} = **36,000 parses** per variant.
- Output: 42,000 lines per variant; `fuzz-orig.txt` vs `fuzz-B.txt` / `fuzz-C.txt` were **byte-identical**.

Every new optimization must extend the generator with at least: a run ending exactly at a buffer boundary, a special character as the last char of a chunk, and a partial `##` at EOF.

### 6.2 Existing tests

From `test/nuts-runtime-test`:

```bash
mvn -o -q test -Dtest='MTFParserTest,NTFParser2Test'
```

Baseline: **83 + 4 tests, 0 failures.** Must stay green after every stage. Add regression tests for BUG-N1..N4 (a test each; BUG-N1 is the one currently absent and the most consequential).

### 6.3 Benchmarks

Ad-hoc harness (not JMH) against `target/classes`, with the patched variant's output directory **first** on the classpath. Bootstrap requirement: `NText` needs a workspace —

```java
System.setProperty("nuts.args", "");
Nuts.openWorkspace("--workspace=<abs>/target/bench-ws", "--standalone", "--!switch",
    "--!init-platforms", "--!init-scripts", "--!init-launchers", "--!progress",
    "--yes", "--skip-welcome", "--shared-instance");
```

Classpath: `test/nuts-runtime-test/target/cp.txt` (from `mvn -o dependency:build-classpath -DincludeScope=test`) plus `nuts-api`, `nuts-boot`, `nuts-runtime` `target/classes` and `target/test-classes`.

Pitfall seen once: a stale `NTFParser2.class` in the variant directory made NTF look unimproved. Verify with

```bash
javap -p -c -cp <variant-dir> net.thevpc.nuts.runtime.standalone.text.parser.v2.NTFParser2 | grep -c peekAt
```

before trusting a number.

**Long-term:** promote the harness to a JMH module behind a profile (`-Pbench`) so regressions are detectable in CI-optional runs, alongside the existing guardrails in `NUTS-RUNTIME-FIX-PLAN.md` (RT-PERF-03).

### 6.4 Acceptance criteria

| Stage | Gate |
|---|---|
| O1 | fuzz byte-identical; tests green; ≥10 % MTF whole-input |
| O2 | fuzz byte-identical **including chunk-1 and chunk-3**; tests green; ≥2× MTF whole-input; allocation not worse than baseline (or O2b closes the gap) |
| O3 | fuzz byte-identical; tests green; no `StringBuffer` reference left in `NTFParser2` |
| O4 | tests green; node-emission semantics documented and unchanged unless explicitly approved; char-by-char ≥20 % |
| O5–O6 | tests green; no regression on the O2 numbers |
| BUG-N1..N4 | a failing-then-passing test per bug |

---

## 7. Sequencing and effort

| Step | Contents | Effort | Depends on |
|---|---|---|---|
| 0 | Re-run baseline + fuzz harness on the current tree; record numbers in this file | 0.5 d | — |
| 1 | **BUG-N1** (`readFully`) + test; **BUG-N2** (charset) + test; **BUG-N4** (`skipValue`) + test | 1 d | 0 |
| 2 | **O1** pattern cache (+ bound), hoist `MTFParser` constant patterns | 0.5 d | 1 |
| 3 | **O2** bulk run scan in both parsers + accessors on `DefaultNCharQueue`; extend fuzz generator | 2 d | 1 |
| 4 | **O2b** allocation follow-up (`appendPlainTo` / `readRun`) | 0.5 d | 3 |
| 5 | **O3** `StringBuilder` | 0.25 d | 3 |
| 6 | **BUG-N3** TITLE `fully` + test | 0.25 d | 3 |
| 7 | **O4** RPI cache, then (separately approved) flush coalescing | 1–2 d | 2 |
| 8 | **O5/O6** micro-cleanups | 1 d | 3 |
| 9 | Promote harness to `-Pbench` JMH module (optional) | 1 d | 8 |

Total: ~7–8 engineer-days for the core (steps 0–8), of which steps 2–5 deliver the measured 2.5–3× and are ~3 days.

Recommended landing order as **separate commits**: (a) BUG-N1/N2/N4 + tests; (b) O1; (c) O2 + O3 + BUG-N3 + fuzz extension; (d) O4; (e) O5/O6. Each commit carries its own before/after numbers in the message.

---

## 8. Risk register and exclusions

| Risk | Mitigation |
|---|---|
| Bulk scan changes incremental semantics (consumes a construct prefix) | Special set is a strict superset of construct prefixes; only consume up to the first special; chunk-1/chunk-3 fuzz is a release gate |
| Pattern cache grows unbounded from caller-supplied patterns | Cap the map (256) and fall back to `Pattern.compile`; the parser's own pattern set is static |
| Node emission granularity change breaks consumers (O4) | Ship the RPI cache first (no observable change); require explicit approval + a consumer audit for coalescing |
| Micro-optimisations bit-rot against future parser changes | Each is guarded by the differential fuzz corpus, which is checked in with the tests |
| Benchmark corpus is documentation-shaped, not terminal-shaped | Add a second corpus of ANSI/terminal text and one of TSON-ish markup before claiming end-to-end wins |

Explicitly **not** in this plan:

- rewriting the parser as a generated/table-driven lexer (correct long-term answer if the grammar keeps growing, but a separate design effort);
- caching `NText` parse results at the API level (different layer, different lifetime problems);
- changing the `NText` public API;
- touching the renderer.

Deliberately deferred elsewhere: the broader runtime items in `ASSESSMENT-AND-FIX-PLAN.md` (PERF-04/09) and the ARCH-08 god-class split.

---

## 9. Appendix — key locations

| File | Symbol | What to know |
|---|---|---|
| `text/parser/v2/NTFParser2.java` | `StringBuilder/StringBuffer buffer` | append-per-char hot loop; `StringBuffer` today |
| | `read(boolean fully)` | returns on first finished node → BUG-N1 |
| | newline branch, TITLE mode | `readNewLine(true)` → BUG-N3 |
| | `#` branch | new `NMultiPattern` + pattern strings per occurrence |
| `text/parser/v2/MTFParser.java` | `readNode` `default:` branch | the 68 % hotspot (`buffer.append(q.read())`) |
| | `readSharp` | per-`#` pattern construction (`simplePattern`, `compositePattern`, `closePattern`) |
| | `readEmphasis` / `parseFragment` | new nested `MTFParser` per fragment |
| | `consumeBuffer` | materialises a `String` per node |
| `collections/DefaultNCharQueue.java` | `pattern(String)` | cache present but commented out — O1 |
| | `skipValue(String)` | wrong loop bound — BUG-N4 |
| | `ensureAvailable` | growth policy — O7, marginal |
| `text/AbstractNTextNodeParser.java` | `parseIncremental(char[]/String)` | default-charset round trip, three copies — BUG-N2 |
| `nuts-api/.../internal/rpi/NTextRPI.java` | `of()` | `NExtensions.of(NTextRPI.class)` per node — O4 |

### Useful greps

```bash
cd core/nuts-runtime/src/main/java/net/thevpc/nuts/runtime/standalone
grep -n 'append(q.read())'            text/parser/v2/*.java
grep -n 'Pattern.compile'             collections/DefaultNCharQueue.java
grep -n 'readNewLine'                 text/parser/v2/*.java
grep -rn 'getBytes()'                 text/AbstractNTextNodeParser.java
grep -n 'SHARPS'                      text/parser/v2/MTFParser.java
```

---

## 10. Implementation status (2026-11-24)

Landed in this change set:

| Item | Status | Notes |
|---|---|---|
| BUG-N1 `NTFParser2.readFully` | fixed | drains every top-level node through `NTextNodeCollector`; returns `null` only for empty input (preserves the existing `NTFTest.test29` contract). `parseRemaining` now loops on `read(true)` so no node is dropped. |
| BUG-N2 charset round trip | fixed | `char[]`/`String` overloads feed characters directly; the `byte[]` overload decodes with `StandardCharsets.UTF_8`. |
| BUG-N3 TITLE `fully` | fixed | `readNewLine(fully)` in TITLE mode; the optional blank after `#)` is deferred through `skipTitleBlank` when it falls in the next chunk. |
| BUG-N4 `skipValue` | fixed | compares `min(value.length(), length())` characters and returns `FULL_MATCH` when the value ends exactly at the window end. |
| O1 pattern cache | done | shared, bounded (`256`) `ConcurrentHashMap`; `MTFParser` style/title/close patterns hoisted to constants and a per-level array. |
| O2 bulk plain-run scan | done | both parsers copy a whole plain run with a single `StringBuilder.append(char[], int, int)`; `DefaultNCharQueue.content()` exposes the live window. This is the O2 + O2b form (no intermediate allocation). |
| O3 `StringBuilder` | done | `NTFParser2` uses `StringBuilder`; all access is already under the queue monitor. |
| O4 RPI cache | done (item 1 only) | the resolved `NTextRPI` is cached per parser and cleared by `reset()`; node-emission granularity is unchanged. |
| O5 fragment parser reuse | done | `MTFParser.parseFragment` uses a per-parser pool so recursive emphasis fragments are safe. |
| O6 micro-cleanups | done | constant patterns hoisted, O2b allocation path used, queue accessors added. The NTF2 backtick probe stayed `peekPattern` on purpose: `peekString` would treat an exact chunk-final `` ``` `` as complete and change incremental semantics. |
| O7 queue growth | skipped | not the bottleneck, as the plan predicted. |

Additional incremental correctness fixes found by the differential harness:

- `NMultiPattern` / `doWithPattern`: when input is not final, a `PARTIAL_MATCH` now suspends parsing even if another pattern currently matches. Previously `##:` at a chunk boundary was consumed as an opening `##` primary style.
- `DefaultNCharQueue.readNewLine`: a lone `\r` at the end of a non-final chunk is no longer consumed, so a split CRLF is preserved.
- `NTFParser2` backtick and escape partial branches no longer consume the pending character.
- `skipTitleBlank` is handled at the top of the parser loop (both parsers), so a special character immediately after `#)` cannot leave the flag armed.

Verification:

- `NTFTest, MTFParserTest, NTFParser2Test, DefaultNCharQueueTest, NMultiPatternTest`: green.
- Differential fuzz, 6,000 generated inputs × {whole, chunk-1, chunk-3} × {MTF, NTF}: all MTF whole-input results are byte-identical to the original; NTF whole-input results now contain the trailing nodes the old `readFully` dropped. A whole-vs-chunk consistency check on the same corpus dropped from 13,541 mismatches on the original tree to 14 (remaining cases involve malformed escaped fences/mixed emphasis delimiters at chunk edges).
- Ad-hoc benchmark on a 109 kB NTF documentation corpus (JDK 25, best-of-20): MTF whole-input ≈ 2.9 ms → ≈ 1.1 ms (**2.6×**); NTF chunk-64 ≈ 4.0 ms → ≈ 1.2 ms (**3.2×**). NTF whole-input is not comparable to the baseline because the baseline stopped after the first node.
