/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.java;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NCommandLine;
import net.thevpc.nuts.format.NMutableTableModel;
import net.thevpc.nuts.format.NTableFormat;
import net.thevpc.nuts.io.NOutStream;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.AbstractNSettingsSubCommand;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * @author thevpc
 */
public class NSettingsJavaSubCommand extends AbstractNSettingsSubCommand {

    @Override
    public boolean exec(NCommandLine cmdLine, Boolean autoSave, NSession session) {
        if (autoSave == null) {
            autoSave = false;
        }
        NOutStream out = session.out();
        NConfigs conf = NConfigs.of(session);
        NPlatformManager platforms = NEnvs.of(session).platforms();
        if (cmdLine.next("add java").isPresent()) {
            if (cmdLine.next("--search").isPresent()) {
                List<String> extraLocations = new ArrayList<>();
                while (cmdLine.hasNext()) {
                    extraLocations.add(cmdLine.next().flatMap(NValue::asString).get(session));
                }
                if (extraLocations.isEmpty()) {
                    for (NPlatformLocation loc : platforms.searchSystemPlatforms(NPlatformFamily.JAVA)) {
                        platforms.addPlatform(loc);
                    }
                } else {
                    for (String extraLocation : extraLocations) {
                        for (NPlatformLocation loc : platforms.searchSystemPlatforms(NPlatformFamily.JAVA, extraLocation)) {
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
                    NPlatformLocation loc = platforms.resolvePlatform(NPlatformFamily.JAVA, cmdLine.next()
                            .flatMap(NValue::asString).get(session), null);
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
                        .flatMap(NValue::asString).get(session);
                NPlatformLocation loc = platforms.findPlatformByName(NPlatformFamily.JAVA, name);
                if (loc == null) {
                    loc = platforms.findPlatformByPath(NPlatformFamily.JAVA, name);
                    if (loc == null) {
                        loc = platforms.findPlatformByVersion(NPlatformFamily.JAVA, name);
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
            NTableFormat t = NTableFormat.of(session)
                    //                    .setBorder(TableFormatter.SPACE_BORDER)
                    .setVisibleHeader(true);
            NMutableTableModel m = NMutableTableModel.of(session);
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
