/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.security;

import net.thevpc.nuts.NutsBlankable;
import net.thevpc.nuts.NutsUser;
import net.thevpc.nuts.NutsUserConfig;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author thevpc
 */
public class DefaultNutsUser implements NutsUser {

    private final String remoteIdentity;
    private final String user;
    private final boolean credentials;
    private final List<String> permissions;
    private final List<String> inheritedPermissions;
    private final List<String> groups;

    public DefaultNutsUser(NutsUserConfig config, List<String> inheritedPermissions) {
        user = config.getUser();
        remoteIdentity = config.getRemoteIdentity();
        credentials = !NutsBlankable.isBlank(config.getCredentials());

        permissions = new ArrayList<>(config.getPermissions());
        groups = new ArrayList<>(config.getGroups());
        this.inheritedPermissions = new ArrayList<>(inheritedPermissions);
    }

    @Override
    public List<String> getInheritedPermissions() {
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
    public List<String> getPermissions() {
        return permissions;
    }

    @Override
    public List<String> getGroups() {
        return groups;
    }

}
