package net.thevpc.nuts.runtime.standalone.format.tson.format;

import net.thevpc.nuts.elem.NElement;

public interface TsonFormat {
    String format(NElement element);

    TsonFormatBuilder builder();
}
