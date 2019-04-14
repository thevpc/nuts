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
public interface NutsPushCommand {

    NutsPushCommand id(NutsId id);

    NutsPushCommand id(String id);

    NutsPushCommand removeId(NutsId id);

    NutsPushCommand addId(NutsId id);

    NutsPushCommand removeId(String id);

    NutsPushCommand addId(String id);

    NutsPushCommand addIds(NutsId... ids);

    NutsPushCommand addIds(String... ids);

    NutsPushCommand ids(NutsId... ids);

    NutsPushCommand ids(String... ids);

    NutsPushCommand clearIds();

    NutsId[] getIds();

    NutsPushCommand frozenId(NutsId id);

    NutsPushCommand frozenId(String id);

    NutsPushCommand removeFrozenId(NutsId id);
    NutsPushCommand addFrozenId(NutsId id);

    NutsPushCommand removeFrozenId(String id);
    NutsPushCommand addFrozenId(String id);

    NutsPushCommand addFrozenIds(NutsId... ids);

    NutsPushCommand addFrozenIds(String... ids);

    NutsPushCommand frozenIds(NutsId... ids);

    NutsPushCommand frozenIds(String... ids);

    NutsPushCommand clearFrozenIds();

    NutsId[] getFrozenIds();

    NutsPushCommand arg(String arg);

    NutsPushCommand addArg(String arg);

    NutsPushCommand args(String... args);

    NutsPushCommand addArgs(String... args);

    NutsPushCommand args(Collection<String> args);

    NutsPushCommand addArgs(Collection<String> args);

    NutsPushCommand clearArgs();

    String[] getArgs();

    NutsPushCommand session(NutsSession session);

    NutsPushCommand setSession(NutsSession session);

    NutsSession getSession();

    NutsPushCommand ask();

    NutsPushCommand ask(boolean enable);

    NutsPushCommand setAsk(boolean enable);

    boolean isAsk();

    NutsPushCommand force();

    NutsPushCommand force(boolean enable);

    NutsPushCommand setForce(boolean enable);

    boolean isForce();

    NutsPushCommand trace();

    NutsPushCommand trace(boolean enable);

    NutsPushCommand setTrace(boolean enable);

    boolean isTrace();

    NutsPushCommand offline();

    NutsPushCommand offline(boolean offline);

    NutsPushCommand setOffline(boolean offline);

    boolean isOffline();

    NutsPushCommand repository(String repository);

    NutsPushCommand setRepository(String repository);

    String getRepository();

    /**
     * execute the push command with the built options and return
     * <code>this</code> instance.
     *
     * @return <code>this</code> instance
     */
    NutsPushCommand push();

}
