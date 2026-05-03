package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementDeserializerContext;
import net.thevpc.nuts.elem.NElementFactoryContext;
import net.thevpc.nuts.elem.NElementSerializerContext;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTextStyle;

import java.io.File;
import java.lang.reflect.Type;

public class NElementMapperFile implements NElementMapper<File> {

    @Override
    public Object toSimple(NElementSerializerContext<File> context) {
        return context.instance();
    }

    @Override
    public NElement toElement(NElementSerializerContext<File> context) {
        File o = context.instance();
        if (context.isNtf()) {
//                NutsText n = ws.text().forStyled(o.toString(), NutsTextStyle.path());
//                return ws.elem().forPrimitive().buildNutsString(n);
            NText n = NText.ofStyled(o.toString(), NTextStyle.path());
            return NElement.ofString(n.toString());
        } else {
            return context.defaultCreateElement(o.toString(), null);
        }
    }

    @Override
    public File toObject(NElementDeserializerContext context) {
        return new File(context.element().asStringValue().get());
    }
}
