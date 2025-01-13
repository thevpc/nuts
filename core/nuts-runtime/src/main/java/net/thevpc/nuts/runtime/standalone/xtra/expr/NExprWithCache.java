package net.thevpc.nuts.runtime.standalone.xtra.expr;

import net.thevpc.nuts.expr.NExprDeclarations;
import net.thevpc.nuts.expr.NExprOpDeclaration;
import net.thevpc.nuts.expr.NExprOpType;
import net.thevpc.nuts.expr.NToken;

import java.util.*;

public class NExprWithCache {
    NExprDeclarations evaluator;

    public NExprWithCache(NExprDeclarations evaluator) {
        this.evaluator = evaluator;
    }


    public NExprOpDeclaration getOp(NToken t, NExprOpType opType) {
        if (t == null) {
            return null;
        }
        switch (t.ttype) {
            case '(':
            case ')':
            case '[':
            case ']':
            case '{':
            case '}':
            case NToken.TT_SPACE:
            case NToken.TT_STRING_LITERAL:
            case NToken.TT_EOL:
            case NToken.TT_EOF:
            case NToken.TT_INT:
            case NToken.TT_LONG:
            case NToken.TT_BIG_INT:
            case NToken.TT_FLOAT:
            case NToken.TT_DOUBLE:
            case NToken.TT_BIG_DECIMAL:
                return null;
        }
        String s = t.sval;
        if (s == null) {
            s = String.valueOf((char) t.ttype);
        }
        return evaluator.getOperator(s, opType).orNull();
    }

    @Override
    public String toString() {
        return "NutsExprWithCache{" +
                "evaluator=" + evaluator +
                '}';
    }
}
