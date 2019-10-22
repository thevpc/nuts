package net.vpc.app.nuts.runtime.util.fprint.renderer;

import net.vpc.app.nuts.runtime.util.fprint.FormattedPrintStream;

public interface StyleRenderer {
    void startFormat(FormattedPrintStream out);

    void endFormat(FormattedPrintStream out);
}
