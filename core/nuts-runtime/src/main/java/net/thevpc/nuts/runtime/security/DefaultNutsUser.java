/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.security;

import net.thevpc.nuts.NutsUser;
import net.thevpc.nuts.NutsUserConfig;

import java.util.Arrays;

import net.thevpc.nuts.runtime.util.common.CoreStringUtils;

/**
 *
 * @author thevpc
 */
public class DefaultNutsUser implements NutsUser {

    private final String remoteIdentity;
    private final String user;
    private final boolean credentials;
    private final String[] permissions;
    private final String[] inheritedPermissions;
    private final String[] groups;

    public DefaultNutsUser(NutsUserConfig config, String[] inheritedPermissions) {
        user = config.getUser();
        remoteIdentity = config.getRemoteIdentity();
        credentials = !CoreStringUtils.isBlank(config.getCredentials());

        String[] rights0 = config.getPermissions();
        permissions = Arrays.copyOf(rights0, rights0.length);

        String[] groups0 = config.getGroups();
        groups = Arrays.copyOf(groups0, groups0.length);
        this.inheritedPermissions = Arrays.copyOf(inheritedPermissions, inheritedPermissions.length);
    }

    @Override
    public String[] getInheritedPermissions() {
        return inheritedPermissions;
    }

    @Override
    public String getRemoteIdentity() {
        return remoteIdentity;
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
    public String[] getPermissions() {
        return permissions;
    }

    @Override
    public String[] getGroups() {
        return groups;
    }

}
