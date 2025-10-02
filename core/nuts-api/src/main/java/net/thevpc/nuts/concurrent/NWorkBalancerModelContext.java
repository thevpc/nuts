package net.thevpc.nuts.concurrent;

import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.util.NCopiable;

import java.util.HashMap;
import java.util.Map;

/**
 * @since 0.8.7
 */
public class NWorkBalancerModelContext implements NCopiable, Cloneable {
    /**
     * global and worker variables.
     * global variables are with key "" (empty string)
     */
    private Map<String, Map<String, NElement>> variables = new HashMap<>();

    public Map<String, Map<String, NElement>> getVariables() {
        return variables;
    }

    public NWorkBalancerModelContext setVariables(Map<String, Map<String, NElement>> variables) {
        this.variables = variables;
        return this;
    }

    @Override
    public NWorkBalancerModelContext copy() {
        return clone();
    }

    @Override
    protected NWorkBalancerModelContext clone() {
        try {
            NWorkBalancerModelContext copy = (NWorkBalancerModelContext) super.clone();
            if(copy.variables!=null){
                copy.variables = new HashMap<>();
                for (Map.Entry<String, Map<String, NElement>> e : this.variables.entrySet()) {
                    copy.variables.put(e.getKey(), e.getValue() == null ? null : new HashMap<>(e.getValue()));
                }
            }
            return copy;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
}
