package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementFactoryContext;
import net.thevpc.nuts.elem.NElementMapper;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTextStyle;

import java.lang.reflect.Type;
import java.nio.file.Path;
import java.nio.file.Paths;

public class NElementMapperPath implements NElementMapper<Path> {

    @Override
    public Object destruct(Path src, Type typeOfSrc, NElementFactoryContext context) {
        return src;
    }

    @Override
    public NElement createElement(Path o, Type typeOfSrc, NElementFactoryContext context) {
        if (context.isNtf()) {
            NText n = NText.ofStyled(o.toString(), NTextStyle.path());
            return NElement.ofString(n.toString());
        } else {
            return context.defaultCreateElement(o.toString(), null);
        }
    }

    @Override
    public Path createObject(NElement o, Type typeOfResult, NElementFactoryContext context) {
        return Paths.get(o.asStringValue().get());
    }
}
