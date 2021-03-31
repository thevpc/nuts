package net.thevpc.nuts.runtime.core.format.text.renderer;

import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.runtime.core.format.text.AnsiEscapeCommand;
import net.thevpc.nuts.runtime.core.format.text.RenderedRawStream;
import net.thevpc.nuts.runtime.core.format.text.FormattedPrintStreamRenderer;

public class StripperFormattedPrintStreamRenderer implements FormattedPrintStreamRenderer {

    public static final FormattedPrintStreamRenderer STRIPPER = new StripperFormattedPrintStreamRenderer();
    public static final MyStyleRenderer EMPTY_STYLE_RENDERER = new MyStyleRenderer();

    @Override
    public void startFormat(RenderedRawStream out, AnsiEscapeCommand format, NutsSession session) {
        //do nothing
    }

    @Override
    public void endFormat(RenderedRawStream out, AnsiEscapeCommand color, NutsSession session) {
        //
    }

    @Override
    public StyleRenderer createStyleRenderer(AnsiEscapeCommand format, RenderedRawStream out, NutsSession session) {
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
