---
title: Expressions
---

# NExpr — Expression & Template Engine (Nuts Ecosystem)

NExpr is a lightweight, embeddable expression and templating engine, part of the `thevpc`/Nuts ecosystem. It parses expression strings into an AST (`NExprNode`), evaluates them against a pluggable `NExprContext`, and powers a templating language (`NExprTemplate`) built on the same parser.

Typical uses: dynamic filtering/config expressions, safe user-facing scripting for CLI tools or config files, string interpolation/templating, and lightweight "scripting glue" inside larger Nuts-based applications (e.g. NAF, NARU).

This is the entry point into a small doc set:

| Doc | Covers |
|---|---|
| **NExpr.md** (this file) | Core concepts, quick start, context building, variables, functions/constructs, the AST, `if`/`else`, literal mapping, design notes. |
| expr-operators | Operator kinds, declaring/removing operators, the full precedence & associativity table, `NExprCommonOp`, the complete built-in operator table. |
| expr-evaluation | The evaluation pipeline, `NExprCallContext`/`NExprCallHandler`/`NExprNodeValue`, custom resolvers, the built-in function table, worked examples. |
| expr-templating | `NExprTemplate`: Moustache/JSP-style directives, `$`-interpolation, and embedding NExpr's templating in other languages via custom delimiters. |
| expr-tokenizer | `NStreamTokenizer`, and how/why it differs from `java.io.StreamTokenizer`. |

---

## 1. Core Concepts

| Concept | Type | Role |
|---|---|---|
| Context | `NExprContext` | Read-only evaluation environment: holds resolvable vars, functions, constructs, operators. Can `parse()` expression strings and `eval*()` calls directly. |
| Mutable Context | `NExprMutableContext extends NExprContext` | A context you can populate/mutate at runtime: declare/undeclare vars, functions, constructs, operators; set variable values. |
| Context Builder | `NExprContextBuilder` | Fluent builder used to assemble a context (built-ins, math/physics constants, custom operators, resolvers) before calling `build()` (→ `NExprContext`) or `buildMutable()` (→ `NExprMutableContext`). |
| Node | `NExprNode` (and subtypes) | The parsed AST. Has a `nodeType()`, a `name()`, `children()`, and can be `eval(context)`. |
| Var | `NExprVar` | A named, gettable/settable value (variable or constant) bound into a context. |
| Function | `NExprFunction` | A named callable, used for both **functions** (`f(args)`) and **constructs** (`new`-like or keyword forms). |
| Operator | `NExprOperator` | A named callable with an `NExprOpType` (`PREFIX` / `POSTFIX` / `INFIX`), a precedence, and an associativity. See expr-operators. |
| Template | `NExprTemplate` | A text-templating layer built on the same expression engine. See expr-templating. |

### Object graph at a glance

```
NExprContextBuilder.of()
      .declareBuiltins()
      .declareVar(...) / .declareOperator(...) / .declareVars(resolver) / ...
      .build()            -> NExprContext         (read-only usage)
      .buildMutable()      -> NExprMutableContext  (can declare/undeclare/set at runtime)

NExprContext
      .parse(String)                 -> NOptional<NExprNode>
      .evalFunction/.evalOperator/... -> NOptional<Object>
      .ofTemplate()                  -> NExprTemplate
```

---

## 2. Quick Start

### 2.1 Parse and evaluate a plain expression

```java
NExprContext expr = NExprContextBuilder.of()
        .declareBuiltins()
        .build();

NExprNode n = expr.parse("1+2*3").get();   // NOptional<NExprNode>
System.out.println(n);                     // "1 + 2 * 3"
```

### 2.2 Build a mutable context, declare variables, evaluate with side effects

```java
NExprMutableContext expr = NExprContextBuilder.of()
        .declareBuiltins()
        .buildMutable();

expr.declareVar("a");
NExprNode n = expr.parse("a=1").get();
Object result = n.eval(expr).get();   // assignment expression; evaluates and stores into "a"
```

### 2.3 String interpolation with `$'...'`

```java
NExprMutableContext expr = NExprContextBuilder.of()
        .declareBuiltins()
        .buildMutable();

expr.declareVar("v");
expr.setVarValue("v", "me");

NExprNode n = expr.parse("$'something for $v'").get();
String out = (String) n.eval(expr).get();   // "something for me"
```

### 2.4 Templating (Moustache style)

```java
Map<String, Object> vars = new HashMap<>();
vars.put("world", "Earth");
vars.put("yellow", true);
vars.put("blue", true);

NExprTemplate tpl = NExprContextBuilder.of()
        .declareBuiltins()
        .declareVars(NExprVarResolver.ofMap(vars))
        .build()
        .ofTemplate()
        .withJspStyle();

String out = tpl.processString(
    "hello <%:if yellow %> <%world%> <%:else if blue %> my  <%:else%> World <%:end%>"
);
// -> "hello  Earth "
```

See expr-templating.md for the full directive set, and expr-evaluation for a formula/constraint-evaluation example closer to numeric/engineering use.

---

## 3. Building a Context: `NExprContextBuilder`

`NExprContextBuilder.of()` starts from an empty context builder (internally derived from `NExprContext.of().childContext()`), so builders are always created as a *child* of a base/root context — this supports layered/nested context composition.

Key builder operations:

| Method | Purpose |
|---|---|
| `declareBuiltins()` | Registers the engine's default operators/functions/constructs (arithmetic, comparison, logical, indexing `[ ]`, grouping `( )`, member access `.`, assignment `=`, `if`, etc.) — see [`NExpr-Operators.md §built-ins`](./NExpr-Operators.md) and [`NExpr-Evaluation.md §built-in functions`](./NExpr-Evaluation.md). |
| `declareMathConstants()` | Adds `pi`/`PI`/`π` (`Math.PI`) and `E` (`Math.E`). |
| `declarePhysicsConstants()` | Adds a set of SI physics constants (`C`, `ε0`, `μ0`, `η0`, `h`, `ħ`, `kB`, `NA`, `me`, `mp`, `G`, `g`, `R`, `σ`, …) — see expr-evaluation for the full table and a worked example. |
| `declareMathFunctions()` | Adds standard `java.lang.Math`-backed functions (`sin`, `cos`, `sqrt`, `pow`, `atan2`, …) — full list in expr-evaluation. |
| `declareVar(NExprVar)` / `declareVars(NExprVarResolver)` | Register a single variable, or a *resolver* that lazily/dynamically resolves variables by name. |
| `declareFunction(NExprFunction)` / `declareFunctions(NExprFunctionResolver)` | Register a function or a function resolver. |
| `declareConstruct(NExprFunction)` / `declareConstructs(NExprFunctionResolver)` | Register a "construct" — syntactically function-like, semantically distinct (see §5 below). |
| `declareOperator(...)` (several overloads) | Register a custom operator — see [`NExpr-Operators.md §declaring custom operators`](./NExpr-Operators.md). |
| `declareResolver(NExprResolver)` | Register a generic, composite resolver — see below. |
| `remove*` counterparts | Symmetric removal methods for every `declare*`. |
| `literalMapper(NExprLiteralMapper)` / `literalMapper()` | Get/set the mapper responsible for turning raw literal tokens into typed values (numbers, strings, booleans, etc). |
| `setAutoDeclareVariables(boolean)` | If enabled, referencing an undeclared variable name auto-declares it instead of failing (useful for loosely-typed scripting contexts). |
| `build()` | Produces an immutable `NExprContext`. |
| `buildMutable()` | Produces a `NExprMutableContext` you can keep mutating after construction. |

### 3.1 Generic resolvers

Beyond the per-kind resolvers (`NExprFunctionResolver`, `NExprVarResolver` — see §5 and §4), the builder also accepts two more general resolver shapes:

```java
public interface NExprResolver {
    default NOptional<NExprFunction> getFunction(String fctName, NExprNodeValue[] args, NExprContext context) {
        return NOptional.ofEmpty(() -> NMsg.ofC("function not found %s", fctName));
    }
    default NOptional<NExprFunction> getConstruct(String constructName, NExprNodeValue[] args, NExprContext context) {
        return NOptional.ofEmpty(() -> NMsg.ofC("construct not found %s", constructName));
    }
    default NOptional<NExprOperator> getOperator(String opName, NExprOpType type, NExprNodeValue[] args, NExprContext context) {
        return NOptional.ofEmpty(() -> NMsg.ofC("operator not found %s of type %s", opName, type));
    }
    default NOptional<NExprVar> getVar(String varName, NExprContext context) {
        return NOptional.ofEmpty(() -> NMsg.ofC("var not found %s", varName));
    }
}

@FunctionalInterface
public interface NExprOperatorResolver {
    NOptional<NExprOperator> getOperator(String opName, NExprOpType type, NExprNodeValue[] args, NExprContext context);
}
```

- **`NExprResolver`** is a *composite* resolver — a single object that can, at your option, back functions, constructs, operators, **and** variables all at once (every method has a sensible "not found" default via `NOptional.ofEmpty(...)`, so you only override the kinds you actually want to resolve). Registered via `NExprContextBuilder.declareResolver(NExprResolver)`. Useful when one backing source (e.g. a scripting bridge, a bean/reflection adapter, an embedding host object) legitimately supplies more than one kind of symbol.
- **`NExprOperatorResolver`** is the operator-specific counterpart to `NExprFunctionResolver`/`NExprVarResolver` — a single-method resolver for dynamically supplying `NExprOperator`s by `(name, type)`, registered via `NExprContextBuilder.declareOperators(NExprOperatorResolver)`.

Note the consistent `NOptional.ofEmpty(() -> NMsg.ofC(...))` idiom for "not found" — a lazily-built diagnostic message rather than an eager string, so resolvers that are asked about hundreds of candidate names during lookup chains don't pay message-formatting cost unless the failure message is actually inspected.

---

## 4. Variables — `NExprVar`

```java
interface NExprVar extends NExprVarReader, NExprVarWriter {
    String name();
    Object get(NExprContext context);
    void set(Object value, NExprContext context);
}
```

Factory methods on `NExprVar`:

| Factory | Semantics |
|---|---|
| `ofVar(name)` | Plain read/write variable, initially `null`. |
| `ofVar(name, value)` | Plain read/write variable with an initial value. |
| `ofVar(name, reader, writer)` | Fully custom get/set behavior (e.g. backed by an external object/bean). |
| `ofLazyConst(name, reader)` | Computed once, lazily, then treated as constant. |
| `ofConst(name, value)` | Fixed constant value. |
| `ofReadOnly(name, reader)` | Read-only, computed via `reader` each time (not cached). |

On a `NExprMutableContext`:
- `declareVar(String name)` — declare with no value yet.
- `declareVar(NExprVar var)` — declare a fully custom variable.
- `declareConstant(String name, Object value)` — declare an immutable constant.
- `setVarValue(String varName, Object value)` — assign a value to an already-declared variable.
- `getOrDeclareVar(String name, Supplier<Object> initialValue)` — get-or-create in one call.
- `undeclareVar(NExprVar)` / `removeVar(NExprVar)` / `removeVar(String)` — remove a variable.

Bulk/dynamic variable resolution uses `NExprVarResolver`:

| Factory | Semantics |
|---|---|
| `ofConst(Function<String,Object>)` | Resolver whose values are treated as lazily-computed constants. |
| `ofReadOnly(Function<String,Object>)` | Resolver whose values are read-only/live. |
| `ofMap(Map<String,Object>)` | Backed by a plain `Map` — used in `TemplateTest` for injecting template variables. |
| `ofReadOnlyMap(Map<String,Object>)` | Read-only map-backed resolver. |

---

## 5. Functions & Constructs — `NExprFunction`

```java
interface NExprFunction {
    static NExprFunction of(String fctName, NExprCallHandler handler);
    String name();
    Object eval(NExprCallContext callContext);
}
```

NExpr distinguishes **functions** from **constructs** even though both are represented by `NExprFunction`:

- **Functions** (`getFunction` / `declareFunction` / `evalFunction`) — standard callable-by-name expressions, e.g. `printChunk(0)` (see `test8`/`test9`).
- **Constructs** (`getConstruct` / `declareConstruct` / `evalConstruct`) — a syntactically similar but semantically separate namespace, typically used for constructor-like or keyword-like call forms (kept distinct from functions so the same name can mean different things in each namespace, and so language-level forms like object construction don't collide with user functions).

Both are resolved either by direct declaration or via a resolver:
- `NExprFunctionResolver.getFunction(fctName, args, context)` — a functional interface, so custom resolution logic (e.g. reflection-based dispatch, bean method lookup) can be plugged in without declaring every function individually.

`NExprCallContextType` tags *how* a call is being evaluated: `FUNCTION`, `CONSTRUCT`, or `OPERATOR` (with alias parsing for strings like `"FCT"`, `"NEW"`/`"CONSTRUCTOR"`, `"OP"`).

For how to actually *implement* a function/construct handler (`NExprCallHandler`, `NExprCallContext`, `NExprNodeValue`) and for two worked examples (a reflection-based function resolver and a physics-formula evaluator), see expr-evaluation. The full built-in function table (`string`, `join`, `format*`, `isBlank`, …) also lives there.

---

## 6. The AST — `NExprNode`

Every successful `context.parse(expression)` returns `NOptional<NExprNode>`.

```java
public interface NExprNode {
    static NExprWordNode ofWord(String name);
    static NExprLiteralNode ofLiteral(Object name);

    NOptional<Object> eval(NExprContext context);
    NExprNodeType nodeType();
    List<NExprNode> children();
    String name();
}
```

### 6.1 Node types — `NExprNodeType` (complete enum)

```java
public enum NExprNodeType implements NEnum {
    FUNCTION,
    OPERATOR,
    WORD,
    LITERAL,
    INTERPOLATED_STR,
    IF,
}
```

| Value | Meaning | Example |
|---|---|---|
| `WORD` | A bare identifier/word token | `a` (`test3`); also the node checked in the built-in `=` and `++`/`--` handlers to confirm the left-hand side is an assignable variable name |
| `OPERATOR` | Any operator application (prefix/postfix/infix, including grouping `(`, indexing `[`, block `{`, and member access `.`) | `a&b`, `(a&&b)`, `a.b` |
| `LITERAL` | A literal value token (number, quoted string, boolean, `null`) | the `1` in `a.b>1` (`test12`) — see `NExprLiteralNode` / `NExprNode.ofLiteral(Object)` |
| `FUNCTION` | A function or construct call node, e.g. `f(a, b)` | `printChunk(0)` (`test8`) |
| `INTERPOLATED_STR` | A `$`-prefixed or Moustache-style interpolated string | `$'something for $v'` (`test10`) — see expr-templating |
| `IF` | An `if [(cond)] then [else]` expression | `if (a) 'hello' else 'hella'` (`test6`) — see §7 below |

### 6.2 Node subtypes seen in the tests

- `NExprWordNode` — a bare word/identifier node (e.g. the `c` operand in `a*b+c`, `test11`). Constructible directly via `NExprNode.ofWord(String)`.
- `NExprLiteralNode` — a literal value node (numbers, quoted strings, booleans — e.g. the `1` in `a.b>1`, `test12`). Constructible directly via `NExprNode.ofLiteral(Object)`.
- `NExprInterpolatedStringNode` — produced by `ofDollarInterpolatedString(...)` / `ofMoustacheInterpolatedString(...)`, and by parsing `$'...'` syntax directly (`test10`).
- `NExprNodeValue extends NExprNode` — the argument-wrapper type passed into every `NExprCallHandler`; see expr-evaluation.

### 6.3 Common node API in practice

- For an `OPERATOR` node, `name()` is the operator's symbol/name (`"+"`, `"."`, `"("`, `"["`, ...) and `children()` holds its operands in order.
- `toString()` renders the expression back out in a normalized, spaced infix form (e.g. `1+2*3` → `"1 + 2 * 3"`).
- `eval(context)` generally needs a *mutable* context for anything stateful (assignment, `++`/`--`, declaring vars on the fly).

### 6.4 Statements & sequencing

Multiple expressions/statements can be chained with `;`, and consecutive/trailing separators are tolerated:

```java
expr.parse("printChunk(0);;;;\n");                              // test8 — trailing empty statements OK
expr.parse("printChunk(0);;printChunk(0);;printChunk(0)\n");    // test9 — chained calls
expr.parse("if (a) 'hello' else {'hella'};x=3");                // test7 — block body `{ ... }`, followed by another statement
```

`{ ... }` acts as a block/grouping for statement bodies (as an `if`/`else` branch, per `test7`). `;` itself is a genuine left-associative infix *operator* (not just a parser-level separator) — see the built-ins table in expr-operators for exactly how it chains evaluation.

---

## 7. `if` / `else` Expressions

```java
NExprNode n = expr.parse("if (a) 'hello' else 'hella' end").get();
// n.nodeType() == NExprNodeType.IF
```

Supported forms (from the test suite and the `TemplateTest` doc-comment):
- `if cond thenExpr else elseExpr end` — single-line, both branches are expressions.
- Multi-branch `else if` chains are supported in the **template** layer (`<%:else if ...%>`) — see expr-templating.

---

## 8. Literal Mapping — `NExprLiteralMapper`

Every context (and builder) exposes a `literalMapper()`. This component is responsible for converting raw literal tokens encountered during parsing (numbers, quoted strings, booleans, etc.) into actual typed Java values used at evaluation time. You can supply a custom mapper via `NExprContextBuilder.literalMapper(NExprLiteralMapper mapper)` — useful if you want, e.g., all integer literals to become `BigDecimal`, or custom date/duration literal formats.

Under the hood, literal *tokenization* (before mapping) is done by `NStreamTokenizer` — see expr-tokenizer for how it decides a token is `TT_INT`/`TT_LONG`/`TT_BIG_INT`/`TT_FLOAT`/`TT_DOUBLE`/`TT_BIG_DECIMAL` in the first place.

---

## 9. API Reference Summary

### `NExprContext` (read-only)

| Method | Returns | Purpose |
|---|---|---|
| `getFunction(name, args...)` | `NOptional<NExprFunction>` | Look up a function by name/args. |
| `getConstruct(name, args...)` | `NOptional<NExprFunction>` | Look up a construct by name/args. |
| `getOperator(name, type, args...)` | `NOptional<NExprOperator>` | Look up an operator by name+kind. |
| `operators()` | `List<NExprOperator>` | All currently registered operators. |
| `getVar(name)` | `NOptional<NExprVar>` | Look up a variable. |
| `getVarValue(name)` | `NOptional<Object>` | Shortcut for current value of a variable. |
| `childContext()` | `NExprContextBuilder` | Start a new builder layered on top of this context. |
| `evalFunction/evalConstruct/evalOperator(...)` | `NOptional<Object>` | Directly invoke by name without pre-parsing an expression. |
| `evalInfixOperator/evalPrefixOperator/evalPostfixOperator(...)` | `NOptional<Object>` | Directly invoke a specific operator kind. |
| `parse(expression)` | `NOptional<NExprNode>` | Parse a full expression string into an AST. |
| `bindLiteral(Object)` / `bindNode(NExprNode)` | `NExprNodeValue` | Wrap a raw value or an existing node as a call argument. |
| `findCommonInfixOp/PrefixOp/PostfixOp(...)` | see expr-operators | Type-based operator-overload resolution. |
| `ofDollarInterpolatedString/ofMoustacheInterpolatedString(String)` | `NExprInterpolatedStringNode` | Build interpolated string nodes programmatically. |
| `ofTemplate()` | `NExprTemplate` | Obtain the templating façade over this context. |
| `literalMapper()` | `NExprLiteralMapper` | Current literal-to-value mapper. |

### `NExprMutableContext` (adds to the above)

`declareFunction`, `declareConstruct`, `declareVar`, `declareConstant`, `declareOperator`, `setVarValue`, `getOrDeclareVar`, and the symmetric `undeclare*` / `remove*` family (by instance or by name).

### `NExprContextBuilder`

See §3.

---

## 10. Design Notes & Practical Patterns

- **Layered contexts:** `childContext()` and the fact that `NExprContextBuilder.of()` itself starts from a child of a root context suggest NExpr is meant to be composed in layers — e.g. a shared "base" context with math/physics constants, further specialized per use case (a CLI expression filter vs. a template renderer) without re-declaring everything.
- **Restricted DSLs via operator pruning:** you can start from `declareBuiltins()` and subtract down to exactly the operator surface you want to expose to untrusted or simplified input — see [`NExpr-Operators.md §removing/restricting`](./NExpr-Operators.md). This is a good pattern for CLI query languages (e.g. filter expressions in `nuts` commands) where you don't want full scripting power.
- **Functions vs. constructs as separate namespaces:** if you're integrating NExpr into a larger framework (e.g. NARU), keep in mind `function` and `construct` calls are resolved independently — a name can be safely reused across both without collision, but that also means declaring a function does *not* make it callable as a construct.
- **`NExprResolver` for multi-kind backing sources:** if one object naturally backs several symbol kinds at once (e.g. an embedding host exposing both variables and methods), prefer a single `NExprResolver` over separately wiring a `NExprVarResolver` and a `NExprFunctionResolver` that both delegate to the same backing object.
- **`NExprCommonOp` decouples semantics from spelling:** if you expose custom operator names/aliases to end users but still want generic numeric/string logic to "know" which one is "plus", implement against `NExprCommonOp` + `findCommonInfixOp` rather than hardcoding operator name strings — see expr-operators.
- **Operators that mutate state (`=`, `+=`, `++`, `--`, …) deliberately control *when* their operands get evaluated**, by calling `.eval(context)` on specific `NExprNodeValue` args themselves rather than letting the engine eager-evaluate all arguments up front — see expr-evaluation.

---

## 11. Remaining Gaps

1. **`NOptional`'s failure-path contract in full detail** — e.g. whether parse errors carry position/line info. `NOptional` is documented separately in the Nuts ecosystem; this doc set only covers the small set of accessor methods (`.get()`, `.orNull()`, `.isPresent()`, …) as they're actually used in NExpr call sites.
2. **`TERNARY_CMP` precedence tier** — confirmed reserved for an upcoming ternary `? :` operator, not yet wired up.
3. **`NExprInterpolatedStringNode`'s full API** beyond construction — only its role (interpolated-string AST node) is evidenced, not its complete method set.

Happy to fold in answers to any of the above and expand the relevant doc.