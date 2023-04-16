/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.delete;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.AbstractNSettingsSubCommand;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTexts;

import java.util.HashSet;
import java.util.Set;

/**
 * @author thevpc
 */
public class NSettingsDeleteFoldersSubCommand extends AbstractNSettingsSubCommand {

    private static void deleteRepoCache(NRepository repository, NSession session, boolean force) {
        NPath s = repository.config().getStoreLocation(NStoreType.CACHE);
        if (s != null) {
            if (s.exists()) {
                session.out().println(NMsg.ofC("```error deleting``` %s folder %s ...",
                        NTexts.of(session).ofStyled("cache", NTextStyle.primary1())
                        , s));
                if (force
                        || session.getTerminal().ask()
                        .resetLine()
                        .forBoolean(NMsg.ofPlain("force delete?")).setDefaultValue(false)
                        .setSession(session).getBooleanValue()) {
                    s.delete();
                }
            }
        }
        if (repository.config().isSupportedMirroring()) {
            for (NRepository mirror : repository.config().getMirrors()) {
                deleteRepoCache(mirror, session, force);
            }
        }
    }

    @Override
    public boolean exec(NCmdLine cmdLine, Boolean autoSave, NSession session) {
        for (NStoreType value : NStoreType.values()) {
            String cmdName = "delete " + value.id();
            cmdLine.setCommandName("settings " + cmdName);
            if (cmdLine.next(cmdName).isPresent()) {
                boolean force = false;
                Set<NStoreType> locationsToDelete = new HashSet<>();
                locationsToDelete.add(value);
                while (cmdLine.hasNext()) {
                    NArg a;
                    if ((a = cmdLine.nextFlag("-y", "--yes").orNull()) != null) {
                        force = a.getBooleanValue().get(session);
                    } else if (!cmdLine.isNextOption()) {
                        String s = cmdLine.peek().get(session).asString().get();
                        try {
                            locationsToDelete.add(NStoreType.valueOf(s.toUpperCase()));
                        } catch (Exception ex) {
                            cmdLine.throwUnexpectedArgument();
                        }
                    } else {
                        cmdLine.throwUnexpectedArgument();
                    }
                }
                if (cmdLine.isExecMode()) {
                    for (NStoreType folder : locationsToDelete) {
                        deleteWorkspaceFolder(session, folder, force);
                    }
                }
                return true;
            }
        }
        return false;
    }

    private void deleteWorkspaceFolder(NSession session, NStoreType folder, boolean force) {
        NPath sstoreLocation = NLocations.of(session).getStoreLocation(folder);
        if (sstoreLocation != null) {
            NTexts factory = NTexts.of(session);
            if (sstoreLocation.exists()) {
                session.out().println(NMsg.ofC("```error deleting``` %s for workspace %s folder %s ...",
                        factory.ofStyled(folder.id(), NTextStyle.primary1()),
                        factory.ofStyled(session.getWorkspace().getName(), NTextStyle.primary1()),
                        factory.ofStyled(sstoreLocation.toString(), NTextStyle.path())));
                if (force
                        || session.getTerminal().ask()
                        .resetLine()
                        .forBoolean(NMsg.ofPlain("force delete?")).setDefaultValue(false).setSession(session)
                        .getBooleanValue()) {
                    sstoreLocation.delete();
                }
            }
        }
        for (NRepository repository : NRepositories.of(session).getRepositories()) {
            deleteRepoFolder(repository, session, folder, force);
        }
    }

    private void deleteRepoFolder(NRepository repository, NSession session, NStoreType folder, boolean force) {
        NPath sstoreLocation = NLocations.of(session).getStoreLocation(folder);
        if (sstoreLocation != null) {
            NTexts factory = NTexts.of(session);
            if (sstoreLocation.exists()) {
                session.out().println(NMsg.ofC("```error deleting``` %s for repository %s folder %s ...",
                        factory.ofStyled(folder.id(), NTextStyle.primary1()),
                        factory.ofStyled(repository.getName(), NTextStyle.primary1()),
                        factory.ofStyled(sstoreLocation.toString(), NTextStyle.path())));
                if (force
                        || session.getTerminal().ask()
                        .resetLine()
                        .forBoolean(NMsg.ofPlain("Force Delete?")).setDefaultValue(false).setSession(session)
                        .getBooleanValue()) {
                    sstoreLocation.delete();
                }
            }
        }
        if (repository.config().isSupportedMirroring()) {
            for (NRepository subRepository : repository.config().getMirrors()) {
                deleteRepoCache(subRepository, session, force);
            }
        }
    }

    private void deleteCache(NSession session, boolean force) {
        NPath sstoreLocation = NLocations.of(session).getStoreLocation(NStoreType.CACHE);
        if (sstoreLocation != null) {
            //            File cache = new File(storeLocation);
            if (sstoreLocation.exists()) {
                sstoreLocation.delete();
            }
            for (NRepository repository : NRepositories.of(session).getRepositories()) {
                deleteRepoCache(repository, session, force);
            }
        }
    }
}
