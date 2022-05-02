/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.log;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NutsCommandLine;
import net.thevpc.nuts.runtime.standalone.workspace.NutsWorkspaceExt;
import net.thevpc.nuts.runtime.standalone.repository.impl.main.NutsInstallLogRecord;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.AbstractNutsSettingsSubCommand;
import net.thevpc.nuts.text.NutsTextStyle;
import net.thevpc.nuts.text.NutsTexts;
import net.thevpc.nuts.util.NutsLogger;
import net.thevpc.nuts.util.NutsStringUtils;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author thevpc
 */
public class NutsSettingsLogSubCommand extends AbstractNutsSettingsSubCommand {

    @Override
    public boolean exec(NutsCommandLine cmdLine, Boolean autoSave, NutsSession session) {
        if (cmdLine.next("set loglevel", "sll").isPresent()) {
//            NutsWorkspaceConfigManager configManager = context.getWorkspace().config();
            if (cmdLine.next("verbose", "finest").isPresent()) {
                if (cmdLine.isExecMode()) {
                    NutsLogger.setTermLevel(Level.FINEST,session);
                }
            } else if (cmdLine.next("fine").isPresent()) {
                if (cmdLine.isExecMode()) {
                    NutsLogger.setTermLevel(Level.FINE,session);
                }
            } else if (cmdLine.next("finer").isPresent()) {
                if (cmdLine.isExecMode()) {
                    NutsLogger.setTermLevel(Level.FINER,session);
                }
            } else if (cmdLine.next("info").isPresent()) {
                if (cmdLine.isExecMode()) {
                    NutsLogger.setTermLevel(Level.INFO,session);
                }
            } else if (cmdLine.next("warning").isPresent()) {
                if (cmdLine.isExecMode()) {
                    NutsLogger.setTermLevel(Level.WARNING,session);
                }
            } else if (cmdLine.next("severe", "error").isPresent()) {
                if (cmdLine.isExecMode()) {
                    NutsLogger.setTermLevel(Level.SEVERE,session);
                }
            } else if (cmdLine.next("config").isPresent()) {
                if (cmdLine.isExecMode()) {
                    NutsLogger.setTermLevel(Level.CONFIG,session);
                }
            } else if (cmdLine.next("off").isPresent()) {
                if (cmdLine.isExecMode()) {
                    NutsLogger.setTermLevel(Level.OFF,session);
                }
            } else if (cmdLine.next("all").isPresent()) {
                if (cmdLine.isExecMode()) {
                    NutsLogger.setTermLevel(Level.ALL,session);
                }
            } else {
                if (cmdLine.isExecMode()) {
                    throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("invalid loglevel"));
                }
            }
            cmdLine.setCommandName("config log").throwUnexpectedArgument(session);
            return true;
        } else if (cmdLine.next("get loglevel").isPresent()) {
            if (cmdLine.isExecMode()) {
                Logger rootLogger = Logger.getLogger("");
                session.out().printf("%s%n", rootLogger.getLevel().toString());
            }
        } else if (cmdLine.next("install-log").isPresent()) {
            if (cmdLine.isExecMode()) {
                if(session.isPlainOut()) {
                    for (NutsInstallLogRecord r : NutsWorkspaceExt.of(session).getInstalledRepository().findLog(session)) {
                        NutsTexts txt = NutsTexts.of(session);
                        session.out().printf("%s %s %s %s %s %s %s%n",
                                r.getDate(),
                                r.getUser(),
                                r.getAction(),
                                r.isSucceeded()?
                                        txt.ofStyled("Succeeded", NutsTextStyle.success())
                                        :
                                        txt.ofStyled("Failed",NutsTextStyle.fail()),
                                r.getId()==null?"":r.getId(),
                                r.getForId()==null?"":r.getForId(),
                                NutsStringUtils.trim(r.getMessage())
                        );
                    }
                }else{
                    session.out().printlnf(
                            NutsWorkspaceExt.of(session).getInstalledRepository().findLog(session).toList()
                    );
                }
            }
            return true;
        }
        return false;
    }

}
