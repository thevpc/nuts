package net.thevpc.nuts.elem;

import java.lang.reflect.Type;

/**
 * NElementDeserializerField interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NElementDeserializerField<T> {
    /**
     * Ignore.
     *
     * @return ignore result
     */
    NElementDeserializerField<T> ignore();

    /**
     * Sets the alias.
     *
     * @param aliases aliases
     * @return set alias result
     */
    NElementDeserializerField<T> setAlias(String... aliases);

    /**
     * Sets the type.
     *
     * @param type type
     * @return set type result
     */
    NElementDeserializerField<T> setType(Type type);

    /**
     * Sets the boolean default true.
     *
     * @return set boolean default true result
     */
    NElementDeserializerField<T> setBooleanDefaultTrue();

    /**
     * Sets the boolean default false.
     *
     * @return set boolean default false result
     */
    NElementDeserializerField<T> setBooleanDefaultFalse();

    /**
     * Sets the default value.
     *
     * @param valueWhenMissing value when missing
     * @return set default value result
     */
    NElementDeserializerField<T> setDefaultValue(Object valueWhenMissing);

    /**
     * Sets the wrap collections.
     *
     * @param value value
     * @return set wrap collections result
     */
    NElementDeserializerField<T> setWrapCollections(Boolean value);

    /**
     * Sets the container is collection.
     *
     * @param value value
     * @return set container is collection result
     */
    NElementDeserializerField<T> setContainerIsCollection(Boolean value);

    /**
     * Sets the param.
     *
     * @param param param
     * @return set param result
     */
    NElementDeserializerField<T> setParam(boolean param);

    /**
     * Sets the child.
     *
     * @param child child
     * @return set child result
     */
    NElementDeserializerField<T> setChild(boolean child);

    /**
     * End.
     *
     * @return end result
     */
    NElementDeserializerBuilder<T> end();

}
