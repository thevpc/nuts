package net.thevpc.nuts.elem;

import net.thevpc.nuts.io.NInputStreamProvider;

public interface NBinaryStreamElement extends NElement{
    NInputStreamProvider value();
    NBinaryStreamElementBuilder builder();
}
