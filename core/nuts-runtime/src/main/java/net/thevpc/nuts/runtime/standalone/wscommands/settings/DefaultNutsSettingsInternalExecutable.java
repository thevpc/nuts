/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.wscommands.settings;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.util.CoreNutsUtils;
import net.thevpc.nuts.runtime.standalone.wscommands.DefaultInternalNutsExecutableCommand;

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
        if (CoreNutsUtils.isIncludesHelpOption(args)) {
            showDefaultHelp();
            return;
        }
//        getSession().getWorkspace().extensions().discoverTypes(
//                getSession().getAppId(),
//                Thread.currentThread().getContextClassLoader());

        Boolean autoSave = true;
        NutsCommandLine cmd = getSession().getWorkspace().commandLine().create(args);
        boolean empty = true;
        NutsArgument a;
        do {
            a = cmd.peek();
            if(a==null){
                break;
            }
            boolean enabled = a.isEnabled();
            if(a.isOption() &&
                    (
                            a.getStringKey().equals("-?")
                            ||a.getStringKey().equals("-h")
                                    ||a.getStringKey().equals("-help")
                    )
                    ){
                    cmd.skip();
                    if (enabled) {
                        if (cmd.isExecMode()) {
                            showDefaultHelp();
                        }
                        cmd.skipAll();
                        throw new NutsExecutionException(getSession(), NutsMessage.cstyle("help"), 0);
                    }
                    break;
                } else{
                    NutsSettingsSubCommand selectedSubCommand = null;
                    for (NutsSettingsSubCommand subCommand : getSubCommands()) {
                        if (subCommand.exec(cmd, autoSave, getSession())) {
                            selectedSubCommand = subCommand;
                            empty = false;
                            break;
                        }
                    }
                    if(selectedSubCommand==null){
                        getSession().configureLast(cmd);
                    }else{
                        if (!cmd.isExecMode()) {
                            return;
                        }
                        if (cmd.hasNext()) {
                            NutsPrintStream out = getSession().err();
                            out.printf("unexpected %s%n", cmd.peek());
                            out.printf("type for more help : nuts settings -h%n");
                            throw new NutsExecutionException(getSession(), NutsMessage.cstyle("unexpected %s", cmd.peek()), 1);
                        }
                        break;
                    }
                }
        } while (cmd.hasNext());
        if (empty) {
            NutsPrintStream out = getSession().err();
            out.printf("missing settings command%n");
            out.printf("type for more help : nuts settings -h%n");
            throw new NutsExecutionException(getSession(), NutsMessage.cstyle("missing settings command"), 1);
        }
    }

    public List<NutsSettingsSubCommand> getSubCommands() {
        if (subCommands == null) {
            subCommands = new ArrayList<>(
                    getSession().getWorkspace().extensions().createAllSupported(NutsSettingsSubCommand.class, this)
            );
        }
        return subCommands;
    }

}
