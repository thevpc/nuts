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
public interface NutsPushRepositoryCommand extends NutsRepositoryCommand {

    NutsPushRepositoryCommand setOffline(boolean offline);

    NutsPushRepositoryCommand setId(NutsId id);

    NutsPushRepositoryCommand id(NutsId id);

    NutsId getId();

    @Override
    NutsPushRepositoryCommand setSession(NutsRepositorySession session);

    @Override
    NutsPushRepositoryCommand session(NutsRepositorySession session);

    @Override
    NutsPushRepositoryCommand run();

    String[] getArgs();

    NutsPushRepositoryCommand addArg(String arg);

    NutsPushRepositoryCommand addArgs(String... args);

    NutsPushRepositoryCommand addArgs(Collection<String> args);

    NutsPushRepositoryCommand clearArgs();

    boolean isOffline();

    NutsPushRepositoryCommand offline();

    NutsPushRepositoryCommand offline(boolean enable);

    NutsPushRepositoryCommand repository(String repository);

    NutsPushRepositoryCommand setRepository(String repository);

    String getRepository();
}
