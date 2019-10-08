package net.vpc.app.nuts.runtime.util.fprint.renderer;

import net.vpc.app.nuts.runtime.util.fprint.TextFormat;
import net.vpc.app.nuts.runtime.util.fprint.FormattedPrintStream;
import net.vpc.app.nuts.runtime.util.fprint.FormattedPrintStreamRenderer;

public class StripperFormattedPrintStreamRenderer implements FormattedPrintStreamRenderer {

    public static final FormattedPrintStreamRenderer STRIPPER = new StripperFormattedPrintStreamRenderer();

    @Override
    public void startFormat(FormattedPrintStream out, TextFormat format) {
        //do nothing
    }

    @Override
    public void endFormat(FormattedPrintStream out, TextFormat color) {
        //
    }
}
