package net.thevpc.nuts.toolbox.nwork.filescanner.eval;

import java.util.function.Predicate;

public interface Evaluator {
    Node createVar(String name);

    Node createLiteral(Object lit);

    Node createFunction(String name, Node[] args);

    interface Node {
        Object eval(Context context);
    }

    interface Context {
        Object getVar(String name);
    }

}
