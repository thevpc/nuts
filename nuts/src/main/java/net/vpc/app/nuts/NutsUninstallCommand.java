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
public interface NutsUninstallCommand {

    NutsUninstallCommand addArg(String arg);

    NutsUninstallCommand addArgs(List<String> args);

    NutsUninstallCommand addArgs(String... args);

    NutsUninstallCommand addId(NutsId id);

    NutsUninstallCommand addId(String id);

    NutsUninstallCommand addIds(NutsId... ids);

    NutsUninstallCommand addIds(String... ids);

    NutsId[] getIds();

    String[] getArgs();

    NutsSession getSession();

    NutsUninstallCommand id(NutsId id);

    NutsUninstallCommand id(String id);

    void uninstall();

    boolean isAsk();

    boolean isForce();

    boolean isTrace();

    NutsUninstallCommand setArgs(List<String> args);

    NutsUninstallCommand setArgs(String... args);

    NutsUninstallCommand setAsk(boolean ask);

    NutsUninstallCommand setForce(boolean forceInstall);

    NutsUninstallCommand setId(NutsId id);

    NutsUninstallCommand setId(String id);

    NutsUninstallCommand setSession(NutsSession session);

    NutsUninstallCommand setTrace(boolean trace);

    boolean isErase();

    NutsUninstallCommand setErase(boolean erase);

}
