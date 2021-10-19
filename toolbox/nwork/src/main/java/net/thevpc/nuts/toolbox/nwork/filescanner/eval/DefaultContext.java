package net.thevpc.nuts.toolbox.nwork.filescanner.eval;

import java.util.LinkedHashMap;
import java.util.Map;

public class DefaultContext implements Evaluator.Context {
    private final Map<String, Object> vars = new LinkedHashMap<>();

    public Map<String, Object> getVars() {
        return vars;
    }

    public DefaultContext setVar(String k, Object v) {
        this.vars.put(k, v);
        return this;
    }

    @Override
    public Object getVar(String name) {
        return vars.get(name);
    }
}
