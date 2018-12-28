package net.vpc.app.nuts;

public interface NutsWorkspaceCommand {
    String getFactoryId();

    NutsId getId();

    String getName();

    String[] getCommand();
}
