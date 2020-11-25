package net.thevpc.nuts.runtime.util.fprint.renderer;

import net.thevpc.nuts.runtime.util.fprint.RenderedRawStream;

import java.io.IOException;

public interface StyleRenderer {
    void startFormat(RenderedRawStream out);

    void endFormat(RenderedRawStream out) ;
}
