//package net.thevpc.nuts.runtime.standalone.xtra.expr;
//
//import net.thevpc.nuts.expr.*;
//
//import java.util.List;
//
//public class DefaultNExprConstructDeclaration implements NExprConstructDeclaration {
//    private String name;
//    private NExprFct impl;
//
//    public DefaultNExprConstructDeclaration(String name, NExprFct impl) {
//        this.name = name;
//        this.impl = impl;
//    }
//
//    @Override
//    public Object eval(List<NExprNodeValue> args, NExprContext context) {
//        return impl.eval(name, args, context);
//    }
//
//    @Override
//    public String getName() {
//        return name;
//    }
//}
