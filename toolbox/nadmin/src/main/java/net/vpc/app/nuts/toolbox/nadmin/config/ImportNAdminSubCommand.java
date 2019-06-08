/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.toolbox.nadmin.config;

import net.vpc.app.nuts.NutsApplicationContext;
import net.vpc.app.nuts.toolbox.nadmin.NAdminMain;
import net.vpc.app.nuts.NutsCommandLine;

/**
 *
 * @author vpc
 */
public class ImportNAdminSubCommand extends AbstractNAdminSubCommand {

    @Override
    public boolean exec(NutsCommandLine cmdLine, NAdminMain config, Boolean autoSave, NutsApplicationContext context) {
        if (cmdLine.next("list imports", "li")!=null) {
            cmdLine.setCommandName("config list imports").unexpectedArgument();
            if (cmdLine.isExecMode()) {
                for (String imp : (context.getWorkspace().config().getImports())) {
                    context.session().getTerminal().fout().printf("%s%n", imp);
                }
            }
            return true;
        } else if (cmdLine.next("clear imports", "ci")!=null) {
            cmdLine.setCommandName("config clear imports").unexpectedArgument();
            if (cmdLine.isExecMode()) {
                context.getWorkspace().config().removeAllImports();
                trySave(context, context.getWorkspace(), null, autoSave, cmdLine);
            }
            return true;
        } else if (cmdLine.next("import", "ia")!=null) {
            do {
                String a = cmdLine.required().nextNonOption(cmdLine.createName("import")).getString();
                if (cmdLine.isExecMode()) {
                    context.getWorkspace().config().addImports(a);
                }
            } while (cmdLine.hasNext());
            if (cmdLine.isExecMode()) {
                trySave(context, context.getWorkspace(), null, autoSave, cmdLine);
            }
            return true;
        } else if (cmdLine.next("unimport", "ir")!=null) {
            while (cmdLine.hasNext()) {
                String ii = cmdLine.required().nextNonOption(cmdLine.createName("import")).getString();
                if (cmdLine.isExecMode()) {
                    context.getWorkspace().config().removeImports(ii);
                }
            }
            if (cmdLine.isExecMode()) {
                trySave(context, context.getWorkspace(), null, autoSave, cmdLine);
            }
            return true;
        }
        return false;
    }

    @Override
    public int getSupportLevel(Object criteria) {
        return DEFAULT_SUPPORT;
    }

}
