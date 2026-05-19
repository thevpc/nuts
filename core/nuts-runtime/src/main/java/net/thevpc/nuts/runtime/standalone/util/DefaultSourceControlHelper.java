/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.util;

import net.thevpc.nuts.core.NConstants;
import net.thevpc.nuts.artifact.*;
import net.thevpc.nuts.command.NDeploy;
import net.thevpc.nuts.command.NFetch;
import net.thevpc.nuts.security.NSecurityManager;
import net.thevpc.nuts.text.NDescriptorWriter;
import net.thevpc.nuts.io.NIOException;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.runtime.standalone.definition.DefaultNDefinitionBuilder;
import net.thevpc.nuts.runtime.standalone.io.util.UnzipOptions;
import net.thevpc.nuts.runtime.standalone.io.util.ZipUtils;
import net.thevpc.nuts.log.NLog;

import net.thevpc.nuts.io.NIOUtils;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author thevpc
 */
@NUnused
public class DefaultSourceControlHelper {

    public DefaultSourceControlHelper() {
    }

    protected NLog _LOG() {
        return NLog.of(DefaultSourceControlHelper.class);
    }

    //    @Override
    public NId commit(Path folder) {
        NSecurityManager.of().checkAllowed(NConstants.Permissions.DEPLOY, "commit");
        if (folder == null || !Files.isDirectory(folder)) {
            throw new NIllegalArgumentException(NMsg.ofC("not a directory %s", folder));
        }

        Path file = folder.resolve(NConstants.Files.DESCRIPTOR_FILE_NAME);
        NDescriptor d = NDescriptorParser.of().parse(file).get();
        String oldVersion = NStringUtils.trim(d.id().version().value());
        if (oldVersion.endsWith(CoreNConstants.Versions.CHECKED_OUT_EXTENSION)) {
            oldVersion = oldVersion.substring(0, oldVersion.length() - CoreNConstants.Versions.CHECKED_OUT_EXTENSION.length());
            String newVersion = NVersion.get(oldVersion).get().inc().value();
            NDefinition newVersionFound = null;
            try {
                newVersionFound = NFetch.of(d.id().builder().version(newVersion).build())
                        .dependencyFilter(NDependencyFilters.of().byRunnable())
                        .getResultDefinition();
            } catch (NArtifactNotFoundException ex) {
                _LOG()
                        .log(NMsg.ofC("failed to fetch %s", d.id().builder().version(newVersion).build()).asFine(ex));
                //ignore
            }
            if (newVersionFound == null) {
                d = d.builder().id(d.id().builder().version(newVersion).build()).build();
            } else {
                d = d.builder().id(d.id().builder().version(oldVersion + ".1").build()).build();
            }
            NId newId = NDeploy.of().content(folder).descriptor(d).getResult().get(0);
            NDescriptorWriter.of().print(d, file);
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
        NSecurityManager.of().checkAllowed(NConstants.Permissions.INSTALL, "checkout");
        NDefinition nutToInstall = NFetch.of(id)
                .dependencyFilter(NDependencyFilters.of().byRunnable())
                .getResultDefinition();
        if ("zip".equals(nutToInstall.descriptor().packaging())) {
            try {
                ZipUtils.unzip(nutToInstall.content().map(Object::toString).get(), NPath.of(folder)
                        .toAbsolute().toString(), new UnzipOptions().setSkipRoot(false));
            } catch (IOException ex) {
                throw new NIOException(ex);
            }

            Path file = folder.resolve(NConstants.Files.DESCRIPTOR_FILE_NAME);
            NDescriptor d = NDescriptorParser.of().parse(file).get();
            NVersion oldVersion = d.id().version();
            NId newId = d.id().builder().version(oldVersion + CoreNConstants.Versions.CHECKED_OUT_EXTENSION).build();
            d = d.builder().id(newId).build();

            NDescriptorWriter.of().print(d, file);

            return new DefaultNDefinitionBuilder()
                    .repositoryUuid(nutToInstall.repositoryUuid())
                    .repositoryName(nutToInstall.repositoryName())
                    .id(newId.longId())
                    .descriptor(d)
                    .content(NPath.of(folder).userCache(false).userTemporary(false))
                    .dependency(id.toDependency())
                    .build()
            ;
        } else {
            throw new NUnsupportedOperationException(NMsg.ofPlain("checkout not supported"));
        }
    }
}
