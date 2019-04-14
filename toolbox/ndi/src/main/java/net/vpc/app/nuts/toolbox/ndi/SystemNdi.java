package net.vpc.app.nuts.toolbox.ndi;


import java.io.IOException;

public interface SystemNdi {
    void configurePath(boolean force, boolean trace) throws IOException;

    void createNutsScript(NdiScriptOptions options) throws IOException;
}
