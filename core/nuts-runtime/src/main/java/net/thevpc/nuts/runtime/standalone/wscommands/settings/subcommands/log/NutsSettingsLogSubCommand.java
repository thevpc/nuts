/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.wscommands.settings.subcommands.log;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.wscommands.settings.subcommands.AbstractNutsSettingsSubCommand;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author thevpc
 */
public class NutsSettingsLogSubCommand extends AbstractNutsSettingsSubCommand {

    @Override
    public boolean exec(NutsCommandLine cmdLine, Boolean autoSave, NutsSession session) {
        if (cmdLine.next("set loglevel", "sll") != null) {
//            NutsWorkspaceConfigManager configManager = context.getWorkspace().config();
            if (cmdLine.next("verbose", "finest") != null) {
                if (cmdLine.isExecMode()) {
                    NutsLogger.setTermLevel(Level.FINEST,session);
                }
            } else if (cmdLine.next("fine") != null) {
                if (cmdLine.isExecMode()) {
                    NutsLogger.setTermLevel(Level.FINE,session);
                }
            } else if (cmdLine.next("finer") != null) {
                if (cmdLine.isExecMode()) {
                    NutsLogger.setTermLevel(Level.FINER,session);
                }
            } else if (cmdLine.next("info") != null) {
                if (cmdLine.isExecMode()) {
                    NutsLogger.setTermLevel(Level.INFO,session);
                }
            } else if (cmdLine.next("warning") != null) {
                if (cmdLine.isExecMode()) {
                    NutsLogger.setTermLevel(Level.WARNING,session);
                }
            } else if (cmdLine.next("severe", "error") != null) {
                if (cmdLine.isExecMode()) {
                    NutsLogger.setTermLevel(Level.SEVERE,session);
                }
            } else if (cmdLine.next("config") != null) {
                if (cmdLine.isExecMode()) {
                    NutsLogger.setTermLevel(Level.CONFIG,session);
                }
            } else if (cmdLine.next("off") != null) {
                if (cmdLine.isExecMode()) {
                    NutsLogger.setTermLevel(Level.OFF,session);
                }
            } else if (cmdLine.next("all") != null) {
                if (cmdLine.isExecMode()) {
                    NutsLogger.setTermLevel(Level.ALL,session);
                }
            } else {
                if (cmdLine.isExecMode()) {
                    throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("invalid loglevel"));
                }
            }
            cmdLine.setCommandName("config log").unexpectedArgument();
            return true;
        } else if (cmdLine.next("get loglevel") != null) {
            if (cmdLine.isExecMode()) {
                Logger rootLogger = Logger.getLogger("");
                session.out().printf("%s%n", rootLogger.getLevel().toString());
            }
        }
        return false;
    }

}
