/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts;

import java.util.List;

/**
 *
 * @author vpc
 */
public interface NutsUpdateWorkspaceCommand {

    NutsUpdateWorkspaceCommand addArg(String arg);

    NutsUpdateWorkspaceCommand addArgs(List<String> args);

    NutsUpdateWorkspaceCommand addArgs(String... args);

    NutsId[] getFrozenIds();

    String[] getArgs();

    NutsSession getSession();

    boolean isEnableInstall();

    boolean isAsk();

    boolean isForce();

    boolean isTrace();

    NutsUpdateWorkspaceCommand setArgs(List<String> args);

    NutsUpdateWorkspaceCommand setArgs(String... args);

    NutsUpdateWorkspaceCommand setAsk(boolean ask);

    NutsUpdateWorkspaceCommand setForce(boolean forceInstall);

    NutsUpdateWorkspaceCommand setSession(NutsSession session);

    NutsUpdateWorkspaceCommand setTrace(boolean trace);

    NutsUpdateWorkspaceCommand setEnableInstall(boolean enableInstall);

    boolean isEnableMajorUpdates();

    NutsUpdateWorkspaceCommand setEnableMajorUpdates(boolean enableMajorUpdates);

    boolean isUpdateExtensions();

    NutsUpdateWorkspaceCommand setUpdateExtensions(boolean updateExtensions);

    String getForceBootAPIVersion();

    NutsUpdateWorkspaceCommand setForceBootAPIVersion(String forceBootAPIVersion);

    /**
     * 
     * @return null if no updates
     */
    NutsWorkspaceUpdateResult update();

    /**
     * 
     * @return  null if no updates
     */
    NutsWorkspaceUpdateResult checkUpdates();

    /**
     * 
     * @param applyUpdates
     * @return  null if no updates
     */
    NutsWorkspaceUpdateResult checkUpdates(boolean applyUpdates);
}
