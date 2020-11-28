package net.thevpc.nuts.runtime.format.text.renderer;

import net.thevpc.nuts.runtime.format.text.RenderedRawStream;

public interface StyleRenderer {
    void startFormat(RenderedRawStream out);

    void endFormat(RenderedRawStream out) ;
}
