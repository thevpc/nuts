package net.thevpc.nuts.spi;

import net.thevpc.nuts.command.NExec;
import net.thevpc.nuts.net.NConnectionString;

import java.io.InputStream;
import java.io.OutputStream;

public interface NExecTargetCommandContext {
    NConnectionString getConnectionString();

    String[] getCommand();

    boolean isRawCommand();

    InputStream in();

    OutputStream out();

    OutputStream err();

    NExec getExecCommand();
}
