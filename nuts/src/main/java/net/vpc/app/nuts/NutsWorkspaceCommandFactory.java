package net.vpc.app.nuts;

import java.util.List;

public interface NutsWorkspaceCommandFactory {
    void configure(NutsWorkspaceCommandFactoryConfig config);

    int getPriority();

    String getFactoryId();

    NutsWorkspaceCommandConfig findCommand(String name, NutsWorkspace workspace);

    List<NutsWorkspaceCommandConfig> findCommands(NutsWorkspace workspace);

}
