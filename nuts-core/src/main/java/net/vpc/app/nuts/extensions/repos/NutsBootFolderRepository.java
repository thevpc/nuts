/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.extensions.repos;

import java.io.File;
import net.vpc.app.nuts.NutsBootWorkspace;
import net.vpc.app.nuts.NutsConstants;
import net.vpc.app.nuts.NutsFile;
import net.vpc.app.nuts.NutsId;
import net.vpc.app.nuts.NutsWorkspace;

/**
 *
 * @author vpc
 */
public class NutsBootFolderRepository extends NutsFolderRepository {

    public NutsBootFolderRepository(NutsBootWorkspace bootWorkspace, NutsWorkspace workspace, File root) {
        super(NutsConstants.BOOTSTRAP_REPOSITORY_NAME, bootWorkspace.getBootstrapLocation(), workspace, null, root);
    }

    @Override
    protected File getStoreRoot() {
        return getConfigManager().getLocationFolder();
    }

    protected NutsFile getLocalNutDescriptorFile(NutsId id) {
        return new NutsFile(
                id, null,
                new File(getLocalVersionFolder(id), NutsConstants.NUTS_DESC_FILE_NAME), true, true, null
        );
    }

}
