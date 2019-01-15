package net.vpc.app.nuts.toolbox.ndi;

import java.io.IOException;

public interface SystemNdi {
    void configurePath(boolean force, boolean silent) throws IOException;

    void createNutsScript(String id, boolean force, boolean forceBoot, boolean silent, boolean fetch) throws IOException;
}
