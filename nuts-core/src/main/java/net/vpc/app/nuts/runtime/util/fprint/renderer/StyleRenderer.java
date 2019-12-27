package net.vpc.app.nuts.runtime.util.fprint.renderer;

import net.vpc.app.nuts.runtime.util.fprint.RenderedRawStream;

import java.io.IOException;

public interface StyleRenderer {
    void startFormat(RenderedRawStream out) throws IOException;

    void endFormat(RenderedRawStream out) throws IOException;
}
