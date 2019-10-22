package net.vpc.app.nuts.runtime.util.fprint;

import net.vpc.app.nuts.runtime.util.fprint.renderer.StyleRenderer;

public interface FormattedPrintStreamRenderer {

    StyleRenderer createStyleRenderer(TextFormat format);

    void startFormat(FormattedPrintStream out, TextFormat format);

    void endFormat(FormattedPrintStream out, TextFormat color);
}
