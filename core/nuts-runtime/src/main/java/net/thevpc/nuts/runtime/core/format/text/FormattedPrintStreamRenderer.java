package net.thevpc.nuts.runtime.core.format.text;

import net.thevpc.nuts.NutsWorkspace;
import net.thevpc.nuts.runtime.core.format.text.renderer.StyleRenderer;

import java.io.IOException;

public interface FormattedPrintStreamRenderer {

    StyleRenderer createStyleRenderer(AnsiEscapeCommand format, NutsWorkspace ws);

    void startFormat(RenderedRawStream out, AnsiEscapeCommand format, NutsWorkspace ws) throws IOException;

    void endFormat(RenderedRawStream out, AnsiEscapeCommand color, NutsWorkspace ws) throws IOException;
}
