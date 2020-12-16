package net.thevpc.nuts.runtime.standalone.format.text.renderer;

import net.thevpc.nuts.runtime.standalone.format.text.RenderedRawStream;

public interface StyleRenderer {
    void startFormat(RenderedRawStream out);

    void endFormat(RenderedRawStream out) ;
}
