/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.toolbox.nadmin.config;

import net.vpc.app.nuts.*;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author vpc
 */
public class LogNAdminSubCommand extends AbstractNAdminSubCommand {

    @Override
    public boolean exec(NutsCommandLine cmdLine, Boolean autoSave, NutsApplicationContext context) {
        NutsUpdateOptions updateOptions = new NutsUpdateOptions().session(context.getSession());
        if (cmdLine.next("set loglevel", "sll") != null) {
            NutsWorkspaceConfigManager configManager = context.getWorkspace().config();
            if (cmdLine.next("verbose", "finest") != null) {
                if (cmdLine.isExecMode()) {
                    configManager.setLogLevel(Level.FINEST, updateOptions);
                }
            } else if (cmdLine.next("fine") != null) {
                if (cmdLine.isExecMode()) {
                    configManager.setLogLevel(Level.FINE, updateOptions);
                }
            } else if (cmdLine.next("finer") != null) {
                if (cmdLine.isExecMode()) {
                    configManager.setLogLevel(Level.FINER, updateOptions);
                }
            } else if (cmdLine.next("info") != null) {
                if (cmdLine.isExecMode()) {
                    configManager.setLogLevel(Level.INFO, updateOptions);
                }
            } else if (cmdLine.next("warning") != null) {
                if (cmdLine.isExecMode()) {
                    configManager.setLogLevel(Level.WARNING, updateOptions);
                }
            } else if (cmdLine.next("severe", "error") != null) {
                if (cmdLine.isExecMode()) {
                    configManager.setLogLevel(Level.SEVERE, updateOptions);
                }
            } else if (cmdLine.next("config") != null) {
                if (cmdLine.isExecMode()) {
                    configManager.setLogLevel(Level.CONFIG, updateOptions);
                }
            } else if (cmdLine.next("off") != null) {
                if (cmdLine.isExecMode()) {
                    configManager.setLogLevel(Level.OFF, updateOptions);
                }
            } else if (cmdLine.next("all") != null) {
                if (cmdLine.isExecMode()) {
                    configManager.setLogLevel(Level.ALL, updateOptions);
                }
            } else {
                if (cmdLine.isExecMode()) {
                    throw new NutsIllegalArgumentException(context.getWorkspace(), "Invalid loglevel");
                }
            }
            cmdLine.setCommandName("config log").unexpectedArgument();
            return true;
        } else if (cmdLine.next("get loglevel") != null) {
            if (cmdLine.isExecMode()) {
                Logger rootLogger = Logger.getLogger("");
                context.session().out().printf("%s%n", rootLogger.getLevel().toString());
            }
        }
        return false;
    }

}
