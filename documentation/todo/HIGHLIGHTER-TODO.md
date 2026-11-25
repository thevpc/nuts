# Code Highlighter — TODO and Explicit Plan

**Module:** `core/nuts-runtime`
**Package:** `net.thevpc.nuts.runtime.standalone.text.highlighter`
**Created:** 2026-11-25
**Status:** active roadmap. Completed items are recorded in §2; open items are in §4–§8.

> Task ids are stable. Use them in commit messages, for example
> `[CHANGED] highlighter: H-011 add extension alias registry`.

---

## 1. Scope and constraints

### In scope

- `NCodeHighlighter` implementations in `core/nuts-runtime/.../text/highlighter/`
- highlighter lookup, aliases and extension/MIME resolution
- bundled theme entries for syntax styles
- keyword resources under `core/nuts-runtime/src/main/resources/net/thevpc/nuts/runtime/highlighter/`
- highlighter tests under `test/nuts-runtime-test/src/test/java/net/thevpc/nuts/runtime/standalone/text/highlighter/`

### Out of scope

- replacing the NTF/MTF parsers (covered by `NTF-PARSER-OPTIMIZATION-PLAN.md`)
- a full TextMate/tree-sitter grammar engine
- editor-specific syntax files (`NSysEditorSupportCmd` sources)

### Non-negotiable rules

1. **Round-trip fidelity.** `highlighter.stringToText(input).filteredText()` must equal `input`, including CRLF, trailing whitespace and unterminated constructs.
2. **No hangs.** Every lexer must terminate on arbitrary input, including unterminated strings/comments and lone delimiters.
3. **No workspace dependency in lexers.** Only `NText` construction may use the workspace; parsing itself must be pure.
4. **Additive aliases.** New MIME/extension aliases must not steal an id or MIME already owned by another highlighter.
5. **Prefer the shared base.** New simple languages should extend `AbstractCodeHighlighter`; only genuinely different grammars get a dedicated lexer.
6. **Every new language gets tests** for registration, round-trip, and at least keyword/string/comment styling.

---

## 2. Baseline — landed as of 2026-11-25

### 2.1 New highlighters

| Id | Class | Notes |
|---|---|---|
| `scala` | `ScalaCodeHighlighter` | nested comments, triple/interpolated strings, char/symbol literals, annotations |
| `ntx` | `NtxCodeHighlighter` | ntexup `.ntx` documents; inherits `NTexupCodeHighlighter` |
| `kotlin` | `KotlinCodeHighlighter` | base-class based |
| `go` | `GoCodeHighlighter` | raw strings, runes, `nil`, `iota` |
| `ruby` | `RubyCodeHighlighter` | `#{}` interpolation, symbols, `@`/`@@`/`$` variables |
| `php` | `PhpCodeHighlighter` | tags, attributes, `$variables`, heredoc/nowdoc |
| `swift` | `SwiftCodeHighlighter` | `\(...)` interpolation, `#if`, nested comments |
| `toml` | `TomlCodeHighlighter` | tables, literal strings, dates |
| `ini` | `IniCodeHighlighter` | INI and `.properties` |
| `dockerfile` | `DockerfileCodeHighlighter` | instructions and variables |
| `makefile` | `MakefileCodeHighlighter` | targets, assignments, `$(...)` |
| `hcl` | `HclCodeHighlighter` | Terraform/HCL |
| `css` | `CssCodeHighlighter` | CSS/SCSS/SASS/LESS |

### 2.2 Fixes already landed

- **XML:** dedicated lexer; closing tags, comments, CDATA, DOCTYPE, PIs, entities.
- **ntexup:** built-in nodes (`primary1`), bundled-theme components (`primary5`), user components (`primary6`), properties/values.
- **TSON/ntexup:** `@annotations`, `null`, whitespace-aware previous-token lookup.
- **Java:** text blocks, annotations, modern keywords.
- **C++:** real C++ keyword resource (previously a Java keyword copy), preprocessor directives.
- **C#:** verbatim/interpolated/raw strings, modern keywords.
- **SQL:** `sql.kw2` now used for types, `--`/`#` comments, backtick identifiers.
- **JSON:** `null`, JSONC/JSON5 aliases.
- **Shell/CMD:** corrected PowerShell aliases, extra MIME aliases.
- **Registry:** lowercase normalization in `addCodeHighlighter`/`removeCodeHighlighter`.
- **Themes:** `COMMENTS`, `ANNOTATION`, `ENTITY`, `ACTION` styles added to `ansi`, `grass`, `horizon`, `whiteboard`.

### 2.3 Shared infrastructure landed

- `AbstractCodeHighlighter` — generic lexer base for small languages.
- `NCodeHighlighterHelper.score(...)` — shared static score helper.
- `JavaCodeHighlighter`, `CppCodeHighlighter`, `SqlCodeHighlighter` migrated to the base class.
- Tests:
  - `ScalaCodeHighlighterTest`
  - `NtxCodeHighlighterTest`
  - `HighlighterFixesTest`
  - `NewLanguagesCodeHighlighterTest`

---

## 3. Milestones and sequencing

| Milestone | Contents | Exit criterion |
|---|---|---|
| **M1 — guardrails** | H-001…H-005 | every registered highlighter has round-trip/no-hang coverage |
| **M2 — infrastructure** | H-010…H-016 | adding a language takes one class + keyword file + test |
| **M3 — common languages** | H-020…H-026 | PowerShell, Groovy/Gradle, Lua, R, Perl, Dart shipped |
| **M4 — ecosystem languages** | H-027…H-031 | Haskell, Elixir, Clojure, GraphQL/Protobuf, diff/patch shipped |
| **M5 — edge-case correctness** | H-040…H-048 | documented gaps closed or explicitly accepted |
| **M6 — performance** | H-050…H-052 | benchmarks recorded in this file; no pathological input |

Do not start M3 before M1 is green. Migrations in M2 may proceed in parallel with M1, but each migration must keep the M1 tests green.

---

## 4. M1 — Regression guardrails (P0)

### H-001 — All-highlighter round-trip and no-hang suite

- [ ] Add `AllHighlightersSmokeTest`.
- [ ] Enumerate `NCodeHighlighter.codeHighlighters()`.
- [ ] For each highlighter, feed a corpus of:
  - empty, null, whitespace-only
  - CRLF and LF text
  - trailing newline / no trailing newline
  - strings with escapes, quotes, backslashes
  - block and line comments
  - unterminated `"`, `'`, `` ` ``, `/*`, `<!--`, `#{`, `${`
  - random ASCII strings (fixed seed)
- [ ] Assert `filteredText().equals(input)` (exclude `ntf` and `mtf`, which intentionally transform text).
- [ ] Assert completion under a per-call timeout.
- **Files:** new test only.
- **Done when:** all highlighters pass; any exemption is documented in the test with a reason.

### H-002 — `tokenToText` contract tests

- [ ] Add a test that calls `tokenToText(text, nodeType)` for every highlighter and every documented nodeType.
- [ ] Verify no exception, no null, and expected style for nodeType-based highlighters (`xml`, `hadra`, `plain`).
- [ ] Decide and document whether highlighters that currently return plain text must keep doing so or should implement node styling.
- **Files:** new test; `NCodeHighlighter` javadoc if the contract is clarified.
- **Done when:** contract is explicit and tested.

### H-003 — ntexup node-category regression

- [ ] Snapshot the built-in node list against `NTxNodeType` constants in the ntexup repository.
- [ ] Add a test that a sample of built-ins, theme components, control words and user-defined components resolve to the intended styles.
- [ ] Add a maintenance note: when ntexup adds a core node or a bundled theme adds a component, update `NTexupCodeHighlighter.defaultNodes` / `themeComponents`.
- **Files:** `NtxCodeHighlighterTest`, `NTexupCodeHighlighter`.
- **Done when:** category drift is visible in a failing test rather than a silent style change.

### H-004 — XML conformance cases

- [ ] Add tests for:
  - namespace names (`ns:tag`, `xml:lang`)
  - attributes with single/double quotes and entities
  - self-closing tags
  - comments containing `>` and `<`
  - CDATA containing `<tag>`
  - DOCTYPE with internal subset
  - processing instructions
  - malformed/unclosed tags
- **Files:** `HighlighterFixesTest` or a dedicated `XmlCodeHighlighterTest`.
- **Done when:** all cases round-trip and closing tag names are `keyword`.

### H-005 — Theme style coverage test

- [ ] Assert every bundled `.ntf-theme` resolves `COMMENTS`, `ANNOTATION`, `ENTITY`, `ACTION`, `VAR`, `KEYWORD`, `OPTION`, `NUMBER`, `BOOLEAN`, `STRING`, `SEPARATOR`, `OPERATOR` to at least one basic style.
- [ ] Load themes through the normal `NTextTheme` path, not by parsing the files manually.
- **Files:** new test.
- **Done when:** adding a new style type without a theme mapping fails the build.

---

## 5. M2 — Infrastructure (P1)

### H-010 — Explicit alias / extension registry

- [ ] Define one authoritative alias table: id, MIME types, extensions, filenames.
- [ ] Decide whether to activate `highlighter-mappings.ini` or replace it with a typed registry class.
- [ ] Include at minimum:
  - `.scala`, `.sc`
  - `.ntx`
  - `.kt`, `.kts`
  - `.go`
  - `.rb`, `Gemfile`, `Rakefile`
  - `.php`, `.phtml`
  - `.swift`
  - `.toml`
  - `.ini`, `.cfg`, `.conf`, `.properties`
  - `Dockerfile`, `Containerfile`
  - `Makefile`, `.mk`
  - `.tf`, `.tfvars`
  - `.css`, `.scss`, `.sass`, `.less`
- [ ] Ensure exact id lookup wins over scoring; ensure equivalent MIME aliases cannot tie.
- [ ] Add resolution tests for every alias.
- **Files:** `DefaultNTextManagerModel`, `NCodeHighlighterHelper`, `highlighter-mappings.ini` (or replacement), tests.
- **Done when:** `NCodeHighlighter.of(".scala")` and `NCodeHighlighter.of("text/x-scala")` both resolve to `scala`.

### H-011 — Migrate `CSharpCodeHighlighter` to the base class

- [ ] Move keyword set to `csharp.kw1` (already done) and remove any duplicated list.
- [ ] Keep verbatim/interpolated/raw string support via base hooks.
- [ ] Add tests for `@"..."`, `$"..."`, `$@"..."`, `"""..."""`.
- **Files:** `CSharpCodeHighlighter`, base hooks if needed, tests.
- **Done when:** no duplicated main lexer loop remains.

### H-012 — Migrate `JsonCodeHighlighter` and `HadraCodeHighlighter`

- [ ] JSON: configure base for `//`, `/* */`, `'` strings, booleans, `null`; preserve negative-number handling as one number token.
- [ ] Hadra: move keyword resource loading and identify whether it needs custom string/comment handling.
- [ ] Keep JSONC/JSON5 aliases.
- **Files:** `JsonCodeHighlighter`, `HadraCodeHighlighter`, base hooks if needed, tests.
- **Done when:** both use the shared base or have a documented reason not to.

### H-013 — Keyword resources for all languages

- [ ] Move remaining inline sets to `.kw` resources:
  - Scala keywords/types
  - Kotlin keywords/types
  - Go keywords/types
  - Ruby keywords/types
  - PHP keywords/types
  - Swift keywords/types
  - HCL keywords/types
  - CSS properties
- [ ] Keep one word per line, `#` comments, trimmed loading.
- [ ] Add a test that every expected resource exists and is non-empty.
- **Files:** `src/main/resources/.../highlighter/*.kw*`, highlighter classes, `NCodeHighlighterHelper`.
- **Done when:** language vocabulary can be updated without touching Java code.

### H-014 — Complete the shared lexer options

- [ ] Add explicit options for:
  - line-comment prefixes per language
  - block-comment nesting
  - raw/triple strings and whether escapes apply
  - per-quote interpolation rules (single vs double quote)
  - variable prefixes (`$`, `@`, `@@`, custom)
  - operators that are also separators (`:`, `.`)
- [ ] Split `readNumber` into integer/float/date-friendly variants instead of one option bag.
- [ ] Document each option in `AbstractCodeHighlighter` javadoc.
- **Files:** `AbstractCodeHighlighter`.
- **Done when:** no language subclass needs to copy a private string/number reader.

### H-015 — Fuzz/property test harness

- [ ] Generate random strings from a language-specific character alphabet.
- [ ] Assert termination and round-trip for every highlighter.
- [ ] Use a deterministic seed in CI and allow an extended local run via a system property.
- **Files:** new test utility + tests.
- **Done when:** a 100k-case local run completes without hang or text loss.

### H-016 — Content-type integration

- [ ] Verify `DefaultNContentTypeResolver` maps new extensions to ids or MIME types.
- [ ] Make the highlighter lookup accept both MIME types and extensions consistently.
- [ ] Add an end-to-end test: file name → content type → highlighter id.
- **Files:** content-type resolver, `DefaultNTextManagerModel`, tests.
- **Done when:** opening a `.scala`, `.ntx`, `.tf`, `.properties` file selects the right highlighter without explicit language id.

---

## 6. M3/M4 — Language coverage (P2)

Each language is one class extending `AbstractCodeHighlighter` unless noted. Each needs: keyword/type sets, comment/string configuration, aliases, registration in `META-INF/services/net.thevpc.nuts.spi.NComponent`, and tests.

### M3 — most common next

- [ ] **H-020 PowerShell** — separate from CMD; `#` comments, `<# #>` blocks, `$vars`, `@'`/`@"` here-strings, `-Verb` parameters, pipes.
- [ ] **H-021 Groovy / Gradle** — Groovy strings (`'''`, `"""`, `GString`), closures, `def`, Gradle DSL keywords.
- [ ] **H-022 Lua** — `--` and `--[[ ]]`, long strings `[[ ]]`, `local`, `function`, `nil`, `true`, `false`.
- [ ] **H-023 R** — `#` comments, `<-`, `function`, `TRUE/FALSE/NULL/NA`, raw strings `r"(...)"`.
- [ ] **H-024 Perl** — `#` comments, `$@%` variables, `qw()`, regex literals, `use`, `my`, `sub`.
- [ ] **H-025 Dart** — C-like, triple strings, `$var`/`${}`, annotations, `async`, `await`, `Future`, `Stream`.
- [ ] **H-026 SCSS-specific nesting** — CSS highlighter exists; add `$variables`, `@mixin`, `@include`, `&` nesting.

### M4 — ecosystem breadth

- [ ] **H-027 Haskell** — `--`, `{- -}` nested, strings/chars, type signatures.
- [ ] **H-028 Elixir** — `#`, `"""` heredocs, `#{...}`, atoms `:foo`, `defmodule`, `do/end`.
- [ ] **H-029 Clojure** — `;` comments, `#_` discard, keywords `:foo`, vectors/maps, `defn`, `ns`.
- [ ] **H-030 GraphQL and Protobuf** — schema keywords, types, directives, comments.
- [ ] **H-031 diff / patch** — line prefixes `+`, `-`, `@@`, file headers; a dedicated line-oriented highlighter.

For every language:

- [ ] Add id + MIME + extension aliases (H-010 table).
- [ ] Add registration line.
- [ ] Add keyword resource (H-013).
- [ ] Add focused tests plus the all-highlighter suite.
- [ ] Update this file status to `done`.

---

## 7. M5 — Edge-case correctness (P3)

- [ ] **H-040 C/C++** — raw strings `R"(...)"`, prefixed strings `u8`, `L`, `u`, `U`, line splices, preprocessor continuations.
- [ ] **H-041 C#** — raw strings with variable quote counts, `$$"""` interpolation, `@` identifiers, `#region`/`#if`.
- [ ] **H-042 SQL** — PostgreSQL dollar-quoted strings, nested block comments, `E'...'`, `U&'...'`, dialect keyword sets.
- [ ] **H-043 Ruby** — heredocs, `%w/%q/%r` literals, `=begin`/`=end`, regex literals, block comments.
- [ ] **H-044 PHP** — interpolation inside heredoc, `<<<` indentation rules, `__halt_compiler`, inline HTML outside tags.
- [ ] **H-045 Swift** — raw strings `#"..."#`, multiline interpolation, `#available`, regex literals.
- [ ] **H-046 Go** — hex floats, imaginary literals, digit separators across all bases.
- [ ] **H-047 TOML** — dotted keys, quoted keys, inline tables, multiline literal strings.
- [ ] **H-048 Shell/PowerShell** — here-docs, process substitution, nested `$()`, Windows PowerShell escapes.
- [ ] **H-049 Unicode** — surrogate pairs, combining marks, emoji in strings/comments, no split characters.

Each task must add a regression test and keep round-trip fidelity.

---

## 8. M6 — Performance and hardening (P4)

- [ ] **H-050 Benchmark harness** — measure MB/s and allocations for a 100 KB sample per highlighter. Record results in this file.
- [ ] **H-051 Allocation pass** — avoid `peekChars(N)` in hot loops; add single-pass `hasPrefix` checks; avoid per-token `StringBuilder` for single chars.
- [ ] **H-052 Streaming safety** — confirm every reader can stop at EOF mid-construct without exception; no lookahead beyond `hasNext`.
- [ ] **H-053 Failure policy** — invalid input is highlighted as plain text, never thrown; log nothing from lexers.
- [ ] **H-054 API review** — make `AbstractCodeHighlighter` options coherent, document extension points, consider extracting a `CodeLexer` interface if the base grows.

Definition of done for M6:

- each highlighter is linear in input size on repeated input
- no unbounded caches
- benchmark table committed
- no `Pattern.compile` inside per-line/per-token loops

---

## 9. Verification commands

```bash
# compile
mvn -q -pl core/nuts-runtime -am -DskipTests -Dmaven.test.skip=true compile

# highlighter tests
mvn -q -pl test/nuts-runtime-test -am \
  -Dtest='*CodeHighlighterTest,HighlighterFixesTest,NewLanguagesCodeHighlighterTest' \
  -DfailIfNoTests=false test

# one language
mvn -q -pl test/nuts-runtime-test -am \
  -Dtest=KotlinCodeHighlighterTest -DfailIfNoTests=false test
```

A task is not done until the compile command and the full highlighter test set pass.

---

## 10. Definition of done (per task)

- [ ] Implementation merged.
- [ ] Registration and aliases added where applicable.
- [ ] Tests added: registration, round-trip, styling, malformed input.
- [ ] No regression in the all-highlighter suite.
- [ ] This file updated: checkbox ticked, status/notes added.
- [ ] Commit message contains the task id.

---

## 11. Open questions

1. Should `highlighter-mappings.ini` be activated, or should alias metadata live in code/annotations?
2. Should `tokenToText` become mandatory for all highlighters, or remain optional with a plain-text default?
3. Do we want an external grammar format (TextMate JSON) for large languages, with the Java lexers only as fallback?
4. Should theme component names in ntexup be discovered dynamically from the theme files, or stay as a curated set?
5. Should the highlighter expose a `supportedAliases()` API for editor integration?

---

## 12. Progress log

| Date | Task | Change |
|---|---|---|
| 2026-11-24 | baseline | Scala/ntx added, XML/ntexup fixes, theme styles |
| 2026-11-25 | baseline | base class, 11 languages, XML lexer, ntexup categories |
| 2026-11-25 | roadmap | this plan created |
