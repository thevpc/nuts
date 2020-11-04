package net.thevpc.nuts;

import java.io.Serializable;

public interface NutsWorkspaceOptionsFormat extends Serializable {
    boolean isInit();

    boolean isRuntime();

    boolean isExported();

    NutsWorkspaceOptionsFormat exported();

    NutsWorkspaceOptionsFormat exported(boolean e);

    NutsWorkspaceOptionsFormat setExported(boolean e);

    String getApiVersion();

    NutsWorkspaceOptionsFormat setApiVersion(String apiVersion);

    NutsWorkspaceOptionsFormat runtime();

    NutsWorkspaceOptionsFormat runtime(boolean e);

    NutsWorkspaceOptionsFormat setRuntime(boolean e);

    NutsWorkspaceOptionsFormat init();

    NutsWorkspaceOptionsFormat init(boolean e);

    NutsWorkspaceOptionsFormat setInit(boolean e);

    String getBootCommandLine();

    String[] getBootCommand();

    NutsWorkspaceOptionsFormat compact();

    NutsWorkspaceOptionsFormat compact(boolean compact);

    NutsWorkspaceOptionsFormat setCompact(boolean compact);

    @Override
    String toString();

    @Override
    boolean equals(Object o);

    @Override
    int hashCode();
}
