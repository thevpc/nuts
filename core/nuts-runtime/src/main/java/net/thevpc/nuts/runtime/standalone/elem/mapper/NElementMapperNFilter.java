package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.artifact.NDefinitionFilters;
import net.thevpc.nuts.artifact.NDependencyFilters;
import net.thevpc.nuts.artifact.NIdFilters;
import net.thevpc.nuts.artifact.NVersionFilters;
import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.core.NRepositoryFilters;
import net.thevpc.nuts.util.NFilter;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NUnsupportedArgumentException;

import java.lang.reflect.Type;

public class NElementMapperNFilter implements NElementMapper<NFilter> {

    @Override
    public Object destruct(NFilter src, Type typeOfSrc, NElementFactoryContext context) {
        return src.toString();
    }

    @Override
    public NElement createElement(NFilter o, Type typeOfSrc, NElementFactoryContext context) {
        return NElement.ofString(o.toString());
    }

    @Override
    public NFilter createObject(NElement o, Type to, NElementFactoryContext context) {
        String s = o.asStringValue().orNull();
        if(s==null || s.trim().isEmpty()){
            s="true";
        }
        if(to instanceof Class) {
            switch (((Class) to).getName()) {
                case "net.thevpc.nuts.artifact.NIdFilter": {
                    return NIdFilters.of().parse(s);
                }
                case "net.thevpc.nuts.artifact.NDefinitionFilter": {
                    return NDefinitionFilters.of().parse(s);
                }
                case "net.thevpc.nuts.artifact.NVersionFilter": {
                    return NVersionFilters.of().parse(s);
                }
                case "net.thevpc.nuts.artifact.NDependencyFilter": {
                    return NDependencyFilters.of().parse(s);
                }
                case "net.thevpc.nuts.core.NRepositoryFilter": {
                    return NRepositoryFilters.of().parse(s);
                }
            }
        }
        throw new NUnsupportedArgumentException(NMsg.ofC("unsupported parse of %s",to));
    }
}
