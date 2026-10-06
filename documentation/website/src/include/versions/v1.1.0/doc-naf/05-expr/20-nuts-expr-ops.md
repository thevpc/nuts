---
title: Expression Operators
---

# NExpr — Operators

Part of the NExpr doc set. Covers `NExprOperator`, operator kinds, declaring and removing operators, precedence & associativity, `NExprCommonOp`, and the complete built-in operator table.

```java
interface NExprOperator {
    static NExprOperator of(String name, NExprOpType operatorType, int operatorPrecedence,
                             NOperatorAssociativity associativity, NExprCallHandler handler);
    NOperatorAssociativity operatorAssociativity();
    String name();
    NExprOpType operatorType();
    int operatorPrecedence();
    Object eval(NExprCallContext callContext);
}
```

---

## 1. Operator kinds — `NExprOpType`

| Kind | Meaning | Example |
|---|---|---|
| `PREFIX` | Unary, precedes its operand | `-x`, `!x` |
| `POSTFIX` | Unary, follows its operand | `x++` |
| `INFIX` | Binary, between two operands | `a + b`, `a.b`, `a[i]` |

An operator is uniquely identified by the pair `(name, type)` — the same symbol can exist as both prefix and infix (e.g. `-` as unary negation vs. binary subtraction, or `++`/`--` as both prefix and postfix) since they're registered/looked-up separately (`getOperator(opName, type, ...)`, `removeOperator(name, type)`).

---

## 2. Declaring custom operators

```java
NExprContextBuilder.of()
    .declareBuiltins()
    .declareOperator("~", NExprOpType.PREFIX, /* precedence */ 1300,
        NOperatorAssociativity.RIGHT,
        callContext -> { /* custom logic */ return null; })
    .build();
```

Shorthand overloads exist (`declareOperator(name, handler)`, `declareOperator(name, type, handler)`) for cases where you don't need to pin an explicit precedence/associativity.

You can also supply a whole `NExprOperatorResolver` instead of declaring operators one at a time — see [`NExpr.md §3.1 Generic resolvers`](./NExpr.md).

---

## 3. Removing / restricting the operator set

A useful pattern from the test suite (`_retain` helper in `ExprTest`) shows how to whittle a built-in context down to only a chosen subset of operators, by iterating `NExprMutableContext.operators()` and calling `removeOperator(operator)` for anything not matching a desired `(type, name)`:

```java
NExprMutableContext ctx = ...;
for (NExprOperator op : ctx.operators()) {
    boolean keep = op.operatorType() == NExprOpType.INFIX && op.name().equals("+");
    if (!keep) {
        ctx.removeOperator(op);
    }
}
// Now ctx.parse("1+2+3") works, but "1+2*3" would fail (no "*" operator left).
```

This is useful for building **restricted DSLs** — e.g. exposing only arithmetic to end users, or only comparison operators for a filter language.

---

## 4. Precedence & associativity

**`NOperatorAssociativity`** — exactly two values:

```java
public enum NOperatorAssociativity implements NEnum {
    LEFT,
    RIGHT;
}
```

**`NExprOpPrecedence`** — the canonical numeric precedence constants (higher number = binds tighter):

```java
public final class NExprOpPrecedence {
    public static final int STATEMENT_SEPARATOR = 50;
    public static final int ASSIGN = 100;
    public static final int TERNARY_CMP = 200;
    public static final int OR = 300;
    public static final int AND = 400;
    public static final int PIPE = 500;        // |
    public static final int COMPLEMENT = 600;
    public static final int AMP = 700;          // &
    public static final int EQ = 800;
    public static final int NEQ = EQ;
    public static final int CMP = 900;          // < <= > >=
    public static final int LT = CMP;
    public static final int LTE = CMP;
    public static final int GT = CMP;
    public static final int GTE = CMP;
    public static final int SHIFT = 1000;       // << >>
    public static int PLUS = 1100;
    public static int MINUS = PLUS;
    public static int MUL = 1200;
    public static int DIV = MUL;
    public static int MOD = MUL;
    public static final int POW = 1250;
    public static final int COALESCE = MUL + 10; // ??  (1210)
    public static final int NOT = 1300;
    public static final int UNARY_PRE = 1300;
    public static final int UNARY_POST = 1400;
    public static final int PARS = 1600;
    public static final int BRACKETS = PARS;
    public static final int BRACES = PARS;
    public static final int DOT = PARS;
    public static final int STATEMENT_SEPARATOR = 50;
}
```

**Tier ladder, high → low (binds tighter → binds looser):**

| Constant | Value | Operator(s) |
|---|---:|---|
| `PARS` / `BRACKETS` / `BRACES` / `DOT` | 1600 | `(` grouping, `[` indexing, `{` block, `.` member access — all share the same, tightest tier |
| `UNARY_POST` | 1400 | postfix `++`/`--` |
| `NOT` / `UNARY_PRE` | 1300 | prefix `-` (negation), prefix `!` (NOT), prefix `++`/`--` |
| `POW` | 1250 | `**` |
| `COALESCE` | 1210 | `??` (null-coalescing) |
| `MUL` / `DIV` / `MOD` | 1200 | `*`, `/` *(NExpr's built-in `%` is registered at `CMP`, not `MOD` — see the caveat below)* |
| `PLUS` / `MINUS` | 1100 | `+`, binary `-` |
| `SHIFT` | 1000 | `<<`, `>>` |
| `CMP` (`LT`/`LTE`/`GT`/`GTE`) | 900 | `<`, `<=`, `>`, `>=` — and, as actually registered by `declareBuiltins()`, also `==`, `!=`/`!==`/`<>`, `=~`, `==~`, `%` |
| `EQ` / `NEQ` | 800 | reserved tier for equality — `declareBuiltins()` registers `==`/`!=` at `CMP` instead |
| `AMP` | 700 | bitwise `&` |
| `COMPLEMENT` | 600 | reserved (likely a future bitwise-complement operator) |
| `PIPE` | 500 | bitwise `\|` |
| `AND` | 400 | logical `&&` (`declareBuiltins()`'s single `AND` operator also aliases plain `&` here — see caveat) |
| `OR` | 300 | logical `\|\|`, and `^` (`XOR`) |
| `TERNARY_CMP` | 200 | reserved for an upcoming ternary `? :` operator — **not yet wired up** |
| `ASSIGN` | 100 | `=` and all compound assigns (`+=`, `-=`, `*=`, `/=`, `%=`, `^=`, `**=`) |
| `STATEMENT_SEPARATOR` | 50 | `;` |

:::info
**Caveat — the `declareBuiltins()` excerpt vs. the full engine:** the excerpt of `declareBuiltins()` available for this doc registers `&` and `|` as *aliases* of the logical `AND`/`OR` operators (precedence tiers `AND`/`OR`), and registers `==`/`!=`/`%` at the coarser `CMP` tier rather than at `EQ`/`MOD`. Separately, `PIPE` (bitwise `|`), `AMP` (bitwise `&`), `SHIFT` (`<<`/`>>`), and `COALESCE` (`??`) are confirmed to be wired to their own distinct operators elsewhere in the full engine — i.e. NExpr distinguishes a *logical* `&`/`|` from a *bitwise* `&`/`|` (much like the difference is only visible through which tier the parser actually binds to, not through the symbol alone). Only `TERNARY_CMP` (200) is confirmed genuinely unused so far, reserved for a future ternary operator. `COMPLEMENT` (600) has no confirmed operator yet.
:::

**Associativity, as registered in the shown `declareBuiltins()` excerpt:**

| Category | Associativity |
|---|---|
| Comparisons, `&`/`\|`/`^` (as shown), arithmetic (`+ - * / %`, `**`), `.` (dot) | `LEFT` |
| Assignment `=` and all compound assigns (`+=`, `-=`, …) | `RIGHT` |
| Prefix `-`, prefix `!` | `RIGHT` |
| Prefix `++`/`--` | `LEFT` (as literally registered — asymmetric vs. prefix `-`/`!`) |
| Postfix `++`/`--`, `;` statement separator, `(`/`[`/`{` grouping markers | `LEFT` |

:::info
Parentheses `( ... )` are themselves represented as an `NExprOperator`/`NExprNode` named `"("` whose single child is the grouped sub-expression (see `test4`, `test5`, `test14` in `ExprTest`) — they are not stripped away during parsing. Their eval handler in `declareBuiltins()` deliberately throws (`"unable to evaluate"`); the actual grouping/indexing/block semantics are resolved by the parser itself, not by evaluating the `(`/`[`/`{` node directly. The same is true for `[` and `{`. If you ever see `IllegalArgumentException("unable to evaluate")`, it usually means something tried to `eval()` one of these structural marker nodes directly instead of walking its children.
:::

---

## 5. Common operator identity — `NExprCommonOp`

`NExprCommonOp` is a portable, symbol-based enum used to identify "well-known" operator semantics independent of exactly how they were declared/named in a given context — useful when writing generic operator implementations that need to defer to a context's notion of, say, "the current `+`":

```java
PLUS("+"), MINUS("-"), MUL("*"), DIV("/"), REM("%"), XOR("^"), POW("**"),
OR_BITS("|"), AND_BITS("&"), OR("||"), AND("&&"),
EQ("=="), NOT("!"), LT("<"), GT(">"), LTE("<="), GTE(">="), NE("!="),
DOT("."), ASSIGN("="), LIKE("=~"), EQ_REGEX("==~")
```

Each constant carries an `image()` (its canonical textual symbol) and supports lenient `parse(String)` (case/whitespace-insensitive, matching name, id, image, or declared aliases).

Contexts expose a way to resolve a **type-specific implementation** of a common operator without hardcoding the operator's declared name:

```java
NOptional<NFunction2<A, B, ?>> findCommonInfixOp(NExprCommonOp op, Class<? extends A> firstType, Class<? extends B> secondType);
NOptional<NFunction<A, ?>>     findCommonPrefixOp(NExprCommonOp op, Class<? extends A> argType);
NOptional<NFunction<A, ?>>     findCommonPostfixOp(NExprCommonOp op, Class<? extends A> argType);
```

This is effectively an operator-overloading lookup: "give me the function that implements `PLUS` for `(Integer, String)`", etc. Note `NExprCommonOp` already distinguishes `OR_BITS`/`AND_BITS` (`|`/`&`) from `OR`/`AND` (`||`/`&&`) — consistent with §4's confirmation that logical and bitwise `&`/`|` are genuinely separate operators in the engine, not just precedence-tier aliases of each other.

---

## 6. Complete built-in operator table (`declareBuiltins()`)

**Constants also declared here (not operators, but part of the same call):**

| Name | Value |
|---|---|
| `true` | `true` |
| `false` | `false` |
| `null` | `null` |

**Operators:**

| Symbol(s) | Type | Precedence | Assoc. | Notes |
|---|---|---|---|---|
| `&` | INFIX | `AND` | LEFT | logical AND (see caveat in §4 re: distinct bitwise `&`) |
| `\|` | INFIX | `OR` | LEFT | logical OR (see caveat re: distinct bitwise `\|`) |
| `<`, `<=`, `>`, `>=`, `==`, `=~` (`LIKE`), `==~` (`EQ_REGEX`) | INFIX | `CMP` | LEFT | comparisons |
| `!=`, `!==`, `<>` | INFIX | `CMP` | LEFT | inequality, all aliasing `NE` |
| `+`, `-` | INFIX | `PLUS` | LEFT | addition, subtraction |
| `*`, `/` | INFIX | `MUL` | LEFT | multiplication, division |
| `%` | INFIX | `CMP` | LEFT | remainder — registered at comparison precedence, not `MUL`/`MOD` (see §4 caveat) |
| `^` | INFIX | `OR` | LEFT | `XOR` |
| `**` | INFIX | `POW` | LEFT | exponentiation |
| `.` | INFIX | `DOT` (`PARS` tier) | LEFT | member access — handler evaluates the left side then delegates to an internal `runDot(instance, rightArg, context)` resolver |
| `-` (unary) | PREFIX | `NOT` | RIGHT | unary negation |
| `!` | PREFIX | `NOT` | RIGHT | logical NOT |
| `=` | INFIX | `ASSIGN` | RIGHT | assignment; requires the LHS node to be `NExprNodeType.WORD` resolvable via `context.getVar(...)`, else throws `IllegalArgumentException` |
| `+=`, `-=`, `*=`, `/=`, `%=`, `^=`, `**=` | INFIX | `ASSIGN` | RIGHT | compound assignment; reads old value, evaluates RHS, delegates to `context.evalInfixOperator(baseOp, old, part)`, then stores and returns the new value |
| `++` | PREFIX | `NOT` | LEFT | pre-increment; adds `(byte) 1` via `evalInfixOperator("+", …)`, stores, returns **new** value |
| `++` | POSTFIX | `NOT` | LEFT | post-increment; same as above but returns the **old** value |
| `--` | PREFIX | `NOT` | LEFT | pre-decrement (mirrors `++`, using `evalInfixOperator("-", …)`) |
| `--` | POSTFIX | `NOT` | LEFT | post-decrement, returns old value |
| `;` | INFIX | `STATEMENT_SEPARATOR` | LEFT | statement chaining; evaluates every operand in order and returns the **last** one's value — this is precisely what makes `"printChunk(0);;printChunk(0);;printChunk(0)"` (`test9`) execute all three calls in sequence, and why empty statements between repeated `;;` (`test8`) don't error out |
| `(` | POSTFIX | `PARS` | LEFT | grouping marker; `eval()` throws — grouping is resolved structurally by the parser, not by evaluating this node |
| `[` | POSTFIX | `BRACKETS` (`PARS` tier) | LEFT | indexing marker; `eval()` throws, same reasoning |
| `{` | POSTFIX | `BRACES` (`PARS` tier) | LEFT | block marker; `eval()` throws, same reasoning |

For the full built-in **function** table (`string`, `join`, `format*`, `isBlank`, …) and math/physics constants and functions, see [`NExpr-Evaluation.md`](./NExpr-Evaluation.md).

---

## 7. Practical patterns

- **Operators are identified by `(name, type)`, not just `name`.** This lets `+` mean different things as prefix vs. infix, and lets `++`/`--` exist as both prefix and postfix simultaneously. Always pass the intended `NExprOpType` when doing manual `getOperator`/`removeOperator` lookups.
- **Restrict, don't rebuild, for DSLs:** start from `declareBuiltins()` and prune down (§3) rather than hand-assembling an operator set from scratch — you get consistent precedence/associativity behavior for free for whatever subset you keep.
- **Watch the `%` precedence gotcha:** because `%` shares `CMP` precedence rather than `MUL`, an expression like `a % b > 0` parses differently than a C-family programmer might expect at first glance — worth a comment in any expression that mixes `%` with comparisons.