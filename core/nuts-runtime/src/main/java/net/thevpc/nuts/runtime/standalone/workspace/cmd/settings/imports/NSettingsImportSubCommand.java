/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.imports;

import net.thevpc.nuts.io.NOut;
import net.thevpc.nuts.core.NWorkspace;

import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.cmdline.NArgName;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.AbstractNSettingsSubCommand;

/**
 *
 * @author thevpc
 */
public class NSettingsImportSubCommand extends AbstractNSettingsSubCommand {
    public NSettingsImportSubCommand(NWorkspace workspace) {
        super();
    }

    @Override
    public boolean exec(NCmdLine cmdLine, Boolean autoSave) {
        if (cmdLine.next("list imports","list import","import list", "li").isPresent()) {
            cmdLine.setCommandName("config list imports").throwUnexpectedArgument();
            if (cmdLine.isExecMode()) {
                for (String imp : (NWorkspace.of().getAllImports())) {
                    NOut.println(NMsg.ofPlain(imp));
                }
            }
            return true;
        } else if (cmdLine.next("clear imports", "ci").isPresent()) {
            cmdLine.setCommandName("config clear imports").throwUnexpectedArgument();
            if (cmdLine.isExecMode()) {
                NWorkspace.of().clearImports();
                NWorkspace.of().saveConfig();
            }
            return true;
        } else if (cmdLine.next("import", "ia").isPresent()) {
            do {
                String a = cmdLine.nextNonOption(NArgName.of("import")).get()
                        .asString().get();
                if (cmdLine.isExecMode()) {
                    NWorkspace.of().addImports(new String[]{a});
                }
            } while (cmdLine.hasNext());
            if (cmdLine.isExecMode()) {
                NWorkspace.of().saveConfig();
            }
            return true;
        } else if (cmdLine.next("unimport", "ir").isPresent()) {
            while (cmdLine.hasNext()) {
                String ii = cmdLine.nextNonOption(NArgName.of("import")).get()
                        .asString().get();
                if (cmdLine.isExecMode()) {
                    NWorkspace.of().removeImports(new String[]{ii});
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
