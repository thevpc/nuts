/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.java;

import net.thevpc.nuts.cmdline.NCmdLine;

import net.thevpc.nuts.core.NSession;
import net.thevpc.nuts.core.NWorkspace;
import net.thevpc.nuts.platform.NExecutionEngines;
import net.thevpc.nuts.platform.NExecutionEngineLocation;
import net.thevpc.nuts.runtime.standalone.util.jclass.NJavaSdkUtils;
import net.thevpc.nuts.text.NMutableTableModel;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.AbstractNSettingsSubCommand;
import net.thevpc.nuts.platform.NExecutionEngineFamily;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTextArt;
import net.thevpc.nuts.util.NRef;
import net.thevpc.nuts.util.NScore;
import net.thevpc.nuts.util.NScorable;
import net.thevpc.nuts.util.NStringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * @author thevpc
 */
@NScore(fixed = NScorable.DEFAULT_SCORE)
public class NSettingsJavaSubCommand extends AbstractNSettingsSubCommand {
    public NSettingsJavaSubCommand() {
        super();
    }

    @Override
    public boolean exec(NCmdLine cmdLine, Boolean autoSave) {
        if (autoSave == null) {
            autoSave = false;
        }
        NSession session = NSession.of();
        NPrintStream out = session.out();
        NWorkspace workspace = NWorkspace.of();
        NExecutionEngines pinstaller = NExecutionEngines.of();
        if (cmdLine.next("add java", "java add").isPresent()) {
            if (cmdLine.next("--search").isPresent()) {
                List<String> extraLocations = new ArrayList<>();
                while (cmdLine.hasNext()) {
                    extraLocations.add(cmdLine.next().get().image());
                }
                if (extraLocations.isEmpty()) {
                    for (NExecutionEngineLocation loc : pinstaller.searchHostExecutionEngines(NExecutionEngineFamily.JAVA)) {
                        pinstaller.addExecutionEngine(loc);
                    }
                } else {
                    for (String extraLocation : extraLocations) {
                        for (NExecutionEngineLocation loc : pinstaller.searchHostExecutionEngines(NExecutionEngineFamily.JAVA, NPath.of(extraLocation))) {
                            pinstaller.addExecutionEngine(loc);
                        }
                    }
                }
                cmdLine.setCommandName("config java").throwUnexpectedArgument();
                if (autoSave) {
                    workspace.saveConfig(false);
                }
            } else if (cmdLine.next("--download").isPresent()) {
                while (cmdLine.hasNext()) {
                    NRef<String> ver = NRef.ofNull();
                    NRef<String> product = NRef.ofNull();
                    cmdLine
                            .matcher()
                            .with("--version").matchEntry(a -> ver.set(a.stringValue()))
                            .with("--jdk").matchTrueFlag(a -> product.set(NExecutionEngineLocation.JAVA_PRODUCT_JDK))
                            .with("--jre").matchTrueFlag(a -> product.set(NExecutionEngineLocation.JAVA_PRODUCT_JRE))
                            .require();
                    NExecutionEngineLocation loc = pinstaller.downloadRemoteExecutionEngine(
                            NExecutionEngineFamily.JAVA,
                            NStringUtils.firstNonBlank(product.get(), NExecutionEngineLocation.JAVA_PRODUCT_JDK), null, NStringUtils.firstNonBlank(ver.get(), String.valueOf(NJavaSdkUtils.defaultJavaMajorVersion()))
                    ).orNull();
                    if (loc != null) {
                        pinstaller.addExecutionEngine(loc);
                    }
                }
            } else {
                while (cmdLine.hasNext()) {
                    NExecutionEngineLocation loc = pinstaller.resolveExecutionEngine(NExecutionEngineFamily.JAVA,
                            NPath.of(cmdLine.next().get().image()), null).orNull();
                    if (loc != null) {
                        pinstaller.addExecutionEngine(loc);
                    }
                }
                if (autoSave) {
                    workspace.saveConfig(false);
                }
            }
            return true;
        } else if (cmdLine.next("remove java", "java remove").isPresent()) {
            while (cmdLine.hasNext()) {
                String name = cmdLine.next().get().image();
                NExecutionEngineLocation loc = pinstaller.findExecutionEngineByName(NExecutionEngineFamily.JAVA, name).orNull();
                if (loc == null) {
                    loc = pinstaller.findExecutionEngineByName(NExecutionEngineFamily.JAVA, name).orNull();
                    if (loc == null) {
                        loc = pinstaller.findExecutionEngineByVersion(NExecutionEngineFamily.JAVA, name).orNull();
                    }
                }
                if (loc != null) {
                    pinstaller.removeExecutionEngine(loc);
                }
            }
            if (autoSave) {
                workspace.saveConfig(false);
            }
            return true;
        } else if (cmdLine.next("list java", "java list").isPresent()) {
            //NTableFormat t = NTableFormat.of()
            //                    .setBorder(TableFormatter.SPACE_BORDER)
            //        .setVisibleHeader(true);
            NMutableTableModel m = NMutableTableModel.of();
            //t.setValue(m);
            m.addHeaderRow(NText.ofPlain("Name"), NText.ofPlain("Version"), NText.ofPlain("Path"));
            while (cmdLine.hasNext()) {
                //if (!t.configureFirst(cmdLine)) {
                cmdLine.setCommandName("config list java").throwUnexpectedArgument();
                //}
            }
            if (cmdLine.isExecMode()) {
                NExecutionEngineLocation[] sdks = pinstaller.findExecutionEngines(NExecutionEngineFamily.JAVA, null).toArray(NExecutionEngineLocation[]::new);
                Arrays.sort(sdks, new Comparator<NExecutionEngineLocation>() {
                    @Override
                    public int compare(NExecutionEngineLocation o1, NExecutionEngineLocation o2) {
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
                for (NExecutionEngineLocation jloc : sdks) {
                    m.addRow(NText.of(jloc.getName()), NText.of(jloc.getVersion()), NText.of(jloc.getPath()));
                }
                out.print(NTextArt.of().getTableRenderer().get().render(m));
            }
            return true;
        }
        return false;
    }

}
