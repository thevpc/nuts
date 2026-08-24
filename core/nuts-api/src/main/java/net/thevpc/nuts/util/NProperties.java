package net.thevpc.nuts.util;

import net.thevpc.nuts.artifact.NDescriptorProperty;
import net.thevpc.nuts.artifact.NEnvCondition;
import net.thevpc.nuts.collections.NCollections;
import net.thevpc.nuts.internal.rpi.NUtilsRPI;

import java.util.List;
import java.util.Set;

/**
 * NProperties interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NProperties {
    /**
     * Creates a new instance of of.
     *
     * @return of result
     */
    static NProperties of(){
        return NUtilsRPI.of().createProperties();
    }
    /**
     * Removes remove.
     *
     * @param name name
     * @return remove result
     */
    NProperties remove(String name);

    /**
     * Removes remove.
     *
     * @param p p
     * @return remove result
     */
    NProperties remove(NDescriptorProperty p);

    /**
     * Key set.
     *
     * @return key set result
     */
    Set<String> keySet();

    /**
     * Converts to list.
     *
     * @return to list result
     */
    List<NDescriptorProperty> toList();

    /**
     * Converts to array.
     *
     * @return to array result
     */
    NDescriptorProperty[] toArray();

    /**
     * Returns the get.
     *
     * @param name name
     * @param cond cond
     * @return get result
     */
    NDescriptorProperty get(String name, NEnvCondition cond);

    /**
     * Returns the all.
     *
     * @param name name
     * @return get all result
     */
    NDescriptorProperty[] getAll(String name);

    /**
     * Adds the specified all.
     *
     * @param arr arr
     * @return add all result
     */
    NProperties addAll(List<NDescriptorProperty> arr);

    /**
     * Adds add.
     *
     * @param p p
     * @return add result
     */
    NProperties add(NDescriptorProperty p);

    /**
     * Clear.
     */
    void clear();
}
