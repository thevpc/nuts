package net.thevpc.nuts.runtime.standalone.elem.mappers;

import net.thevpc.nuts.*;

import java.lang.reflect.Type;
import java.time.Instant;
import java.util.Date;

public class NutsElementMapperNutsFilter implements NutsElementMapper<NutsFilter> {

    @Override
    public Object destruct(NutsFilter src, Type typeOfSrc, NutsElementFactoryContext context) {
        return src.toString();
    }

    @Override
    public NutsElement createElement(NutsFilter o, Type typeOfSrc, NutsElementFactoryContext context) {
        return context.elem().ofString(o.toString());
    }

    @Override
    public NutsFilter createObject(NutsElement o, Type to, NutsElementFactoryContext context) {
        String s = o.asSafeString();
        if(s==null || s.trim().isEmpty()){
            s="true";
        }
        if(to instanceof Class) {
            switch (((Class) to).getName()) {
                case "net.thevpc.nuts.NutsIdFilter": {
                    return NutsIdFilters.of(context.getSession()).parse(s);
                }
                case "net.thevpc.nuts.NutsDescriptorFilter": {
                    return NutsDescriptorFilters.of(context.getSession()).parse(s);
                }
                case "net.thevpc.nuts.NutsVersionFilter": {
                    return NutsVersionFilters.of(context.getSession()).parse(s);
                }
                case "net.thevpc.nuts.NutsDependencyFilter": {
                    return NutsDependencyFilters.of(context.getSession()).parse(s);
                }
                case "net.thevpc.nuts.NutsInstallStatusFilter": {
                    return NutsInstallStatusFilters.of(context.getSession()).parse(s);
                }
                case "net.thevpc.nuts.NutsRepositoryFilter": {
                    return NutsRepositoryFilters.of(context.getSession()).parse(s);
                }
            }
        }
        throw new NutsUnsupportedArgumentException(context.getSession(),NutsMessage.cstyle("unsupported parse of %s",to));
    }
}
