/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.imports;

import net.thevpc.nuts.NConfigs;
import net.thevpc.nuts.NImports;
import net.thevpc.nuts.NMsg;
import net.thevpc.nuts.cmdline.NArgName;
import net.thevpc.nuts.cmdline.NCommandLine;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.AbstractNSettingsSubCommand;

/**
 *
 * @author thevpc
 */
public class NSettingsImportSubCommand extends AbstractNSettingsSubCommand {

    @Override
    public boolean exec(NCommandLine cmdLine, Boolean autoSave, NSession session) {
        if (cmdLine.next("list imports", "li").isPresent()) {
            cmdLine.setCommandName("config list imports").throwUnexpectedArgument();
            if (cmdLine.isExecMode()) {
                for (String imp : (NImports.of(session).getAllImports())) {
                    session.out().println(NMsg.ofPlain(imp));
                }
            }
            return true;
        } else if (cmdLine.next("clear imports", "ci").isPresent()) {
            cmdLine.setCommandName("config clear imports").throwUnexpectedArgument();
            if (cmdLine.isExecMode()) {
                NImports.of(session).clearImports();
                NConfigs.of(session).save();
            }
            return true;
        } else if (cmdLine.next("import", "ia").isPresent()) {
            do {
                String a = cmdLine.nextNonOption(NArgName.of("import",session)).get(session)
                        .asString().get(session);
                if (cmdLine.isExecMode()) {
                    NImports.of(session).addImports(new String[]{a});
                }
            } while (cmdLine.hasNext());
            if (cmdLine.isExecMode()) {
                NConfigs.of(session).save();
            }
            return true;
        } else if (cmdLine.next("unimport", "ir").isPresent()) {
            while (cmdLine.hasNext()) {
                String ii = cmdLine.nextNonOption(NArgName.of("import",session)).get(session)
                        .asString().get(session);
                if (cmdLine.isExecMode()) {
                    NImports.of(session).removeImports(new String[]{ii});
                }
            }
            if (cmdLine.isExecMode()) {
                NConfigs.of(session).save();
            }
            return true;
        }
        return false;
    }

}
