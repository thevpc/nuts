/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.backup;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCommandLine;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.elem.NObjectElement;
import net.thevpc.nuts.io.NCompress;
import net.thevpc.nuts.io.NUncompressVisitor;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.io.NUncompress;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.AbstractNSettingsSubCommand;
import net.thevpc.nuts.util.NPlatformUtils;

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

    @Override
    public boolean exec(NCommandLine commandLine, Boolean autoSave, NSession session) {
        if (commandLine.next("backup").isPresent()) {
            commandLine.setCommandName("settings backup");
            String file = null;
            NArg a;
            while (commandLine.hasNext()) {
                if ((a = commandLine.nextEntry("--file", "-f").orNull()) != null) {
                    file = a.getValue().asString().orElse("");
                } else if (commandLine.peek().get(session).isNonOption()) {
                    file = commandLine.nextEntry().get(session).getValue().asString().orElse("");
                } else {
                    session.configureLast(commandLine);
                }
            }
            if (commandLine.isExecMode()) {
                List<String> all = new ArrayList<>();
                all.add(NLocations.of(session).getWorkspaceLocation().toFile()
                        .resolve("nuts-workspace.json").toString()
                );
                for (NStoreLocation value : NStoreLocation.values()) {
                    NPath r = NLocations.of(session).getStoreLocation(value);
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
                NCompress cmp = NCompress.of(session);
                for (String s : all) {
                    cmp.addSource(NPath.of(s,session));
                }
                cmp.to(file).run();
            }
            return true;
        } else if (commandLine.next("restore").isPresent()) {
            commandLine.setCommandName("settings restore");
            String file = null;
            String ws = null;
            NArg a;
            while (commandLine.hasNext()) {
                if ((a = commandLine.nextEntry("--file", "-f").orNull()) != null) {
                    file = a.getValue().asString().orElse("");
                } else if ((a = commandLine.nextEntry("--workspace", "-w").orNull()) != null) {
                    ws = a.getValue().asString().orElse("");
                } else if (commandLine.peek().get(session).isNonOption()) {
                    file = commandLine.nextEntry().get(session).getValue().asString().orElse("");
                } else {
                    session.configureLast(commandLine);
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
                commandLine.throwMissingArgument(NMsg.ofC("not a valid file : %s", file));
            }
            if (commandLine.isExecMode()) {
                NObjectElement[] nutsWorkspaceConfigRef = new NObjectElement[1];
                NElements elem = NElements.of(session);
                NUncompress.of(session)
                        .from(NPath.of(file,session))
                        .visit(new NUncompressVisitor() {
                            @Override
                            public boolean visitFolder(String path) {
                                return true;
                            }

                            @Override
                            public boolean visitFile(String path, InputStream inputStream) {
                                if ("/nuts-workspace.json".equals(path)) {
                                    NObjectElement e = elem.json()
                                            .parse(inputStream, NObjectElement.class).asObject().get(session);
                                    nutsWorkspaceConfigRef[0] = e;
                                    return false;
                                }
                                return true;
                            }
                        });
                if (nutsWorkspaceConfigRef[0] == null) {
                    commandLine.throwMissingArgument(NMsg.ofC("not a valid file : %s", file));
                }
                if (ws == null || ws.isEmpty()) {
                    NElements prv = elem.setSession(session);
                    ws = nutsWorkspaceConfigRef[0].getString("name").get(session);
                }
                if (ws == null || ws.isEmpty()) {
                    commandLine.throwMissingArgument(NMsg.ofC("not a valid file : %s", file));
                }
                String platformHomeFolder = NPlatformUtils.getWorkspaceLocation(null,
                        NConfigs.of(session).stored().isGlobal(), ws);
                NUncompress.of(session)
                        .from(NPath.of(file,session))
                        .to(NPath.of(platformHomeFolder,session))
                        .setSkipRoot(true).run();
                if (session.isPlainTrace()) {
                    session.out().println(NMsg.ofC("restore %s to %s", file, platformHomeFolder));
                }
            }
            return true;
        }
        return false;
    }

}
