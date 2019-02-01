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
            deleteLog(context,readForce(cmdLine,name));
            return true;
        } else if (cmdLine.readAll("delete var")) {
            deleteVar(context,readForce(cmdLine,name));
            return true;
        } else if (cmdLine.readAll("delete programs")) {
            deletePrograms(context,readForce(cmdLine,name));
            return true;
        } else if (cmdLine.readAll("delete config")) {
            deleteConfig(context,readForce(cmdLine,name));
            return true;
        } else if (cmdLine.readAll("delete cache")) {
            deleteCache(context,readForce(cmdLine,name));
            return true;
        } else if (cmdLine.readAll("cleanup")) {
            boolean force = readForce(cmdLine, name);
            deleteCache(context, force);
            deleteLog(context,force);
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
        deleteFolder(context,force,NutsStoreFolder.LOGS);
    }

    private void deleteVar(NutsApplicationContext context, boolean force) {
        deleteFolder(context, force,NutsStoreFolder.VAR);
    }

    private void deletePrograms(NutsApplicationContext context, boolean force) {
        deleteFolder(context, force,NutsStoreFolder.PROGRAMS);
    }

    private void deleteConfig(NutsApplicationContext context, boolean force) {
        deleteFolder(context, force,NutsStoreFolder.CONFIG);
    }

    private void deleteFolder(NutsApplicationContext context, boolean force,NutsStoreFolder folder) {
        deleteFolder(context,context.getWorkspace().getConfigManager().getStoreLocation(folder),folder.name().toLowerCase(),force);
    }

    private void deleteFolder(NutsApplicationContext context, String storeLocation, String name,boolean force) {
        if(storeLocation!=null) {
            File file = new File(storeLocation);
            if (file.exists()) {
                context.out().printf("@@Deleting@@ ##%s## folder %s ...\n", name,file.getPath());
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
        String s = repository.getStoreLocation(NutsStoreFolder.CACHE);
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
    private boolean readForce(CommandLine cmdLine,String name){
        boolean force=false;
        Argument a;
        while(cmdLine.hasNext()) {
            if ((a = cmdLine.readBooleanOption("-f","--force")) != null) {
                force=a.getBooleanValue();
            }else{
                cmdLine.unexpectedArgument(name);
            }
        }
        return force;
    }
}
