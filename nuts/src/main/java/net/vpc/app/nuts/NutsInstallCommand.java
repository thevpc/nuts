/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts;

import java.util.Collection;
import java.util.List;

/**
 *
 * @author vpc
 */
public interface NutsInstallCommand {

    NutsInstallCommand id(NutsId id);

    NutsInstallCommand setId(NutsId id);

    NutsInstallCommand id(String id);

    NutsInstallCommand setId(String id);

    NutsInstallCommand addArg(String arg);

    NutsInstallCommand addArgs(Collection<String> args);

    NutsInstallCommand args(Collection<String> args);

    NutsInstallCommand addArgs(String... args);

    NutsInstallCommand args(String... args);

    NutsInstallCommand addId(NutsId id);

    NutsInstallCommand addId(String id);

    NutsInstallCommand addIds(NutsId... ids);

    NutsInstallCommand addIds(String... ids);

    NutsInstallCommand ids(NutsId... ids);

    NutsInstallCommand ids(String... ids);

    NutsId[] getIds();

    String[] getArgs();

    NutsSession getSession();

    NutsDefinition[] install();

    boolean isAsk();

    boolean isForce();

    boolean isTrace();

    boolean isDefaultVersion();

    NutsInstallCommand setArgs(List<String> args);

    NutsInstallCommand setArgs(String... args);

    NutsInstallCommand setAsk(boolean ask);

    NutsInstallCommand setForce(boolean forceInstall);

    NutsInstallCommand setSession(NutsSession session);

    NutsInstallCommand session(NutsSession session);

    NutsInstallCommand setTrace(boolean trace);

    NutsInstallCommand trace(boolean trace);

    NutsInstallCommand trace();

    /**
     *
     * @param defaultVersion when true, the installed version will be defined as
     * default
     * @return
     */
    NutsInstallCommand setDefaultVersion(boolean defaultVersion);

    NutsInstallCommand defaultVersion(boolean defaultVersion);

    NutsInstallCommand defaultVersion();

    boolean isIncludecompanions();

    NutsInstallCommand setIncludeCompanions(boolean includecompanions);

    NutsInstallCommand includeCompanions(boolean includecompanions);

    NutsInstallCommand includecompanions();

}
