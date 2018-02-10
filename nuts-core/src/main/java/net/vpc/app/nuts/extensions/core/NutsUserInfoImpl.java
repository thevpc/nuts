/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.extensions.core;

import java.util.Arrays;
import net.vpc.app.nuts.NutsSecurityEntityConfig;
import net.vpc.app.nuts.NutsUserInfo;
import net.vpc.app.nuts.extensions.util.CoreStringUtils;

/**
 *
 * @author vpc
 */
public class NutsUserInfoImpl implements NutsUserInfo {

    private final String mappedUser;
    private final String user;
    private final boolean credentials;
    private final String[] rights;
    private final String[] groups;

    public NutsUserInfoImpl(NutsSecurityEntityConfig config) {
        user = config.getUser();
        mappedUser = config.getMappedUser();
        credentials = !CoreStringUtils.isEmpty(config.getCredentials());

        String[] rights0 = config.getRights();
        rights = Arrays.copyOf(rights0, rights0.length);

        String[] groups0 = config.getGroups();
        groups = Arrays.copyOf(groups0, groups0.length);
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
