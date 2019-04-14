/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts;

import java.util.Collection;

/**
 *
 * @author vpc
 */
public interface NutsUpdateCommand {

    NutsUpdateCommand id(NutsId id);

    NutsUpdateCommand id(String id);

    NutsUpdateCommand removeId(NutsId id);

    NutsUpdateCommand addId(NutsId id);

    NutsUpdateCommand removeId(String id);

    NutsUpdateCommand addId(String id);

    NutsUpdateCommand ids(NutsId... id);

    NutsUpdateCommand ids(String... id);

    NutsUpdateCommand addIds(NutsId... ids);

    NutsUpdateCommand addIds(String... ids);

    NutsUpdateCommand clearIds();

    NutsId[] getIds();
//////

    NutsUpdateCommand frozenId(NutsId id);

    NutsUpdateCommand frozenId(String id);

    NutsUpdateCommand addFrozenId(NutsId id);

    NutsUpdateCommand addFrozenId(String id);

    NutsUpdateCommand frozenIds(NutsId... id);

    NutsUpdateCommand frozenIds(String... id);

    NutsUpdateCommand addFrozenIds(NutsId... ids);

    NutsUpdateCommand addFrozenIds(String... ids);

    NutsUpdateCommand clearFrozenIds();

    NutsId[] getFrozenIds();

    NutsUpdateCommand arg(String arg);

    NutsUpdateCommand addArg(String arg);

    NutsUpdateCommand args(String... arg);

    NutsUpdateCommand addArgs(Collection<String> args);

    NutsUpdateCommand args(Collection<String> arg);

    NutsUpdateCommand addArgs(String... args);

    NutsUpdateCommand clearArgs();

    String[] getArgs();

    NutsUpdateCommand session(NutsSession session);

    NutsUpdateCommand setSession(NutsSession session);

    NutsSession getSession();

    NutsUpdateCommand enableInstall();

    NutsUpdateCommand enableInstall(boolean enableInstall);

    NutsUpdateCommand setEnableInstall(boolean enableInstall);

    boolean isEnableInstall();

    NutsUpdateCommand ask();

    NutsUpdateCommand ask(boolean ask);

    NutsUpdateCommand setAsk(boolean ask);

    boolean isAsk();

    NutsUpdateCommand force();

    /**
     * @see #setForce(boolean)
     * @param enable
     * @return
     */
    NutsUpdateCommand force(boolean enable);

    /**
     * arm or disarm force install non already installed components
     *
     * @param enable if true force install if not yet installed
     * @return current builder instance
     */
    NutsUpdateCommand setForce(boolean enable);

    boolean isForce();

    NutsUpdateCommand trace();

    NutsUpdateCommand trace(boolean trace);

    NutsUpdateCommand setTrace(boolean trace);

    boolean isTrace();

    NutsUpdateCommand includeOptional();

    NutsUpdateCommand includeOptional(boolean includeOptional);

    NutsUpdateCommand setIncludeOptional(boolean includeOptional);

    boolean isIncludeOptional();

    NutsUpdateCommand apiVersion(String forceBootAPIVersion);

    NutsUpdateCommand setApiVersion(String forceBootAPIVersion);

    String getApiVersion();

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

    int getUpdateResultCount();

    NutsUpdateCommand workspace();

    NutsUpdateCommand updateWorkspace();

    NutsUpdateCommand updateWorkspace(boolean enable);

    NutsUpdateCommand setUpdateApi(boolean enable);

    boolean isUpdateApi();

    NutsUpdateCommand extensions();

    NutsUpdateCommand updateExtensions();

    NutsUpdateCommand updateExtensions(boolean enable);

    NutsUpdateCommand setUpdateExtensions(boolean enable);

    boolean isUpdateExtensions();

    NutsUpdateCommand runtime();

    NutsUpdateCommand updateRunime();

    NutsUpdateCommand updateRuntime(boolean enable);

    NutsUpdateCommand setUpdateRuntime(boolean enable);

    boolean isUpdateRuntime();

    NutsUpdateCommand installed();

    NutsUpdateCommand updateInstalled();

    NutsUpdateCommand updateInstalled(boolean updateExtensions);

    NutsUpdateCommand setUpdateInstalled(boolean updateExtensions);

    boolean isUpdateInstalled();

    NutsUpdateCommand all();

    NutsUpdateCommand scope(NutsDependencyScope scope);

    NutsUpdateCommand addScope(NutsDependencyScope scope);

    NutsUpdateCommand scopes(Collection<NutsDependencyScope> scopes);

    NutsUpdateCommand addScopes(Collection<NutsDependencyScope> scopes);

    NutsUpdateCommand scopes(NutsDependencyScope... scopes);

    NutsUpdateCommand addScopes(NutsDependencyScope... scopes);

    NutsUpdateCommand clearScopes();

    NutsUpdateCommand parseOptions(String... applicationArguments);

    NutsUpdateCommand api();

    NutsUpdateCommand api(boolean enable);

    NutsUpdateCommand runtime(boolean enable);

    NutsUpdateCommand extensions(boolean enable);

    NutsUpdateCommand installed(boolean enable);

}
