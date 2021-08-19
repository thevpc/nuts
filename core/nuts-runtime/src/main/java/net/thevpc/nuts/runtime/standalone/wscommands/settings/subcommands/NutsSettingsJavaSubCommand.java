/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.wscommands.settings.subcommands;

import net.thevpc.nuts.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * @author thevpc
 */
public class NutsSettingsJavaSubCommand extends AbstractNutsSettingsSubCommand {

    @Override
    public boolean exec(NutsCommandLine cmdLine, Boolean autoSave, NutsSession session) {
        if (autoSave == null) {
            autoSave = false;
        }
        NutsWorkspace ws = session.getWorkspace();
        NutsPrintStream out = session.out();
        NutsWorkspaceConfigManager conf = ws.config();
        if (cmdLine.next("add java") != null) {
            if (cmdLine.next("--search") != null) {
                List<String> extraLocations = new ArrayList<>();
                while (cmdLine.hasNext()) {
                    extraLocations.add(cmdLine.next().getString());
                }
                if (extraLocations.isEmpty()) {
                    for (NutsSdkLocation loc : ws.sdks().searchSystem("java")) {
                        ws.sdks().add(loc);
                    }
                } else {
                    for (String extraLocation : extraLocations) {
                        for (NutsSdkLocation loc : ws.sdks().searchSystem("java", extraLocation)) {
                            ws.sdks().add(loc);
                        }
                    }
                }
                cmdLine.setCommandName("config java").unexpectedArgument();
                if (autoSave) {
                    conf.save(false);
                }
            } else {
                while (cmdLine.hasNext()) {
                    NutsSdkLocation loc = ws.sdks().resolve("java", cmdLine.next().getString(), null);
                    if (loc != null) {
                        ws.sdks().add(loc);
                    }
                }
                if (autoSave) {
                    conf.save(false);
                }
            }
            return true;
        } else if (cmdLine.next("remove java") != null) {
            while (cmdLine.hasNext()) {
                String name = cmdLine.next().getString();
                NutsSdkLocation loc = ws.sdks().findByName("java", name);
                if (loc == null) {
                    loc = ws.sdks().findByPath("java", name);
                    if (loc == null) {
                        loc = ws.sdks().findByVersion("java", name);
                    }
                }
                if (loc != null) {
                    ws.sdks().remove(loc);
                }
            }
            if (autoSave) {
                conf.save(false);
            }
            return true;
        } else if (cmdLine.next("list java") != null) {
            NutsTableFormat t = session.getWorkspace().formats().table()
                    //                    .setBorder(TableFormatter.SPACE_BORDER)
                    .setVisibleHeader(true);
            NutsMutableTableModel m = t.createModel();
            t.setValue(m);
            m.addHeaderCells("Name", "Version", "Path");
            while (cmdLine.hasNext()) {
                if (!t.configureFirst(cmdLine)) {
                    cmdLine.setCommandName("config list java").unexpectedArgument();
                }
            }
            if (cmdLine.isExecMode()) {
                NutsSdkLocation[] sdks = ws.sdks().find("java", null);
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
