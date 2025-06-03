package net.thevpc.nuts;

import java.io.InputStream;
import java.io.OutputStream;

public interface NExecCmdExtensionContext  {
    String getTarget();

    String[] getCommand();

    InputStream in();

    OutputStream out();

    OutputStream err();

    NExecCmd getExecCommand();
}
