/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.nadmin.config;

import net.thevpc.nuts.toolbox.nadmin.NAdminSubCommand;
import net.thevpc.nuts.*;

import java.io.PrintStream;

/**
 *
 * @author thevpc
 */
public abstract class AbstractNAdminSubCommand implements NAdminSubCommand {

    public static boolean trySave(NutsApplicationContext context, NutsWorkspace workspace, NutsRepository repository, Boolean save, NutsCommandLine cmdLine) {
        if (save == null) {
            if (cmdLine == null || cmdLine.isExecMode()) {
                if (repository != null) {
                    save = Boolean.parseBoolean(repository.env().get("autosave", "false"));
                } else {
                    save = Boolean.parseBoolean(context.getWorkspace().env().get("autosave", "false"));
                }
            } else {
                save = false;
            }
        } else {
            save = false;
        }
        if (cmdLine != null) {
            while (cmdLine.hasNext()) {
                if (cmdLine.next("--save") != null) {
                    save = true;
                } else {
                    cmdLine.setCommandName("config").unexpectedArgument();
                }
            }
        }
        //always save  in admin command!
        save = true;
        if (save) {
            if (cmdLine == null || cmdLine.isExecMode()) {
                PrintStream out = context.getSession().out();
                if (repository == null) {
                    workspace.config().save(false, context.getSession());
                    //out.print("##workspace saved.##\n");
                } else {
                    //out.printf("##repository %s saved.##%n", repository.config().getName());
                    repository.config().save(context.getSession());
                }
            }
        }
        return save;
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext<Object> criteria) {
        return DEFAULT_SUPPORT;
    }
}
