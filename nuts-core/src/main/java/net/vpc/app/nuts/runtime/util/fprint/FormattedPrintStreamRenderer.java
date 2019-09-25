package net.vpc.app.nuts.runtime.util.fprint;

public interface FormattedPrintStreamRenderer {

    void startFormat(FormattedPrintStream out, TextFormat format);

    void endFormat(FormattedPrintStream out, TextFormat color);
}
