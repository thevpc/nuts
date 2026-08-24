package net.thevpc.nuts.reflect;

import net.thevpc.nuts.util.NEqualizer;
import net.thevpc.nuts.util.NAssignmentPolicy;
import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.util.NSetter;

import java.lang.reflect.Type;

/**
 * NReflectMapper interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NReflectMapper {
    /**
     * Creates a new instance of of.
     *
     * @return of result
     */
    static NReflectMapper of() {
        return NReflect.of().createMapper();
    }

    /**
     * Creates a new instance of of.
     *
     * @param repository repository
     * @return of result
     */
    static NReflectMapper of(NReflectRepository repository) {
        return NReflect.of().createMapper().repository(repository);
    }

    /**
     * Map to type.
     *
     * @param value value
     * @param toType to type
     * @return map to type result
     */
    Object mapToType(Object value, NReflectType toType);

    /**
     * Map to type.
     *
     * @param value value
     * @param toType to type
     * @return map to type result
     */
    Object mapToType(Object value, Type toType);

    /**
     * Copy.
     *
     * @param from from
     * @param to to
     * @return copy result
     */
    boolean copy(Object from, Object to);

    // type configuration

    /**
     * Include property.
     *
     * @param names names
     * @return include property result
     */
    NReflectMapper includeProperty(String... names);

    /**
     * Exclude property.
     *
     * @param names names
     * @return exclude property result
     */
    NReflectMapper excludeProperty(String... names);

    /**
     * Rename property.
     *
     * @param from from
     * @param to to
     * @return rename property result
     */
    NReflectMapper renameProperty(String from, String to);


    // global configuration

    /**
     * Returns the mapping strategy.
     *
     * @param from from
     * @param to to
     * @return get mapping strategy result
     */
    NOptional<NReflectMappingStrategy> getMappingStrategy(NReflectType from, NReflectType to);

    /**
     * Equalizer.
     *
     * @return equalizer result
     */
    NEqualizer<Object> equalizer();

    /**
     * Equalizer.
     *
     * @param eq eq
     * @return equalizer result
     */
    @NSetter
    NReflectMapper equalizer(NEqualizer<Object> eq);

    /**
     * Assignment policy.
     *
     * @return assignment policy result
     */
    NAssignmentPolicy assignmentPolicy();

    /**
     * Assignment policy.
     *
     * @param mapStrategy map strategy
     * @return assignment policy result
     */
    @NSetter
    NReflectMapper assignmentPolicy(NAssignmentPolicy mapStrategy);

    /**
     * Repository.
     *
     * @return repository result
     */
    NReflectRepository repository();

    /**
     * Repository.
     *
     * @param repository repository
     * @return repository result
     */
    @NSetter
    NReflectMapper repository(NReflectRepository repository);

    /**
     * Property converter.
     *
     * @param property property
     * @param converter converter
     * @return property converter result
     */
    @NSetter
    NReflectMapper propertyConverter(String property, NReflectConverter converter);

    /**
     * Type converter.
     *
     * @param fromType from type
     * @param toType to type
     * @param converter converter
     * @return type converter result
     */
    @NSetter
    NReflectMapper typeConverter(NReflectType fromType, NReflectType toType, NReflectConverter converter);

}
