/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.settings;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NArgument;
import net.thevpc.nuts.cmdline.NCommandLine;
import net.thevpc.nuts.io.NOutStream;
import net.thevpc.nuts.runtime.standalone.app.util.NAppUtils;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.exec.DefaultInternalNExecutableCommand;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author thevpc
 */
public class DefaultNSettingsInternalExecutable extends DefaultInternalNExecutableCommand {

    public DefaultNSettingsInternalExecutable(String[] args, NSession session) {
        super("fetch", args, session);
    }

    private List<NSettingsSubCommand> subCommands;
    @Override
    public void execute() {
        if(getSession().isDry()){
            dryExecute();
            return;
        }
        NSession session = getSession();
        if (NAppUtils.processHelpOptions(args, session)) {
            showDefaultHelp();
            return;
        }
//        getSession().getWorkspace().extensions().discoverTypes(
//                getSession().getAppId(),
//                Thread.currentThread().getContextClassLoader());

        Boolean autoSave = true;
        NCommandLine cmd = NCommandLine.of(args);
        boolean empty = true;
        NArgument a;
        do {
            a = cmd.peek().get(session);
            if(a==null){
                break;
            }
            boolean enabled = a.isActive();
            if(a.isOption() &&
                    (
                            a.key().equals("-?")
                            ||a.key().equals("-h")
                                    ||a.key().equals("-help")
                    )
                    ){
                    cmd.skip();
                    if (enabled) {
                        if (cmd.isExecMode()) {
                            showDefaultHelp();
                        }
                        cmd.skipAll();
                        throw new NExecutionException(session, NMsg.ofPlain("help"), 0);
                    }
                    break;
                } else{
                    NSettingsSubCommand selectedSubCommand = null;
                    for (NSettingsSubCommand subCommand : getSubCommands()) {
                        if (subCommand.exec(cmd, autoSave, session)) {
                            selectedSubCommand = subCommand;
                            empty = false;
                            break;
                        }
                    }
                    if(selectedSubCommand==null){
                        session.configureLast(cmd);
                    }else{
                        if (!cmd.isExecMode()) {
                            return;
                        }
                        if (cmd.hasNext()) {
                            NOutStream out = session.err();
                            out.printf("unexpected %s%n", cmd.peek());
                            out.printf("type for more help : nuts settings -h%n");
                            throw new NExecutionException(session, NMsg.ofCstyle("unexpected %s", cmd.peek()), 1);
                        }
                        break;
                    }
                }
        } while (cmd.hasNext());
        if (empty) {
            NOutStream out = session.err();
            out.printf("missing settings command%n");
            out.printf("type for more help : nuts settings -h%n");
            throw new NExecutionException(session, NMsg.ofPlain("missing settings command"), 1);
        }
    }

    public List<NSettingsSubCommand> getSubCommands() {
        if (subCommands == null) {
            subCommands = new ArrayList<>(
                    getSession().extensions().createAllSupported(NSettingsSubCommand.class, this)
            );
        }
        return subCommands;
    }

}
