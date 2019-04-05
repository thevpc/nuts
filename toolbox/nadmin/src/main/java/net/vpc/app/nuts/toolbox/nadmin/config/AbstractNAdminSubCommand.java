/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.toolbox.nadmin.config;

import net.vpc.app.nuts.NutsRepository;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.app.NutsApplicationContext;
import net.vpc.app.nuts.toolbox.nadmin.NAdminSubCommand;
import net.vpc.common.commandline.CommandLine;

import java.io.PrintStream;

/**
 *
 * @author vpc
 */
public abstract class AbstractNAdminSubCommand implements NAdminSubCommand {
    public static boolean trySave(NutsApplicationContext context, NutsWorkspace workspace, NutsRepository repository, Boolean save, CommandLine cmdLine) {
        if (save == null) {
            if (cmdLine == null || cmdLine.isExecMode()) {
                if (repository != null) {
                    save = Boolean.parseBoolean(repository.config().getEnv("autosave", "false", true));
                } else {
                    save = Boolean.parseBoolean(context.getWorkspace().config().getEnv("autosave", "false"));
                }
            } else {
                save = false;
            }
        } else {
            save = false;
        }
        if (cmdLine != null) {
            while (cmdLine.hasNext()) {
                if (cmdLine.readAll("--save")) {
                    save = true;
                } else {
                    cmdLine.unexpectedArgument("config");
                }
            }
        }
        //always save  in admin command!
        save=true;
        if (save) {
            if (cmdLine == null || cmdLine.isExecMode()) {
                PrintStream out = context.out();
                if (repository == null) {
                    workspace.config().save(false);
                    out.print("##workspace saved.##\n");
                } else {
                    out.printf("##repository %s saved.##\n", repository.getName());
                    repository.config().save();
                }
            }
        }
        return save;
    }

}
