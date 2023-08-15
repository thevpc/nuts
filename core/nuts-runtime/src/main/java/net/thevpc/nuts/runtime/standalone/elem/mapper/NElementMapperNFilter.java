package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementFactoryContext;
import net.thevpc.nuts.elem.NElementMapper;
import net.thevpc.nuts.util.NFilter;
import net.thevpc.nuts.util.NMsg;

import java.lang.reflect.Type;

public class NElementMapperNFilter implements NElementMapper<NFilter> {

    @Override
    public Object destruct(NFilter src, Type typeOfSrc, NElementFactoryContext context) {
        return src.toString();
    }

    @Override
    public NElement createElement(NFilter o, Type typeOfSrc, NElementFactoryContext context) {
        return context.elem().ofString(o.toString());
    }

    @Override
    public NFilter createObject(NElement o, Type to, NElementFactoryContext context) {
        String s = o.asString().orNull();
        if(s==null || s.trim().isEmpty()){
            s="true";
        }
        if(to instanceof Class) {
            switch (((Class) to).getName()) {
                case "net.thevpc.nuts.NIdFilter": {
                    return NIdFilters.of(context.getSession()).parse(s);
                }
                case "net.thevpc.nuts.NDescriptorFilter": {
                    return NDescriptorFilters.of(context.getSession()).parse(s);
                }
                case "net.thevpc.nuts.NVersionFilter": {
                    return NVersionFilters.of(context.getSession()).parse(s);
                }
                case "net.thevpc.nuts.NDependencyFilter": {
                    return NDependencyFilters.of(context.getSession()).parse(s);
                }
                case "net.thevpc.nuts.NInstallStatusFilter": {
                    return NInstallStatusFilters.of(context.getSession()).parse(s);
                }
                case "net.thevpc.nuts.NRepositoryFilter": {
                    return NRepositoryFilters.of(context.getSession()).parse(s);
                }
            }
        }
        throw new NUnsupportedArgumentException(context.getSession(), NMsg.ofC("unsupported parse of %s",to));
    }
}
