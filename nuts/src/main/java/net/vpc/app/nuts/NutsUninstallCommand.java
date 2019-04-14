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
public interface NutsUninstallCommand {

    NutsUninstallCommand id(NutsId id);

    NutsUninstallCommand addId(NutsId id);

    NutsUninstallCommand removeId(NutsId id);

    NutsUninstallCommand id(String id);

    NutsUninstallCommand addId(String id);

    NutsUninstallCommand removeId(String id);

    NutsUninstallCommand addIds(NutsId... ids);

    NutsUninstallCommand addIds(String... ids);

    NutsUninstallCommand clearIds();

    NutsId[] getIds();

    NutsUninstallCommand arg(String arg);

    NutsUninstallCommand addArg(String arg);

    NutsUninstallCommand args(List<String> args);

    NutsUninstallCommand addArgs(Collection<String> args);

    NutsUninstallCommand args(String... args);

    NutsUninstallCommand addArgs(String... args);

    NutsUninstallCommand clearArgs();

    String[] getArgs();

    NutsUninstallCommand ask();

    NutsUninstallCommand ask(boolean ask);

    NutsUninstallCommand setAsk(boolean ask);

    boolean isAsk();

    NutsUninstallCommand force();

    NutsUninstallCommand force(boolean forceInstall);

    NutsUninstallCommand setForce(boolean forceInstall);

    boolean isForce();

    NutsUninstallCommand trace();

    NutsUninstallCommand trace(boolean trace);

    NutsUninstallCommand setTrace(boolean trace);

    boolean isTrace();

    NutsUninstallCommand erase();

    NutsUninstallCommand erase(boolean erase);

    NutsUninstallCommand setErase(boolean erase);

    boolean isErase();

    NutsUninstallCommand session(NutsSession session);

    NutsUninstallCommand setSession(NutsSession session);

    NutsSession getSession();

    NutsUninstallCommand parseOptions(String... args);

    NutsUninstallCommand uninstall();

}
