/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.wscommands.settings.subcommands.imports;

import net.thevpc.nuts.NutsArgumentName;
import net.thevpc.nuts.NutsCommandLine;
import net.thevpc.nuts.NutsCommandLines;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.runtime.standalone.wscommands.settings.subcommands.AbstractNutsSettingsSubCommand;

/**
 *
 * @author thevpc
 */
public class NutsSettingsImportSubCommand extends AbstractNutsSettingsSubCommand {

    @Override
    public boolean exec(NutsCommandLine cmdLine, Boolean autoSave, NutsSession session) {
        if (cmdLine.next("list imports", "li") != null) {
            cmdLine.setCommandName("config list imports").unexpectedArgument();
            if (cmdLine.isExecMode()) {
                for (String imp : (session.imports().getAll())) {
                    session.out().printf("%s%n", imp);
                }
            }
            return true;
        } else if (cmdLine.next("clear imports", "ci") != null) {
            cmdLine.setCommandName("config clear imports").unexpectedArgument();
            if (cmdLine.isExecMode()) {
                session.imports().removeAll();
                session.config().save();
            }
            return true;
        } else if (cmdLine.next("import", "ia") != null) {
            do {
                String a = cmdLine.required().nextNonOption(NutsArgumentName.of("import",session)).getString();
                if (cmdLine.isExecMode()) {
                    session.imports().add(new String[]{a});
                }
            } while (cmdLine.hasNext());
            if (cmdLine.isExecMode()) {
                session.config().save();
            }
            return true;
        } else if (cmdLine.next("unimport", "ir") != null) {
            while (cmdLine.hasNext()) {
                String ii = cmdLine.required().nextNonOption(NutsArgumentName.of("import",session)).getString();
                if (cmdLine.isExecMode()) {
                    session.imports().remove(new String[]{ii});
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
