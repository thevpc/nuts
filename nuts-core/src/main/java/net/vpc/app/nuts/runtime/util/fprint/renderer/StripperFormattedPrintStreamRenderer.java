package net.vpc.app.nuts.runtime.util.fprint.renderer;

import net.vpc.app.nuts.runtime.util.fprint.RenderedRawStream;
import net.vpc.app.nuts.runtime.util.fprint.TextFormat;
import net.vpc.app.nuts.runtime.util.fprint.FormattedPrintStreamRenderer;

public class StripperFormattedPrintStreamRenderer implements FormattedPrintStreamRenderer {

    public static final FormattedPrintStreamRenderer STRIPPER = new StripperFormattedPrintStreamRenderer();
    public static final MyStyleRenderer EMPTY_STYLE_RENDERER = new MyStyleRenderer();

    @Override
    public void startFormat(RenderedRawStream out, TextFormat format) {
        //do nothing
    }

    @Override
    public void endFormat(RenderedRawStream out, TextFormat color) {
        //
    }

    @Override
    public StyleRenderer createStyleRenderer(TextFormat format) {
        return EMPTY_STYLE_RENDERER;
    }

    private static class MyStyleRenderer implements StyleRenderer {
        @Override
        public void startFormat(RenderedRawStream out) {

        }

        @Override
        public void endFormat(RenderedRawStream out) {

        }
    }
}
