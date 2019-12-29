/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.toolbox.nadmin.config;

import net.vpc.app.nuts.*;

import java.io.PrintStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import net.vpc.app.nuts.NutsCommandLine;

/**
 * @author vpc
 */
public class JavaNAdminSubCommand extends AbstractNAdminSubCommand {

    @Override
    public boolean exec(NutsCommandLine cmdLine, Boolean autoSave, NutsApplicationContext context) {
        if (autoSave == null) {
            autoSave = false;
        }
        NutsWorkspace ws = context.getWorkspace();
        PrintStream out = context.session().out();
        NutsWorkspaceConfigManager conf = ws.config();
        if (cmdLine.next("add java") != null) {
            if (cmdLine.next("--search") != null) {
                List<String> extraLocations = new ArrayList<>();
                while (cmdLine.hasNext()) {
                    extraLocations.add(cmdLine.next().getString());
                }
                if (extraLocations.isEmpty()) {
                    for (NutsSdkLocation loc : conf.searchSdkLocations("java", context.session())) {
                        conf.addSdk(loc, null);
                    }
                } else {
                    for (String extraLocation : extraLocations) {
                        for (NutsSdkLocation loc : conf.searchSdkLocations("java", Paths.get(extraLocation), context.session())) {
                            conf.addSdk(loc, null);
                        }
                    }
                }
                cmdLine.setCommandName("config java").unexpectedArgument();
                if (autoSave) {
                    conf.save(false, context.getSession());
                }
            } else {
                while (cmdLine.hasNext()) {
                    NutsSdkLocation loc = conf.resolveSdkLocation("java", Paths.get(cmdLine.next().getString()), null, context.session());
                    if (loc != null) {
                        conf.addSdk(loc, null);
                    }
                }
                if (autoSave) {
                    conf.save(false, context.getSession());
                }
            }
            return true;
        } else if (cmdLine.next("remove java") != null) {
            while (cmdLine.hasNext()) {
                String name = cmdLine.next().getString();
                NutsSdkLocation loc = conf.findSdkByName("java", name, context.getSession());
                if (loc == null) {
                    loc = conf.findSdkByPath("java", Paths.get(name), context.getSession());
                    if (loc == null) {
                        loc = conf.findSdkByVersion("java", name);
                    }
                }
                if (loc != null) {
                    conf.removeSdk(loc, null);
                }
            }
            if (autoSave) {
                conf.save(false, context.getSession());
            }
            return true;
        } else if (cmdLine.next("list java") != null) {
            NutsTableFormat t = context.getWorkspace().table()
                    //                    .setBorder(TableFormatter.SPACE_BORDER)
                    .setVisibleHeader(true);
            NutsMutableTableModel m = t.createModel();
            t.setModel(m);
            m.addHeaderCells("Name", "Version", "Path");
            while (cmdLine.hasNext()) {
                if (!t.configureFirst(cmdLine)) {
                    cmdLine.setCommandName("config list java").unexpectedArgument();
                }
            }
            if (cmdLine.isExecMode()) {
                NutsSdkLocation[] sdks = conf.getSdks("java", context.getSession());
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
                    m.addRow(jloc.getName(), jloc.getVersion(), jloc.getPath());
                }
                out.print(t.format());
            }
            return true;
        }
        return false;
    }

}
