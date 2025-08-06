package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.*;
import net.thevpc.nuts.format.NDependencyFormat;
import net.thevpc.nuts.runtime.standalone.DefaultNDependencyBuilder;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementFactoryContext;
import net.thevpc.nuts.elem.NElementMapper;
import net.thevpc.nuts.format.NFormats;
import net.thevpc.nuts.text.NText;

import java.lang.reflect.Type;

public class NElementMapperNDependency implements NElementMapper<NDependency> {

    @Override
    public Object destruct(NDependency o, Type typeOfSrc, NElementFactoryContext context) {
        if (o.getExclusions().isEmpty()) {
            //use compact form
            if (context.isNtf()) {
                return NDependencyFormat.of().setNtf(true).setValue(o).format();
            } else {

                return context.defaultDestruct(NFormats.of().ofFormat(o).get()
                        .setNtf(context.isNtf())
                        .format(), null);
            }
        }
        return context.defaultDestruct(NDependencyBuilder.of().copyFrom(o), null);
    }

    @Override
    public NElement createElement(NDependency o, Type typeOfSrc, NElementFactoryContext context) {
//            if (o.getExclusions().length == 0) {
        //use compact form
//                if (context.element().isNtf()) {
//                    NutsWorkspace ws = context.getSession().getWorkspace();
////                    NutsText n = ws.text().parse(
////                            ws.dependency().formatter().setNtf(true).setValue(o).format()
////                    );
////                    return ws.elem().forPrimitive().buildNutsString(n);
//                    return ws.elem().forString(ws.dependency().formatter().setNtf(true).setValue(o).format());
//                } else {

        NText format = NFormats.of().ofFormat(o).get()
                .setNtf(context.isNtf())
                .format();
        return context.defaultCreateElement(
                format, null);
//                }
//            }
//            return context.defaultObjectToElement(context.getSession().dependency().builder().set(o), null);
    }

    @Override
    public NDependency createObject(NElement o, Type typeOfResult, NElementFactoryContext context) {
        if (o.type().isAnyString()) {
            return NDependency.get(o.asStringValue().get()).get();
        }
        NDependencyBuilder builder = context.defaultCreateObject(o, DefaultNDependencyBuilder.class);
        return NDependencyBuilder.of().copyFrom(builder).build();
    }

}
