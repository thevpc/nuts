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

/**
 *
 * @author vpc
 */
public class LogNAdminSubCommand extends AbstractNAdminSubCommand {

    @Override
    public boolean exec(NutsCommandLine cmdLine, NAdminMain config, Boolean autoSave, NutsApplicationContext context) {
        if (cmdLine.readAll("set loglevel", "sll")) {
            NutsWorkspaceConfigManager configManager = context.getWorkspace().config();
            if (cmdLine.readAll("verbose", "finest")) {
                if (cmdLine.isExecMode()) {
                    configManager.setLogLevel(Level.FINEST);
                }
            } else if (cmdLine.readAll("fine")) {
                if (cmdLine.isExecMode()) {
                    configManager.setLogLevel(Level.FINE);
                }
            } else if (cmdLine.readAll("finer")) {
                if (cmdLine.isExecMode()) {
                    configManager.setLogLevel(Level.FINER);
                }
            } else if (cmdLine.readAll("info")) {
                if (cmdLine.isExecMode()) {
                    configManager.setLogLevel(Level.INFO);
                }
            } else if (cmdLine.readAll("warning")) {
                if (cmdLine.isExecMode()) {
                    configManager.setLogLevel(Level.WARNING);
                }
            } else if (cmdLine.readAll("severe", "error")) {
                if (cmdLine.isExecMode()) {
                    configManager.setLogLevel(Level.SEVERE);
                }
            } else if (cmdLine.readAll("config")) {
                if (cmdLine.isExecMode()) {
                    configManager.setLogLevel(Level.CONFIG);
                }
            } else if (cmdLine.readAll("off")) {
                if (cmdLine.isExecMode()) {
                    configManager.setLogLevel(Level.OFF);
                }
            } else if (cmdLine.readAll("all")) {
                if (cmdLine.isExecMode()) {
                    configManager.setLogLevel(Level.ALL);
                }
            } else {
                if (cmdLine.isExecMode()) {
                    throw new NutsIllegalArgumentException("Invalid loglevel");
                }
            }
            cmdLine.setCommandName("config log").unexpectedArgument();
            return true;
        } else if (cmdLine.readAll("get loglevel")) {
            if (cmdLine.isExecMode()) {
                Logger rootLogger = Logger.getLogger("");
                context.getTerminal().fout().printf("%s%n", rootLogger.getLevel().toString());
            }
        }
        return false;
    }

    @Override
    public int getSupportLevel(Object criteria) {
        return DEFAULT_SUPPORT;
    }

}
