/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.commands;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.DefaultNutsContent;
import net.vpc.app.nuts.core.DefaultNutsDefinition;
import net.vpc.app.nuts.core.DefaultNutsExecCommand;
import net.vpc.app.nuts.core.util.NutsWorkspaceUtils;
import net.vpc.app.nuts.core.util.io.URLBuilder;
import net.vpc.app.nuts.core.util.io.CoreIOUtils;
import static net.vpc.app.nuts.core.util.io.CoreIOUtils.createInputSource;
import static net.vpc.app.nuts.core.util.io.CoreIOUtils.resolveNutsDescriptorFromFileContent;
import static net.vpc.app.nuts.core.util.io.CoreIOUtils.toPathInputSource;
import net.vpc.app.nuts.core.util.io.InputSource;
import net.vpc.app.nuts.core.util.io.ZipOptions;
import net.vpc.app.nuts.core.util.io.ZipUtils;

/**
 *
 * @author vpc
 */
public class DefaultNutsPathComponentExecutable extends AbstractNutsExecutableCommand {

    String cmdName;
    String[] args;
    String[] executorOptions;
    NutsExecutionType executionType;
    NutsWorkspace ws;
    NutsSession session;
    DefaultNutsExecCommand execCommand;

    public DefaultNutsPathComponentExecutable(String cmdName, String[] args, String[] executorOptions, NutsExecutionType executionType, NutsWorkspace ws, NutsSession session, DefaultNutsExecCommand execCommand) {
        super(cmdName,
                ws.parse().command(args).toString(),
                NutsExecutableType.COMPONENT);
        this.cmdName = cmdName;
        this.args = args;
        this.executorOptions = executorOptions;
        this.executionType = executionType;
        this.ws = ws;
        this.session = session;
        this.execCommand = execCommand;
    }

    @Override
    public NutsId getId() {
        NutsFetchCommand p = ws.fetch();
        p.setTransitive(true);
        try (final CharacterizedExecFile c = characterizeForExec(ws, CoreIOUtils.createInputSource(cmdName), p, session)) {
            return c.descriptor == null ? null : c.descriptor.getId();
        }
    }

    @Override
    public void execute() {
        NutsFetchCommand p = ws.fetch();
        p.setTransitive(true);
        try (final CharacterizedExecFile c = characterizeForExec(ws, CoreIOUtils.createInputSource(cmdName), p, session)) {
            if (c.descriptor == null) {
                //this is a native file?
                c.descriptor = DefaultNutsExecCommand.TEMP_DESC;
            }
            NutsDefinition nutToRun = new DefaultNutsDefinition(
                    null, 
                    null, 
                    c.descriptor.getId(), 
                    c.descriptor, 
                    new DefaultNutsContent(c.getContentPath(), false, c.temps.size() > 0), 
                    null
            );
            execCommand.ws_exec(nutToRun, cmdName, args, executorOptions, execCommand.getEnv(), execCommand.getDirectory(), execCommand.isFailFast(),true, session, executionType);
        }
    }

    private static CharacterizedExecFile characterizeForExec(NutsWorkspace ws, InputSource contentFile, NutsFetchCommand options, NutsSession session) {
        session = NutsWorkspaceUtils.validateSession(ws, session);
        CharacterizedExecFile c = new CharacterizedExecFile();
        try {
            c.baseFile = contentFile;
            c.contentFile = toPathInputSource(contentFile, c.temps, ws);
            Path fileSource = c.contentFile.getPath();
            if (!Files.exists(fileSource)) {
                throw new NutsIllegalArgumentException(ws, "File does not exists " + fileSource);
            }
            if (Files.isDirectory(fileSource)) {
                Path ext = fileSource.resolve(NutsConstants.Files.DESCRIPTOR_FILE_NAME);
                if (Files.exists(ext)) {
                    c.descriptor = ws.parse().descriptor(ext);
                } else {
                    c.descriptor = resolveNutsDescriptorFromFileContent(ws, c.contentFile, options, session);
                }
                if (c.descriptor != null) {
                    if ("zip".equals(c.descriptor.getPackaging())) {
                        Path zipFilePath = ws.io().path(ws.io().expandPath(fileSource.toString() + ".zip"));
                        ZipUtils.zip(fileSource.toString(), new ZipOptions(), zipFilePath.toString());
                        c.contentFile = createInputSource(zipFilePath).multi();
                        c.addTemp(zipFilePath);
                    } else {
                        throw new NutsIllegalArgumentException(ws, "Invalid Nut Folder source. expected 'zip' ext in descriptor");
                    }
                }
            } else if (Files.isRegularFile(fileSource)) {
                if (c.contentFile.getName().endsWith(NutsConstants.Files.DESCRIPTOR_FILE_NAME)) {
                    c.descriptor = ws.parse().descriptor(c.contentFile.open());
                    c.contentFile = null;
                    if (c.baseFile.isURL()) {
                        URLBuilder ub = new URLBuilder(c.baseFile.getURL().toString());
                        try {
                            c.contentFile = toPathInputSource(
                                    createInputSource(ub.resolveSibling(ws.config().getDefaultIdFilename(c.descriptor.getId())).toURL()),
                                    c.temps, ws);
                        } catch (Exception ex) {

                        }
                    }
                    if (c.contentFile == null) {
                        for (String location : c.descriptor.getLocations()) {
                            if (location.startsWith("http://") || location.startsWith("https://")) {
                                try {
                                    c.contentFile = toPathInputSource(
                                            createInputSource(new URL(location)),
                                            c.temps, ws);
                                } catch (Exception ex) {

                                }
                            } else {
                                URLBuilder ub = new URLBuilder(c.baseFile.getURL().toString());
                                try {
                                    c.contentFile = toPathInputSource(
                                            createInputSource(ub.resolveSibling(ws.config().getDefaultIdFilename(c.descriptor.getId())).toURL()),
                                            c.temps, ws);
                                } catch (Exception ex) {

                                }
                            }
                            if (c.contentFile == null) {
                                break;
                            }
                        }
                    }
                    if (c.contentFile == null) {
                        throw new NutsIllegalArgumentException(ws, "Unabel to locale component for " + c.baseFile);
                    }
                } else {
                    c.descriptor = resolveNutsDescriptorFromFileContent(ws, c.contentFile, options, session);
                    if(c.descriptor==null){
                        c.descriptor=ws.descriptorBuilder()
                                .setId("temp")
                                .setPackaging(CoreIOUtils.getFileExtension(contentFile.getName()))
                                .build();
                    }
                }
            } else {
                throw new NutsIllegalArgumentException(ws, "Path does not denote a valid file or folder " + c.baseFile);
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
        return c;
    }

    @Override
    public String toString() {
        return "NUTS " + cmdName + " " + ws.parse().command(args).toString();
    }

    private static class CharacterizedExecFile implements AutoCloseable {

        public InputSource contentFile;
        public InputSource baseFile;
        public List<Path> temps = new ArrayList<>();
        public NutsDescriptor descriptor;
        public NutsId executor;

        public Path getContentPath() {
            return (Path) contentFile.getSource();
        }

        public void addTemp(Path f) {
            temps.add(f);
        }

        @Override
        public void close() {
            for (Iterator<Path> it = temps.iterator(); it.hasNext();) {
                Path temp = it.next();
                try {
                    Files.delete(temp);
                } catch (IOException ex) {
                    throw new UncheckedIOException(ex);
                }
                it.remove();
            }
        }
    }

}
