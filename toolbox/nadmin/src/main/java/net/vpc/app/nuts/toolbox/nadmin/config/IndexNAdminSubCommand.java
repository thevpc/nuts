/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.toolbox.nadmin.config;

import net.vpc.app.nuts.app.NutsApplicationContext;
import net.vpc.app.nuts.toolbox.nadmin.NAdminMain;
import net.vpc.common.commandline.Argument;
import net.vpc.common.commandline.CommandLine;

import java.util.ArrayList;
import java.util.List;
import net.vpc.app.nuts.NutsRepository;

/**
 * @author vpc
 */
public class IndexNAdminSubCommand extends AbstractNAdminSubCommand {

    @Override
    public boolean exec(CommandLine cmdLine, NAdminMain config, Boolean autoSave, NutsApplicationContext context) {
        String name = "nadmin index";
        Argument a;
        if (cmdLine.readAll("update index")) {
            List<String> repos = new ArrayList<>();
            while (cmdLine.hasNext()) {
                repos.add(cmdLine.read().getExpression());
            }
            updateIndex(context, repos.toArray(new String[0]));
            cmdLine.unexpectedArgument(name);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public int getSupportLevel(Object criteria) {
        return DEFAULT_SUPPORT;
    }

    private void updateIndex(NutsApplicationContext context, String[] repos) {
        if (repos.length == 0) {
            context.out().printf("[[%s]] Updating all indices\n", context.getWorkspace().config().getWorkspaceLocation());
            for (NutsRepository repo : context.getWorkspace().config().getRepositories()) {
                context.out().printf("[[%s]] Updating index %s\n", context.getWorkspace().config().getWorkspaceLocation(), repo);
                repo.updateStatistics();
            }
        } else {
            for (String repo : repos) {
                context.out().printf("[[%s]] Updating index %s\n", context.getWorkspace().config().getWorkspaceLocation(), repo);
                context.getWorkspace().config().getRepository(repo).updateStatistics();
            }
        }
    }
}
