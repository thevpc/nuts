package net.thevpc.nuts.runtime.util.fprint;

import net.thevpc.nuts.runtime.util.fprint.renderer.StyleRenderer;

import java.io.IOException;

public interface FormattedPrintStreamRenderer {

    StyleRenderer createStyleRenderer(TextFormat format);

    void startFormat(RenderedRawStream out, TextFormat format) throws IOException;

    void endFormat(RenderedRawStream out, TextFormat color) throws IOException;
}
