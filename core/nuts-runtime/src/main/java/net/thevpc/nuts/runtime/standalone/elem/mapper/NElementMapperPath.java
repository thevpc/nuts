package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementDeserializerContext;
import net.thevpc.nuts.elem.NElementFactoryContext;
import net.thevpc.nuts.elem.NElementSerializerContext;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTextStyle;

import java.lang.reflect.Type;
import java.nio.file.Path;
import java.nio.file.Paths;

public class NElementMapperPath implements NElementMapper<Path> {

    @Override
    public Object toSimple(NElementSerializerContext<Path> context) {
        return context.instance();
    }

    @Override
    public NElement toElement(NElementSerializerContext<Path> context) {
        Path o = context.instance();
        if (context.isNtf()) {
            NText n = NText.ofStyled(o.toString(), NTextStyle.path());
            return NElement.ofString(n.toString());
        } else {
            return context.defaultCreateElement(o.toString(), null);
        }
    }

    @Override
    public Path toObject(NElementDeserializerContext context) {
        return Paths.get(context.element().asStringValue().get());
    }
}
