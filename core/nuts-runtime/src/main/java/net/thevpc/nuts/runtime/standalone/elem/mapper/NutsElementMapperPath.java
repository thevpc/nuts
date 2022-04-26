package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.*;

import java.lang.reflect.Type;
import java.nio.file.Path;
import java.nio.file.Paths;

public class NutsElementMapperPath implements NutsElementMapper<Path> {

    @Override
    public Object destruct(Path src, Type typeOfSrc, NutsElementFactoryContext context) {
        return src;
    }

    @Override
    public NutsElement createElement(Path o, Type typeOfSrc, NutsElementFactoryContext context) {
        if (context.isNtf()) {
            NutsSession session = context.getSession();
            NutsText n = NutsTexts.of(session).ofStyled(o.toString(), NutsTextStyle.path());
            return context.elem().ofString(n.toString());
        } else {
            return context.defaultObjectToElement(o.toString(), null);
        }
    }

    @Override
    public Path createObject(NutsElement o, Type typeOfResult, NutsElementFactoryContext context) {
        NutsSession session = context.getSession();
        return Paths.get(o.asString().get(session));
    }
}
