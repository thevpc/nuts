package net.thevpc.nuts.elem;


import net.thevpc.nuts.util.NNameFormat;

import java.lang.reflect.Type;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * NElementDeserializerBuilder interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NElementDeserializerBuilder<T> {
    /**
     * Build.
     *
     * @return build result
     */
    NElementDeserializer<T> build();

    /**
     * Param field filter.
     *
     * @return param field filter result
     */
    Predicate<String> paramFieldFilter();

    /**
     * Param field filter.
     *
     * @param paramFieldFilter param field filter
     * @return param field filter result
     */
    NElementDeserializerBuilder<T> paramFieldFilter(Predicate<String> paramFieldFilter);

    /**
     * Child field name filter.
     *
     * @return child field name filter result
     */
    Predicate<String> childFieldNameFilter();

    /**
     * Child field name filter.
     *
     * @param childFieldNameFilter child field name filter
     * @return child field name filter result
     */
    NElementDeserializerBuilder<T> childFieldNameFilter(Predicate<String> childFieldNameFilter);

    /**
     * Wrap collections.
     *
     * @param wrapCollections wrap collections
     * @return wrap collections result
     */
    NElementDeserializerBuilder<T> wrapCollections(boolean wrapCollections);

    /**
     * Container is collection.
     *
     * @param value value
     * @return container is collection result
     */
    NElementDeserializerBuilder<T> containerIsCollection(boolean value);

    /**
     * On unsupported param.
     *
     * @param a a
     * @return on unsupported param result
     */
    NElementDeserializerBuilder<T> onUnsupportedParam(NElementDeserializerFieldConfigurer<T> a);

    /**
     * On unsupported child.
     *
     * @param a a
     * @return on unsupported child result
     */
    NElementDeserializerBuilder<T> onUnsupportedChild(NElementDeserializerFieldConfigurer<T> a);

    /**
     * On initialize instance.
     *
     * @param a a
     * @return on initialize instance result
     */
    NElementDeserializerBuilder<T> onInitializeInstance(NElementDeserializerInitializer<T> a);

    /**
     * Type default value.
     *
     * @param type type
     * @param defaultValue default value
     * @return type default value result
     */
    NElementDeserializerBuilder<T> typeDefaultValue(Type type, Object defaultValue);

    /**
     * Instance factory.
     *
     * @param instanceFactory instance factory
     * @return instance factory result
     */
    NElementDeserializerBuilder<T> instanceFactory(NElementDeserializerInstanceFactory<T> instanceFactory);

    /**
     * Boolean default true.
     *
     * @return boolean default true result
     */
    NElementDeserializerBuilder<T> booleanDefaultTrue();

    /**
     * Boolean default false.
     *
     * @return boolean default false result
     */
    NElementDeserializerBuilder<T> booleanDefaultFalse();

    /**
     * Configure lenient.
     *
     * @return configure lenient result
     */
    NElementDeserializerBuilder<T> configureLenient();

    /**
     * Field name normalizer.
     *
     * @param normalizer normalizer
     * @return field name normalizer result
     */
    NElementDeserializerBuilder<T> fieldNameNormalizer(Function<String, String> normalizer);

    /**
     * Field name normalizer.
     *
     * @param normalizer normalizer
     * @return field name normalizer result
     */
    NElementDeserializerBuilder<T> fieldNameNormalizer(NNameFormat normalizer);

    /**
     * Field.
     *
     * @param name name
     * @return field result
     */
    NElementDeserializerField<T> field(String name);

}
