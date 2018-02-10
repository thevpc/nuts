/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.extensions.cmd.config;

import java.util.logging.Level;
import java.util.logging.Logger;
import net.vpc.app.nuts.NutsCommandContext;
import net.vpc.app.nuts.NutsIllegalArgumentsException;
import net.vpc.app.nuts.extensions.cmd.AbstractConfigSubCommand;
import net.vpc.app.nuts.extensions.cmd.ConfigCommand;
import net.vpc.app.nuts.extensions.cmd.cmdline.CmdLine;
import net.vpc.app.nuts.extensions.util.CoreLogUtils;

/**
 *
 * @author vpc
 */
public class LogConfigSubCommand extends AbstractConfigSubCommand {

    @Override
    public boolean exec(CmdLine cmdLine, ConfigCommand config, Boolean autoSave, NutsCommandContext context) {
        if (cmdLine.read("set loglevel", "sll")) {
            if (cmdLine.read("verbose", "finest")) {
                if (cmdLine.isExecMode()) {
                    CoreLogUtils.setLevel(Level.FINEST);
                }
            } else if (cmdLine.read("fine")) {
                if (cmdLine.isExecMode()) {
                    CoreLogUtils.setLevel(Level.FINE);
                }
            } else if (cmdLine.read("finer")) {
                if (cmdLine.isExecMode()) {
                    CoreLogUtils.setLevel(Level.FINER);
                }
            } else if (cmdLine.read("info")) {
                if (cmdLine.isExecMode()) {
                    CoreLogUtils.setLevel(Level.INFO);
                }
            } else if (cmdLine.read("warning")) {
                if (cmdLine.isExecMode()) {
                    CoreLogUtils.setLevel(Level.WARNING);
                }
            } else if (cmdLine.read("severe", "error")) {
                if (cmdLine.isExecMode()) {
                    CoreLogUtils.setLevel(Level.SEVERE);
                }
            } else if (cmdLine.read("config")) {
                if (cmdLine.isExecMode()) {
                    CoreLogUtils.setLevel(Level.CONFIG);
                }
            } else if (cmdLine.read("off")) {
                if (cmdLine.isExecMode()) {
                    CoreLogUtils.setLevel(Level.OFF);
                }
            } else if (cmdLine.read("all")) {
                if (cmdLine.isExecMode()) {
                    CoreLogUtils.setLevel(Level.ALL);
                }
            } else {
                if (cmdLine.isExecMode()) {
                    throw new NutsIllegalArgumentsException("Invalid loglevel");
                }
            }
            cmdLine.requireEmpty();
            return true;
        } else if (cmdLine.read("get loglevel")) {
            if (cmdLine.isExecMode()) {
                Logger rootLogger = Logger.getLogger("");
                System.out.println(rootLogger.getLevel().toString());
            }
        }
        return false;
    }

    @Override
    public int getSupportLevel(Object criteria) {
        return CORE_SUPPORT;
    }

}
