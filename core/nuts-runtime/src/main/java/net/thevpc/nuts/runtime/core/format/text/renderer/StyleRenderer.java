package net.thevpc.nuts.runtime.core.format.text.renderer;

import net.thevpc.nuts.runtime.core.format.text.RenderedRawStream;

public interface StyleRenderer {
    void startFormat(RenderedRawStream out);

    void endFormat(RenderedRawStream out) ;
}
