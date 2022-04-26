/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.settings;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.app.util.NutsAppUtils;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.exec.DefaultInternalNutsExecutableCommand;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author thevpc
 */
public class DefaultNutsSettingsInternalExecutable extends DefaultInternalNutsExecutableCommand {

    public DefaultNutsSettingsInternalExecutable(String[] args, NutsSession session) {
        super("fetch", args, session);
    }

    private List<NutsSettingsSubCommand> subCommands;
    @Override
    public void execute() {
        NutsSession session = getSession();
        if (NutsAppUtils.processHelpOptions(args, session)) {
            showDefaultHelp();
            return;
        }
//        getSession().getWorkspace().extensions().discoverTypes(
//                getSession().getAppId(),
//                Thread.currentThread().getContextClassLoader());

        Boolean autoSave = true;
        NutsCommandLine cmd = NutsCommandLine.of(args);
        boolean empty = true;
        NutsArgument a;
        do {
            a = cmd.peek().get(session);
            if(a==null){
                break;
            }
            boolean enabled = a.isActive();
            if(a.isOption() &&
                    (
                            a.getKey().asString().get(session).equals("-?")
                            ||a.getKey().asString().get(session).equals("-h")
                                    ||a.getKey().asString().get(session).equals("-help")
                    )
                    ){
                    cmd.skip();
                    if (enabled) {
                        if (cmd.isExecMode()) {
                            showDefaultHelp();
                        }
                        cmd.skipAll();
                        throw new NutsExecutionException(session, NutsMessage.cstyle("help"), 0);
                    }
                    break;
                } else{
                    NutsSettingsSubCommand selectedSubCommand = null;
                    for (NutsSettingsSubCommand subCommand : getSubCommands()) {
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
                            NutsPrintStream out = session.err();
                            out.printf("unexpected %s%n", cmd.peek());
                            out.printf("type for more help : nuts settings -h%n");
                            throw new NutsExecutionException(session, NutsMessage.cstyle("unexpected %s", cmd.peek()), 1);
                        }
                        break;
                    }
                }
        } while (cmd.hasNext());
        if (empty) {
            NutsPrintStream out = session.err();
            out.printf("missing settings command%n");
            out.printf("type for more help : nuts settings -h%n");
            throw new NutsExecutionException(session, NutsMessage.cstyle("missing settings command"), 1);
        }
    }

    public List<NutsSettingsSubCommand> getSubCommands() {
        if (subCommands == null) {
            subCommands = new ArrayList<>(
                    getSession().extensions().createAllSupported(NutsSettingsSubCommand.class, this)
            );
        }
        return subCommands;
    }

}
