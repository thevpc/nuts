/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.toolbox.nadmin.config;

import net.vpc.app.nuts.NutsQuestion;
import net.vpc.app.nuts.NutsRepository;
import net.vpc.app.nuts.NutsStoreFolder;
import net.vpc.app.nuts.app.NutsApplicationContext;
import net.vpc.app.nuts.toolbox.nadmin.NAdminMain;
import net.vpc.common.commandline.Argument;
import net.vpc.common.commandline.CommandLine;
import net.vpc.common.io.IOUtils;

import java.io.File;

/**
 *
 * @author vpc
 */
public class ConfigNAdminSubCommand extends AbstractNAdminSubCommand {

    @Override
    public boolean exec(CommandLine cmdLine, NAdminMain config, Boolean autoSave, NutsApplicationContext context) {
        String name="nadmin config";
        Argument a;
        if (cmdLine.readAll("delete log")) {
            boolean force=false;
            while(cmdLine.hasNext()) {
                if ((a = cmdLine.readBooleanOption("-f","--force")) != null) {
                    force=a.getBooleanValue();
                }else{
                    cmdLine.unexpectedArgument(name);
                }
            }
            deleteLog(context,force);
            return true;
        } else if (cmdLine.readAll("delete var")) {
            boolean force=false;
            while(cmdLine.hasNext()) {
                if ((a = cmdLine.readBooleanOption("-f","--force")) != null) {
                    force=a.getBooleanValue();
                }else{
                    cmdLine.unexpectedArgument(name);
                }
            }
            deleteVar(context,force);
            cmdLine.unexpectedArgument(name);
            return true;
        } else if (cmdLine.readAll("delete programs")) {
            boolean force=false;
            while(cmdLine.hasNext()) {
                if ((a = cmdLine.readBooleanOption("-f","--force")) != null) {
                    force=a.getBooleanValue();
                }else{
                    cmdLine.unexpectedArgument(name);
                }
            }
            deletePrgrams(context,force);
            cmdLine.unexpectedArgument(name);
            return true;
        } else if (cmdLine.readAll("delete config")) {
            boolean force=false;
            while(cmdLine.hasNext()) {
                if ((a = cmdLine.readBooleanOption("-f","--force")) != null) {
                    force=a.getBooleanValue();
                }else{
                    cmdLine.unexpectedArgument(name);
                }
            }
            deleteConfig(context,force);
            cmdLine.unexpectedArgument(name);
            return true;
        } else if (cmdLine.readAll("delete-cache")) {
            boolean force=false;
            while(cmdLine.hasNext()) {
                if ((a = cmdLine.readBooleanOption("-f","--force")) != null) {
                    force=a.getBooleanValue();
                }else{
                    cmdLine.unexpectedArgument(name);
                }
            }
            cmdLine.unexpectedArgument(name);
            return true;
        } else if (cmdLine.readAll("cleanup")) {
            boolean force=false;
            while(cmdLine.hasNext()) {
                if ((a = cmdLine.readBooleanOption("-f","--force")) != null) {
                    force=a.getBooleanValue();
                }else{
                    cmdLine.unexpectedArgument(name);
                }
            }
            deleteCache(context,force);
            deleteLog(context,force);
            cmdLine.unexpectedArgument(name);
            return true;
        }else{
            return false;
        }
    }

    @Override
    public int getSupportLevel(Object criteria) {
        return DEFAULT_SUPPORT;
    }

    private void deleteLog(NutsApplicationContext context, boolean force) {
        String storeLocation = context.getWorkspace().getConfigManager().getStoreLocation(NutsStoreFolder.LOGS);
        if(storeLocation!=null) {
            File file = new File(storeLocation);
            if (file.exists()) {
                context.out().printf("@@Deleting@@ ##log## folder %s ...\n", file.getPath());
                if (force || context.getTerminal().ask(NutsQuestion.forBoolean("Force Delete ?").setDefautValue(false))) {
                    IOUtils.delete(file);
                }
            }
            file = new File(context.getWorkspace().getConfigManager().getHomeLocation(), "log");
            if (file.exists()) {
                context.out().printf("@@Deleting@@ ##log## folder %s ...\n", file.getPath());
                if (force || context.getTerminal().ask(NutsQuestion.forBoolean("Force Delete ?").setDefautValue(false))) {
                    IOUtils.delete(file);
                }
            }
        }
    }

    private void deleteVar(NutsApplicationContext context, boolean force) {
        String storeLocation = context.getWorkspace().getConfigManager().getStoreLocation(NutsStoreFolder.VAR);
        if(storeLocation!=null) {
            File file = new File(storeLocation);
            if (file.exists()) {
                context.out().printf("@@Deleting@@ ##var## folder %s ...\n", file.getPath());
                if (force || context.getTerminal().ask(NutsQuestion.forBoolean("Force Delete ?").setDefautValue(false))) {
                    IOUtils.delete(file);
                }
            }
        }
    }

    private void deletePrgrams(NutsApplicationContext context, boolean force) {
        String storeLocation = context.getWorkspace().getConfigManager().getStoreLocation(NutsStoreFolder.PROGRAMS);
        if(storeLocation!=null) {
            File file = new File(storeLocation);
            if (file.exists()) {
                context.out().printf("@@Deleting@@ ##programs## folder %s ...\n", file.getPath());
                if (force || context.getTerminal().ask(NutsQuestion.forBoolean("Force Delete ?").setDefautValue(false))) {
                    IOUtils.delete(file);
                }
            }
        }
    }

    private void deleteConfig(NutsApplicationContext context, boolean force) {
        String storeLocation = context.getWorkspace().getConfigManager().getStoreLocation(NutsStoreFolder.CONFIG);
        if(storeLocation!=null) {
            File file = new File(storeLocation);
            if (file.exists()) {
                context.out().printf("@@Deleting@@ ##config## folder %s ...\n", file.getPath());
                if (force || context.getTerminal().ask(NutsQuestion.forBoolean("Force Delete ?").setDefautValue(false))) {
                    IOUtils.delete(file);
                }
            }
        }
    }

    private void deleteCache(NutsApplicationContext context, boolean force) {
        String storeLocation = context.getWorkspace().getConfigManager().getStoreLocation(NutsStoreFolder.CACHE);
        if(storeLocation!=null) {
            File cache = new File(storeLocation);
            if (cache.exists()) {
                IOUtils.delete(cache);
            }
            for (NutsRepository repository : context.getWorkspace().getRepositoryManager().getRepositories()) {
                deleteRepoCache(repository, context, force);
            }
        }
    }

    private static void deleteRepoCache(NutsRepository repository, NutsApplicationContext context, boolean force){
        String s = repository.getStoreLocation();
        if(s!=null){
            File file = new File(s);
            if(file.exists()) {
                context.out().printf("@@Deleting@@ ##cache## folder %s ...\n", s);
                if (force || context.getTerminal().ask(NutsQuestion.forBoolean("Force Delete ?").setDefautValue(false))) {
                    IOUtils.delete(file);
                }
            }
        }
        for (NutsRepository mirror : repository.getMirrors()) {
            deleteRepoCache(mirror,context,force);
        }
    }

}
