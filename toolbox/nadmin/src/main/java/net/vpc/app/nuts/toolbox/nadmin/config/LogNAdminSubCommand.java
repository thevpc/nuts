/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.toolbox.nadmin.config;

import net.vpc.app.nuts.NutsIllegalArgumentException;
import net.vpc.app.nuts.NutsWorkspaceConfigManager;
import net.vpc.app.nuts.toolbox.nadmin.NAdminMain;

import java.util.logging.Level;
import java.util.logging.Logger;
import net.vpc.app.nuts.NutsApplicationContext;
import net.vpc.app.nuts.NutsCommandLine;
import net.vpc.app.nuts.NutsSupportLevelContext;

/**
 *
 * @author vpc
 */
public class LogNAdminSubCommand extends AbstractNAdminSubCommand {

    @Override
    public boolean exec(NutsCommandLine cmdLine, NAdminMain config, Boolean autoSave, NutsApplicationContext context) {
        if (cmdLine.next("set loglevel", "sll") != null) {
            NutsWorkspaceConfigManager configManager = context.getWorkspace().config();
            if (cmdLine.next("verbose", "finest") != null) {
                if (cmdLine.isExecMode()) {
                    configManager.setLogLevel(Level.FINEST);
                }
            } else if (cmdLine.next("fine") != null) {
                if (cmdLine.isExecMode()) {
                    configManager.setLogLevel(Level.FINE);
                }
            } else if (cmdLine.next("finer") != null) {
                if (cmdLine.isExecMode()) {
                    configManager.setLogLevel(Level.FINER);
                }
            } else if (cmdLine.next("info") != null) {
                if (cmdLine.isExecMode()) {
                    configManager.setLogLevel(Level.INFO);
                }
            } else if (cmdLine.next("warning") != null) {
                if (cmdLine.isExecMode()) {
                    configManager.setLogLevel(Level.WARNING);
                }
            } else if (cmdLine.next("severe", "error") != null) {
                if (cmdLine.isExecMode()) {
                    configManager.setLogLevel(Level.SEVERE);
                }
            } else if (cmdLine.next("config") != null) {
                if (cmdLine.isExecMode()) {
                    configManager.setLogLevel(Level.CONFIG);
                }
            } else if (cmdLine.next("off") != null) {
                if (cmdLine.isExecMode()) {
                    configManager.setLogLevel(Level.OFF);
                }
            } else if (cmdLine.next("all") != null) {
                if (cmdLine.isExecMode()) {
                    configManager.setLogLevel(Level.ALL);
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

    @Override
    public int getSupportLevel(NutsSupportLevelContext<Object> criteria) {
        return DEFAULT_SUPPORT;
    }

}
