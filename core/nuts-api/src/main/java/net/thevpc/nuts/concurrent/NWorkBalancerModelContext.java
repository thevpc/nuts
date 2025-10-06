package net.thevpc.nuts.concurrent;

import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.util.NCopiable;

import java.util.HashMap;
import java.util.Map;

/**
 * Context for a {@link NWorkBalancerModel}, holding global and per-worker variables.
 * These variables can be used at runtime and are also persisted as part of the model.
 *
 * <ul>
 *     <li>Global variables are stored under the empty string key ""</li>
 *     <li>Worker-specific variables are stored under the worker's name</li>
 * </ul>
 *
 * @since 0.8.7
 */
public class NWorkBalancerModelContext implements NCopiable, Cloneable {
    /**
     * Variables grouped by scope:
     * - key "" -> global variables
     * - key "<workerName>" -> per-worker variables
     */
    private Map<String, Map<String, NElement>> variables = new HashMap<>();


    /**
     * Returns the current variables map.
     *
     * @return map of worker name -> (variable name -> value)
     */
    public Map<String, Map<String, NElement>> getVariables() {
        return variables;
    }

    /**
     * Sets the variables map. Replaces all existing variables.
     *
     * @param variables new variables map
     * @return this context for chaining
     */
    public NWorkBalancerModelContext setVariables(Map<String, Map<String, NElement>> variables) {
        this.variables = variables;
        return this;
    }


    /**
     * Returns a deep copy of this context.
     *
     * @return cloned context
     */
    @Override
    public NWorkBalancerModelContext copy() {
        return clone();
    }

    /**
     * Clones this context including a deep copy of all variables.
     *
     * @return cloned context
     */
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
