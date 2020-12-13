/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.core.config;

import net.thevpc.nuts.NutsRemoveOptions;
import net.thevpc.nuts.NutsRepositoryConfigManager;
import net.thevpc.nuts.NutsUpdateOptions;
import net.thevpc.nuts.NutsUserConfig;

/**
 *
 * @author thevpc
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
