package net.thevpc.nuts.reflect;

import net.thevpc.nuts.util.NEqualizer;
import net.thevpc.nuts.util.NAssignmentPolicy;
import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.util.NSetter;

import java.lang.reflect.Type;

public interface NReflectMapper {
    static NReflectMapper of() {
        return NReflect.of().createMapper();
    }

    static NReflectMapper of(NReflectRepository repository) {
        return NReflect.of().createMapper().repository(repository);
    }

    Object mapToType(Object value, NReflectType toType);

    Object mapToType(Object value, Type toType);

    boolean copy(Object from, Object to);

    // type configuration

    NReflectMapper includeProperty(String... names);

    NReflectMapper excludeProperty(String... names);

    NReflectMapper renameProperty(String from, String to);


    // global configuration

    NOptional<NReflectMappingStrategy> getMappingStrategy(NReflectType from, NReflectType to);

    NEqualizer<Object> equalizer();

    @NSetter
    NReflectMapper equalizer(NEqualizer<Object> eq);

    NAssignmentPolicy assignmentPolicy();

    @NSetter
    NReflectMapper assignmentPolicy(NAssignmentPolicy mapStrategy);

    NReflectRepository repository();

    @NSetter
    NReflectMapper repository(NReflectRepository repository);

    @NSetter
    NReflectMapper propertyConverter(String property, NReflectConverter converter);

    @NSetter
    NReflectMapper typeConverter(NReflectType fromType, NReflectType toType, NReflectConverter converter);

}
