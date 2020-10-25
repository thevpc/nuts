/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.toolbox.nadmin.config;

import net.vpc.app.nuts.NutsRepository;
import net.vpc.app.nuts.NutsStoreLocation;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import net.vpc.app.nuts.NutsApplicationContext;
import net.vpc.app.nuts.NutsArgument;
import net.vpc.app.nuts.NutsCommandLine;

/**
 * @author vpc
 */
public class DeleteFoldersNAdminSubCommand extends AbstractNAdminSubCommand {

    @Override
    public boolean exec(NutsCommandLine cmdLine, Boolean autoSave, NutsApplicationContext context) {
        for (NutsStoreLocation value : NutsStoreLocation.values()) {
            String cmdName = "delete " + value.id();
            cmdLine.setCommandName("nadmin " + cmdName);
            if (cmdLine.next(cmdName) != null) {
                boolean force = false;
                Set<NutsStoreLocation> locationsToDelete = new HashSet<>();
                locationsToDelete.add(value);
                while (cmdLine.hasNext()) {
                    NutsArgument a;
                    if ((a = cmdLine.nextBoolean("-f", "--force")) != null) {
                        force = a.getBooleanValue();
                    } else if (!cmdLine.peek().isOption()) {
                        String s = cmdLine.peek().toString();
                        try {
                            locationsToDelete.add(NutsStoreLocation.valueOf(s.toUpperCase()));
                        } catch (Exception ex) {
                            cmdLine.unexpectedArgument();
                        }
                    } else {
                        cmdLine.unexpectedArgument();
                    }
                }
                if (cmdLine.isExecMode()) {
                    for (NutsStoreLocation folder : locationsToDelete) {
                        deleteWorkspaceFolder(context, folder, force);
                    }
                }
                return true;
            }
        }
        return false;
    }

    private void deleteWorkspaceFolder(NutsApplicationContext context, NutsStoreLocation folder, boolean force) {
        Path storeLocation = context.getWorkspace().config().getStoreLocation(folder);
        if (storeLocation != null) {
            if (Files.exists(storeLocation)) {
                context.getSession().out().printf("@@Deleting@@ ##%s## for workspace ##%s## folder %s ...%n", folder.id(), context.getWorkspace().name(), storeLocation);
                if (force
                        || context.getSession().getTerminal().ask()
                        .forBoolean("Force Delete?").setDefaultValue(false).setSession(context.getSession())
                        .getBooleanValue()) {
                    try {
                        Files.delete(storeLocation);
                    } catch (IOException ex) {
                        throw new UncheckedIOException(ex);
                    }
                }
            }
        }
        for (NutsRepository repository : context.getWorkspace().repos().getRepositories(context.getSession())) {
            deleteRepoFolder(repository, context, folder, force);
        }
    }

    private void deleteRepoFolder(NutsRepository repository, NutsApplicationContext context, NutsStoreLocation folder, boolean force) {
        Path storeLocation = context.getWorkspace().config().getStoreLocation(folder);
        if (storeLocation != null) {
            if (Files.exists(storeLocation)) {
                context.getSession().out().printf("@@Deleting@@ ##%s## for repository ##%s## folder %s ...%n", folder.id(), repository.getName(), storeLocation);
                if (force
                        || context.getSession().getTerminal().ask()
                        .forBoolean("Force Delete?").setDefaultValue(false).setSession(context.getSession())
                        .getBooleanValue()) {
                    try {
                        Files.delete(storeLocation);
                    } catch (IOException ex) {
                        throw new UncheckedIOException(ex);
                    }
                }
            }
        }
        if (repository.config().isSupportedMirroring()) {
            for (NutsRepository subRepository : repository.config().getMirrors(context.getSession())) {
                deleteRepoCache(subRepository, context, force);
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
            for (NutsRepository repository : context.getWorkspace().repos().getRepositories(context.getSession())) {
                deleteRepoCache(repository, context, force);
            }
        }
    }

    private static void deleteRepoCache(NutsRepository repository, NutsApplicationContext context, boolean force) {
        Path s = repository.config().getStoreLocation(NutsStoreLocation.CACHE);
        if (s != null) {
            if (Files.exists(s)) {
                context.getSession().out().printf("@@Deleting@@ ##cache## folder %s ...%n", s);
                if (force
                        || context.getSession().getTerminal().ask()
                        .forBoolean("Force Delete?").setDefaultValue(false)
                        .setSession(context.getSession()).getBooleanValue()) {
                    try {
                        Files.delete(s);
                    } catch (IOException ex) {
                        throw new UncheckedIOException(ex);
                    }
                }
            }
        }
        if (repository.config().isSupportedMirroring()) {
            for (NutsRepository mirror : repository.config().getMirrors(context.getSession())) {
                deleteRepoCache(mirror, context, force);
            }
        }
    }

    private boolean readForce(NutsCommandLine cmdLine, String name) {
        boolean force = false;
        NutsArgument a;
        while (cmdLine.hasNext()) {
            if ((a = cmdLine.nextBoolean("-f", "--force")) != null) {
                force = a.getBooleanValue();
            } else {
                cmdLine.setCommandName(name).unexpectedArgument();
            }
        }
        return force;
    }
}
