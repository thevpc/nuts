/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.spi;

import net.vpc.app.nuts.NutsRepositoryConfigManager;
import net.vpc.app.nuts.NutsUserConfig;

/**
 *
 * @author vpc
 */
public interface NutsRepositoryConfigManagerExt {

    public static NutsRepositoryConfigManagerExt of(NutsRepositoryConfigManager o) {
        return ((NutsRepositoryConfigManagerExt) o);
    }

    NutsRepositoryConfigManager removeUser(String userId);

    NutsRepositoryConfigManager setUser(NutsUserConfig user);

    NutsUserConfig getUser(String userId);

    NutsUserConfig[] getUsers();

}
