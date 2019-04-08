package net.vpc.app.nuts.core.util.bundledlibs.fprint.renderer;

import net.vpc.app.nuts.core.util.bundledlibs.fprint.TextFormat;
import net.vpc.app.nuts.core.util.bundledlibs.fprint.FormattedPrintStream;
import net.vpc.app.nuts.core.util.bundledlibs.fprint.FormattedPrintStreamRenderer;

public class StripperFormattedPrintStreamRenderer implements FormattedPrintStreamRenderer {
    public static final FormattedPrintStreamRenderer STRIPPER=new StripperFormattedPrintStreamRenderer();
    @Override
    public void startFormat(FormattedPrintStream out, TextFormat format) {
       //do nothing
    }

    @Override
    public void endFormat(FormattedPrintStream out, TextFormat color) {
        //
    }
}
