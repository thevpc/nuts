---
title: Expression Tokenization
---

# NExpr — Low-Level Tokenization (`NStreamTokenizer`)

Part of the NExpr doc set. Covers `NStreamTokenizer`, the lexer backing NExpr's parser, and why it exists as a separate class instead of reusing `java.io.StreamTokenizer`.

`NStreamTokenizer` is usable standalone for custom lexing needs, independent of the rest of NExpr.

```java
NStreamTokenizer st = new NStreamTokenizer(new StringReader("8.0.0"));
st.xmlComments(true);
st.parseNumbers(false);
st.wordChars('0', '9');
st.wordChars('.', '.');
st.wordChars('-', '-');

int tokenType;
while ((tokenType = st.nextToken()) != NToken.TT_EOF) {
    NOut.println(st.image);
}
```

---

## 1. Why not `java.io.StreamTokenizer`?

`NStreamTokenizer` is deliberately modeled on `java.io.StreamTokenizer`'s API shape (`nextToken()`, `ttype`, `sval`, `pushBack()`, `wordChars(...)`, `whitespaceChars(...)`, `quoteChar(...)`, `ordinaryChar(...)`, `commentChar(...)`, `lineno()` — all present, doc-comment style and all) but reimplements it from scratch to lift several hard limitations of the JDK class that matter for parsing an *expression language* rather than a generic config/data format:

| Limitation in `java.io.StreamTokenizer`                                                                                                                                                                       | How `NStreamTokenizer` differs                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Numeric tokens are always coerced into a single `double nval` field — no way to distinguish `1` (int) from `1.0` (double) from a value too large for `double` to represent precisely.                         | `nval` is a `Number`, and the tokenizer chooses the *narrowest precise* Java numeric type for each literal: `Integer` → `Long` → `BigInteger` for integral literals (`TT_INT`/`TT_LONG`/`TT_BIG_INT`), and `Float` → `Double` → `BigDecimal` for fractional literals (`TT_FLOAT`/`TT_DOUBLE`/`TT_BIG_DECIMAL`), falling through to the next-wider type only if parsing at the narrower type fails. This matters for an expression language where literal precision should be preserved through evaluation (e.g. large integer IDs, or exact decimal literals for financial/scientific values) rather than silently rounded through `double`.                                                                                                                                            |
| No notion of an "operator" token at all — every non-word, non-number, non-quote, non-comment character is returned one at a time as its own single-character `ttype`.                                         | `NStreamTokenizer` has a dedicated `_read_op()` path (`isOp(c)`) that greedily consumes a *run* of operator-class characters into a single `TT_OP` token — so `<<`, `!=`, `**`, `==~`, or a custom multi-char operator you've declared, all come back as one token with `image`/`sval` set to the full operator string, not as several one-character tokens the parser would have to glue back together. Operator-class characters include the obvious ASCII set (`+ - * / > < ! ~ # = & \| ^ % : ?`) plus, notably, entire Unicode symbol categories (`MATH_SYMBOL`, `OTHER_SYMBOL`, `MODIFIER_SYMBOL` — covering things like `≤ ≥ ≠ × ÷ ± √ ∑ ∏ ∞ ∂ ∫`), so mathematical/scientific notation can be used as operator symbols directly, fitting NExpr's physics/math-oriented built-ins. |
| Only two configurable comment styles (`slashSlashComments`, `slashStarComments`), always silently discarded — no XML comments, no shell-style `#` comments, and no way to get comment *text* back as a token. | Dedicated handling for four comment families — C `//`/`/* */` (`slashSlashComments`/`slashStarComments`), XML `<!-- -->` (`xmlComments(boolean)`), and shell/Python `#` (bundled into `pythonComments()`) — plus convenience presets `javaComments()` (enables both C styles) and `pythonComments()` (enables `#`-style). Critically, `returnComments` lets comments be returned as real tokens (`TT_COMMENTS`) carrying their full text via `image`, instead of always being silently swallowed — useful for tooling that needs to preserve or inspect comments (doc generation, formatters), not just skip them.                                                                                                                                                                      |
| Whitespace is always silently skipped between tokens (only newlines can be made "significant" via `eolIsSignificant`).                                                                                        | `returnSpaces` lets *all* whitespace runs be returned as their own `TT_SPACE` token carrying the exact whitespace text via `image` — letting a consumer reconstruct original formatting/spacing exactly, which `test17` in `ExprTest` relies on directly (`"1 .. 3"` tokenizes to `["1", " ", "..", " ", "3"]`, not just `["1", "..", "3"]`).                                                                                                                                                                                                                                                                                                                                                                                                                                           |
| One fixed pair of quote characters can be configured, and there's no interpolated-string concept — a quoted string is always just a plain string.                                                             | Three quote characters are pre-registered by default (`"`, `'`), and a `$`-prefixed variant of each is recognized as a distinct *interpolated*-string token type (`TT_ISTR_SQ`, `TT_ISTR_DQ`, `TT_ISTR_AQ` for `$'...'`, `$"..."`, `` $`...` `` respectively) — this is the lexical foundation for NExpr's `$`-interpolated string literals,  distinguished at the *tokenizer* level so the parser can build an `NExprInterpolatedStringNode` directly instead of post-processing a plain string for embedded `$var` references.                                                                                                                                                                                                                                             |
| No concept of a range/ellipsis token — `1..3` and `1.3` are lexically ambiguous under the stock class's number-parsing rules.                                                                                 | `NStreamTokenizer`'s number reader explicitly looks ahead for a second `.` immediately following one already consumed as a decimal point, and if found, stops the numeric token *before* consuming either `.`, leaving `..` to be tokenized separately by `_read_op()` as its own operator token. This is exactly what `test18`/`test19` in `ExprTest` verify: `"1..3"` → `["1", "..", "3"]`, and `"-1..-3"` → `["-1", "..", "-3"]` (with a correctly-tokenized signed number on each side).                                                                                                                                                                                                                                                                                            |
| The token-type/flag surface is fixed — you can't register new "kinds" of parsable content without subclassing.                                                                                                | `acceptTokenType(int tt, boolean b)` / `isParsable(int tt)` work against a `parsableTokenTypes` bitmask keyed by negative token-type constants (defined on the companion `NToken` class), so new comment/token kinds (like the XML-comment and Python-comment additions above) can be toggled through the same generic mechanism instead of each needing its own dedicated boolean field, as `java.io.StreamTokenizer` does for its two comment styles.                                                                                                                                                                                                                                                                                                                                 |
| Extended/accented Latin characters (128–255) aren't word-constituent by default.                                                                                                                              | The private no-arg constructor pre-registers `wordChars(128 + 32, 255)` in addition to the ASCII letter ranges, so accented/extended-Latin identifier characters are usable out of the box — relevant for NExpr's own physics-constant names (`ε0`, `μ0`, `η0`, `ħ`, `σ`, `π`), which rely on non-ASCII characters being valid word constituents.                                                                                                                                                                                                                                                                                                                                                                                                                                       |

## 2. Key configuration methods

| Method | Effect |
|---|---|
| `wordChars(low, hi)` / `wordChar(c)` | Mark a character range (or single char) as word-constituent. |
| `whitespaceChars(low, hi)` | Mark a range as whitespace-only (clears any other attribute). |
| `ordinaryChars(low, hi)` / `ordinaryChar(ch)` | Strip all special meaning from a range/character — returned as single-char tokens. |
| `commentChar(ch)` | Mark a character as starting a to-end-of-line comment. |
| `quoteChar(ch)` | Mark a character as a string delimiter. |
| `parseNumbers(boolean)` | Toggle numeric-literal recognition for `0-9`, `.`, `-`. |
| `eolIsSignificant(boolean)` | Toggle whether line breaks are returned as `TT_EOL` tokens vs. treated as plain whitespace. |
| `slashStarComments(boolean)` / `slashSlashComments(boolean)` | Toggle C-style `/* */` / `//` comment recognition individually. |
| `xmlComments(boolean)` | Toggle XML `<!-- -->` comment recognition (and, as implemented, also disables the two slash-comment styles when called). |
| `javaComments()` | Convenience preset: `commentChar('/')` + both slash-comment styles enabled. |
| `pythonComments()` | Convenience preset: `#`-style line comments enabled, XML/slash styles disabled. |
| `lowerCaseMode(boolean)` | Force word tokens to lowercase in `sval` (raw case preserved in `image`). |
| `acceptTokenType(int tt, boolean b)` / `isParsable(int tt)` | Generic enable/disable and query for extensible token kinds (comments, interpolated strings, …), keyed by `NToken` constants. |
| `pushBack()` | Un-consume the current token so the next `nextToken()` call returns it again. |
| `hasNext()` | Peek whether another character is available without consuming a token. |

## 3. Fields populated after `nextToken()`

| Field | Meaning |
|---|---|
| `ttype` | The token type just read — either one of the `NToken.TT_*` constants, or (for single-character/operator/quote tokens) the character value itself. |
| `image` | The **exact raw source text** of the token — including original whitespace/case/escaping as written, unlike `sval` which may be normalized (e.g. lowercased, or with escape sequences already resolved). |
| `sval` | The token's string value — the word text for `TT_WORD`, or the *decoded* body for quoted-string tokens (escape sequences like `\n`, `\t`, octal escapes already resolved). |
| `nval` | The token's numeric value as a `Number`, for any of the `TT_INT`/`TT_LONG`/`TT_BIG_INT`/`TT_FLOAT`/`TT_DOUBLE`/`TT_BIG_DECIMAL` types. |

## 4. Notable tokenizing behaviors (verified by `ExprTest`)

- With `parseNumbers(true)` and `returnSpaces` on, whitespace is preserved as its own token image (`test17`: `"1 .. 3"` → tokens `["1", " ", "..", " ", "3"]`), letting a consumer reconstruct spacing exactly.
- `".."` is recognized as a distinct range-like operator token, separate from decimal points, even directly adjacent to digits with no surrounding spaces (`test18`: `"1..3"` → `["1", "..", "3"]`).
- Signed numbers are tokenized as a single signed literal, and `..` still splits correctly around them (`test19`: `"-1..-3"` → `["-1", "..", "-3"]`).
- Multi-character operator runs like `"<<"` are recognized as one `TT_OP` token, not two single-character tokens (`testTokenize2`).
- Word-char ranges are reconfigurable per instance (`wordChars('0','9')` + `wordChars('.', '.')` + `wordChars('-', '-')` with `parseNumbers(false)`), which is how a version-string-like token such as `"8.0.0"` can be tokenized as one plain word rather than being (mis)parsed as a number.

`NToken.TT_EOF` is the end-of-stream sentinel returned by `nextToken()`; `NToken` also defines the other constants referenced throughout this doc (`TT_WORD`, `TT_INT`, `TT_LONG`, `TT_BIG_INT`, `TT_FLOAT`, `TT_DOUBLE`, `TT_BIG_DECIMAL`, `TT_OP`, `TT_EOL`, `TT_NOTHING`, `TT_COMMENTS`, `TT_COMMENT_LINE_C`, `TT_COMMENT_MULTILINE_C`, `TT_COMMENT_MULTILINE_XML`, `TT_COMMENT_LINE_SH`, `TT_ISTR_SQ`, `TT_ISTR_DQ`, `TT_ISTR_AQ`).

