/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.extensions.repos;

import net.vpc.app.nuts.*;

import java.io.File;

/**
 *
 * @author vpc
 */
public class NutsBootFolderRepository extends NutsFolderRepository {

    public NutsBootFolderRepository(NutsBootWorkspace bootWorkspace, NutsWorkspace workspace, String root) {
        super(NutsConstants.BOOTSTRAP_REPOSITORY_NAME, bootWorkspace.getBootstrapLocation(), workspace, null);
    }

    @Override
    protected String getStoreRoot() {
        return getConfigManager().getLocationFolder();
    }

    protected NutsFile getLocalNutDescriptorFile(NutsId id) {
        return new NutsFile(
                id, null,
                new File(getLocalVersionFolder(id), NutsConstants.NUTS_DESC_FILE_NAME).getPath(), true, true, null
        );
    }

}
