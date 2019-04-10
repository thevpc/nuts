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
public interface NutsUpdateCommand {

    NutsUpdateCommand addArg(String arg);

    NutsUpdateCommand addArgs(List<String> args);

    NutsUpdateCommand addArgs(String... args);

    NutsId[] getFrozenIds();

    String[] getArgs();

    NutsSession getSession();

    boolean isEnableInstall();

    boolean isAsk();

    boolean isForce();

    boolean isTrace();

    NutsUpdateCommand setArgs(List<String> args);

    NutsUpdateCommand setArgs(String... args);

    NutsUpdateCommand setAsk(boolean ask);

    NutsUpdateCommand setForce(boolean forceInstall);

    NutsUpdateCommand setSession(NutsSession session);

    NutsUpdateCommand setTrace(boolean trace);

    NutsUpdateCommand setEnableInstall(boolean enableInstall);

    boolean isUpdateWorkspace();

    NutsUpdateCommand setUpdateWorkspace(boolean enableMajorUpdates);

    boolean isUpdateExtensions();

    NutsUpdateCommand setUpdateExtensions(boolean updateExtensions);

    String getApiVersion();

    NutsUpdateCommand setApiVersion(String forceBootAPIVersion);

    /**
     *
     * @return null if no updates
     */
    NutsUpdateCommand update();

    /**
     *
     * @return null if no updates
     */
    NutsUpdateCommand checkUpdates();

    /**
     *
     * @param applyUpdates
     * @return null if no updates
     */
    NutsUpdateCommand checkUpdates(boolean applyUpdates);

    NutsWorkspaceUpdateResult getUpdateResult();

    int getUpdatesCount();

    NutsUpdateCommand addId(NutsId id);

    NutsUpdateCommand addId(String id);

    NutsUpdateCommand addIds(NutsId... ids);

    NutsUpdateCommand addIds(String... ids);

    NutsId[] getIds();

    NutsUpdateCommand id(NutsId id);

    NutsUpdateCommand id(String id);

    NutsUpdateCommand ids(NutsId... id);

    NutsUpdateCommand ids(String... id);

    NutsUpdateCommand setId(NutsId id);

    NutsUpdateCommand setId(String id);

    boolean isUpdateRuntime();

    boolean isUpdateInstalled();

    NutsUpdateCommand workspace();

    NutsUpdateCommand runtime();

    NutsUpdateCommand extensions();

    NutsUpdateCommand installed();

    NutsUpdateCommand all();

}
