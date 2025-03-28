package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementFactoryContext;
import net.thevpc.nuts.elem.NElementMapper;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTextStyle;

import java.io.File;
import java.lang.reflect.Type;

public class NElementMapperFile implements NElementMapper<File> {

    @Override
    public Object destruct(File src, Type typeOfSrc, NElementFactoryContext context) {
        return src;
    }

    @Override
    public NElement createElement(File o, Type typeOfSrc, NElementFactoryContext context) {
        if (context.isNtf()) {
//                NutsText n = ws.text().forStyled(o.toString(), NutsTextStyle.path());
//                return ws.elem().forPrimitive().buildNutsString(n);
            NText n = NText.ofStyled(o.toString(), NTextStyle.path());
            return context.elem().ofString(n.toString());
        } else {
            return context.defaultObjectToElement(o.toString(), null);
        }
    }

    @Override
    public File createObject(NElement o, Type typeOfResult, NElementFactoryContext context) {
        return new File(o.asString().get());
    }
}
