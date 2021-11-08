/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.wscommands.settings.subcommands.java;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.wscommands.settings.subcommands.AbstractNutsSettingsSubCommand;

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
        NutsPrintStream out = session.out();
        NutsWorkspaceConfigManager conf = session.config();
        NutsPlatformManager platforms = session.env().platforms();
        if (cmdLine.next("add java") != null) {
            if (cmdLine.next("--search") != null) {
                List<String> extraLocations = new ArrayList<>();
                while (cmdLine.hasNext()) {
                    extraLocations.add(cmdLine.next().getString());
                }
                if (extraLocations.isEmpty()) {
                    for (NutsPlatformLocation loc : platforms.searchSystemPlatforms(NutsPlatformType.JAVA)) {
                        platforms.addPlatform(loc);
                    }
                } else {
                    for (String extraLocation : extraLocations) {
                        for (NutsPlatformLocation loc : platforms.searchSystemPlatforms(NutsPlatformType.JAVA, extraLocation)) {
                            platforms.addPlatform(loc);
                        }
                    }
                }
                cmdLine.setCommandName("config java").unexpectedArgument();
                if (autoSave) {
                    conf.save(false);
                }
            } else {
                while (cmdLine.hasNext()) {
                    NutsPlatformLocation loc = platforms.resolvePlatform(NutsPlatformType.JAVA, cmdLine.next().getString(), null);
                    if (loc != null) {
                        platforms.addPlatform(loc);
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
                NutsPlatformLocation loc = platforms.findPlatformByName(NutsPlatformType.JAVA, name);
                if (loc == null) {
                    loc = platforms.findPlatformByPath(NutsPlatformType.JAVA, name);
                    if (loc == null) {
                        loc = platforms.findPlatformByVersion(NutsPlatformType.JAVA, name);
                    }
                }
                if (loc != null) {
                    platforms.removePlatform(loc);
                }
            }
            if (autoSave) {
                conf.save(false);
            }
            return true;
        } else if (cmdLine.next("list java") != null) {
            NutsTableFormat t = NutsTableFormat.of(session)
                    //                    .setBorder(TableFormatter.SPACE_BORDER)
                    .setVisibleHeader(true);
            NutsMutableTableModel m = NutsMutableTableModel.of(session);
            t.setValue(m);
            m.addHeaderCells("Name", "Version", "Path");
            while (cmdLine.hasNext()) {
                if (!t.configureFirst(cmdLine)) {
                    cmdLine.setCommandName("config list java").unexpectedArgument();
                }
            }
            if (cmdLine.isExecMode()) {
                NutsPlatformLocation[] sdks = platforms.findPlatforms(NutsPlatformType.JAVA, null);
                Arrays.sort(sdks, new Comparator<NutsPlatformLocation>() {
                    @Override
                    public int compare(NutsPlatformLocation o1, NutsPlatformLocation o2) {
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
                for (NutsPlatformLocation jloc : sdks) {
                    m.addRow(jloc.getName(), jloc.getVersion(), jloc.getPath());
                }
                out.print(t.format());
            }
            return true;
        }
        return false;
    }

}
