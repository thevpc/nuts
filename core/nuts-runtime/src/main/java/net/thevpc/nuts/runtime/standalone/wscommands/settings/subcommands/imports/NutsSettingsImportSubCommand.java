/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.wscommands.settings.subcommands.imports;

import net.thevpc.nuts.NutsCommandLine;
import net.thevpc.nuts.NutsCommandLineManager;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.runtime.standalone.wscommands.settings.subcommands.AbstractNutsSettingsSubCommand;

/**
 *
 * @author thevpc
 */
public class NutsSettingsImportSubCommand extends AbstractNutsSettingsSubCommand {

    @Override
    public boolean exec(NutsCommandLine cmdLine, Boolean autoSave, NutsSession session) {
        NutsCommandLineManager commandLineFormat = session.getWorkspace().commandLine();
        if (cmdLine.next("list imports", "li") != null) {
            cmdLine.setCommandName("config list imports").unexpectedArgument();
            if (cmdLine.isExecMode()) {
                for (String imp : (session.getWorkspace().imports().getAll())) {
                    session.out().printf("%s%n", imp);
                }
            }
            return true;
        } else if (cmdLine.next("clear imports", "ci") != null) {
            cmdLine.setCommandName("config clear imports").unexpectedArgument();
            if (cmdLine.isExecMode()) {
                session.getWorkspace().imports().removeAll();
                session.getWorkspace().config().save();
            }
            return true;
        } else if (cmdLine.next("import", "ia") != null) {
            do {
                String a = cmdLine.required().nextNonOption(commandLineFormat.createName("import")).getString();
                if (cmdLine.isExecMode()) {
                    session.getWorkspace().imports().add(new String[]{a});
                }
            } while (cmdLine.hasNext());
            if (cmdLine.isExecMode()) {
                session.getWorkspace().config().save();
            }
            return true;
        } else if (cmdLine.next("unimport", "ir") != null) {
            while (cmdLine.hasNext()) {
                String ii = cmdLine.required().nextNonOption(commandLineFormat.createName("import")).getString();
                if (cmdLine.isExecMode()) {
                    session.getWorkspace().imports().remove(new String[]{ii});
                }
            }
            if (cmdLine.isExecMode()) {
                session.getWorkspace().config().save();
            }
            return true;
        }
        return false;
    }

}
