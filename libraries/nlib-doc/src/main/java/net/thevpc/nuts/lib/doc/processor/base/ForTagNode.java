package net.thevpc.nuts.lib.doc.processor.base;

import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NLiteral;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Collections;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class ForTagNode extends TagNode {
    private final String exprLang;
    private String expr;
    private VarLoopInfo varLoopInfo;
    TagNode body;
    private static String WN = "[a-zA-Z][a-zA-Z0-9_]*";
    static Pattern forVarPattern = Pattern.compile("(?<varName>" + WN + ")\\s*(,\\s*(?<indexName>" + WN + "))?\\s*:(?<any>.*)");

    public ForTagNode(String exprLang, String expr) {
        this.exprLang = exprLang;
        this.expr = expr.trim();
        Matcher m1 = forVarPattern.matcher(expr.trim());
        if (m1.matches()) {
            varLoopInfo = new VarLoopInfo(m1.group("varName"), m1.group("indexName"), m1.group("any"));
        } else {
            throw new IllegalArgumentException("for expression must be like  \":for <var>:<expr>\" or :for <init>,<cond>,<inc>");
        }
    }

    private static class VarLoopInfo {
        String varName;
        String indexName;
        String iterableExpr;

        public VarLoopInfo(String varName, String indexName, String iterableExpr) {
            this.varName = varName;
            this.indexName = indexName;
            this.iterableExpr = iterableExpr;
        }
    }

    @Override
    public void run(ProcessStreamContext ctx) throws IOException {
        if (varLoopInfo != null) {
            Object o = ctx.context.eval(varLoopInfo.iterableExpr, exprLang);
            Iterator it = null;
            if (o != null) {
                if (o.getClass().isArray()) {
                    int len = Array.getLength(o);
                    it = new Iterator() {
                        int i = 0;

                        @Override
                        public boolean hasNext() {
                            return i < len;
                        }

                        @Override
                        public Object next() {
                            Object v = Array.get(o, i);
                            i++;
                            return v;
                        }
                    };
                } else if (o instanceof Iterator) {
                    it = (Iterator) o;
                } else if (o instanceof Iterable) {
                    it = ((Iterable) o).iterator();
                } else {
                    throw new IllegalArgumentException("not iterable " + varLoopInfo.iterableExpr + " as " + o.getClass());
                }
            } else {
                it = Collections.emptyIterator();
            }
            int index = 0;
            while (it.hasNext()) {
                Object v = it.next();
                ctx.context.setVar(varLoopInfo.varName, v);
                if (!NBlankable.isBlank(varLoopInfo.indexName)) {
                    ctx.context.setVar(varLoopInfo.indexName, index);
                }
                if (body != null) {
                    body.run(ctx);
                }
                index++;
            }
        } else {
//            if (!NBlankable.isBlank(initExpr)) {
//                ctx.context.eval(initExpr, exprLang);
//            }
//            while (true) {
//                if (!NBlankable.isBlank(condExpr)) {
//                    if (NLiteral.of(ctx.context.eval(condExpr, exprLang)).asBoolean().orElse(false)) {
//                        break;
//                    }
//                }
//                if(body!=null){
//                    body.run(ctx);
//                }
//                if (!NBlankable.isBlank(incExpr)) {
//                    ctx.context.eval(new ByteArrayInputStream(incExpr.getBytes()), exprLang);
//                }
        }

    }

    @Override
    public String toString() {
        return "For{" +
                "exprLang='" + exprLang + '\'' +
                ", expr='" + expr + '\'' +
                ", varLoopInfo=" + varLoopInfo +
                ", body=" + body +
                '}';
    }
}
