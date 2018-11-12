/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.toolbox.nsh.cmds.config;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.toolbox.nsh.util.TableFormatter;
import net.vpc.app.nuts.toolbox.nsh.NutsCommandContext;
import net.vpc.app.nuts.toolbox.nsh.cmds.ConfigCommand;
import net.vpc.common.commandline.CommandLine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * @author vpc
 */
public class JavaConfigSubCommand extends AbstractConfigSubCommand {

    @Override
    public boolean exec(CommandLine cmdLine, ConfigCommand config, Boolean autoSave, NutsCommandContext context) {
        if (autoSave == null) {
            autoSave = false;
        }
        NutsWorkspace ws = context.getWorkspace();
        NutsPrintStream out = context.getTerminal().getFormattedOut();
        if (cmdLine.read("add java")) {
            if (cmdLine.read("--search")) {
                List<String> extraLocations = new ArrayList<>();
                while (cmdLine.isEmpty()) {
                    extraLocations.add(cmdLine.readValue());
                }
                if (extraLocations.isEmpty()) {
                    for (NutsSdkLocation loc : ws.getConfigManager().searchJdkLocations(out)) {
                        ws.getConfigManager().addSdk("java", loc);
                    }
                } else {
                    for (String extraLocation : extraLocations) {
                        for (NutsSdkLocation loc : ws.getConfigManager().searchJdkLocations(extraLocation, out)) {
                            ws.getConfigManager().addSdk("java", loc);
                        }
                    }
                }
                cmdLine.requireEmpty();
                if (autoSave) {
                    ws.getConfigManager().save();
                }
            } else {
                while (cmdLine.isEmpty()) {
                    NutsSdkLocation loc = ws.getConfigManager().resolveJdkLocation(cmdLine.readValue());
                    if (loc != null) {
                        ws.getConfigManager().addSdk("java", loc);
                    }
                }
                if (autoSave) {
                    ws.getConfigManager().save();
                }
            }
            return true;
        } else if (cmdLine.read("remove java")) {
            NutsWorkspaceConfigManager cm = ws.getConfigManager();
            while (cmdLine.isEmpty()) {
                String name = cmdLine.readValue();
                NutsSdkLocation loc = cm.findSdkByName("java", name);
                if (loc == null) {
                    loc = cm.findSdkByPath("java", name);
                    if (loc == null) {
                        loc = cm.findSdkByVersion("java", name);
                    }
                }
                if (loc != null) {
                    cm.removeSdk("java", loc);
                }
            }
            if (autoSave) {
                ws.getConfigManager().save();
            }
            return true;
        } else if (cmdLine.read("list java")) {
            TableFormatter t = new TableFormatter(ws)
                    .setBorder(TableFormatter.SPACE_BORDER)
                    .setVisibleHeader(true)
                    .setColumnsConfig("name", "version", "path")
                    .addHeaderCells("==Name==", "==Version==", "==Path==");
            while (!cmdLine.isEmpty()) {
                if (!t.configure(cmdLine)) {
                    cmdLine.requireEmpty();
                }
            }
            if (cmdLine.isExecMode()) {

                NutsSdkLocation[] sdks = ws.getConfigManager().getSdks("java");
                Arrays.sort(sdks, new Comparator<NutsSdkLocation>() {
                    @Override
                    public int compare(NutsSdkLocation o1, NutsSdkLocation o2) {
                        int x = o1.getName().compareTo(o2.getName());
                        if (x != 0) {
                            return x;
                        }
                        x = o1.getVersion().compareTo(o2.getVersion());
                        if (x != 0) {
                            return x;
                        }
                        x = o1.getPath().compareTo(o2.getPath());
                        if (x != 0) {
                            return x;
                        }
                        return x;
                    }
                });
                for (NutsSdkLocation jloc : sdks) {
                    t.addRow(jloc.getName(), jloc.getVersion(), jloc.getPath());
                }
                out.printf(t.toString());
            }
            return true;
        }
        return false;
    }

    @Override
    public int getSupportLevel(Object criteria) {
        return DEFAULT_SUPPORT;
    }

}
