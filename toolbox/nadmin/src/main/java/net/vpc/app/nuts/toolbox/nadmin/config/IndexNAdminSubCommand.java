/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.toolbox.nadmin.config;

import java.nio.file.Paths;
import net.vpc.app.nuts.app.NutsApplicationContext;
import net.vpc.app.nuts.toolbox.nadmin.NAdminMain;
import net.vpc.common.commandline.Argument;
import net.vpc.common.commandline.CommandLine;

import java.util.ArrayList;
import java.util.List;
import net.vpc.app.nuts.NutsRepository;
import net.vpc.app.nuts.NutsWorkspaceConfigManager;

/**
 * @author vpc
 */
public class IndexNAdminSubCommand extends AbstractNAdminSubCommand {

    @Override
    public boolean exec(CommandLine cmdLine, NAdminMain config, Boolean autoSave, NutsApplicationContext context) {
        String name = "nadmin update stats";
        Argument a;
        if (cmdLine.readAll("update stats")) {
            List<String> repos = new ArrayList<>();
            while (cmdLine.hasNext()) {
                repos.add(cmdLine.read().getExpression());
            }
            updateStatistics(context, repos.toArray(new String[0]));
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

    private void updateStatistics(NutsApplicationContext context, String[] repos) {
        NutsWorkspaceConfigManager cfg = context.getWorkspace().config();
        if (repos.length == 0) {
            context.out().printf("[[%s]] Updating all indices\n", cfg.getWorkspaceLocation());
            for (NutsRepository repo : cfg.getRepositories()) {
                context.out().printf("[[%s]] Updating index %s\n", cfg.getWorkspaceLocation(), repo);
                repo.updateStatistics();
            }
        } else {
            for (String repo : repos) {
                context.out().printf("[[%s]] Updating index %s\n", cfg.getWorkspaceLocation(), repo);
                if (repo.contains("/") || repo.contains("\\")) {
                    cfg.updateStatistics(Paths.get(repo));
                } else {
                    cfg.getRepository(repo).updateStatistics();
                }
            }
        }
    }
}
