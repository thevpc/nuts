package net.thevpc.nuts.lib.ssh;

import net.thevpc.nuts.NutsMessage;
import net.thevpc.nuts.NutsString;

import java.io.InputStream;

public interface SshListener {
    void onExec(String command);

    void onGet(String from, String to, boolean mkdir);

    void onPut(String from, String to, boolean mkdir);

    InputStream monitorInputStream(InputStream stream, long length, NutsMessage message);
}
