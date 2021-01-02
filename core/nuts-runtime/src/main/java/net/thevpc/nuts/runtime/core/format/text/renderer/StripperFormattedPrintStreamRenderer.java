package net.thevpc.nuts.runtime.core.format.text.renderer;

import net.thevpc.nuts.NutsWorkspace;
import net.thevpc.nuts.runtime.core.format.text.AnsiEscapeCommand;
import net.thevpc.nuts.runtime.core.format.text.RenderedRawStream;
import net.thevpc.nuts.runtime.core.format.text.FormattedPrintStreamRenderer;

public class StripperFormattedPrintStreamRenderer implements FormattedPrintStreamRenderer {

    public static final FormattedPrintStreamRenderer STRIPPER = new StripperFormattedPrintStreamRenderer();
    public static final MyStyleRenderer EMPTY_STYLE_RENDERER = new MyStyleRenderer();

    @Override
    public void startFormat(RenderedRawStream out, AnsiEscapeCommand format, NutsWorkspace ws) {
        //do nothing
    }

    @Override
    public void endFormat(RenderedRawStream out, AnsiEscapeCommand color, NutsWorkspace ws) {
        //
    }

    @Override
    public StyleRenderer createStyleRenderer(AnsiEscapeCommand format, NutsWorkspace ws) {
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
