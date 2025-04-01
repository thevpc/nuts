/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.delete;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;


import net.thevpc.nuts.NStoreType;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.AbstractNSettingsSubCommand;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.util.NAsk;
import net.thevpc.nuts.util.NMsg;

import java.util.HashSet;
import java.util.Set;

/**
 * @author thevpc
 */
public class NSettingsDeleteFoldersSubCommand extends AbstractNSettingsSubCommand {
    public NSettingsDeleteFoldersSubCommand(NWorkspace workspace) {
        super();
    }

    private static void deleteRepoCache(NRepository repository, boolean force) {
        NPath s = repository.config().getStoreLocation(NStoreType.CACHE);
        if (s != null) {
            NSession session = repository.getWorkspace().currentSession();
            if (s.exists()) {
                NOut.println(NMsg.ofC("```error deleting``` %s folder %s ...",
                        NText.ofStyledPrimary1("cache")
                        , s));
                if (force
                        || NAsk.of()
                        .forBoolean(NMsg.ofPlain("force delete?")).setDefaultValue(false)
                        .getBooleanValue()) {
                    s.delete();
                }
            }
        }
        if (repository.config().isSupportedMirroring()) {
            for (NRepository mirror : repository.config().getMirrors()) {
                deleteRepoCache(mirror, force);
            }
        }
    }

    @Override
    public boolean exec(NCmdLine cmdLine, Boolean autoSave) {
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
                        force = a.getBooleanValue().get();
                    } else if (!cmdLine.isNextOption()) {
                        String s = cmdLine.peek().get().asString().get();
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
                        deleteWorkspaceFolder(folder, force);
                    }
                }
                return true;
            }
        }
        return false;
    }

    private void deleteWorkspaceFolder(NStoreType folder, boolean force) {
        NPath sstoreLocation = NWorkspace.of().getStoreLocation(folder);
        if (sstoreLocation != null) {
            NTexts factory = NTexts.of();
            if (sstoreLocation.exists()) {
                NOut.println(NMsg.ofC("```error deleting``` %s for workspace %s folder %s ...",
                        factory.ofStyled(folder.id(), NTextStyle.primary1()),
                        factory.ofStyled(NWorkspace.of().getName(), NTextStyle.primary1()),
                        factory.ofStyled(sstoreLocation.toString(), NTextStyle.path())));
                if (force
                        || NAsk.of()
                        .forBoolean(NMsg.ofPlain("force delete?")).setDefaultValue(false)
                        .getBooleanValue()) {
                    sstoreLocation.delete();
                }
            }
        }
        for (NRepository repository : NWorkspace.of().getRepositories()) {
            deleteRepoFolder(repository, folder, force);
        }
    }

    private void deleteRepoFolder(NRepository repository, NStoreType folder, boolean force) {
        NPath sstoreLocation = NWorkspace.of().getStoreLocation(folder);
        if (sstoreLocation != null) {
            NTexts factory = NTexts.of();
            if (sstoreLocation.exists()) {
                NOut.println(NMsg.ofC("```error deleting``` %s for repository %s folder %s ...",
                        factory.ofStyled(folder.id(), NTextStyle.primary1()),
                        factory.ofStyled(repository.getName(), NTextStyle.primary1()),
                        factory.ofStyled(sstoreLocation.toString(), NTextStyle.path())));
                if (force
                        || NAsk.of()
                        .forBoolean(NMsg.ofPlain("Force Delete?")).setDefaultValue(false)
                        .getBooleanValue()) {
                    sstoreLocation.delete();
                }
            }
        }
        if (repository.config().isSupportedMirroring()) {
            for (NRepository subRepository : repository.config().getMirrors()) {
                deleteRepoCache(subRepository, force);
            }
        }
    }

    private void deleteCache(NSession session, boolean force) {
        NPath sstoreLocation = NWorkspace.of().getStoreLocation(NStoreType.CACHE);
        if (sstoreLocation != null) {
            //            File cache = new File(storeLocation);
            if (sstoreLocation.exists()) {
                sstoreLocation.delete();
            }
            for (NRepository repository : NWorkspace.of().getRepositories()) {
                deleteRepoCache(repository, force);
            }
        }
    }
}
