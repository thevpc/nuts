package net.thevpc.nuts.elem;

import net.thevpc.nuts.util.NBlankable;

import java.util.List;

/**
 * NElementAnnotationBuilder interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NElementAnnotationBuilder extends NBlankable {
    /**
     * Name.
     *
     * @return name result
     */
    String name();

    /**
     * Checks if is parametrized.
     *
     * @return is parametrized result
     */
    boolean isParametrized();

    /**
     * Sets the parameterized.
     *
     * @param p p
     * @return set parameterized result
     */
    NElementAnnotationBuilder setParameterized(boolean p);

    /**
     * Param.
     *
     * @param index index
     * @return param result
     */
    NElement param(int index);

    /**
     * Removes remove.
     *
     * @param child child
     * @return remove result
     */
    NElementAnnotationBuilder remove(NElement child);

    /**
     * Removes the specified pair.
     *
     * @param entryKey entry key
     * @return remove pair result
     */
    NElementAnnotationBuilder removePair(NElement entryKey);

    /**
     * Removes the specified all.
     *
     * @param child child
     * @return remove all result
     */
    NElementAnnotationBuilder removeAll(NElement child);

    /**
     * Removes the specified all pairs.
     *
     * @param name name
     * @return remove all pairs result
     */
    NElementAnnotationBuilder removeAllPairs(NElement name);

    /**
     * Removes the specified pair.
     *
     * @param name name
     * @return remove pair result
     */
    NElementAnnotationBuilder removePair(String name);

    /**
     * Removes the specified all pairs.
     *
     * @param name name
     * @return remove all pairs result
     */
    NElementAnnotationBuilder removeAllPairs(String name);

    /**
     * Clear.
     *
     * @return clear result
     */
    NElementAnnotationBuilder clear();

    /**
     * Sets the name.
     *
     * @param name name
     * @return set name result
     */
    NElementAnnotationBuilder setName(String name);

    /**
     * Build.
     *
     * @return build result
     */
    NElementAnnotation build();

    /**
     * Params.
     *
     * @return params result
     */
    List<NElement> params();

    /**
     * Adds add.
     *
     * @param element element
     * @return add result
     */
    NElementAnnotationBuilder add(NElement element);

    /**
     * Removes the specified at.
     *
     * @param index index
     * @return remove at result
     */
    NElementAnnotationBuilder removeAt(int index);

    /**
     * Size.
     *
     * @return size result
     */
    int size();

    /**
     * Adds the specified all.
     *
     * @param all all
     * @return add all result
     */
    NElementAnnotationBuilder addAll(List<NElement> all);

    /**
     * Sets the param at.
     *
     * @param index index
     * @param nElement n element
     * @return set param at result
     */
    NElementAnnotationBuilder setParamAt(int index, NElement nElement);

    /**
     * Sets the params.
     *
     * @param elements elements
     * @return set params result
     */
    NElementAnnotationBuilder setParams(List<NElement> elements);
}
