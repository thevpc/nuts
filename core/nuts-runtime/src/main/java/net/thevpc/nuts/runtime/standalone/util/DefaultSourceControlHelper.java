/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.util;

import net.thevpc.nuts.*;
import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.format.NDescriptorFormat;
import net.thevpc.nuts.io.NIOException;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.runtime.standalone.definition.DefaultNDefinitionBuilder;
import net.thevpc.nuts.runtime.standalone.io.util.UnzipOptions;
import net.thevpc.nuts.runtime.standalone.io.util.ZipUtils;
import net.thevpc.nuts.runtime.standalone.definition.DefaultNDefinition;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.log.NLogOp;
import net.thevpc.nuts.io.NIOUtils;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NUnused;
import net.thevpc.nuts.util.NStringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;

/**
 * @author thevpc
 */
@NUnused
public class DefaultSourceControlHelper {

    public DefaultSourceControlHelper(NWorkspace workspace) {
    }

    protected NLogOp _LOGOP() {
        return _LOG().with();
    }

    protected NLog _LOG() {
        return NLog.of(DefaultSourceControlHelper.class);
    }

    //    @Override
    public NId commit(Path folder) {
        NWorkspaceSecurityManager.of().checkAllowed(NConstants.Permissions.DEPLOY, "commit");
        if (folder == null || !Files.isDirectory(folder)) {
            throw new NIllegalArgumentException(NMsg.ofC("not a directory %s", folder));
        }

        Path file = folder.resolve(NConstants.Files.DESCRIPTOR_FILE_NAME);
        NDescriptor d = NDescriptorParser.of().parse(file).get();
        String oldVersion = NStringUtils.trim(d.getId().getVersion().getValue());
        if (oldVersion.endsWith(CoreNConstants.Versions.CHECKED_OUT_EXTENSION)) {
            oldVersion = oldVersion.substring(0, oldVersion.length() - CoreNConstants.Versions.CHECKED_OUT_EXTENSION.length());
            String newVersion = NVersion.get(oldVersion).get().inc().getValue();
            NDefinition newVersionFound = null;
            try {
                newVersionFound = NFetchCmd.of(d.getId().builder().setVersion(newVersion).build()).getResultDefinition();
            } catch (NNotFoundException ex) {
                _LOGOP().level(Level.FINE).error(ex)
                        .log(NMsg.ofC("failed to fetch %s", d.getId().builder().setVersion(newVersion).build()));
                //ignore
            }
            if (newVersionFound == null) {
                d = d.builder().setId(d.getId().builder().setVersion(newVersion).build()).build();
            } else {
                d = d.builder().setId(d.getId().builder().setVersion(oldVersion + ".1").build()).build();
            }
            NId newId = NDeployCmd.of().setContent(folder).setDescriptor(d).getResult().get(0);
            NDescriptorFormat.of(d).print(file);
            NIOUtils.delete(folder);
            return newId;
        } else {
            throw new NUnsupportedOperationException(NMsg.ofPlain("commit not supported"));
        }
    }

    //    @Override
    public NDefinition checkout(String id, Path folder) {
        return checkout(NId.get(id).get(), folder);
    }

    //    @Override
    public NDefinition checkout(NId id, Path folder) {
        NWorkspace workspace = NWorkspace.get().get();
        NWorkspaceSecurityManager.of().checkAllowed(NConstants.Permissions.INSTALL, "checkout");
        NDefinition nutToInstall = NFetchCmd.of(id).setOptional(false).setDependencies(true).getResultDefinition();
        if ("zip".equals(nutToInstall.getDescriptor().getPackaging())) {

            try {
                ZipUtils.unzip(nutToInstall.getContent().map(Object::toString).get(), NPath.of(folder)
                        .toAbsolute().toString(), new UnzipOptions().setSkipRoot(false));
            } catch (IOException ex) {
                throw new NIOException(ex);
            }

            Path file = folder.resolve(NConstants.Files.DESCRIPTOR_FILE_NAME);
            NDescriptor d = NDescriptorParser.of().parse(file).get();
            NVersion oldVersion = d.getId().getVersion();
            NId newId = d.getId().builder().setVersion(oldVersion + CoreNConstants.Versions.CHECKED_OUT_EXTENSION).build();
            d = d.builder().setId(newId).build();

            NDescriptorFormat.of(d).print(file);

            return new DefaultNDefinitionBuilder()
                    .setRepositoryUuid(nutToInstall.getRepositoryUuid())
                    .setRepositoryName(nutToInstall.getRepositoryName())
                    .setId(newId.getLongId())
                    .setDescriptor(d)
                    .setContent(NPath.of(folder).setUserCache(false).setUserTemporary(false))
                    .build()
            ;
        } else {
            throw new NUnsupportedOperationException(NMsg.ofPlain("checkout not supported"));
        }
    }
}
