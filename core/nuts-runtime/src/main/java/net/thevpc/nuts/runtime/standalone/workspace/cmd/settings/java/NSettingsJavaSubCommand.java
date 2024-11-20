/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.java;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.format.NMutableTableModel;
import net.thevpc.nuts.format.NTableFormat;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.AbstractNSettingsSubCommand;
import net.thevpc.nuts.util.NLiteral;
import net.thevpc.nuts.env.NPlatformFamily;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * @author thevpc
 */
public class NSettingsJavaSubCommand extends AbstractNSettingsSubCommand {
    public NSettingsJavaSubCommand(NWorkspace workspace) {
        super(workspace);
    }

    @Override
    public boolean exec(NCmdLine cmdLine, Boolean autoSave) {
        if (autoSave == null) {
            autoSave = false;
        }
        NSession session=workspace.currentSession();
        NPrintStream out = session.out();
        NConfigs conf = NConfigs.of();
        NPlatforms platforms = NPlatforms.of();
        if (cmdLine.next("add java").isPresent()) {
            if (cmdLine.next("--search").isPresent()) {
                List<String> extraLocations = new ArrayList<>();
                while (cmdLine.hasNext()) {
                    extraLocations.add(cmdLine.next().flatMap(NLiteral::asString).get());
                }
                if (extraLocations.isEmpty()) {
                    for (NPlatformLocation loc : platforms.searchSystemPlatforms(NPlatformFamily.JAVA)) {
                        platforms.addPlatform(loc);
                    }
                } else {
                    for (String extraLocation : extraLocations) {
                        for (NPlatformLocation loc : platforms.searchSystemPlatforms(NPlatformFamily.JAVA, NPath.of(extraLocation))) {
                            platforms.addPlatform(loc);
                        }
                    }
                }
                cmdLine.setCommandName("config java").throwUnexpectedArgument();
                if (autoSave) {
                    conf.save(false);
                }
            } else {
                while (cmdLine.hasNext()) {
                    NPlatformLocation loc = platforms.resolvePlatform(NPlatformFamily.JAVA,
                            NPath.of(cmdLine.next().flatMap(NLiteral::asString).get()), null).orNull();
                    if (loc != null) {
                        platforms.addPlatform(loc);
                    }
                }
                if (autoSave) {
                    conf.save(false);
                }
            }
            return true;
        } else if (cmdLine.next("remove java").isPresent()) {
            while (cmdLine.hasNext()) {
                String name = cmdLine.next()
                        .flatMap(NLiteral::asString).get();
                NPlatformLocation loc = platforms.findPlatformByName(NPlatformFamily.JAVA, name).orNull();
                if (loc == null) {
                    loc = platforms.findPlatformByName(NPlatformFamily.JAVA, name).orNull();
                    if (loc == null) {
                        loc = platforms.findPlatformByVersion(NPlatformFamily.JAVA, name).orNull();
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
        } else if (cmdLine.next("list java").isPresent()) {
            NTableFormat t = NTableFormat.of()
                    //                    .setBorder(TableFormatter.SPACE_BORDER)
                    .setVisibleHeader(true);
            NMutableTableModel m = NMutableTableModel.of();
            t.setValue(m);
            m.addHeaderCells("Name", "Version", "Path");
            while (cmdLine.hasNext()) {
                if (!t.configureFirst(cmdLine)) {
                    cmdLine.setCommandName("config list java").throwUnexpectedArgument();
                }
            }
            if (cmdLine.isExecMode()) {
                NPlatformLocation[] sdks = platforms.findPlatforms(NPlatformFamily.JAVA, null).toArray(NPlatformLocation[]::new);
                Arrays.sort(sdks, new Comparator<NPlatformLocation>() {
                    @Override
                    public int compare(NPlatformLocation o1, NPlatformLocation o2) {
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
                for (NPlatformLocation jloc : sdks) {
                    m.addRow(jloc.getName(), jloc.getVersion(), jloc.getPath());
                }
                out.print(t.format());
            }
            return true;
        }
        return false;
    }

}
