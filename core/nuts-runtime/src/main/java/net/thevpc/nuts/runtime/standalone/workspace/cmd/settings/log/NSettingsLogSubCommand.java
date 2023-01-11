/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.log;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NCommandLine;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.runtime.standalone.repository.impl.main.NInstallLogRecord;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.AbstractNSettingsSubCommand;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.util.NLogger;
import net.thevpc.nuts.util.NStringUtils;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author thevpc
 */
public class NSettingsLogSubCommand extends AbstractNSettingsSubCommand {

    @Override
    public boolean exec(NCommandLine cmdLine, Boolean autoSave, NSession session) {
        if (cmdLine.next("set loglevel", "sll").isPresent()) {
//            NutsWorkspaceConfigManager configManager = context.getWorkspace().config();
            if (cmdLine.next("verbose", "finest").isPresent()) {
                if (cmdLine.isExecMode()) {
                    NLogger.setTermLevel(Level.FINEST,session);
                }
            } else if (cmdLine.next("fine").isPresent()) {
                if (cmdLine.isExecMode()) {
                    NLogger.setTermLevel(Level.FINE,session);
                }
            } else if (cmdLine.next("finer").isPresent()) {
                if (cmdLine.isExecMode()) {
                    NLogger.setTermLevel(Level.FINER,session);
                }
            } else if (cmdLine.next("info").isPresent()) {
                if (cmdLine.isExecMode()) {
                    NLogger.setTermLevel(Level.INFO,session);
                }
            } else if (cmdLine.next("warning").isPresent()) {
                if (cmdLine.isExecMode()) {
                    NLogger.setTermLevel(Level.WARNING,session);
                }
            } else if (cmdLine.next("severe", "error").isPresent()) {
                if (cmdLine.isExecMode()) {
                    NLogger.setTermLevel(Level.SEVERE,session);
                }
            } else if (cmdLine.next("config").isPresent()) {
                if (cmdLine.isExecMode()) {
                    NLogger.setTermLevel(Level.CONFIG,session);
                }
            } else if (cmdLine.next("off").isPresent()) {
                if (cmdLine.isExecMode()) {
                    NLogger.setTermLevel(Level.OFF,session);
                }
            } else if (cmdLine.next("all").isPresent()) {
                if (cmdLine.isExecMode()) {
                    NLogger.setTermLevel(Level.ALL,session);
                }
            } else {
                if (cmdLine.isExecMode()) {
                    throw new NIllegalArgumentException(session, NMsg.ofPlain("invalid loglevel"));
                }
            }
            cmdLine.setCommandName("config log").throwUnexpectedArgument();
            return true;
        } else if (cmdLine.next("get loglevel").isPresent()) {
            if (cmdLine.isExecMode()) {
                Logger rootLogger = Logger.getLogger("");
                session.out().println(rootLogger.getLevel());
            }
        } else if (cmdLine.next("install-log").isPresent()) {
            if (cmdLine.isExecMode()) {
                if(session.isPlainOut()) {
                    for (NInstallLogRecord r : NWorkspaceExt.of(session).getInstalledRepository().findLog(session)) {
                        NTexts txt = NTexts.of(session);
                        session.out().print(
                                NMsg.ofC("%s %s %s %s %s %s %s%n",
                                        r.getDate(),
                                        r.getUser(),
                                        r.getAction(),
                                        r.isSucceeded()?
                                                txt.ofStyled("Succeeded", NTextStyle.success())
                                                :
                                                txt.ofStyled("Failed", NTextStyle.fail()),
                                        r.getId()==null?"":r.getId(),
                                        r.getForId()==null?"":r.getForId(),
                                        NStringUtils.trim(r.getMessage()))
                        );
                    }
                }else{
                    session.out().println(
                            NWorkspaceExt.of(session).getInstalledRepository().findLog(session).toList()
                    );
                }
            }
            return true;
        }
        return false;
    }

}
