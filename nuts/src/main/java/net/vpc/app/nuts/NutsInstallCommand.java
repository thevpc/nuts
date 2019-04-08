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
public interface NutsInstallCommand {

    NutsInstallCommand addArg(String arg);

    NutsInstallCommand addArgs(List<String> args);

    NutsInstallCommand addArgs(String... args);

    NutsInstallCommand addId(NutsId id);

    NutsInstallCommand addId(String id);

    NutsInstallCommand addIds(NutsId... ids);

    NutsInstallCommand addIds(String... ids);

    NutsId[] getIds();
    
    String[] getArgs();

    NutsSession getSession();

    NutsInstallCommand id(NutsId id);

    NutsInstallCommand id(String id);

    NutsDefinition[] install();

    boolean isAsk();

    boolean isForce();

    boolean isTrace();

    NutsInstallCommand setArgs(List<String> args);

    NutsInstallCommand setArgs(String ... args);

    NutsInstallCommand setAsk(boolean ask);

    NutsInstallCommand setForce(boolean forceInstall);

    NutsInstallCommand setId(NutsId id);

    NutsInstallCommand setId(String id);

    NutsInstallCommand setSession(NutsSession session);

    NutsInstallCommand setTrace(boolean trace);

    boolean isIncludecompanions();

    NutsInstallCommand setIncludecompanions(boolean includecompanions);


}
