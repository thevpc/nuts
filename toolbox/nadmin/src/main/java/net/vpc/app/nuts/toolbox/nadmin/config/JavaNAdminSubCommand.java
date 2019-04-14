/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.toolbox.nadmin.config;

import net.vpc.app.nuts.NutsSdkLocation;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.NutsWorkspaceConfigManager;
import net.vpc.app.nuts.app.NutsApplicationContext;
import net.vpc.app.nuts.toolbox.nadmin.NAdminMain;
import net.vpc.app.nuts.app.util.DefaultWorkspaceCellFormatter;
import net.vpc.common.commandline.CommandLine;
import net.vpc.common.commandline.format.TableFormatter;

import java.io.PrintStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * @author vpc
 */
public class JavaNAdminSubCommand extends AbstractNAdminSubCommand {

    @Override
    public boolean exec(CommandLine cmdLine, NAdminMain config, Boolean autoSave, NutsApplicationContext context) {
        if (autoSave == null) {
            autoSave = false;
        }
        NutsWorkspace ws = context.getWorkspace();
        PrintStream out = context.getTerminal().getFormattedOut();
        NutsWorkspaceConfigManager conf = ws.config();
        if (cmdLine.readAll("add java")) {
            if (cmdLine.readAll("--search")) {
                List<String> extraLocations = new ArrayList<>();
                while (cmdLine.hasNext()) {
                    extraLocations.add(cmdLine.read().getExpression());
                }
                if (extraLocations.isEmpty()) {
                    for (NutsSdkLocation loc : conf.searchSdkLocations(sdkType, out)) {
                        conf.addSdk("java", loc);
                    }
                } else {
                    for (String extraLocation : extraLocations) {
                        for (NutsSdkLocation loc : conf.searchSdkLocations(sdkType, ws.io().path(extraLocation), out)) {
                            conf.addSdk("java", loc);
                        }
                    }
                }
                cmdLine.unexpectedArgument("config java");
                if (autoSave) {
                    conf.save(false);
                }
            } else {
                while (cmdLine.hasNext()) {
                    NutsSdkLocation loc = conf.resolveSdkLocation(ws.io().path(cmdLine.read().getExpression()));
                    if (loc != null) {
                        conf.addSdk("java", loc);
                    }
                }
                if (autoSave) {
                    conf.save(false);
                }
            }
            return true;
        } else if (cmdLine.readAll("remove java")) {
            while (cmdLine.hasNext()) {
                String name = cmdLine.read().getExpression();
                NutsSdkLocation loc = conf.findSdkByName("java", name);
                if (loc == null) {
                    loc = conf.findSdkByPath("java", ws.io().path(name));
                    if (loc == null) {
                        loc = conf.findSdkByVersion("java", name);
                    }
                }
                if (loc != null) {
                    conf.removeSdk("java", loc);
                }
            }
            if (autoSave) {
                conf.save(false);
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
                    cmdLine.unexpectedArgument("config list java");
                }
            }
            if (cmdLine.isExecMode()) {

                NutsSdkLocation[] sdks = conf.getSdks("java");
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
