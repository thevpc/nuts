/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.java;

import net.thevpc.nuts.cmdline.NCmdLine;

import net.thevpc.nuts.core.NSession;
import net.thevpc.nuts.core.NWorkspace;
import net.thevpc.nuts.platform.NRuntimeDistributionManager;
import net.thevpc.nuts.platform.NRuntimeDistribution;
import net.thevpc.nuts.runtime.standalone.util.jclass.NJavaSdkUtils;
import net.thevpc.nuts.text.NMutableTableModel;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.AbstractNSettingsSubCommand;
import net.thevpc.nuts.platform.NRuntimeDistributionFamily;
import net.thevpc.nuts.text.NTableCell;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTextArt;
import net.thevpc.nuts.util.NRef;
import net.thevpc.nuts.reflect.NScore;
import net.thevpc.nuts.reflect.NScorable;
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
        NRuntimeDistributionManager pinstaller = NRuntimeDistributionManager.of();
        if (cmdLine.next("add java", "java add").isPresent()) {
            if (cmdLine.next("--search").isPresent()) {
                List<String> extraLocations = new ArrayList<>();
                while (cmdLine.hasNext()) {
                    extraLocations.add(cmdLine.next().get().image());
                }
                if (extraLocations.isEmpty()) {
                    for (NRuntimeDistribution loc : pinstaller.searchHostRuntimeDistributions(NRuntimeDistributionFamily.JAVA)) {
                        pinstaller.addRuntimeDistribution(loc);
                    }
                } else {
                    for (String extraLocation : extraLocations) {
                        for (NRuntimeDistribution loc : pinstaller.searchHostRuntimeDistributions(NRuntimeDistributionFamily.JAVA, NPath.of(extraLocation))) {
                            pinstaller.addRuntimeDistribution(loc);
                        }
                    }
                }
                cmdLine.commandName("config java").throwUnexpectedArgument();
                if (autoSave) {
                    workspace.saveConfig(false);
                }
            } else if (cmdLine.next("--download").isPresent()) {
                while (cmdLine.hasNext()) {
                    NRef<String> ver = NRef.ofNull();
                    NRef<String> product = NRef.ofNull();
                    NRef<String> vendor = NRef.ofNull();
                    cmdLine
                            .matcher()
                            .when("--version").asEntry(a -> ver.set(a.stringValue()))
                            .when("--jdk").asTrueFlag(a -> product.set(NRuntimeDistribution.JAVA_PRODUCT_JDK))
                            .when("--jre").asTrueFlag(a -> product.set(NRuntimeDistribution.JAVA_PRODUCT_JRE))
                            .when("--vendor").asEntry(a -> vendor.set(a.stringValue()))
                            .require();
                    NRuntimeDistribution loc = pinstaller.downloadRemoteRuntimeDistribution(
                            NRuntimeDistributionFamily.JAVA,
                            NStringUtils.firstNonBlank(product.get(), NRuntimeDistribution.JAVA_PRODUCT_JDK),
                            vendor.get(),
                            NStringUtils.firstNonBlank(ver.get(), String.valueOf(NJavaSdkUtils.defaultJavaMajorVersion()))
                    ).orNull();
                    if (loc != null) {
                        pinstaller.addRuntimeDistribution(loc);
                    }
                }
            } else {
                while (cmdLine.hasNext()) {
                    NRuntimeDistribution loc = pinstaller.resolveRuntimeDistribution(NRuntimeDistributionFamily.JAVA,
                            NPath.of(cmdLine.next().get().image()), null).orNull();
                    if (loc != null) {
                        pinstaller.addRuntimeDistribution(loc);
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
                NRuntimeDistribution loc = pinstaller.findRuntimeDistributionByName(NRuntimeDistributionFamily.JAVA, name).orNull();
                if (loc == null) {
                    loc = pinstaller.findRuntimeDistributionByName(NRuntimeDistributionFamily.JAVA, name).orNull();
                    if (loc == null) {
                        loc = pinstaller.findRuntimeDistributionByVersion(NRuntimeDistributionFamily.JAVA, name).orNull();
                    }
                }
                if (loc != null) {
                    pinstaller.removeRuntimeDistribution(loc);
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
            m.addHeaderRow(
                    NTableCell.of(NText.of("Name")),
                    NTableCell.of(NText.of("Version")),
                    NTableCell.of(NText.of("Path")));
            while (cmdLine.hasNext()) {
                //if (!t.configureFirst(cmdLine)) {
                cmdLine.commandName("config list java").throwUnexpectedArgument();
                //}
            }
            if (cmdLine.isExecMode()) {
                NRuntimeDistribution[] sdks = pinstaller.findRuntimeDistributions(NRuntimeDistributionFamily.JAVA, null).toArray(NRuntimeDistribution[]::new);
                Arrays.sort(sdks, new Comparator<NRuntimeDistribution>() {
                    @Override
                    public int compare(NRuntimeDistribution o1, NRuntimeDistribution o2) {
                        int x = o1.name().compareTo(o2.name());
                        if (x != 0) {
                            return x;
                        }
                        x = o1.version().compareTo(o2.version());
                        if (x != 0) {
                            return x;
                        }
                        x = o1.path().compareTo(o2.path());
                        if (x != 0) {
                            return x;
                        }
                        return x;
                    }
                });
                for (NRuntimeDistribution jloc : sdks) {
                    m.addRow(NTableCell.of(NText.of(jloc.name())), NTableCell.of(NText.of(jloc.version())), NTableCell.of(NText.of(jloc.path())));
                }
                out.print(NTextArt.of().tableRenderer().get().render(m));
            }
            return true;
        }
        return false;
    }

}
