package net.thevpc.nuts.runtime.standalone.text.renderer;

import net.thevpc.nuts.runtime.standalone.text.RenderedRawStream;

public interface StyleRenderer {
    void startFormat(RenderedRawStream out);

    void endFormat(RenderedRawStream out) ;
}
