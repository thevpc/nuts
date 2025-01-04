/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.config;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.repository.config.NRepositoryConfigModel;


/**
 *
 * @author thevpc
 */
public interface NRepositoryConfigManagerExt {

    static NRepositoryConfigManagerExt of(NRepositoryConfigManager o) {
        return ((NRepositoryConfigManagerExt) o);
    }

    
    NRepositoryConfigModel getModel();
//    NutsRepositoryConfigManager removeUser(String userId, NutsRemoveOptions options);
//
//    NutsRepositoryConfigManager setUser(NutsUserConfig user, NutsUpdateOptions options);
//
//    NutsUserConfig getUser(String userId);
//
//    NutsUserConfig[] getUsers();
//
//    ////    @Override
//    //    public NutsRepositoryRef[] getMirrorRefs() {
//    //        return configMirrorRefs.values().toArray(new NutsRepositoryRef[0]);
//    //    }
//    boolean save(boolean force, NSession session);
//
//    void save(NSession session);
//
//    Path getTempMirrorsRoot();
//
//    Path getMirrorsRoot();
}
