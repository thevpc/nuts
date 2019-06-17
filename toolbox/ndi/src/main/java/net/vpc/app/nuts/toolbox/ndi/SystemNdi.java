package net.vpc.app.nuts.toolbox.ndi;

import java.io.IOException;

public interface SystemNdi {

    void configurePath(boolean force, boolean trace) throws IOException;

    NdiScriptnfo[] createNutsScript(NdiScriptOptions options) throws IOException;
}
