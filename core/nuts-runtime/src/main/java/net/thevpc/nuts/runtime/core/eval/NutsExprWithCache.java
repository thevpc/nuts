package net.thevpc.nuts.runtime.core.eval;

import net.thevpc.nuts.NutsExpr;

import java.util.*;

class NutsExprWithCache {
    NutsExpr evaluator;
    int[] precedences;
    Map<Integer, Set<String>> prefixOpByName = new HashMap<>();
    Map<Integer, Set<String>> infixOpByName = new HashMap<>();
    Map<Integer, Set<String>> postfixOpByName = new HashMap<>();

    public NutsExprWithCache(DefaultNutsExpr evaluator) {
        this.evaluator=evaluator;
        SortedSet<Integer> precedences = new TreeSet<>();
        for (NutsExpr.OpType opType : NutsExpr.OpType.values()) {
            Map<Integer, Set<String>> byName = getByName(opType);
            for (String f : ((NutsExpr) evaluator).getOperatorNames(opType)) {
                NutsExpr.Op ff = ((NutsExpr) evaluator).getOperator(f, opType);
                if (ff != null) {
                    int p = ff.getPrecedence();
                    precedences.add(p);
                    Set<String> li = byName.get(p);
                    if (li == null) {
                        li = new TreeSet<>();
                        byName.put(p, li);
                    }
                    li.add(f);
                }
            }
        }
        this.precedences = precedences.stream().mapToInt(Integer::intValue).toArray();
    }

    public NutsExpr getEvaluator() {
        return evaluator;
    }

    private Map<Integer, Set<String>> getByName(NutsExpr.OpType opType) {
        return opType == NutsExpr.OpType.PREFIX ? this.prefixOpByName
                : opType == NutsExpr.OpType.INFIX ? this.infixOpByName
                : this.postfixOpByName;
    }

    public boolean isOp(String str, NutsExpr.OpType opType, int precIndex) {
        return str!=null && getAt(opType, precIndex).contains(str);
    }

    public Set<String> getAt(NutsExpr.OpType opType, int precIndex) {
        if (precIndex < precedences.length) {
            Set<String> q = getByName(opType).get(precedences[precIndex]);
            if (q != null) {
                return q;
            }
        }
        return Collections.emptySet();
    }

    @Override
    public String toString() {
        return "NutsExprWithCache{" +
                "evaluator=" + evaluator +
                ", precedences=" + Arrays.toString(precedences) +
                ", prefixOpByName=" + prefixOpByName +
                ", infixOpByName=" + infixOpByName +
                ", postfixOpByName=" + postfixOpByName +
                '}';
    }
}
