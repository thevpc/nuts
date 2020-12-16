package net.thevpc.nuts.runtime.standalone.format.text;

import net.thevpc.nuts.runtime.standalone.format.text.renderer.StyleRenderer;

import java.io.IOException;

public interface FormattedPrintStreamRenderer {

    StyleRenderer createStyleRenderer(TextFormat format);

    void startFormat(RenderedRawStream out, TextFormat format) throws IOException;

    void endFormat(RenderedRawStream out, TextFormat color) throws IOException;
}
