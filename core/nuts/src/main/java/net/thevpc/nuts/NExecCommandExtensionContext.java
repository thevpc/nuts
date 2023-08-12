package net.thevpc.nuts;

import java.io.InputStream;
import java.io.OutputStream;

public interface NExecCommandExtensionContext extends NSessionProvider {
    String getHost();

    String[] getCommand();

    InputStream in();

    OutputStream out();

    OutputStream err();
}
