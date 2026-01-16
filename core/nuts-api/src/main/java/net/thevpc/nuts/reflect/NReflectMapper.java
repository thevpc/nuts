package net.thevpc.nuts.reflect;

import net.thevpc.nuts.util.NEqualizer;
import net.thevpc.nuts.util.NAssignmentPolicy;
import net.thevpc.nuts.util.NOptional;

import java.lang.reflect.Type;

public interface NReflectMapper {
    static NReflectMapper of() {
        return NReflect.of().createMapper();
    }

    static NReflectMapper of(NReflectRepository repository) {
        return NReflect.of().createMapper().setRepository(repository);
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

    NEqualizer<Object> getEqualizer();

    NReflectMapper setEqualizer(NEqualizer<Object> eq);

    NAssignmentPolicy getAssignmentPolicy();

    NReflectMapper setAssignmentPolicy(NAssignmentPolicy mapStrategy);

    NReflectRepository getRepository();

    NReflectMapper setRepository(NReflectRepository repository);

    NReflectMapper setPropertyConverter(String property, NReflectConverter converter);

    NReflectMapper setTypeConverter(NReflectType fromType, NReflectType toType, NReflectConverter converter);

}
