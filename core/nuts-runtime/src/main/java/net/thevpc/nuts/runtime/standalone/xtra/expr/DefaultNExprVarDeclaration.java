//package net.thevpc.nuts.runtime.standalone.xtra.expr;
//
//import net.thevpc.nuts.expr.NExprContext;
//import net.thevpc.nuts.expr.NExprVar;
//import net.thevpc.nuts.expr.NExprVarDeclaration;
//import net.thevpc.nuts.text.NMsg;
//import net.thevpc.nuts.util.NIllegalArgumentException;
//
//public class DefaultNExprVarDeclaration implements NExprVarDeclaration {
//    private String name;
//    private NExprVar impl;
//    private NExprVar v;
//
//    public DefaultNExprVarDeclaration(String name, NExprVar impl) {
//        this.name = name;
//        this.impl = impl;
//    }
//
//    @Override
//    public String getName() {
//        return name;
//    }
//
//    @Override
//    public Object get(NExprContext context) {
//        return impl.get(name, context);
//    }
//
//    @Override
//    public void set(Object value, NExprContext context) {
//        if(!(context instanceof NExprMutableContextImpl)){
//            throw new NIllegalArgumentException(NMsg.ofC("cannot set variable %s in a readonly context",name));
//        }
//        impl.set(name, value, context);
//    }
//
//    @Override
//    public NExprVar asVar() {
//        if (v == null) {
//            v = new NExprVar() {
//                @Override
//                public Object get(String name, NExprContext context) {
//                    return DefaultNExprVarDeclaration.this.get(context);
//                }
//
//                @Override
//                public Object set(String name, Object value, NExprContext context) {
//                    return DefaultNExprVarDeclaration.this.set(value, context);
//                }
//            };
//        }
//        return v;
//    }
//}
