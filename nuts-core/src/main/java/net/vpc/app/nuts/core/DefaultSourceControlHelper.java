/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import net.vpc.app.nuts.NutsConstants;
import net.vpc.app.nuts.NutsDefinition;
import net.vpc.app.nuts.NutsDescriptor;
import net.vpc.app.nuts.NutsId;
import net.vpc.app.nuts.NutsIllegalArgumentException;
import net.vpc.app.nuts.NutsNotFoundException;
import net.vpc.app.nuts.NutsSession;
import net.vpc.app.nuts.NutsUnsupportedOperationException;
import net.vpc.app.nuts.NutsVersion;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.core.util.io.CoreIOUtils;
import net.vpc.app.nuts.core.util.common.CoreStringUtils;
import net.vpc.app.nuts.core.util.NutsWorkspaceUtils;
import net.vpc.app.nuts.core.util.io.UnzipOptions;
import net.vpc.app.nuts.core.util.io.ZipUtils;

/**
 *
 * @author vpc
 */
public class DefaultSourceControlHelper {
    private NutsWorkspace ws;

    public DefaultSourceControlHelper(NutsWorkspace ws) {
        this.ws = ws;
    }
    
//    @Override
    public NutsId commit(Path folder, NutsSession session) {
        session = NutsWorkspaceUtils.validateSession(ws, session);
        ws.security().checkAllowed(NutsConstants.Rights.DEPLOY, "commit");
        if (folder == null || !Files.isDirectory(folder)) {
            throw new NutsIllegalArgumentException(ws, "Not a directory " + folder);
        }

        Path file = folder.resolve(NutsConstants.Files.DESCRIPTOR_FILE_NAME);
        NutsDescriptor d = ws.format().descriptor().read(file);
        String oldVersion = CoreStringUtils.trim(d.getId().getVersion().getValue());
        if (oldVersion.endsWith(NutsConstants.Versions.CHECKED_OUT_EXTENSION)) {
            oldVersion = oldVersion.substring(0, oldVersion.length() - NutsConstants.Versions.CHECKED_OUT_EXTENSION.length());
            String newVersion = ws.format().version().parseVersion(oldVersion).inc().getValue();
            NutsDefinition newVersionFound = null;
            try {
                newVersionFound = ws.fetch().id(d.getId().setVersion(newVersion)).setSession(session).getResultDefinition();
            } catch (NutsNotFoundException ex) {
                //ignore
            }
            if (newVersionFound == null) {
                d = d.setId(d.getId().setVersion(newVersion));
            } else {
                d = d.setId(d.getId().setVersion(oldVersion + ".1"));
            }
            NutsId newId = ws.deploy().setContent(folder).setDescriptor(d).setSession(session).getResult()[0];
            ws.format().descriptor().set(d).print(file);
            try {
                CoreIOUtils.delete(folder);
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
            return newId;
        } else {
            throw new NutsUnsupportedOperationException(ws,"commit not supported");
        }
    }

//    @Override
    public NutsDefinition checkout(String id, Path folder, NutsSession session) {
        return checkout(ws.format().id().parseRequired(id), folder, session);
    }

//    @Override
    public NutsDefinition checkout(NutsId id, Path folder, NutsSession session) {
        session = NutsWorkspaceUtils.validateSession(ws, session);
        ws.security().checkAllowed(NutsConstants.Rights.INSTALL, "checkout");
        NutsDefinition nutToInstall = ws.fetch().id(id).setSession(session).setOptional(false).dependencies().getResultDefinition();
        if ("zip".equals(nutToInstall.getDescriptor().getPackaging())) {

            try {
                ZipUtils.unzip(nutToInstall.getPath().toString(), ws.io().expandPath(folder), new UnzipOptions().setSkipRoot(false));
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }

            Path file = folder.resolve(NutsConstants.Files.DESCRIPTOR_FILE_NAME);
            NutsDescriptor d = ws.format().descriptor().read(file);
            NutsVersion oldVersion = d.getId().getVersion();
            NutsId newId = d.getId().setVersion(oldVersion + NutsConstants.Versions.CHECKED_OUT_EXTENSION);
            d = d.setId(newId);

            ws.format().descriptor().set(d).print(file);

            return new DefaultNutsDefinition(
                    nutToInstall.getRepositoryUuid(),
                    nutToInstall.getRepositoryName(),
                    newId,
                    d,
                    new DefaultNutsContent(folder,
                            false,
                            false),
                    null
            );
        } else {
            throw new NutsUnsupportedOperationException(ws,"Checkout not supported");
        }
    }
}
