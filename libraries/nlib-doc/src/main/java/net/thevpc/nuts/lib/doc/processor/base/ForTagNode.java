package net.thevpc.nuts.lib.doc.processor.base;

import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NLiteral;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Collections;
import java.util.Iterator;

class ForTagNode extends TagNode {
    private final String exprLang;
    private String expr;
    private String varName;
    private String initExpr;
    private String condExpr;
    private String incExpr;
    private String iterableExpr;
    private boolean iterableType;
    TagNode body;

    public ForTagNode(String exprLang, String expr) {
        this.exprLang = exprLang;
        this.expr = expr.trim();
        int i = expr.indexOf(':');
        int j = expr.indexOf(',');
        if (i <= 0 && j < 0) {
            throw new IllegalArgumentException("for expression must be like  \":for <var>:<expr>\" or :for <init>,<cond>,<inc>");
        }
        if (i > 0) {
            varName = expr.substring(0, i);
            iterableExpr = expr.substring(i + 1).trim();
            iterableType = true;
        } else {
            throw new IllegalArgumentException("not supported yet :for <init>,<cond>,<inc>");
        }
    }

    @Override
    public void run(ProcessStreamContext ctx) throws IOException {
        if (iterableType) {
            Object o = ctx.context.eval(new ByteArrayInputStream(iterableExpr.getBytes()), exprLang);
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
                    throw new IllegalArgumentException("not iterable " + iterableExpr + " as " + o.getClass());
                }
            } else {
                it = Collections.emptyIterator();
            }
            while (it.hasNext()) {
                Object v = it.next();
                ctx.context.setVar(varName, v);
                if(body!=null){
                    body.run(ctx);
                }
            }
        } else {
            if (!NBlankable.isBlank(initExpr)) {
                ctx.context.eval(initExpr, exprLang);
            }
            while (true) {
                if (!NBlankable.isBlank(condExpr)) {
                    if (NLiteral.of(ctx.context.eval(condExpr, exprLang)).asBoolean().orElse(false)) {
                        break;
                    }
                }
                if(body!=null){
                    body.run(ctx);
                }
                if (!NBlankable.isBlank(incExpr)) {
                    ctx.context.eval(new ByteArrayInputStream(incExpr.getBytes()), exprLang);
                }
            }
        }
    }
}
