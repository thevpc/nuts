/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.filters;

import net.vpc.app.nuts.NutsDescriptor;
import net.vpc.app.nuts.NutsId;
import net.vpc.app.nuts.NutsSearchId;
import net.vpc.app.nuts.NutsVersion;
import net.vpc.app.nuts.NutsWorkspace;

/**
 *
 * @author vpc
 */
public class NutsSearchIdByDescriptor implements NutsSearchId {

    private NutsDescriptor desc;

    public NutsSearchIdByDescriptor(NutsDescriptor desc) {
        this.desc = desc;
    }

    @Override
    public NutsVersion getVersion(NutsWorkspace ws) {
        return desc.getId().getVersion();
    }

    @Override
    public NutsId getId(NutsWorkspace ws) {
        return desc.getId();
    }

    @Override
    public NutsDescriptor getDescriptor(NutsWorkspace ws) {
        return desc;
    }

}
