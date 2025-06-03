/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.backup;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.elem.NElementParser;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.elem.NObjectElement;


import net.thevpc.nuts.NStoreType;
import net.thevpc.nuts.io.NCompress;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.io.NUncompress;
import net.thevpc.nuts.io.NUncompressVisitor;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.AbstractNSettingsSubCommand;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.NPlatformHome;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * @author thevpc
 */
public class NSettingsBackupSubCommand extends AbstractNSettingsSubCommand {
    public NSettingsBackupSubCommand(NWorkspace workspace) {
        super();
    }

    @Override
    public boolean exec(NCmdLine cmdLine, Boolean autoSave) {
        NSession session=NSession.of();
        if (cmdLine.next("backup").isPresent()) {
            cmdLine.setCommandName("settings backup");
            String file = null;
            NArg a;
            while (cmdLine.hasNext()) {
                if ((a = cmdLine.nextEntry("--file", "-f").orNull()) != null) {
                    file = a.getValue().asString().orElse("");
                } else if (cmdLine.peek().get().isNonOption()) {
                    file = cmdLine.nextEntry().get().getValue().asString().orElse("");
                } else {
                    session.configureLast(cmdLine);
                }
            }
            if (cmdLine.isExecMode()) {
                List<String> all = new ArrayList<>();
                all.add(NWorkspace.of().getWorkspaceLocation().toPath().get()
                        .resolve(NConstants.Files.WORKSPACE_CONFIG_FILE_NAME).toString()
                );
                for (NStoreType value : NStoreType.values()) {
                    NPath r = NWorkspace.of().getStoreLocation(value);
                    if (r.isDirectory()) {
                        all.add(r.toString());
                    }
                }
                if (file == null || file.isEmpty()) {
                    file = session.getWorkspace().getName() + ".zip";
                } else if (file.endsWith("/") || file.endsWith("\\")) {
                    file += session.getWorkspace().getName() + ".zip";
                } else if (Files.isDirectory(Paths.get(file))) {
                    file += File.separator + session.getWorkspace().getName() + ".zip";
                }
                if (Paths.get(file).getFileName().toString().indexOf('.') < 0) {
                    file += ".zip";
                }
                NCompress cmp = NCompress.of();
                for (String s : all) {
                    cmp.addSource(NPath.of(s));
                }
                cmp.to(file).run();
            }
            return true;
        } else if (cmdLine.next("restore").isPresent()) {
            cmdLine.setCommandName("settings restore");
            String file = null;
            String ws = null;
            NArg a;
            while (cmdLine.hasNext()) {
                if ((a = cmdLine.nextEntry("--file", "-f").orNull()) != null) {
                    file = a.getValue().asString().orElse("");
                } else if ((a = cmdLine.nextEntry("--workspace", "-w").orNull()) != null) {
                    ws = a.getValue().asString().orElse("");
                } else if (cmdLine.peek().get().isNonOption()) {
                    file = cmdLine.nextEntry().get().getValue().asString().orElse("");
                } else {
                    session.configureLast(cmdLine);
                }
            }
            if (file == null || file.isEmpty()) {
                file = session.getWorkspace().getName() + ".zip";
            } else if (file.endsWith("/") || file.endsWith("\\")) {
                file += session.getWorkspace().getName() + ".zip";
            } else if (Files.isDirectory(Paths.get(file))) {
                file += File.separator + session.getWorkspace().getName() + ".zip";
            }
            if (Paths.get(file).getFileName().toString().indexOf('.') < 0 && !Files.exists(Paths.get(file))) {
                file += ".zip";
            }
            if (!Files.isRegularFile(Paths.get(file))) {
                cmdLine.throwMissingArgument(NMsg.ofC("not a valid file : %s", file));
            }
            if (cmdLine.isExecMode()) {
                NObjectElement[] nutsWorkspaceConfigRef = new NObjectElement[1];
                NUncompress.of()
                        .from(NPath.of(file))
                        .visit(new NUncompressVisitor() {
                            @Override
                            public boolean visitFolder(String path) {
                                return true;
                            }

                            @Override
                            public boolean visitFile(String path, InputStream inputStream) {
                                if (("/"+NConstants.Files.WORKSPACE_CONFIG_FILE_NAME).equals(path)) {
                                    NObjectElement e = NElementParser.ofJson().parse(inputStream, NObjectElement.class).asObject().get();
                                    nutsWorkspaceConfigRef[0] = e;
                                    return false;
                                }
                                return true;
                            }
                        }).run();
                if (nutsWorkspaceConfigRef[0] == null) {
                    cmdLine.throwMissingArgument(NMsg.ofC("not a valid file : %s", file));
                }
                if (ws == null || ws.isEmpty()) {
                    ws = nutsWorkspaceConfigRef[0].getStringValue("name").get();
                }
                if (ws == null || ws.isEmpty()) {
                    cmdLine.throwMissingArgument(NMsg.ofC("not a valid file : %s", file));
                }
                String platformHomeFolder = NPlatformHome.of(null, NWorkspace.of().getStoredConfig().isSystem()).getWorkspaceLocation(ws);
                NUncompress.of()
                        .from(NPath.of(file))
                        .to(NPath.of(platformHomeFolder))
                        .setSkipRoot(true).run();
                if (session.isPlainTrace()) {
                    NOut.println(NMsg.ofC("restore %s to %s", file, platformHomeFolder));
                }
            }
            return true;
        }
        return false;
    }

}
