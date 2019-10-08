/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.main.commands;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.NutsDefaultContent;
import net.vpc.app.nuts.runtime.DefaultNutsDefinition;
import net.vpc.app.nuts.runtime.DefaultNutsInstallInfo;
import net.vpc.app.nuts.main.wscommands.DefaultNutsExecCommand;
import net.vpc.app.nuts.runtime.log.NutsLogVerb;
import net.vpc.app.nuts.core.NutsWorkspaceExt;
import net.vpc.app.nuts.runtime.util.CoreNutsUtils;
import net.vpc.app.nuts.NutsLogger;
import net.vpc.app.nuts.runtime.util.io.URLBuilder;
import net.vpc.app.nuts.runtime.util.io.CoreIOUtils;
import static net.vpc.app.nuts.runtime.util.io.CoreIOUtils.createInputSource;
import static net.vpc.app.nuts.runtime.util.io.CoreIOUtils.resolveNutsDescriptorFromFileContent;
import static net.vpc.app.nuts.runtime.util.io.CoreIOUtils.toPathInputSource;
import net.vpc.app.nuts.runtime.util.io.InputSource;
import net.vpc.app.nuts.runtime.util.io.ZipOptions;
import net.vpc.app.nuts.runtime.util.io.ZipUtils;

/**
 *
 * @author vpc
 */
public class DefaultNutsArtifactPathExecutable extends AbstractNutsExecutableCommand {
    private final NutsLogger LOG;
    String cmdName;
    String[] args;
    String[] executorOptions;
    NutsExecutionType executionType;
    NutsSession session;
    DefaultNutsExecCommand execCommand;

    public DefaultNutsArtifactPathExecutable(String cmdName, String[] args, String[] executorOptions, NutsExecutionType executionType, NutsSession session, DefaultNutsExecCommand execCommand) {
        super(cmdName,
                session.getWorkspace().commandLine().create(args).toString(),
                NutsExecutableType.ARTIFACT);
        LOG=session.getWorkspace().log().of(DefaultNutsArtifactPathExecutable.class);
        this.cmdName = cmdName;
        this.args = args;
        this.executionType = executionType;
        this.session = session;
        this.execCommand = execCommand;
        List<String> executorOptionsList=new ArrayList<>();
        for (String option : executorOptions) {
            NutsArgument a = session.getWorkspace().commandLine().createArgument(option);
            if(a.getStringKey().equals("--nuts-auto-install")){
                if(a.isKeyValue()){
//                    autoInstall= a.isNegated() != a.getBooleanValue();
                }else{
//                    autoInstall=true;
                }
            }else{
                executorOptionsList.add(option);
            }
        }
        this.executorOptions=executorOptionsList.toArray(new String[0]);
    }

    @Override
    public NutsId getId() {
        NutsFetchCommand p = session.getWorkspace().fetch().transitive();
        try (final CharacterizedExecFile c = characterizeForExec(CoreIOUtils.createInputSource(cmdName), p, session)) {
            return c.descriptor == null ? null : c.descriptor.getId();
        }
    }

    @Override
    public void execute() {
        executeHelper(false);
    }

    @Override
    public void dryExecute() {
        executeHelper(true);
    }

    public void executeHelper(boolean dry) {
        NutsWorkspace ws = session.getWorkspace();
        NutsFetchCommand p = ws.fetch().transitive();
        try (final CharacterizedExecFile c = characterizeForExec(CoreIOUtils.createInputSource(cmdName), p, session)) {
            if (c.descriptor == null) {
                throw new NutsNotFoundException(ws, "", "Unable to resolve a valid descriptor for " + cmdName, null);
            }
            Path tempFolder = ws.io().createTempFolder("exec-path-");
            NutsId _id = c.descriptor.getId();
            NutsIdType idType=NutsWorkspaceExt.of(ws).resolveNutsIdType(_id);
            NutsDefinition nutToRun = new DefaultNutsDefinition(
                    null,
                    null,
                    _id,
                    c.descriptor,
                    new NutsDefaultContent(c.getContentPath(), false, c.temps.size() > 0),
                    new DefaultNutsInstallInfo(false,false,
                            tempFolder
                            ,null,ws.security().getCurrentUsername()
                            ),
                    idType, null
            );
            try {
                execCommand.ws_exec(nutToRun, cmdName, args, executorOptions, execCommand.getEnv(), execCommand.getDirectory(), execCommand.isFailFast(), true, session, executionType, dry);
            }finally {
                try {
                    CoreIOUtils.delete(ws,tempFolder);
                } catch (IOException e) {
                    LOG.log(Level.FINEST, NutsLogVerb.FAIL, "Unable to delete temp folder created for execution : "+tempFolder);
                }
            }
        }
    }

    private static CharacterizedExecFile characterizeForExec(InputSource contentFile, NutsFetchCommand options, NutsSession session) {
        NutsWorkspace ws=session.getWorkspace();
        String classifier=null;//TODO how to get classifier?
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
                    c.descriptor = ws.descriptor().parse(ext);
                } else {
                    c.descriptor = resolveNutsDescriptorFromFileContent(c.contentFile, options, session);
                }
                if (c.descriptor != null) {
                    if ("zip".equals(c.descriptor.getPackaging())) {
                        Path zipFilePath = Paths.get(ws.io().expandPath(fileSource.toString() + ".zip"));
                        ZipUtils.zip(session.getWorkspace(),fileSource.toString(), new ZipOptions(), zipFilePath.toString());
                        c.contentFile = createInputSource(zipFilePath).multi();
                        c.addTemp(zipFilePath);
                    } else {
                        throw new NutsIllegalArgumentException(ws, "Invalid Nut Folder source. expected 'zip' ext in descriptor");
                    }
                }
            } else if (Files.isRegularFile(fileSource)) {
                if (c.contentFile.getName().endsWith(NutsConstants.Files.DESCRIPTOR_FILE_NAME)) {
                    try (InputStream in = c.contentFile.open()) {
                        c.descriptor = ws.descriptor().parse(in);
                    }
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
                        for (NutsIdLocation location0 : c.descriptor.getLocations()) {
                            if(CoreNutsUtils.acceptClassifier(location0,classifier)) {
                                String location=location0.getUrl();
                                if (CoreIOUtils.isPathHttp(location)) {
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
                    }
                    if (c.contentFile == null) {
                        throw new NutsIllegalArgumentException(ws, "Unable to locale component for " + c.baseFile);
                    }
                } else {
                    c.descriptor = resolveNutsDescriptorFromFileContent(c.contentFile, options, session);
                    if (c.descriptor == null) {
                        c.descriptor = ws.descriptor().descriptorBuilder()
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
        return "NUTS " + cmdName + " " + session.getWorkspace().commandLine().create(args).toString();
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
