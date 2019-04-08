package net.vpc.app.nuts.core.util.bundledlibs.fprint;

public interface FormattedPrintStreamRenderer {
    void startFormat(FormattedPrintStream out, TextFormat format);
    void endFormat(FormattedPrintStream out, TextFormat color);
}
