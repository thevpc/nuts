/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.toolbox.nsh.cmds.config;

import net.vpc.app.nuts.NutsSdkLocation;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.NutsWorkspaceConfigManager;
import net.vpc.app.nuts.toolbox.nsh.NutsCommandContext;
import net.vpc.app.nuts.toolbox.nsh.cmds.ConfigCommand;
import net.vpc.app.nuts.toolbox.nsh.util.DefaultWorkspaceCellFormatter;
import net.vpc.common.commandline.CommandLine;
import net.vpc.common.commandline.format.TableFormatter;

import java.io.PrintStream;
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
        PrintStream out = context.getTerminal().getFormattedOut();
        if (cmdLine.readAll("add java")) {
            if (cmdLine.readAll("--search")) {
                List<String> extraLocations = new ArrayList<>();
                while (cmdLine.hasNext()) {
                    extraLocations.add(cmdLine.read().getExpression());
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
                cmdLine.unexpectedArgument();
                if (autoSave) {
                    ws.getConfigManager().save();
                }
            } else {
                while (cmdLine.hasNext()) {
                    NutsSdkLocation loc = ws.getConfigManager().resolveJdkLocation(cmdLine.read().getExpression());
                    if (loc != null) {
                        ws.getConfigManager().addSdk("java", loc);
                    }
                }
                if (autoSave) {
                    ws.getConfigManager().save();
                }
            }
            return true;
        } else if (cmdLine.readAll("remove java")) {
            NutsWorkspaceConfigManager cm = ws.getConfigManager();
            while (cmdLine.hasNext()) {
                String name = cmdLine.read().getExpression();
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
        } else if (cmdLine.readAll("list java")) {
            TableFormatter t = new TableFormatter(new DefaultWorkspaceCellFormatter(ws))
                    .setBorder(TableFormatter.SPACE_BORDER)
                    .setVisibleHeader(true)
                    .setColumnsConfig("name", "version", "path")
                    .addHeaderCells("==Name==", "==Version==", "==Path==");
            while (cmdLine.hasNext()) {
                if (!t.configure(cmdLine)) {
                    cmdLine.unexpectedArgument();
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
