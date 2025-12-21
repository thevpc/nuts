/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.installeditorsyntax;

import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.command.NSysEditorFamily;
import net.thevpc.nuts.command.NSysEditorSupportCmd;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.AbstractNSettingsSubCommand;
import net.thevpc.nuts.util.NBooleanRef;
import net.thevpc.nuts.util.NScore;
import net.thevpc.nuts.util.NScorable;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 *
 * @author thevpc
 */
@NScore(fixed = NScorable.DEFAULT_SCORE)
public class NSettingsInstallEditorSyntaxSubCommand extends AbstractNSettingsSubCommand {
    public NSettingsInstallEditorSyntaxSubCommand() {
        super();
    }

    @Override
    public boolean exec(NCmdLine cmdLine, Boolean autoSave) {
        for (String mode : new String[]{"ntf", "tson"}) {
            if (cmdLine.next("install-" + mode + "-editor-syntax").isPresent()) {
                NBooleanRef force = NBooleanRef.ofFalse();
                Set<NSysEditorFamily> editors = new LinkedHashSet<>();
                while (cmdLine.hasNext()) {
                    cmdLine.matcher()
                            .with("--force","-f").matchFlag(a -> {
                                force.set(a.booleanValue());
                            })
                            .withNonOption().matchAny(a -> {

                                String e = a.asString().get();
                                if ("all".equalsIgnoreCase(e)) {
                                    editors.addAll(Arrays.asList(NSysEditorFamily.values()));
                                } else {
                                    editors.addAll(NSysEditorFamily.parseSet(e).get());
                                }
                            })
                            .require();
                }
                if (editors.isEmpty()) {
                    editors.addAll(Arrays.asList(NSysEditorFamily.values()));
                }
                if (cmdLine.isExecMode()) {
                    String title = mode;
                    switch (mode) {
                        case "ntf": {
                            title = "NutsTextFormat";
                            break;
                        }
                        case "tson": {
                            title = "TSON";
                            break;
                        }
                    }
                    NSysEditorSupportCmd.of()
                            .setSource(NPath.of("https://github.com/thevpc/nuts/raw/refs/heads/master/documentation/integration/" + mode + "-support"))
                            .setLanguageId(mode)
                            .setLanguageName(title)
                            .setLanguageVersion("1.0.0")
                            .setFileExtension(mode)
                            .setFileName("*." + mode)
                            .addEditorFamilies(editors.toArray(new NSysEditorFamily[0]))
                            .run();
                }
                return true;
            }
        }

        return false;
    }
}
