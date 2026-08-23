# NExpr — Evaluation

Part of the NExpr doc set. Covers how a parsed `NExprNode` actually gets executed: `NExprCallContext`, `NExprCallHandler`, `NExprNodeValue`, custom function/operator resolvers, the complete built-in function table, and two worked examples.

---

## 1. The evaluation pipeline

Parsing (`context.parse(expr)`) only produces an `NExprNode`; nothing runs until you call `eval(...)`.

```
String expression
   │  context.parse(expression)
   ▼
NOptional<NExprNode>
   │  .get()
   ▼
NExprNode
   │  node.eval(context)
   ▼
NOptional<Object>
   │  .get()
   ▼
Object   (the actual runtime result: Boolean, Number, String, custom object, …)
```

`NOptional<T>` has its own documentation elsewhere in the Nuts ecosystem (it's a tri-state optional — `present` / `empty` / `error` — richer than `java.util.Optional`), so it isn't re-explained here. In NExpr call sites specifically, you'll mostly see:

| Call | Used for |
|---|---|
| `.get()` | Unwrap, throwing if absent/errored — the default when failure should propagate loudly (e.g. `n.eval(expr).get()` throughout the test suite). |
| `.orNull()` | Unwrap to `null` on absence/error — for optional/best-effort values (e.g. inside `firstNonNull`, `join`). |
| `.ifErrorThrow()` | Combined with `.orNull()` in production code (`x.value().ifErrorThrow().orNull()`) — propagate any real error as an exception, but still tolerate a legitimately absent/`null` value. |
| `.isPresent()` | Presence-only checks, e.g. the `isNumber`/`isBoolean` built-ins. |

---

## 2. `NExprCallContext` / `NExprCallHandler`

Every function, construct, and operator call is driven by these two interfaces:

```java
public interface NExprCallContext {
    String name();
    List<NExprNodeValue> args();
    NOptional<NExprNodeValue> arg(int index);
    NExprContext context();
    NExprCallContextType contextType();     // FUNCTION, CONSTRUCT, or OPERATOR
    NExprOpType operatorType();
    int operatorPrecedence();
    NOperatorAssociativity operatorAssociativity();
}

@FunctionalInterface
public interface NExprCallHandler {
    Object eval(NExprCallContext callContext);
}
```

`NExprCallHandler` is the single method you implement for **every** custom function, construct, or operator. `NExprCallContext` gives you everything about the current call site: its resolved `name()`, its (unevaluated!) `args()`, the `context()` to evaluate against, which of the three call kinds this is, and — for operators — the operator's own type/precedence/associativity (rarely needed inside a handler, but available for handlers that want to behave differently depending on how they were invoked).

---

## 3. `NExprNodeValue`

```java
public interface NExprNodeValue extends NExprNode {
    NExprNode node();
    NOptional<Object> value();
}
```

`NExprNodeValue` **extends `NExprNode`** — every call argument is itself a full AST node, not a pre-computed value. It adds:
- `node()` — the underlying wrapped `NExprNode` (useful if you need to inspect structure, e.g. checking `nodeType() == WORD` the way the built-in `=` operator does — since `NExprNodeValue` inherits `nodeType()`/`children()`/`name()`/`eval()` directly from `NExprNode`, `node()` mainly matters when you specifically want the *unwrapped* node rather than going through the value-wrapper's own inherited AST methods).
- `value()` — resolve straight to a value (`NOptional<Object>`), the "just give me the result" shortcut most functions use instead of calling `.eval(context)` themselves.

Because it's a full `NExprNode`, an `NExprNodeValue` also has its own `eval(NExprContext context)` (inherited), which is what lets an operator evaluate an argument against a *different* context than the one it was originally bound to, or defer evaluation entirely.

It's what `context.bindLiteral(Object)` and `context.bindNode(NExprNode)` produce, and what you pass back into `context.evalInfixOperator/evalPrefixOperator/evalPostfixOperator(...)` when a handler needs to delegate to *another* operator — e.g. how `+=` computes its result:

```java
Object newValue = context.evalInfixOperator(
        "+",
        context.bindLiteral(oldValue),
        context.bindLiteral(partValue)
).get();
```

### Two evaluation styles, and why both exist

Every built-in handler follows one of two patterns:

- **Value-only functions** call `.value()` on each argument and don't care whether/how it was computed — e.g. `string`, `boolean`, `join`, `format*`, `isBlank`.
- **Structural/stateful operators** call `.eval(context)` themselves, deliberately, and often inspect the *unevaluated* node first — e.g. `=` checks `args.get(0).nodeType() == WORD` **before** evaluating anything, so it can validate the left-hand side is an assignable variable name and resolve it via `context.getVar(varName)`, rather than receiving an already-computed value it could no longer trace back to a variable. The same pattern shows up in `++`/`--` and all compound assignments (`+=` etc.): read the *old* value first, compute the *new* one, then write it back — an order that's only possible because the operator controls evaluation timing itself instead of the engine eager-evaluating all arguments up front.

---

## 4. Custom function/operator resolution

Beyond declaring functions one at a time with `declareFunction(NExprFunction.of(name, handler))`, you can register a whole **resolver** that computes callables on demand:

```java
@FunctionalInterface
public interface NExprFunctionResolver {
    NOptional<NExprFunction> getFunction(String fctName, NExprNodeValue[] args, NExprContext context);
}

@FunctionalInterface
public interface NExprOperatorResolver {
    NOptional<NExprOperator> getOperator(String opName, NExprOpType type, NExprNodeValue[] args, NExprContext context);
}
```

registered via `NExprContextBuilder.declareFunctions(...)` / `.declareOperators(...)` respectively (or `.declareConstructs(...)` for the construct namespace). There's also a composite `NExprResolver` that can back functions, constructs, operators, *and* variables from one object — see [`NExpr.md §3.1`](./NExpr.md).

### 4.1 Worked example — reflection-based function dispatch

The pattern used in production to expose an arbitrary Java object's methods as callable expression functions, gating a feature flag with a user-supplied `onlyIf` expression:

```java
NExprMutableContext d = NExprContextBuilder.of()
        .declareBuiltins()
        .declareFunctions((fctName, args, context) -> {
            Method m = TypeHelper.getDeclaredMethodRuntime(
                    targetClass, fctName,
                    Arrays.stream(args).map(x -> x == null ? null : x.getClass()).toArray(Class[]::new)
            );
            if (m == null) {
                throw new IllegalArgumentException("not found method " + fctName + Arrays.asList(args));
            }
            m.setAccessible(true);
            return NOptional.of(NExprFunction.of(fctName, callContext -> {
                List<NExprNodeValue> callArgs = callContext.args();
                try {
                    return m.invoke(targetInstance,
                            callArgs.stream().map(x -> x.value().ifErrorThrow().orNull()).toArray());
                } catch (InvocationTargetException e) {
                    throw new RuntimeException(NException.getErrorMessage(e.getCause()), e.getCause());
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(NException.getErrorMessage(e), e);
                }
            }));
        })
        .buildMutable();

NExprNode parsed = d.parse(onlyIfExpression.trim()).get();
boolean enabled = (boolean) parsed.eval(d).get();
```

This is the "is this feature/command enabled" boolean-gating shape: a user-authored expression string is evaluated once per check, and any function call in it transparently dispatches to a real Java method via reflection.

### 4.2 Worked example — evaluating an antenna design constraint

A different shape of the same idea — not reflection dispatch, but a small, user-editable **formula/constraint** evaluated against physical constants and runtime design variables, closer to the kind of MoM/antenna tooling in Hadruwaves:

```java
NExprMutableContext ctx = NExprContextBuilder.of()
        .declareBuiltins()
        .declareMathFunctions()
        .declarePhysicsConstants()
        .buildMutable();

ctx.declareVar("f");          // operating frequency (Hz)
ctx.declareVar("epsilonR");   // substrate relative permittivity
ctx.declareVar("W");          // patch width (m)

ctx.setVarValue("f", 2.4e9);
ctx.setVarValue("epsilonR", 4.4);
ctx.setVarValue("W", 0.0286);

// A constraint a user could type into a config field:
// "is the free-space wavelength at f at least 5x the patch width?"
NExprNode constraint = ctx.parse("(C / f) >= 5 * W").get();
boolean ok = (boolean) constraint.eval(ctx).get();

// A derived quantity using a physics constant directly:
NExprNode lambdaExpr = ctx.parse("C / f").get();
double lambda = ((Number) lambdaExpr.eval(ctx).get()).doubleValue();
```

The general recipe: build a context once with `declareBuiltins()` + `declareMathFunctions()` + `declarePhysicsConstants()`, expose a handful of `declareVar`s for the caller's actual inputs, then repeatedly `parse`-and-`eval` *user- or config-supplied* formula strings against it. Useful for design-rule checks, validation rules, or computed defaults — anywhere you want end users to author small formulas without embedding a full scripting language.

---

## 5. Complete built-in function table (`declareBuiltins()`)

| Name | Aliases | Behavior |
|---|---|---|
| `string(x)` | — | Converts `x` to `String` via `NLiteral.of(x).asString()` |
| `boolean(x)` | — | Converts `x` to `Boolean` |
| `double(x)` | — | Converts `x` to `Double` |
| `long(x)` | — | Converts `x` to `Long` |
| `int(x)` | — | Converts `x` to `Integer` |
| `float(x)` | — | Converts `x` to `Float` |
| `isNumber(x)` | — | `true` if `x` parses as a number |
| `isBoolean(x)` | — | `true` if `x` parses as a boolean |
| `isBlank(x)` | — | `true` if `x` is blank per `NBlankable.isBlank` |
| `firstNonNull(a, b, …)` | — | Returns the first non-`null` argument |
| `firstNonBlank(a, b, …)` | — | Returns the first non-blank argument |
| `format(pattern, args…)` | `formatC` | `NMsg.ofC(pattern, args)` — C-style message formatting |
| `formatJ(pattern, args…)` | — | `NMsg.ofJ(pattern, args)` — Java-style message formatting |
| `formatV(pattern, args…)` | — | `NMsg.ofV(...)` — supports a trailing `Map`, `Function`, `NMsgParam`, or `NMsgParam[]` as the substitution source |
| `join(sep, iterable)` | — | Joins an `Iterable` with `sep`; falls back to `String.valueOf` if the second arg isn't iterable |

### Math functions (`declareMathFunctions()`)

All single- or double-argument wrappers around `java.lang.Math`, with arguments coerced via `NLiteral.of(...).asDouble()`:

`sin`, `cos`, `tan`, `sinh`, `cosh`, `tanh`, `asin`, `acos`, `atan2(y, x)`, `toRadians`, `toDegrees`, `exp`, `log`, `log10`, `sqrt`, `cbrt`, `abs`, `signum`, `ulp`, `ceil`, `floor`, `rint`, `round`, `pow(x, y)`, `max(a, b)`, `min(a, b)`.

### Math constants (`declareMathConstants()`)

| Name | Value |
|---|---|
| `pi`, `PI`, `π` | `Math.PI` |
| `E` | `Math.E` |

### Physics constants (`declarePhysicsConstants()`)

| Name | Value | Meaning |
|---|---|---|
| `C` | 299792458.0 | speed of light (m/s) |
| `ε0` | 8.8541878128×10⁻¹² | vacuum permittivity (F/m) |
| `μ0` | 1.25663706212×10⁻⁶ | vacuum permeability (H/m) |
| `η0` | 376.730313668 | free-space impedance (Ω), = μ0·C |
| `e` | 1.602176634×10⁻¹⁹ | elementary charge (C) |
| `h` | 6.62607015×10⁻³⁴ | Planck constant (J·s) |
| `ħ` | 1.054571817×10⁻³⁴ | reduced Planck constant (J·s) |
| `kB` | 1.380649×10⁻²³ | Boltzmann constant (J/K) |
| `NA` | 6.02214076×10²³ | Avogadro constant (mol⁻¹) |
| `me` | 9.1093837015×10⁻³¹ | electron mass (kg) |
| `mp` | 1.67262192369×10⁻²⁷ | proton mass (kg) |
| `G` | 6.67430×10⁻¹¹ | gravitational constant (m³·kg⁻¹·s⁻²) |
| `g` | 9.80665 | standard gravity (m/s²) |
| `R` | 8.314462618 | ideal gas constant (J/mol/K) |
| `σ` | 5.670374419×10⁻⁸ | Stefan–Boltzmann constant (W/m²/K⁴) |

> Note the constants use their proper physics symbols (`ε0`, `μ0`, `η0`, `ħ`, `σ`, `π`) rather than ASCII transliterations — if you want ASCII-friendly aliases (`epsilon0`, `mu0`, `eta0`, `hbar`, `sigma`) for keyboard-friendly formula entry, declare them yourself as additional `NExprVar.ofConst(...)` bindings alongside `declarePhysicsConstants()`.

