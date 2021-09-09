package net.thevpc.nuts.runtime.core.format.elem.mappers;

import net.thevpc.nuts.*;

import java.lang.reflect.Type;
import java.util.*;

public class NutsElementMapperNutsObjectElement implements NutsElementMapper<NutsObjectElement> {

    public NutsElementMapperNutsObjectElement() {
    }

    @Override
    public Object destruct(NutsObjectElement src, Type typeOfSrc, NutsElementFactoryContext context) {
        Set<Object> visited = new HashSet<>();
        boolean map = true;
        List<Map.Entry<Object, Object>> all = new ArrayList<>();
        for (NutsElementEntry nutsElementEntry : src.children()) {
            Object k = context.defaultDestruct(nutsElementEntry.getKey(), null);
            Object v = context.defaultDestruct(nutsElementEntry.getValue(), null);
            if (map && visited.contains(k)) {
                map = false;
            } else {
                visited.add(k);
            }
            all.add(new AbstractMap.SimpleEntry<>(k, v));
        }
        if (map) {
            LinkedHashMap<Object, Object> m = new LinkedHashMap<>();
            for (Map.Entry<Object, Object> entry : all) {
                m.put(entry.getKey(), entry.getValue());
            }
            return m;
        }
        return all;
    }

    @Override
    public NutsElement createElement(NutsObjectElement src, Type typeOfSrc, NutsElementFactoryContext context) {
        return src;
    }

    @Override
    public NutsObjectElement createObject(NutsElement o, Type typeOfResult, NutsElementFactoryContext context) {
        if (o.type() == NutsElementType.OBJECT) {
            return o.asObject();
        }
        return context.element().forObject().set("value", o).build();
    }
}
