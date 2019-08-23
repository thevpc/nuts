package net.vpc.app.nuts.toolbox.ndi;

import java.io.IOException;

import net.vpc.app.nuts.NutsSdkLocation;
import net.vpc.app.nuts.NutsSession;

public interface SystemNdi {
    void configurePath(NutsSession session) throws IOException;

    NdiScriptnfo[] createNutsScript(NdiScriptOptions options) throws IOException;

    void removeNutsScript(String id, NutsSession session) throws IOException;
}
