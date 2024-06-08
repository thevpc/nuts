package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.*;
import net.thevpc.nuts.DefaultNDependencyBuilder;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementFactoryContext;
import net.thevpc.nuts.elem.NElementMapper;
import net.thevpc.nuts.elem.NElementType;
import net.thevpc.nuts.text.NString;

import java.lang.reflect.Type;

public class NElementMapperNDependency implements NElementMapper<NDependency> {

    @Override
    public Object destruct(NDependency o, Type typeOfSrc, NElementFactoryContext context) {
        NSession session = context.getSession();
        if (o.getExclusions().isEmpty()) {
            //use compact form
            if (context.isNtf()) {
                return NDependencyFormat.of(session).setNtf(true).setValue(o).format();
            } else {

                return context.defaultDestruct(o.formatter(session)
                                .setSession(session)
                        .setNtf(context.isNtf())
                        .format(), null);
            }
        }
        return context.defaultDestruct(NDependencyBuilder.of().setAll(o), null);
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

        NString format = o.formatter(context.getSession())
                .setNtf(context.isNtf())
                .format();
        return context.defaultObjectToElement(
                format, null);
//                }
//            }
//            return context.defaultObjectToElement(context.getSession().dependency().builder().set(o), null);
    }

    @Override
    public NDependency createObject(NElement o, Type typeOfResult, NElementFactoryContext context) {
        NSession session = context.getSession();
        if (o.type() == NElementType.STRING) {
            return NDependency.of(o.asString().get(session)).get(session);
        }
        NDependencyBuilder builder = context.defaultElementToObject(o, DefaultNDependencyBuilder.class);
        return NDependencyBuilder.of().setAll(builder).build();
    }

}
