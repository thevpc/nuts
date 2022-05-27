package net.thevpc.nuts.runtime.standalone.xtra.expr;

import net.thevpc.nuts.util.*;

import java.util.*;

class NutsExprWithCache {
    NutsExprDeclarations evaluator;
    int[] precedences;

    public NutsExprWithCache(NutsExprDeclarations evaluator) {
        this.evaluator = evaluator;
        SortedSet<Integer> precedences = new TreeSet<>();
        for (int f : evaluator.getOperatorPrecedences()) {
            precedences.add(f);
        }
        this.precedences = precedences.stream().mapToInt(Integer::intValue).toArray();
    }

    public boolean isOp(String str, NutsExprOpType opType, int precIndex) {
        NutsExprOpDeclaration o = evaluator.getOperator(str, opType).orNull();
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
