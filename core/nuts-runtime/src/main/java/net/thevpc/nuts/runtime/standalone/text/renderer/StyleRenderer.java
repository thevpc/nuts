package net.thevpc.nuts.runtime.standalone.text.renderer;

import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.runtime.standalone.text.RenderedRawStream;

public interface StyleRenderer {
    void startFormat(RenderedRawStream out, NutsSession session);

    void endFormat(RenderedRawStream out, NutsSession session) ;
}
