/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.imports;

import net.thevpc.nuts.cmdline.NutsArgumentName;
import net.thevpc.nuts.cmdline.NutsCommandLine;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.AbstractNutsSettingsSubCommand;

/**
 *
 * @author thevpc
 */
public class NutsSettingsImportSubCommand extends AbstractNutsSettingsSubCommand {

    @Override
    public boolean exec(NutsCommandLine cmdLine, Boolean autoSave, NutsSession session) {
        if (cmdLine.next("list imports", "li").isPresent()) {
            cmdLine.setCommandName("config list imports").throwUnexpectedArgument(session);
            if (cmdLine.isExecMode()) {
                for (String imp : (session.imports().getAllImports())) {
                    session.out().printf("%s%n", imp);
                }
            }
            return true;
        } else if (cmdLine.next("clear imports", "ci").isPresent()) {
            cmdLine.setCommandName("config clear imports").throwUnexpectedArgument(session);
            if (cmdLine.isExecMode()) {
                session.imports().clearImports();
                session.config().save();
            }
            return true;
        } else if (cmdLine.next("import", "ia").isPresent()) {
            do {
                String a = cmdLine.nextNonOption(NutsArgumentName.of("import",session)).get(session)
                        .asString().get(session);
                if (cmdLine.isExecMode()) {
                    session.imports().addImports(new String[]{a});
                }
            } while (cmdLine.hasNext());
            if (cmdLine.isExecMode()) {
                session.config().save();
            }
            return true;
        } else if (cmdLine.next("unimport", "ir").isPresent()) {
            while (cmdLine.hasNext()) {
                String ii = cmdLine.nextNonOption(NutsArgumentName.of("import",session)).get(session)
                        .asString().get(session);
                if (cmdLine.isExecMode()) {
                    session.imports().removeImports(new String[]{ii});
                }
            }
            if (cmdLine.isExecMode()) {
                session.config().save();
            }
            return true;
        }
        return false;
    }

}
