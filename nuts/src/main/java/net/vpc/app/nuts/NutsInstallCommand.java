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
public interface NutsInstallCommand {

    NutsInstallCommand id(NutsId id);

    NutsInstallCommand id(String id);

    NutsInstallCommand removeId(NutsId id);

    NutsInstallCommand removeId(String id);

    NutsInstallCommand ids(NutsId... ids);

    NutsInstallCommand ids(String... ids);

    NutsInstallCommand addId(NutsId id);

    NutsInstallCommand addId(String id);

    NutsInstallCommand addIds(NutsId... ids);

    NutsInstallCommand addIds(String... ids);

    NutsInstallCommand clearIds();

    NutsId[] getIds();

    NutsInstallCommand arg(String arg);

    NutsInstallCommand addArg(String arg);

    NutsInstallCommand args(Collection<String> args);

    NutsInstallCommand addArgs(Collection<String> args);

    NutsInstallCommand addArgs(String... args);

    NutsInstallCommand args(String... args);

    NutsInstallCommand clearArgs();

    String[] getArgs();

    NutsInstallCommand setSession(NutsSession session);

    NutsInstallCommand session(NutsSession session);

    NutsSession getSession();

    NutsInstallCommand ask();

    NutsInstallCommand ask(boolean ask);

    NutsInstallCommand setAsk(boolean ask);

    boolean isAsk();

    NutsInstallCommand force();

    NutsInstallCommand force(boolean forceInstall);

    NutsInstallCommand setForce(boolean forceInstall);

    boolean isForce();

    NutsInstallCommand trace();

    NutsInstallCommand trace(boolean trace);

    NutsInstallCommand setTrace(boolean trace);

    boolean isTrace();

    NutsInstallCommand defaultVersion();

    NutsInstallCommand defaultVersion(boolean defaultVersion);

    /**
     *
     * @param defaultVersion when true, the installed version will be defined as
     * default
     * @return
     */
    NutsInstallCommand setDefaultVersion(boolean defaultVersion);

    boolean isDefaultVersion();

    boolean isIncludeCompanions();

    NutsInstallCommand includeCompanions();

    NutsInstallCommand includeCompanions(boolean includecompanions);

    NutsInstallCommand setIncludeCompanions(boolean includecompanions);

    NutsInstallCommand parseOptions(String... args);

    NutsInstallCommand install();

    int getInstallResultCount();

    NutsDefinition[] getInstallResult();
}
