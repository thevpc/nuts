/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.security;

import net.thevpc.nuts.runtime.standalone.util.CoreNUtils;
import net.thevpc.nuts.security.NUserSpec;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.security.NUser;
import net.thevpc.nuts.security.NUserConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * @author thevpc
 */
public class DefaultNUser implements NUser {

    private final String username;
    private final boolean credentials;
    private final List<String> permissions;
    private final List<String> inheritedPermissions;
    private final List<String> groups;

    public DefaultNUser(String username, boolean credentials, List<String> permissions, List<String> inheritedPermissions, List<String> groups) {
        this.username = username;
        this.credentials = credentials;
        this.permissions = CoreNUtils.copyNonNullUnmodifiableList(permissions);
        this.inheritedPermissions = CoreNUtils.copyNonNullUnmodifiableList(inheritedPermissions);
        this.groups = CoreNUtils.copyNonNullUnmodifiableList(groups);
    }

    public DefaultNUser(NUserConfig config, List<String> inheritedPermissions) {
        username = config.userName();
        credentials = !NBlankable.isBlank(config.credential());

        permissions = config.permissions() == null ? new ArrayList<>() : new ArrayList<>(config.permissions());
        groups = config.groups() == null ? new ArrayList<>() : new ArrayList<>(config.groups());
        this.inheritedPermissions = inheritedPermissions == null ? new ArrayList<>() : new ArrayList<>(inheritedPermissions);
    }

    @Override
    public List<String> inheritedPermissions() {
        return inheritedPermissions;
    }

    @Override
    public String username() {
        return username;
    }

    @Override
    public boolean hasCredentials() {
        return credentials;
    }

    @Override
    public List<String> permissions() {
        return permissions;
    }

    @Override
    public List<String> groups() {
        return groups;
    }

    @Override
    public NUserSpec toSpec() {
        return new DefaultNUserSpec(username, null, null, new ArrayList<>(permissions), new ArrayList<>(groups));
    }
}
