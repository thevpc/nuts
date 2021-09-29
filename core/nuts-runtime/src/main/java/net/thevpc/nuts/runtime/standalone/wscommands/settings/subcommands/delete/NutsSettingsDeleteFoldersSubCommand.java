/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.wscommands.settings.subcommands.delete;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.wscommands.settings.subcommands.AbstractNutsSettingsSubCommand;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

/**
 * @author thevpc
 */
public class NutsSettingsDeleteFoldersSubCommand extends AbstractNutsSettingsSubCommand {

    @Override
    public boolean exec(NutsCommandLine cmdLine, Boolean autoSave, NutsSession session) {
        for (NutsStoreLocation value : NutsStoreLocation.values()) {
            String cmdName = "delete " + value.id();
            cmdLine.setCommandName("settings " + cmdName);
            if (cmdLine.next(cmdName) != null) {
                boolean force = false;
                Set<NutsStoreLocation> locationsToDelete = new HashSet<>();
                locationsToDelete.add(value);
                while (cmdLine.hasNext()) {
                    NutsArgument a;
                    if ((a = cmdLine.nextBoolean("-y", "--yes")) != null) {
                        force = a.getValue().getBoolean();
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
                        deleteWorkspaceFolder(session, folder, force);
                    }
                }
                return true;
            }
        }
        return false;
    }

    private void deleteWorkspaceFolder(NutsSession session, NutsStoreLocation folder, boolean force) {
        String sstoreLocation = session.locations().getStoreLocation(folder);
        if (sstoreLocation != null) {
            NutsTextManager factory = session.text();
            Path storeLocation = Paths.get(sstoreLocation);
            if (Files.exists(storeLocation)) {
                session.out().printf("```error deleting``` %s for workspace %s folder %s ...%n",
                        factory.forStyled(folder.id(),NutsTextStyle.primary1()),
                        factory.forStyled(session.getWorkspace().getName(),NutsTextStyle.primary1()),
                        factory.forStyled(storeLocation.toString(),NutsTextStyle.path()));
                if (force
                        || session.getTerminal().ask()
                        .resetLine()
                        .forBoolean("force delete?").setDefaultValue(false).setSession(session)
                        .getBooleanValue()) {
                    try {
                        Files.delete(storeLocation);
                    } catch (IOException ex) {
                        throw new UncheckedIOException(ex);
                    }
                }
            }
        }
        for (NutsRepository repository : session.repos().getRepositories()) {
            deleteRepoFolder(repository, session, folder, force);
        }
    }

    private void deleteRepoFolder(NutsRepository repository, NutsSession session, NutsStoreLocation folder, boolean force) {
        String sstoreLocation = session.locations().getStoreLocation(folder);
        if (sstoreLocation != null) {
            Path storeLocation=Paths.get(sstoreLocation);
            NutsTextManager factory = session.text();
            if (Files.exists(storeLocation)) {
                session.out().printf("```error deleting``` %s for repository %s folder %s ...%n",
                        factory.forStyled(folder.id(),NutsTextStyle.primary1()),
                        factory.forStyled(repository.getName(),NutsTextStyle.primary1()),
                        factory.forStyled(storeLocation.toString(),NutsTextStyle.path()));
                if (force
                        || session.getTerminal().ask()
                        .resetLine()
                        .forBoolean("Force Delete?").setDefaultValue(false).setSession(session)
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
            for (NutsRepository subRepository : repository.config().getMirrors()) {
                deleteRepoCache(subRepository, session, force);
            }
        }
    }

    private void deleteCache(NutsSession session, boolean force) {
        String sstoreLocation = session.locations().getStoreLocation(NutsStoreLocation.CACHE);
        if (sstoreLocation != null) {
            Path storeLocation = Paths.get(sstoreLocation);
//            File cache = new File(storeLocation);
            if (Files.exists(storeLocation)) {
                try {
                    Files.delete(storeLocation);
                } catch (IOException ex) {
                    throw new UncheckedIOException(ex);
                }
            }
            for (NutsRepository repository : session.repos().getRepositories()) {
                deleteRepoCache(repository, session, force);
            }
        }
    }

    private static void deleteRepoCache(NutsRepository repository, NutsSession session, boolean force) {
        Path s = Paths.get(repository.config().getStoreLocation(NutsStoreLocation.CACHE));
        if (s != null) {
            if (Files.exists(s)) {
                session.out().printf("```error deleting``` %s folder %s ...%n",
                        session.text().forStyled("cache",NutsTextStyle.primary1())
                        ,s);
                if (force
                        || session.getTerminal().ask()
                        .resetLine()
                        .forBoolean("force delete?").setDefaultValue(false)
                        .setSession(session).getBooleanValue()) {
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
                deleteRepoCache(mirror, session, force);
            }
        }
    }
}
