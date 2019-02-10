package net.vpc.app.nuts.toolbox.ndi;

import net.vpc.app.nuts.NutsExecutionType;

import java.io.IOException;
import java.util.List;

public interface SystemNdi {
    void configurePath(boolean force, boolean silent) throws IOException;

    void createNutsScript(NdiScriptOptions options) throws IOException;
}
