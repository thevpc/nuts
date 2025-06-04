package net.thevpc.nuts.reflect;

import net.thevpc.nuts.util.NEqualizer;
import net.thevpc.nuts.util.NMapStrategy;
import net.thevpc.nuts.util.NOptional;

public interface NReflectMapperContext {
    void include(String... names);

    void excludeProperty(String... names);

    void rename(String from, String to);

    void setPropertyConverter(String property, NReflectMapper.Converter converter);

    void setTypeConverter(NReflectType fromType, NReflectType toType, NReflectMapper.Converter converter);

    Object get(Object a);

    Object put(Object a, Object b);

    NOptional<NReflectTypeMapper> findTypeMapper(NReflectType from, NReflectType to);

    Object mapToType(Object value, NReflectType toType);

    NEqualizer<Object> equalizer();

    NReflectMapperContext setEqqualizer(NEqualizer<Object> eq);

    NMapStrategy mapStrategy();

    NReflectMapperContext setMapStrategy(NMapStrategy mapStrategy);

    NReflectMapper mapper();

    NReflectRepository repository();


    boolean copy(Object from, Object to);
}
