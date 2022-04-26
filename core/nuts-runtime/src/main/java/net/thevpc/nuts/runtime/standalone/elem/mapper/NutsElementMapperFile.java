package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.*;

import java.io.File;
import java.lang.reflect.Type;

public class NutsElementMapperFile implements NutsElementMapper<File> {

    @Override
    public Object destruct(File src, Type typeOfSrc, NutsElementFactoryContext context) {
        return src;
    }

    @Override
    public NutsElement createElement(File o, Type typeOfSrc, NutsElementFactoryContext context) {
        if (context.isNtf()) {
            NutsSession session = context.getSession();
//                NutsText n = ws.text().forStyled(o.toString(), NutsTextStyle.path());
//                return ws.elem().forPrimitive().buildNutsString(n);
            NutsText n = NutsTexts.of(session).ofStyled(o.toString(), NutsTextStyle.path());
            return context.elem().ofString(n.toString());
        } else {
            return context.defaultObjectToElement(o.toString(), null);
        }
    }

    @Override
    public File createObject(NutsElement o, Type typeOfResult, NutsElementFactoryContext context) {
        NutsSession session = context.getSession();
        return new File(o.asString().get(session));
    }
}
