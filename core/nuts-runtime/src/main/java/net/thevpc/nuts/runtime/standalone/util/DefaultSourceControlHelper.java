/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.util;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NIOException;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.runtime.standalone.io.util.CoreIOUtils;
import net.thevpc.nuts.runtime.standalone.io.util.UnzipOptions;
import net.thevpc.nuts.runtime.standalone.io.util.ZipUtils;
import net.thevpc.nuts.runtime.standalone.definition.DefaultNDefinition;
import net.thevpc.nuts.runtime.standalone.session.NSessionUtils;
import net.thevpc.nuts.util.NLog;
import net.thevpc.nuts.util.NLogOp;
import net.thevpc.nuts.util.NStringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;

/**
 * @author thevpc
 */
public class DefaultSourceControlHelper {

    private NLog LOG;
    private NWorkspace ws;

    public DefaultSourceControlHelper(NWorkspace ws) {
        this.ws = ws;
    }

    protected NLogOp _LOGOP(NSession session) {
        return _LOG(session).with().session(session);
    }

    protected NLog _LOG(NSession session) {
        if (LOG == null) {
            LOG = NLog.of(DefaultSourceControlHelper.class, session);
        }
        return LOG;
    }

    //    @Override
    public NId commit(Path folder, NSession session) {
        NSessionUtils.checkSession(ws, session);
        NWorkspaceSecurityManager.of(session).checkAllowed(NConstants.Permissions.DEPLOY, "commit");
        if (folder == null || !Files.isDirectory(folder)) {
            throw new NIllegalArgumentException(session, NMsg.ofC("not a directory %s", folder));
        }

        Path file = folder.resolve(NConstants.Files.DESCRIPTOR_FILE_NAME);
        NDescriptor d = NDescriptorParser.of(session).parse(file).get(session);
        String oldVersion = NStringUtils.trim(d.getId().getVersion().getValue());
        if (oldVersion.endsWith(CoreNConstants.Versions.CHECKED_OUT_EXTENSION)) {
            oldVersion = oldVersion.substring(0, oldVersion.length() - CoreNConstants.Versions.CHECKED_OUT_EXTENSION.length());
            String newVersion = NVersion.of(oldVersion).get(session).inc().getValue();
            NDefinition newVersionFound = null;
            try {
                newVersionFound = NFetchCommand.of(d.getId().builder().setVersion(newVersion).build(), session).getResultDefinition();
            } catch (NNotFoundException ex) {
                _LOGOP(session).level(Level.FINE).error(ex)
                        .log(NMsg.ofJ("failed to fetch {0}", d.getId().builder().setVersion(newVersion).build()));
                //ignore
            }
            if (newVersionFound == null) {
                d = d.builder().setId(d.getId().builder().setVersion(newVersion).build()).build();
            } else {
                d = d.builder().setId(d.getId().builder().setVersion(oldVersion + ".1").build()).build();
            }
            NId newId = NDeployCommand.of(session).setContent(folder).setDescriptor(d).setSession(session).getResult().get(0);
            d.formatter(session).print(file);
            CoreIOUtils.delete(session, folder);
            return newId;
        } else {
            throw new NUnsupportedOperationException(session, NMsg.ofPlain("commit not supported"));
        }
    }

    //    @Override
    public NDefinition checkout(String id, Path folder, NSession session) {
        return checkout(NId.of(id).get(session), folder, session);
    }

    //    @Override
    public NDefinition checkout(NId id, Path folder, NSession session) {
        NSessionUtils.checkSession(ws, session);
        NWorkspaceSecurityManager.of(session).checkAllowed(NConstants.Permissions.INSTALL, "checkout");
        NDefinition nutToInstall = NFetchCommand.of(id, session).setOptional(false).setDependencies(true).getResultDefinition();
        if ("zip".equals(nutToInstall.getDescriptor().getPackaging())) {

            try {
                ZipUtils.unzip(session, nutToInstall.getContent().map(Object::toString).get(session), NPath.of(folder, session)
                        .toAbsolute().toString(), new UnzipOptions().setSkipRoot(false));
            } catch (IOException ex) {
                throw new NIOException(session, ex);
            }

            Path file = folder.resolve(NConstants.Files.DESCRIPTOR_FILE_NAME);
            NDescriptor d = NDescriptorParser.of(session).parse(file).get(session);
            NVersion oldVersion = d.getId().getVersion();
            NId newId = d.getId().builder().setVersion(oldVersion + CoreNConstants.Versions.CHECKED_OUT_EXTENSION).build();
            d = d.builder().setId(newId).build();

            d.formatter(session).print(file);

            return new DefaultNDefinition(
                    nutToInstall.getRepositoryUuid(),
                    nutToInstall.getRepositoryName(),
                    newId.getLongId(),
                    d, NPath.of(folder, session).setUserCache(false).setUserTemporary(false),
                    null, null, session
            );
        } else {
            throw new NUnsupportedOperationException(session, NMsg.ofPlain("checkout not supported"));
        }
    }
}
