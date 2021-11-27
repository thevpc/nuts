/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.util;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.io.util.CoreIOUtils;
import net.thevpc.nuts.runtime.standalone.io.util.UnzipOptions;
import net.thevpc.nuts.runtime.standalone.io.util.ZipUtils;
import net.thevpc.nuts.runtime.standalone.definition.DefaultNutsDefinition;
import net.thevpc.nuts.runtime.standalone.workspace.NutsWorkspaceUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;

/**
 * @author thevpc
 */
public class DefaultSourceControlHelper {

    private NutsLogger LOG;
    private NutsWorkspace ws;

    public DefaultSourceControlHelper(NutsWorkspace ws) {
        this.ws = ws;
    }

    protected NutsLoggerOp _LOGOP(NutsSession session) {
        return _LOG(session).with().session(session);
    }

    protected NutsLogger _LOG(NutsSession session) {
        if (LOG == null) {
            LOG = NutsLogger.of(DefaultSourceControlHelper.class,session);
        }
        return LOG;
    }

    //    @Override
    public NutsId commit(Path folder, NutsSession session) {
        NutsWorkspaceUtils.checkSession(ws, session);
        session.security().setSession(session).checkAllowed(NutsConstants.Permissions.DEPLOY, "commit");
        if (folder == null || !Files.isDirectory(folder)) {
            throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("not a directory %s", folder));
        }

        Path file = folder.resolve(NutsConstants.Files.DESCRIPTOR_FILE_NAME);
        NutsDescriptor d = NutsDescriptorParser.of(session).parse(file);
        String oldVersion = NutsUtilStrings.trim(d.getId().getVersion().getValue());
        if (oldVersion.endsWith(CoreNutsConstants.Versions.CHECKED_OUT_EXTENSION)) {
            oldVersion = oldVersion.substring(0, oldVersion.length() - CoreNutsConstants.Versions.CHECKED_OUT_EXTENSION.length());
            String newVersion = NutsVersion.of(oldVersion,session).inc().getValue();
            NutsDefinition newVersionFound = null;
            try {
                newVersionFound = session.fetch().setId(d.getId().builder().setVersion(newVersion).build()).setSession(session).getResultDefinition();
            } catch (NutsNotFoundException ex) {
                _LOGOP(session).level(Level.FINE).error(ex)
                        .log(NutsMessage.jstyle("failed to fetch {0}", d.getId().builder().setVersion(newVersion).build()));
                //ignore
            }
            if (newVersionFound == null) {
                d = d.builder().setId(d.getId().builder().setVersion(newVersion).build()).build();
            } else {
                d = d.builder().setId(d.getId().builder().setVersion(oldVersion + ".1").build()).build();
            }
            NutsId newId = session.deploy().setContent(folder).setDescriptor(d).setSession(session).getResult()[0];
            d.formatter().setSession(session).print(file);
            CoreIOUtils.delete(session, folder);
            return newId;
        } else {
            throw new NutsUnsupportedOperationException(session, NutsMessage.cstyle("commit not supported"));
        }
    }

    //    @Override
    public NutsDefinition checkout(String id, Path folder, NutsSession session) {
        return checkout(NutsId.of(id,session), folder, session);
    }

    //    @Override
    public NutsDefinition checkout(NutsId id, Path folder, NutsSession session) {
        NutsWorkspaceUtils.checkSession(ws, session);
        session.security().checkAllowed(NutsConstants.Permissions.INSTALL, "checkout");
        NutsDefinition nutToInstall = session.fetch().setId(id).setSession(session).setOptional(false).setDependencies(true).getResultDefinition();
        if ("zip".equals(nutToInstall.getDescriptor().getPackaging())) {

            try {
                ZipUtils.unzip(session, nutToInstall.getFile().toString(), NutsPath.of(folder,session)
                        .toAbsolute().toString(), new UnzipOptions().setSkipRoot(false));
            } catch (IOException ex) {
                throw new NutsIOException(session, ex);
            }

            Path file = folder.resolve(NutsConstants.Files.DESCRIPTOR_FILE_NAME);
            NutsDescriptor d = NutsDescriptorParser.of(session).parse(file);
            NutsVersion oldVersion = d.getId().getVersion();
            NutsId newId = d.getId().builder().setVersion(oldVersion + CoreNutsConstants.Versions.CHECKED_OUT_EXTENSION).build();
            d = d.builder().setId(newId).build();

            d.formatter().setSession(session).print(file);

            return new DefaultNutsDefinition(
                    nutToInstall.getRepositoryUuid(),
                    nutToInstall.getRepositoryName(),
                    newId.getLongId(),
                    d,
                    new NutsDefaultContent(
                            NutsPath.of(folder,session),
                            false,
                            false),
                    null, null, session
            );
        } else {
            throw new NutsUnsupportedOperationException(session, NutsMessage.cstyle("checkout not supported"));
        }
    }
}
