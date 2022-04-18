package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.*;
import net.thevpc.nuts.DefaultNutsDependencyBuilder;

import java.lang.reflect.Type;

public class NutsElementMapperNutsDependency implements NutsElementMapper<NutsDependency> {

    @Override
    public Object destruct(NutsDependency o, Type typeOfSrc, NutsElementFactoryContext context) {
        NutsSession session = context.getSession();
        if (o.getExclusions().isEmpty()) {
            //use compact form
            if (context.isNtf()) {
                return NutsDependencyFormat.of(session).setNtf(true).setValue(o).format();
            } else {

                return context.defaultDestruct(o.formatter(session)
                                .setSession(session)
                        .setNtf(context.isNtf())
                        .format(), null);
            }
        }
        return context.defaultDestruct(new DefaultNutsDependencyBuilder().setAll(o), null);
    }

    @Override
    public NutsElement createElement(NutsDependency o, Type typeOfSrc, NutsElementFactoryContext context) {
//            if (o.toString().contains("jai_imageio")) {
//                System.out.print("");
//            }
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

        NutsString format = o.formatter(context.getSession())
                .setNtf(context.isNtf())
                .format();
        return context.defaultObjectToElement(
                format, null);
//                }
//            }
//            return context.defaultObjectToElement(context.getSession().dependency().builder().set(o), null);
    }

    @Override
    public NutsDependency createObject(NutsElement o, Type typeOfResult, NutsElementFactoryContext context) {
        NutsSession session = context.getSession();
        if (o.type() == NutsElementType.STRING) {
            return NutsDependency.of(o.asPrimitive().getString()).get(session);
        }
        NutsDependencyBuilder builder = context.defaultElementToObject(o, DefaultNutsDependencyBuilder.class);
        return new DefaultNutsDependencyBuilder(builder).build();
    }

}
