---
title: Expression Parsing & Evaluation with NExpr
subTitle:  |
  NExpr is a powerful expression parser and evaluator designed to
  handle dynamic, runtime expressions within the Nuts ecosystem. It
  allows declaration of constants, variables, and functions, supports
  custom operators, and can evaluate expressions in a context-aware
  way. This makes it ideal for dynamic queries, runtime computation,
  scripting, and configuration-driven logic in Java applications — all
  while remaining type-safe, extensible, and embeddable.
contentType: java
---

NExprMutableDeclarations d = NExprs.of().newMutableDeclarations();
d.declareConstant("pi", Math.PI);
d.declareFunction("sin", (name, args, ctx) -> {
    NExprNodeValue a = args.get(0);
    return Math.sin(asDouble(a.eval(ctx), rendererContext));
});

NOptional&lt;NExprNode> ne = d.parse("sin(x*pi)");
if (ne.isPresent()) {
    NExprNode node = ne.get();
    NDoubleFunction fct = x -> {
        NOptional&lt;Object> r = node.eval(new NExprEvaluator() {
            @Override
            public NOptional&lt;NExprVar> getVar(String varName, NExprDeclarations ctx) {
                return "x".equals(varName)
                        ? Optional.of(x)
                        : NOptional.ofNamedEmpty("var " + varName);
            }
        });
        return NTxExprHelper.asDouble(r, rendererContext);
    };

    double result = fct.apply(0.5); // evaluates sin(0.5*pi)
}
