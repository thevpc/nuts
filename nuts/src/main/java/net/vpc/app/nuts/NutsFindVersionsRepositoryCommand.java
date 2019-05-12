/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts;

import java.util.Iterator;

/**
 *
 * @author vpc
 */
public interface NutsFindVersionsRepositoryCommand extends NutsRepositoryCommand {

    NutsFindVersionsRepositoryCommand filter(NutsIdFilter filter);

    NutsFindVersionsRepositoryCommand setFilter(NutsIdFilter filter);

    NutsIdFilter getFilter();

    NutsFindVersionsRepositoryCommand setId(NutsId id);

    NutsId getId();

    @Override
    public NutsFindVersionsRepositoryCommand run();

    @Override
    public NutsFindVersionsRepositoryCommand session(NutsRepositorySession session);

    @Override
    public NutsFindVersionsRepositoryCommand setSession(NutsRepositorySession session);

    Iterator<NutsId> getResult();

    NutsFindVersionsRepositoryCommand id(NutsId id);

}
