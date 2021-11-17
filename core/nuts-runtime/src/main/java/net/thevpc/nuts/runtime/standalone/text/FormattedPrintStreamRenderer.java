package net.thevpc.nuts.runtime.standalone.text;

import net.thevpc.nuts.runtime.standalone.text.renderer.StyleRenderer;

import java.io.IOException;
import net.thevpc.nuts.NutsSession;

public interface FormattedPrintStreamRenderer {

    StyleRenderer createStyleRenderer(AnsiEscapeCommand format, RenderedRawStream out, NutsSession session);

    void startFormat(RenderedRawStream out, AnsiEscapeCommand format, NutsSession session) throws IOException;

    void endFormat(RenderedRawStream out, AnsiEscapeCommand color, NutsSession session) throws IOException;
}
