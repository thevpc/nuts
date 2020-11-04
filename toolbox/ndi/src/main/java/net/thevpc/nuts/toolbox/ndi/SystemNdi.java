package net.thevpc.nuts.toolbox.ndi;

import java.io.IOException;

import net.thevpc.nuts.NutsSession;

public interface SystemNdi {
    void configurePath(NutsSession session) throws IOException;

    NdiScriptnfo[] createNutsScript(NdiScriptOptions options) throws IOException;

    void removeNutsScript(String id, NutsSession session) throws IOException;
}
