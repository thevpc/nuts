/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.backup;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.AbstractNutsSettingsSubCommand;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * @author thevpc
 */
public class NutsSettingsBackupSubCommand extends AbstractNutsSettingsSubCommand {

    @Override
    public boolean exec(NutsCommandLine commandLine, Boolean autoSave, NutsSession session) {
        if (commandLine.next("backup") != null) {
            commandLine.setCommandName("settings backup");
            String file = null;
            NutsArgument a;
            while (commandLine.hasNext()) {
                if ((a = commandLine.nextString("--file", "-f")) != null) {
                    file = a.getValue().getString("");
                } else if (commandLine.peek().isNonOption()) {
                    file = commandLine.nextString().getValue().getString("");
                } else {
                    session.configureLast(commandLine);
                }
            }
            if (commandLine.isExecMode()) {
                List<String> all = new ArrayList<>();
                all.add(session.locations().getWorkspaceLocation().toFile()
                        .resolve("nuts-workspace.json").toString()
                );
                for (NutsStoreLocation value : NutsStoreLocation.values()) {
                    NutsPath r = session.locations().getStoreLocation(value);
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
                NutsCompress cmp = NutsCompress.of(session);
                for (String s : all) {
                    cmp.addSource(s);
                }
                cmp.to(file).run();
            }
            return true;
        } else if (commandLine.next("restore") != null) {
            commandLine.setCommandName("settings restore");
            String file = null;
            String ws = null;
            NutsArgument a;
            while (commandLine.hasNext()) {
                if ((a = commandLine.nextString("--file", "-f")) != null) {
                    file = a.getValue().getString("");
                } else if ((a = commandLine.nextString("--workspace", "-w")) != null) {
                    ws = a.getValue().getString("");
                } else if (commandLine.peek().isNonOption()) {
                    file = commandLine.nextString().getValue().getString("");
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
                commandLine.required(NutsMessage.cstyle("not a valid file : %s", file));
            }
            if (commandLine.isExecMode()) {
                NutsObjectElement[] nutsWorkspaceConfigRef = new NutsObjectElement[1];
                NutsElements elem = NutsElements.of(session);
                NutsUncompress.of(session)
                        .from(file)
                        .visit(new NutsIOUncompressVisitor() {
                            @Override
                            public boolean visitFolder(String path) {
                                return true;
                            }

                            @Override
                            public boolean visitFile(String path, InputStream inputStream) {
                                if ("/nuts-workspace.json".equals(path)) {
                                    NutsObjectElement e = elem.json()
                                            .parse(inputStream, NutsObjectElement.class).asObject();
                                    nutsWorkspaceConfigRef[0] = e;
                                    return false;
                                }
                                return true;
                            }
                        });
                if (nutsWorkspaceConfigRef[0] == null) {
                    commandLine.required(NutsMessage.cstyle("not a valid file : %s", file));
                }
                if (ws == null || ws.isEmpty()) {
                    NutsElements prv = elem.setSession(session);
                    ws = nutsWorkspaceConfigRef[0].get(prv.ofString("name")).asString();
                }
                if (ws == null || ws.isEmpty()) {
                    commandLine.required(NutsMessage.cstyle("not a valid file : %s", file));
                }
                String platformHomeFolder = NutsUtilPlatforms.getWorkspaceLocation(null,
                        session.config().stored().isGlobal(), ws);
                NutsUncompress.of(session)
                        .from(file)
                        .to(platformHomeFolder)
                        .setSkipRoot(true).run();
                if (session.isPlainTrace()) {
                    session.out().printf("restore %s to %s %n", file, platformHomeFolder);
                }
            }
            return true;
        }
        return false;
    }

}
