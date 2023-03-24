package net.thevpc.nuts.runtime.standalone.xtra.expr;

import net.thevpc.nuts.expr.NExprDeclarations;
import net.thevpc.nuts.expr.NExprOpDeclaration;
import net.thevpc.nuts.expr.NExprOpType;

import java.util.*;

class NExprWithCache {
    NExprDeclarations evaluator;
    int[] precedences;

    public NExprWithCache(NExprDeclarations evaluator) {
        this.evaluator = evaluator;
        SortedSet<Integer> precedences = new TreeSet<>();
        for (int f : evaluator.getOperatorPrecedences()) {
            precedences.add(f);
        }
        this.precedences = precedences.stream().mapToInt(Integer::intValue).toArray();
    }

    public boolean isOp(String str, NExprOpType opType, int precIndex) {
        NExprOpDeclaration o = evaluator.getOperator(str, opType).orNull();
        if (o != null) {
            return o.getPrecedence() == this.precedences[precIndex];
        }
        return false;
    }

    @Override
    public String toString() {
        return "NutsExprWithCache{" +
                "evaluator=" + evaluator +
                ", precedences=" + Arrays.toString(precedences) +
                '}';
    }
}
