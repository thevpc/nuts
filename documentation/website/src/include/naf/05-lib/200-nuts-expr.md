---
title: NExpr
---


**nuts** expressions


```java
NExprMutableDeclarations expr = NExprs.of().newMutableDeclarations();
NExprNode n1 = expr.parse("1+2*3").get();
NExprNode n = expr.parse("a.b>1").get();
```


```java
NExprs nExprs = NExprs.of();
NDocNExprVar v = new NDocNExprVar();
decl = nExprs.newMutableDeclarations(true, new NExprEvaluator() {
    @Override
    public NOptional<NExprVar> getVar(String varName, NExprDeclarations context2) {
        return NOptional.of(new MyVar());
    }
});

decl.declareConstant("cwd", System.getProperty("user.dir"));
decl.declareFunction("myfct", new MyFct());
```


