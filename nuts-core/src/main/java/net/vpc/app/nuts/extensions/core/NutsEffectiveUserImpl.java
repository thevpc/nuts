/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.extensions.core;

import java.util.Arrays;
import net.vpc.app.nuts.NutsUserConfig;
import net.vpc.app.nuts.NutsEffectiveUser;
import net.vpc.app.nuts.extensions.util.CoreStringUtils;

/**
 *
 * @author vpc
 */
public class NutsEffectiveUserImpl implements NutsEffectiveUser {

    private final String mappedUser;
    private final String user;
    private final boolean credentials;
    private final String[] rights;
    private final String[] inheritedRights;
    private final String[] groups;

    public NutsEffectiveUserImpl(NutsUserConfig config, String[] inheritedRights) {
        user = config.getUser();
        mappedUser = config.getMappedUser();
        credentials = !CoreStringUtils.isEmpty(config.getCredentials());

        String[] rights0 = config.getRights();
        rights = Arrays.copyOf(rights0, rights0.length);

        String[] groups0 = config.getGroups();
        groups = Arrays.copyOf(groups0, groups0.length);
        this.inheritedRights = Arrays.copyOf(inheritedRights, inheritedRights.length);
    }

    @Override
    public String[] getInheritedRights() {
        return inheritedRights;
    }

    @Override
    public String getMappedUser() {
        return mappedUser;
    }

    @Override
    public String getUser() {
        return user;
    }

    @Override
    public boolean hasCredentials() {
        return credentials;
    }

    @Override
    public String[] getRights() {
        return rights;
    }

    @Override
    public String[] getGroups() {
        return groups;
    }

}
