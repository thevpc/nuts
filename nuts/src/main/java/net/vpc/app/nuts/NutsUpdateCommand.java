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

    NutsUpdateCommand addId(NutsId id);

    NutsUpdateCommand addId(String id);

    NutsUpdateCommand addIds(NutsId... ids);

    NutsUpdateCommand addIds(String... ids);

    NutsId[] getIds();

    NutsId[] getFrozenIds();

    String[] getArgs();

    NutsSession getSession();

    NutsUpdateCommand id(NutsId id);

    NutsUpdateCommand id(String id);

    NutsUpdateCommand ids(NutsId... id);

    NutsUpdateCommand ids(String... id);

    boolean isEnableInstall();

    boolean isAsk();

    boolean isForce();

    boolean isTrace();

    NutsUpdateCommand setArgs(List<String> args);

    NutsUpdateCommand setArgs(String... args);

    NutsUpdateCommand setAsk(boolean ask);

    NutsUpdateCommand setForce(boolean forceInstall);

    NutsUpdateCommand setId(NutsId id);

    NutsUpdateCommand setId(String id);

    NutsUpdateCommand setSession(NutsSession session);

    NutsUpdateCommand setTrace(boolean trace);

    NutsUpdateCommand setEnableInstall(boolean enableInstall);

    NutsUpdateCommand update();
    
    boolean isUpdateAvailable() ;

    NutsUpdateResult[] getUpdateResult();

    NutsUpdateCommand checkUpdates();

    NutsUpdateCommand checkUpdates(boolean applyUpdates);
}
