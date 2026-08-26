/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.imports;

import net.thevpc.nuts.cmdline.NArgValueComplete;
import net.thevpc.nuts.io.NOut;
import net.thevpc.nuts.core.NWorkspace;

import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.AbstractNSettingsSubCommand;
import net.thevpc.nuts.reflect.NScore;
import net.thevpc.nuts.reflect.NScorable;

/**
 *
 * @author thevpc
 */
@NScore(fixed = NScorable.DEFAULT_SCORE)
public class NSettingsImportSubCommand extends AbstractNSettingsSubCommand {
    public NSettingsImportSubCommand() {
        super();
    }

    @Override
    public boolean exec(NCmdLine cmdLine, Boolean autoSave) {
        if (cmdLine.next("list imports", "list import", "import list", "li").isPresent()) {
            cmdLine.commandName("config list imports").throwUnexpectedArgument();
            if (cmdLine.isExecMode()) {
                for (String imp : (NWorkspace.of().allImports())) {
                    NOut.println(NMsg.ofP(imp));
                }
            }
            return true;
        } else if (cmdLine.next("clear imports", "ci").isPresent()) {
            cmdLine.commandName("config clear imports").throwUnexpectedArgument();
            if (cmdLine.isExecMode()) {
                NWorkspace.of().clearImports();
                NWorkspace.of().saveConfig();
            }
            return true;
        } else if (cmdLine.next("import", "ia").isPresent()) {
            do {
                String a = cmdLine.nextNonOption("import", null).get()
                        .asString().get();
                if (cmdLine.isExecMode()) {
                    NWorkspace.of().addImports(a);
                }
            } while (cmdLine.hasNext());
            if (cmdLine.isExecMode()) {
                NWorkspace.of().saveConfig();
            }
            return true;
        } else if (cmdLine.next("unimport", "ir").isPresent()) {
            while (cmdLine.hasNext()) {
                String ii = cmdLine.nextNonOption("import", NArgValueComplete.ofSimpleCandidatesListSupplier(()->NWorkspace.of().allImports())).get()
                        .asString().get();
                if (cmdLine.isExecMode()) {
                    NWorkspace.of().removeImports(ii);
                }
            }
            if (cmdLine.isExecMode()) {
                NWorkspace.of().saveConfig();
            }
            return true;
        }
        return false;
    }

}
