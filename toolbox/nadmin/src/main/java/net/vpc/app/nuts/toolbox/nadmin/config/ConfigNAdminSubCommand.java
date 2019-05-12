/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.toolbox.nadmin.config;

import net.vpc.app.nuts.NutsQuestion;
import net.vpc.app.nuts.NutsRepository;
import net.vpc.app.nuts.NutsStoreLocation;
import net.vpc.app.nuts.toolbox.nadmin.NAdminMain;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import net.vpc.app.nuts.NutsApplicationContext;
import net.vpc.app.nuts.NutsCommandLine;
import net.vpc.app.nuts.NutsArgument;

/**
 *
 * @author vpc
 */
public class ConfigNAdminSubCommand extends AbstractNAdminSubCommand {

    @Override
    public boolean exec(NutsCommandLine cmdLine, NAdminMain config, Boolean autoSave, NutsApplicationContext context) {
        String name = "nadmin config";
        NutsArgument a;
        if (cmdLine.readAll("delete log")) {
            deleteLog(context, readForce(cmdLine, name));
            return true;
        } else if (cmdLine.readAll("delete var")) {
            deleteVar(context, readForce(cmdLine, name));
            return true;
        } else if (cmdLine.readAll("delete programs")) {
            deletePrograms(context, readForce(cmdLine, name));
            return true;
        } else if (cmdLine.readAll("delete config")) {
            deleteConfig(context, readForce(cmdLine, name));
            return true;
        } else if (cmdLine.readAll("delete cache")) {
            deleteCache(context, readForce(cmdLine, name));
            return true;
        } else if (cmdLine.readAll("cleanup")) {
            boolean force = readForce(cmdLine, name);
            deleteCache(context, force);
            deleteLog(context, force);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public int getSupportLevel(Object criteria) {
        return DEFAULT_SUPPORT;
    }

    private void deleteLog(NutsApplicationContext context, boolean force) {
        deleteFolder(context, force, NutsStoreLocation.LOGS);
    }

    private void deleteVar(NutsApplicationContext context, boolean force) {
        deleteFolder(context, force, NutsStoreLocation.VAR);
    }

    private void deletePrograms(NutsApplicationContext context, boolean force) {
        deleteFolder(context, force, NutsStoreLocation.PROGRAMS);
    }

    private void deleteConfig(NutsApplicationContext context, boolean force) {
        deleteFolder(context, force, NutsStoreLocation.CONFIG);
    }

    private void deleteFolder(NutsApplicationContext context, boolean force, NutsStoreLocation folder) {
        deleteFolder(context, context.getWorkspace().config().getStoreLocation(folder), folder.name().toLowerCase(), force);
    }

    private void deleteFolder(NutsApplicationContext context, Path storeLocation, String name, boolean force) {
        if (storeLocation != null) {
            if (Files.exists(storeLocation)) {
                context.out().printf("@@Deleting@@ ##%s## folder %s ...%n", name, storeLocation);
                if (force
                        || context.getWorkspace().config().getOptions().isYes()
                        || context.getTerminal().ask(NutsQuestion.forBoolean("Force Delete").setDefautValue(false))) {
                    try {
                        Files.delete(storeLocation);
                    } catch (IOException ex) {
                        throw new UncheckedIOException(ex);
                    }
                }
            }
        }
    }

    private void deleteCache(NutsApplicationContext context, boolean force) {
        Path storeLocation = context.getWorkspace().config().getStoreLocation(NutsStoreLocation.CACHE);
        if (storeLocation != null) {
//            File cache = new File(storeLocation);
            if (Files.exists(storeLocation)) {
                try {
                    Files.delete(storeLocation);
                } catch (IOException ex) {
                    throw new UncheckedIOException(ex);
                }
            }
            for (NutsRepository repository : context.getWorkspace().config().getRepositories()) {
                deleteRepoCache(repository, context, force);
            }
        }
    }

    private static void deleteRepoCache(NutsRepository repository, NutsApplicationContext context, boolean force) {
        Path s = repository.config().getStoreLocation(NutsStoreLocation.CACHE);
        if (s != null) {
            if (Files.exists(s)) {
                context.out().printf("@@Deleting@@ ##cache## folder %s ...%n", s);
                if (force
                        || context.getWorkspace().config().getOptions().isYes()
                        || context.getTerminal().ask(NutsQuestion.forBoolean("Force Delete").setDefautValue(false))) {
                    try {
                        Files.delete(s);
                    } catch (IOException ex) {
                        throw new UncheckedIOException(ex);
                    }
                }
            }
        }
        if (repository.config().isSupportedMirroring()) {
            for (NutsRepository mirror : repository.config().getMirrors()) {
                deleteRepoCache(mirror, context, force);
            }
        }
    }

    private boolean readForce(NutsCommandLine cmdLine, String name) {
        boolean force = false;
        NutsArgument a;
        while (cmdLine.hasNext()) {
            if ((a = cmdLine.readBooleanOption("-f", "--force")) != null) {
                force = a.getBooleanValue();
            } else {
                cmdLine.setCommandName(name).unexpectedArgument();
            }
        }
        return force;
    }
}
