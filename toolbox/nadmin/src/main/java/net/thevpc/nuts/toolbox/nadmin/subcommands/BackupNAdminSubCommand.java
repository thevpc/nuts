/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.nadmin.subcommands;

import net.thevpc.common.io.PathFilter;
import net.thevpc.common.io.UnzipOptions;
import net.thevpc.common.io.ZipOptions;
import net.thevpc.common.io.ZipUtils;
import net.thevpc.nuts.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * @author thevpc
 */
public class BackupNAdminSubCommand extends AbstractNAdminSubCommand {

    @Override
    public boolean exec(NutsCommandLine commandLine, Boolean autoSave, NutsApplicationContext context) {
        if (commandLine.next("backup") != null) {
            commandLine.setCommandName("nadmin backup");
            String file = null;
            NutsArgument a;
            while (commandLine.hasNext()) {
                if ((a = commandLine.nextString("--file", "-f")) != null) {
                    file = a.getStringValue("");
                } else if (commandLine.peek().isNonOption()) {
                    file = commandLine.nextString().getStringValue("");
                } else {
                    context.configureLast(commandLine);
                }
            }
            if (commandLine.isExecMode()) {
                List<ZipUtils.Folder> all = new ArrayList<>();
                all.add(new ZipUtils.Folder(
                        "",
                        Paths.get(context.getWorkspace().locations().getWorkspaceLocation())
                                .resolve("nuts-workspace.json").toString(),
                        null
                ));
                for (NutsStoreLocation value : NutsStoreLocation.values()) {
                    Path r = Paths.get(context.getWorkspace().locations().getStoreLocation(value));
                    if (Files.isDirectory(r)) {
                        ZipUtils.Folder folder = new ZipUtils.Folder(
                                value.id(),
                                r.toString(),
                                null
                        );
                        all.add(folder);
                    }
                }
                if (file == null || file.isEmpty()) {
                    file = context.getWorkspace().name() + ".zip";
                } else if (file.endsWith("/") || file.endsWith("\\")) {
                    file += context.getWorkspace().name() + ".zip";
                } else if (Files.isDirectory(Paths.get(file))) {
                    file += File.separator + context.getWorkspace().name() + ".zip";
                }
                if (Paths.get(file).getFileName().toString().indexOf('.') < 0) {
                    file += ".zip";
                }
                ZipUtils.zip(file, new ZipOptions(), all.toArray(new ZipUtils.Folder[0]));
            }
            return true;
        } else if (commandLine.next("restore") != null) {
            commandLine.setCommandName("nadmin restore");
            String file = null;
            String ws = null;
            NutsArgument a;
            while (commandLine.hasNext()) {
                if ((a = commandLine.nextString("--file", "-f")) != null) {
                    file = a.getStringValue("");
                } else if ((a = commandLine.nextString("--workspace", "-w")) != null) {
                    ws = a.getStringValue("");
                } else if (commandLine.peek().isNonOption()) {
                    file = commandLine.nextString().getStringValue("");
                } else {
                    context.configureLast(commandLine);
                }
            }
            if (file == null || file.isEmpty()) {
                file = context.getWorkspace().name() + ".zip";
            } else if (file.endsWith("/") || file.endsWith("\\")) {
                file += context.getWorkspace().name() + ".zip";
            } else if (Files.isDirectory(Paths.get(file))) {
                file += File.separator + context.getWorkspace().name() + ".zip";
            }
            if (Paths.get(file).getFileName().toString().indexOf('.') < 0 && !Files.exists(Paths.get(file))) {
                file += ".zip";
            }
            if (!Files.isRegularFile(Paths.get(file))) {
                commandLine.required("not a valid file : " + file);
            }
            if (commandLine.isExecMode()) {
                NutsObjectElement[] nutsWorkspaceConfigRef = new NutsObjectElement[1];
                ZipUtils.visitZipFile(new File(file), (PathFilter) "/nuts-workspace.json"::equals,
                        (path, inputStream) -> {
                            NutsObjectElement e = context.getWorkspace().formats().element().setContentType(NutsContentType.JSON)
                                    .parse(inputStream, NutsObjectElement.class).object();
                            nutsWorkspaceConfigRef[0] = e;
                            return false;
                        }
                );
                if (nutsWorkspaceConfigRef[0] == null) {
                    commandLine.required("not a valid file : " + file);
                }
                if (ws == null || ws.isEmpty()) {
                    ws = nutsWorkspaceConfigRef[0].get("name").primitive().getString();
                }
                if (ws == null || ws.isEmpty()) {
                    commandLine.required("not a valid file : " + file);
                }
                String platformHomeFolder = Nuts.getPlatformHomeFolder(null, NutsStoreLocation.CONFIG, null, false, ws);
                ZipUtils.unzip(file, platformHomeFolder, new UnzipOptions().setSkipRoot(true));
                if (context.getSession().isPlainTrace()) {
                    context.getSession().out().printf("restore %s to %s %n", file, platformHomeFolder);
                }
            }
            return true;
        }
        return false;
    }

}
