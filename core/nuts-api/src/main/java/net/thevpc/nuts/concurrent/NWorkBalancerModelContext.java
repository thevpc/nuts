package net.thevpc.nuts.concurrent;

import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

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
    @NGetter
    public Map<String, Map<String, NElement>> variables() {
        return variables;
    }

    /**
     * Sets the variables map. Replaces all existing variables.
     *
     * @param variables new variables map
     * @return this context for chaining
     */
    @NSetter
    public NWorkBalancerModelContext variables(Map<String, Map<String, NElement>> variables) {
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
        /**
         * Clone.
         *
         * @return clone result
         */
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
            throw new NUnexpectedException(NMsg.ofC("clone unsupported for %s",getClass()),e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        NWorkBalancerModelContext that = (NWorkBalancerModelContext) o;
        return Objects.equals(variables, that.variables);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(variables);
    }

    @Override
    public String toString() {
        NToStringBuilder b = NToStringBuilder.of(this).omitBlanks(true).omitProcessingSuppliers(true);
        for (Map.Entry<String, Map<String, NElement>> e : variables.entrySet()) {
            b.add(e.getKey(), e.getValue());
        }
        return b.toString();
    }

}
