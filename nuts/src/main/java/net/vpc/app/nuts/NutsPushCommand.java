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
public interface NutsPushCommand {

    NutsPushCommand addArg(String arg);

    NutsPushCommand addArgs(List<String> args);

    NutsPushCommand addArgs(String... args);

    NutsPushCommand addId(NutsId id);

    NutsPushCommand addId(String id);

    NutsPushCommand addIds(NutsId... ids);

    NutsPushCommand addIds(String... ids);

    NutsId[] getIds();

    NutsId[] getFrozenIds();

    String[] getArgs();

    NutsSession getSession();

    NutsPushCommand id(NutsId id);

    NutsPushCommand id(String id);

    NutsPushCommand ids(NutsId... id);

    NutsPushCommand ids(String... id);

    boolean isAsk();

    boolean isForce();

    boolean isTrace();

    boolean isOffline();

    NutsPushCommand setArgs(List<String> args);

    NutsPushCommand setArgs(String... args);

    NutsPushCommand setAsk(boolean ask);

    NutsPushCommand setForce(boolean force);

    NutsPushCommand setOffline(boolean offline);

    NutsPushCommand setId(NutsId id);

    NutsPushCommand setId(String id);

    NutsPushCommand setSession(NutsSession session);

    NutsPushCommand setTrace(boolean trace);

    void push();

    String getRepository();

    NutsPushCommand setRepository(String repository);

    NutsPushCommand repository(String repository);

}
