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
import java.util.logging.Level;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.NutsLogger;
import net.vpc.app.nuts.core.spi.NutsWorkspaceExt;
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
    private final NutsLogger LOG;
    private NutsWorkspace ws;

    public DefaultSourceControlHelper(NutsWorkspace ws) {
        this.ws = ws;
        LOG=ws.log().of(DefaultSourceControlHelper.class);
    }

//    @Override
    public NutsId commit(Path folder, NutsSession session) {
        session = NutsWorkspaceUtils.of(ws).validateSession( session);
        ws.security().checkAllowed(NutsConstants.Permissions.DEPLOY, "commit");
        if (folder == null || !Files.isDirectory(folder)) {
            throw new NutsIllegalArgumentException(ws, "Not a directory " + folder);
        }

        Path file = folder.resolve(NutsConstants.Files.DESCRIPTOR_FILE_NAME);
        NutsDescriptor d = ws.descriptor().parse(file);
        String oldVersion = CoreStringUtils.trim(d.getId().getVersion().getValue());
        if (oldVersion.endsWith(CoreNutsConstants.Versions.CHECKED_OUT_EXTENSION)) {
            oldVersion = oldVersion.substring(0, oldVersion.length() - CoreNutsConstants.Versions.CHECKED_OUT_EXTENSION.length());
            String newVersion = ws.version().parse(oldVersion).inc().getValue();
            NutsDefinition newVersionFound = null;
            try {
                newVersionFound = ws.fetch().id(d.getId().builder().setVersion(newVersion).build()).setSession(session).getResultDefinition();
            } catch (NutsNotFoundException ex) {
                LOG.log(Level.FINE, "Failed to fetch " + d.getId().builder().setVersion(newVersion).build(),ex);
                //ignore
            }
            if (newVersionFound == null) {
                d = d.builder().setId(d.getId().builder().setVersion(newVersion).build()).build();
            } else {
                d = d.builder().setId(d.getId().builder().setVersion(oldVersion + ".1").build()).build();
            }
            NutsId newId = ws.deploy().setContent(folder).setDescriptor(d).setSession(session).getResult()[0];
            ws.descriptor().value(d).print(file);
            try {
                CoreIOUtils.delete(ws,folder);
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
            return newId;
        } else {
            throw new NutsUnsupportedOperationException(ws, "commit not supported");
        }
    }

//    @Override
    public NutsDefinition checkout(String id, Path folder, NutsSession session) {
        return checkout(ws.id().parseRequired(id), folder, session);
    }

//    @Override
    public NutsDefinition checkout(NutsId id, Path folder, NutsSession session) {
        session = NutsWorkspaceUtils.of(ws).validateSession( session);
        ws.security().checkAllowed(NutsConstants.Permissions.INSTALL, "checkout");
        NutsDefinition nutToInstall = ws.fetch().id(id).setSession(session).setOptional(false).dependencies().getResultDefinition();
        if ("zip".equals(nutToInstall.getDescriptor().getPackaging())) {

            try {
                ZipUtils.unzip(ws,nutToInstall.getPath().toString(), ws.io().expandPath(folder), new UnzipOptions().setSkipRoot(false));
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }

            Path file = folder.resolve(NutsConstants.Files.DESCRIPTOR_FILE_NAME);
            NutsDescriptor d = ws.descriptor().parse(file);
            NutsVersion oldVersion = d.getId().getVersion();
            NutsId newId = d.getId().builder().setVersion(oldVersion + CoreNutsConstants.Versions.CHECKED_OUT_EXTENSION).build();
            d = d.builder().setId(newId).build();

            ws.descriptor().value(d).print(file);

            NutsIdType idType= NutsWorkspaceExt.of(ws).resolveNutsIdType(newId);
            return new DefaultNutsDefinition(
                    nutToInstall.getRepositoryUuid(),
                    nutToInstall.getRepositoryName(),
                    newId,
                    d,
                    new NutsDefaultContent(folder,
                            false,
                            false),
                    null,
                    idType, null
            );
        } else {
            throw new NutsUnsupportedOperationException(ws, "Checkout not supported");
        }
    }
}
