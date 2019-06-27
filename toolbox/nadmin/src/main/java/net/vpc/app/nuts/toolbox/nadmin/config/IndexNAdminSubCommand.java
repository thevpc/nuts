/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.toolbox.nadmin.config;

import java.nio.file.Paths;
import net.vpc.app.nuts.toolbox.nadmin.NAdminMain;

import java.util.ArrayList;
import java.util.List;
import net.vpc.app.nuts.NutsApplicationContext;
import net.vpc.app.nuts.NutsRepository;
import net.vpc.app.nuts.NutsWorkspaceConfigManager;
import net.vpc.app.nuts.NutsArgument;
import net.vpc.app.nuts.NutsCommandLine;
import net.vpc.app.nuts.NutsSupportLevelContext;

/**
 * @author vpc
 */
public class IndexNAdminSubCommand extends AbstractNAdminSubCommand {

    @Override
    public boolean exec(NutsCommandLine cmdLine, NAdminMain config, Boolean autoSave, NutsApplicationContext context) {
        String name = "nadmin update stats";
        NutsArgument a;
        if (cmdLine.next("update stats") != null) {
            List<String> repos = new ArrayList<>();
            while (cmdLine.hasNext()) {
                repos.add(cmdLine.next().getString());
            }
            updateStatistics(context, repos.toArray(new String[0]));
            cmdLine.setCommandName(name).unexpectedArgument();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext<Object> criteria) {
        return DEFAULT_SUPPORT;
    }

    private void updateStatistics(NutsApplicationContext context, String[] repos) {
        NutsWorkspaceConfigManager cfg = context.getWorkspace().config();
        if (repos.length == 0) {
            context.session().out().printf("[[%s]] Updating all indices%n", cfg.getWorkspaceLocation());
            for (NutsRepository repo : cfg.getRepositories()) {
                context.session().out().printf("[[%s]] Updating index %s%n", cfg.getWorkspaceLocation(), repo);
                repo.updateStatistics();
            }
        } else {
            for (String repo : repos) {
                if (repo.equals(".") || repo.equals("..") || repo.contains("/") || repo.contains("\\")) {
                    context.getWorkspace().updateStatistics().addPath(Paths.get(repo)).run();
                } else {
                    cfg.getRepository(repo).updateStatistics();
                }
            }
        }
    }
}
