package net.thevpc.nuts.elem;

import net.thevpc.nuts.io.NReaderProvider;

public interface NCharStreamElement extends NElement{
    NReaderProvider value();
    NCharStreamElementBuilder builder();
}
