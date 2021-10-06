package net.thevpc.nuts.runtime.core.format.elem.mappers;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.model.DefaultNutsDependencyBuilder;

import java.lang.reflect.Type;

public class NutsElementMapperNutsDependency implements NutsElementMapper<NutsDependency> {

    @Override
    public Object destruct(NutsDependency o, Type typeOfSrc, NutsElementFactoryContext context) {
        if (o.getExclusions().length == 0) {
            //use compact form
            if (context.elem().isNtf()) {
                return context.getSession().dependency().formatter().setNtf(true).setValue(o).format();
            } else {

                return context.defaultDestruct(context.getSession().dependency().formatter(o)
                        .setNtf(context.elem().isNtf())
                        .format(), null);
            }
        }
        return context.defaultDestruct(context.getSession().dependency().builder().set(o), null);
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

        return context.defaultObjectToElement(context.getSession().dependency().formatter(o)
                .setNtf(context.elem().isNtf())
                .format(), null);
//                }
//            }
//            return context.defaultObjectToElement(context.getSession().dependency().builder().set(o), null);
    }

    @Override
    public NutsDependency createObject(NutsElement o, Type typeOfResult, NutsElementFactoryContext context) {
        if (o.type() == NutsElementType.STRING) {
            return context.getSession().dependency().parser().setLenient(false).parseDependency(o.asPrimitive().getString());
        }
        DefaultNutsDependencyBuilder builder = (DefaultNutsDependencyBuilder) context.defaultElementToObject(o, DefaultNutsDependencyBuilder.class);
        return context.getSession().dependency().builder().set(builder).build();
    }

}
