/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.security;

import net.thevpc.nuts.runtime.standalone.util.CoreNUtils;
import net.thevpc.nuts.security.NCredentialId;
import net.thevpc.nuts.security.NRepositoryAccessSpec;
import net.thevpc.nuts.security.NUserSpec;
import net.thevpc.nuts.util.NBlankable;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author thevpc
 */
public class DefaultNUserSpec implements NUserSpec {

    private String username;
    private NCredentialId credential;
    private List<String> permissions;
    private List<String> groups;

    public DefaultNUserSpec(String username, NCredentialId credential, List<String> permissions, List<String> groups) {
        this.username = username;
        this.credential = credential;
        this.permissions = CoreNUtils.copyNonNullList(permissions);
        this.groups = CoreNUtils.copyNonNullList(groups);
    }

    public NCredentialId getCredential() {
        return credential;
    }

    public DefaultNUserSpec setCredential(NCredentialId credential) {
        this.credential = credential;
        return this;
    }

    @Override
    public DefaultNUserSpec setPermissions(List<String> permissions) {
        this.permissions = permissions;
        return this;
    }

    @Override
    public DefaultNUserSpec setGroups(List<String> groups) {
        this.groups = groups;
        return this;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public List<String> getPermissions() {
        return permissions;
    }

    @Override
    public List<String> getGroups() {
        return groups;
    }


    @Override
    public NUserSpec addPermissions(String... permissions) {
        if (permissions != null) {
            for (String p : permissions) {
                if (!NBlankable.isBlank(p)) {
                    if (this.permissions == null) {
                        this.permissions = new ArrayList<>();
                    }
                    this.permissions.add(p);
                }
            }
        }
        return this;
    }

    @Override
    public NUserSpec removePermissions(String... permissions) {
        if (permissions != null) {
            if (this.permissions != null) {
                for (String p : permissions) {
                    if (!NBlankable.isBlank(p)) {
                        this.permissions.remove(p);
                    }
                }
            }
        }
        return this;
    }

    @Override
    public NUserSpec addGroups(String... groups) {
        if (groups != null) {
            for (String p : groups) {
                if (!NBlankable.isBlank(p)) {
                    if (this.groups == null) {
                        this.groups = new ArrayList<>();
                    }
                    this.groups.add(p);
                }
            }
        }
        return this;
    }

    @Override
    public NUserSpec removeGroups(String... groups) {
        if (groups != null) {
            if (this.groups != null) {
                for (String p : groups) {
                    if (!NBlankable.isBlank(p)) {
                        this.groups.remove(p);
                    }
                }
            }
        }
        return this;
    }
}
