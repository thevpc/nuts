/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.delete;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NutsArgument;
import net.thevpc.nuts.cmdline.NutsCommandLine;
import net.thevpc.nuts.io.NutsPath;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.AbstractNutsSettingsSubCommand;
import net.thevpc.nuts.text.NutsTextStyle;
import net.thevpc.nuts.text.NutsTexts;

import java.util.HashSet;
import java.util.Set;

/**
 * @author thevpc
 */
public class NutsSettingsDeleteFoldersSubCommand extends AbstractNutsSettingsSubCommand {

    private static void deleteRepoCache(NutsRepository repository, NutsSession session, boolean force) {
        NutsPath s = repository.config().getStoreLocation(NutsStoreLocation.CACHE);
        if (s != null) {
            if (s.exists()) {
                session.out().printf("```error deleting``` %s folder %s ...%n",
                        NutsTexts.of(session).ofStyled("cache", NutsTextStyle.primary1())
                        , s);
                if (force
                        || session.getTerminal().ask()
                        .resetLine()
                        .forBoolean("force delete?").setDefaultValue(false)
                        .setSession(session).getBooleanValue()) {
                    s.delete();
                }
            }
        }
        if (repository.config().isSupportedMirroring()) {
            for (NutsRepository mirror : repository.config().getMirrors()) {
                deleteRepoCache(mirror, session, force);
            }
        }
    }

    @Override
    public boolean exec(NutsCommandLine cmdLine, Boolean autoSave, NutsSession session) {
        for (NutsStoreLocation value : NutsStoreLocation.values()) {
            String cmdName = "delete " + value.id();
            cmdLine.setCommandName("settings " + cmdName);
            if (cmdLine.next(cmdName).isPresent()) {
                boolean force = false;
                Set<NutsStoreLocation> locationsToDelete = new HashSet<>();
                locationsToDelete.add(value);
                while (cmdLine.hasNext()) {
                    NutsArgument a;
                    if ((a = cmdLine.nextBoolean("-y", "--yes").orNull()) != null) {
                        force = a.getBooleanValue().get(session);
                    } else if (!cmdLine.isNextOption()) {
                        String s = cmdLine.peek().get(session).asString().get();
                        try {
                            locationsToDelete.add(NutsStoreLocation.valueOf(s.toUpperCase()));
                        } catch (Exception ex) {
                            cmdLine.throwUnexpectedArgument(session);
                        }
                    } else {
                        cmdLine.throwUnexpectedArgument(session);
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
        NutsPath sstoreLocation = session.locations().getStoreLocation(folder);
        if (sstoreLocation != null) {
            NutsTexts factory = NutsTexts.of(session);
            if (sstoreLocation.exists()) {
                session.out().printf("```error deleting``` %s for workspace %s folder %s ...%n",
                        factory.ofStyled(folder.id(), NutsTextStyle.primary1()),
                        factory.ofStyled(session.getWorkspace().getName(), NutsTextStyle.primary1()),
                        factory.ofStyled(sstoreLocation.toString(), NutsTextStyle.path()));
                if (force
                        || session.getTerminal().ask()
                        .resetLine()
                        .forBoolean("force delete?").setDefaultValue(false).setSession(session)
                        .getBooleanValue()) {
                    sstoreLocation.delete();
                }
            }
        }
        for (NutsRepository repository : session.repos().getRepositories()) {
            deleteRepoFolder(repository, session, folder, force);
        }
    }

    private void deleteRepoFolder(NutsRepository repository, NutsSession session, NutsStoreLocation folder, boolean force) {
        NutsPath sstoreLocation = session.locations().getStoreLocation(folder);
        if (sstoreLocation != null) {
            NutsTexts factory = NutsTexts.of(session);
            if (sstoreLocation.exists()) {
                session.out().printf("```error deleting``` %s for repository %s folder %s ...%n",
                        factory.ofStyled(folder.id(), NutsTextStyle.primary1()),
                        factory.ofStyled(repository.getName(), NutsTextStyle.primary1()),
                        factory.ofStyled(sstoreLocation.toString(), NutsTextStyle.path()));
                if (force
                        || session.getTerminal().ask()
                        .resetLine()
                        .forBoolean("Force Delete?").setDefaultValue(false).setSession(session)
                        .getBooleanValue()) {
                    sstoreLocation.delete();
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
        NutsPath sstoreLocation = session.locations().getStoreLocation(NutsStoreLocation.CACHE);
        if (sstoreLocation != null) {
            //            File cache = new File(storeLocation);
            if (sstoreLocation.exists()) {
                sstoreLocation.delete();
            }
            for (NutsRepository repository : session.repos().getRepositories()) {
                deleteRepoCache(repository, session, force);
            }
        }
    }
}
