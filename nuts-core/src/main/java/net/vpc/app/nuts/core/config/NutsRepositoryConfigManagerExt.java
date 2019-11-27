/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.config;

import net.vpc.app.nuts.NutsRemoveOptions;
import net.vpc.app.nuts.NutsRepositoryConfigManager;
import net.vpc.app.nuts.NutsUpdateOptions;
import net.vpc.app.nuts.NutsUserConfig;

/**
 *
 * @author vpc
 */
public interface NutsRepositoryConfigManagerExt {

    static NutsRepositoryConfigManagerExt of(NutsRepositoryConfigManager o) {
        return ((NutsRepositoryConfigManagerExt) o);
    }

    NutsRepositoryConfigManager removeUser(String userId, NutsRemoveOptions options);

    NutsRepositoryConfigManager setUser(NutsUserConfig user, NutsUpdateOptions options);

    NutsUserConfig getUser(String userId);

    NutsUserConfig[] getUsers();

}
