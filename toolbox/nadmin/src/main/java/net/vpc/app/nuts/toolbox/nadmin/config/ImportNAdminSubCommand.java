/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.toolbox.nadmin.config;

import net.vpc.app.nuts.NutsApplicationContext;
import net.vpc.app.nuts.NutsCommandLine;
import net.vpc.app.nuts.toolbox.nadmin.NAdminMain;

/**
 *
 * @author vpc
 */
public class ImportNAdminSubCommand extends AbstractNAdminSubCommand {

    @Override
    public boolean exec(NutsCommandLine cmdLine, NAdminMain config, Boolean autoSave, NutsApplicationContext context) {
        if (cmdLine.readAll("list imports", "li")) {
            cmdLine.unexpectedArgument("config list imports");
            if (cmdLine.isExecMode()) {
                for (String imp : (context.getWorkspace().config().getImports())) {
                    context.getTerminal().fout().printf("%s%n", imp);
                }
            }
            return true;
        } else if (cmdLine.readAll("clear imports", "ci")) {
            cmdLine.unexpectedArgument("config clear imports");
            if (cmdLine.isExecMode()) {
                context.getWorkspace().config().removeAllImports();
                trySave(context, context.getWorkspace(), null, autoSave, cmdLine);
            }
            return true;
        } else if (cmdLine.readAll("import", "ia")) {
            do {
                String a = cmdLine.readRequiredNonOption(cmdLine.createNonOption("import")).getString();
                if (cmdLine.isExecMode()) {
                    context.getWorkspace().config().addImports(a);
                }
            } while (cmdLine.hasNext());
            if (cmdLine.isExecMode()) {
                trySave(context, context.getWorkspace(), null, autoSave, cmdLine);
            }
            return true;
        } else if (cmdLine.readAll("unimport", "ir")) {
            while (cmdLine.hasNext()) {
                String ii = cmdLine.readRequiredNonOption(cmdLine.createNonOption("import")).getString();
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
