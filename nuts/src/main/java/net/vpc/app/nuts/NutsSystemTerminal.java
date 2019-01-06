package net.vpc.app.nuts;

import java.io.InputStream;
import java.io.OutputStream;

public interface NutsSystemTerminal extends NutsSystemTerminalBase {
    void setMode(NutsTerminalMode mode);

    <T> T ask(NutsQuestion<T> question);

    boolean isStandardOutputStream(OutputStream out);

    boolean isStandardErrorStream(OutputStream out);

    boolean isStandardInputStream(InputStream in);
}
