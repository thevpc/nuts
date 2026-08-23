---
title: Expression Templating
---

# NExpr — Templating

Part of the [NExpr doc set](./NExpr.md). Covers `NExprTemplate`: its built-in styles, the Moustache directive set, `$`-string interpolation, and how the templating layer is meant to be embedded inside other host languages/formats.

---

## 1. What `NExprTemplate` is

`NExprTemplate` is a text-templating layer built directly on top of the expression engine — every `{ {...}}` (or equivalent) block is just an NExpr expression/statement, evaluated against the same kind of `NExprContext` used everywhere else in NExpr. That means anything you can `parse()`/`eval()` as a plain expression — function calls, operators, `if`/`else`, variable access — is available inside a template block, with no separate templating-specific expression grammar to learn.

Obtained from a context via `context.ofTemplate()`, then configured with a syntax **style**, and driven with `processString(String)`:

```java
NExprTemplate tpl = context.ofTemplate().withMoustacheStyle();
String out = tpl.processString(someTemplateText);
```

## 2. Built-in styles

NExpr ships more than one delimiter convention, so the same underlying directive language can be dropped into different kinds of host documents without looking out of place:

- **Moustache style** — `{ { }}` delimiters, in the spirit of Mustache/Handlebars-family templating. This is the style demonstrated in `TemplateTest` and in the quick-start example below.
- **JSP-style** — a delimiter convention in the spirit of JSP/EL-style templating (`<% %>` / `${ }`-flavored), for embedding into contexts where that's the more natural or expected marker syntax (e.g. HTML-adjacent templates, or teams already used to JSP-family tag delimiters).
- **Custom delimiters** — because the directive *language* is decoupled from the delimiter *syntax*, NExpr's templating is extensible with your own prefix/suffix markers, letting you embed the same `{ {:if}}`/`{ {:for}}`/interpolation semantics inside essentially any host language or file format (config files, other DSLs, code-generation templates, …) simply by choosing marker characters that don't collide with that host format's own syntax — without having to reimplement the underlying `if`/`for`/expression evaluation logic each time.

:::info
The exact method names/signatures for selecting the JSP-style preset and for supplying fully custom prefix/suffix delimiters weren't part of the sources reviewed for this doc set — only `.withMoustacheStyle()` was directly evidenced. If you can share that part of the `NExprTemplate` API, this section can be filled in with exact usage.
:::

## 3. Moustache-style directives

| Syntax                                                                        | Meaning |
|-------------------------------------------------------------------------------|---|
| `<%: statement %>`                                                            | Execute a statement, emit nothing |
| `<% expression %>`                                                            | Evaluate an expression and emit its result |
| `<%:for varName:<expression %> ... <%:end %>`                                 | Loop over an iterable expression, binding `varName` each iteration |
| `<%:for varName,index:<expression}} ... <%:end %>`                            | Same, also binding a loop index |
| `<%:if expression %> ... <%:else if expression %> ... <%:else %> ... <%:end %>` | Conditional block, with any number of `else if` branches and an optional `else` |

Since the expression inside every block is a normal NExpr expression, anything declared on the underlying context — custom functions, operators, physics/math constants, whatever your app registers — is usable directly inside `{ { }}`.

### 3.1 Worked example

```java
Map<String, Object> vars = new HashMap<>();
vars.put("world", "Earth");
vars.put("yellow", true);
vars.put("blue", true);
vars.put("my", true);

String out = NExprContextBuilder.of()
        .declareBuiltins()
        .declareVars(NExprVarResolver.ofMap(vars))
        .build()
        .ofTemplate().withJspStyle()
        .processString("hello <%:if yellow %> <%world%> <%:else if blue %> my  <%:else%> World <%:end%>");
// yellow=true            -> "hello  Earth "
// yellow=false, blue=true -> "hello  my  "
```

```java
Map<String, Object> vars = new HashMap<>();
vars.put("world", "Earth");
vars.put("yellow", true);
vars.put("blue", true);
vars.put("my", true);

String out = NExprContextBuilder.of()
        .declareBuiltins()
        .declareVars(NExprVarResolver.ofMap(vars))
        .build()
        .ofTemplate().withMoustacheStyle()
        .processString("hello { {:if yellow }} { {world}} { {:else if blue }} my  { {:else}} World { {:end}}");
// yellow=true            -> "hello  Earth "
// yellow=false, blue=true -> "hello  my  "
```

## 4. `$`-style string interpolation

Independent of the Moustache templating layer, the **expression parser itself** understands dollar-interpolated string literals inline in any expression — not just inside a template:

```java
expr.parse("$'something for $v'");   // parses to an NExprInterpolatedStringNode
```

which evaluates to a string with `$v` substituted by the current value of variable `v`. The context also exposes this programmatically, without going through `parse(...)`:

```java
NExprInterpolatedStringNode ofDollarInterpolatedString(String a);
NExprInterpolatedStringNode ofMoustacheInterpolatedString(String a);
```

Note there are two distinct interpolation flavors available at the node level — a dollar-prefixed quoted-string flavor (`$'...'`, `$"..."`, `` $`...` ``, driven by the tokenizer's `TT_ISTR_SQ`/`TT_ISTR_DQ`/`TT_ISTR_AQ` token types — see [`NExpr-Tokenizer.md`](./NExpr-Tokenizer.md)) and a Moustache-flavored one (`ofMoustacheInterpolatedString`), which is presumably what backs `{ {expression}}` substitution inside a template body itself.

## 5. Design takeaway: templating as a thin skin over the expression engine

Because directive *semantics* (`if`/`for`/expression-eval) live in the shared expression engine and only the *delimiter syntax* varies by style, NExpr's templating isn't really a separate feature from the rest of NExpr — it's the same context, the same operators/functions/constants, the same parser, wrapped with a marker-recognition pass. Practically, this means:

- Any restriction you apply to a context for security/DSL reasons (see [`NExpr-Operators.md §3 removing/restricting`](./NExpr-Operators.md)) applies equally whether that context is used for plain `parse()`/`eval()` or for `ofTemplate()` — there's no separate "template sandbox" to configure.
- Custom functions/operators/constants declared once are usable both as plain expressions *and* inside template blocks, with no re-registration needed.
- Choosing (or defining) a delimiter style is purely about not colliding with the host document's own syntax — it doesn't change what the templating layer is capable of expressing.
