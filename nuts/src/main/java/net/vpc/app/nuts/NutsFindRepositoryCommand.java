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
public interface NutsFindRepositoryCommand extends NutsRepositoryCommand {

    NutsFindRepositoryCommand filter(NutsIdFilter filter);

    NutsFindRepositoryCommand setFilter(NutsIdFilter filter);

    NutsIdFilter getFilter();

    @Override
    public NutsFindRepositoryCommand run();

    @Override
    public NutsFindRepositoryCommand session(NutsRepositorySession session);

    @Override
    public NutsFindRepositoryCommand setSession(NutsRepositorySession session);

    Iterator<NutsId> getResult();

}
