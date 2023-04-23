/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.settings;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.runtime.standalone.app.util.NAppUtils;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.exec.DefaultInternalNExecutableCommand;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author thevpc
 */
public class DefaultNSettingsInternalExecutable extends DefaultInternalNExecutableCommand {

    public DefaultNSettingsInternalExecutable(String[] args, NExecCommand execCommand) {
        super("fetch", args, execCommand);
    }

    private List<NSettingsSubCommand> subCommands;
    @Override
    public int execute() {
        if(getSession().isDry()){
            dryExecute();
            return NExecutionException.SUCCESS;
        }
        NSession session = getSession();
        if (NAppUtils.processHelpOptions(args, session)) {
            showDefaultHelp();
            return NExecutionException.SUCCESS;
        }
//        getSession().getWorkspace().extensions().discoverTypes(
//                getSession().getAppId(),
//                Thread.currentThread().getContextClassLoader());

        Boolean autoSave = true;
        NCmdLine cmd = NCmdLine.of(args);
        boolean empty = true;
        NArg a;
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
                        throw new NExecutionException(session, NMsg.ofPlain("help"), NExecutionException.SUCCESS);
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
                            return NExecutionException.SUCCESS;
                        }
                        if (cmd.hasNext()) {
                            NPrintStream out = session.err();
                            out.println(NMsg.ofC("unexpected %s", cmd.peek()));
                            out.println("type for more help : nuts settings -h");
                            throw new NExecutionException(session, NMsg.ofC("unexpected %s", cmd.peek()), NExecutionException.ERROR_1);
                        }
                        break;
                    }
                }
        } while (cmd.hasNext());
        if (empty) {
            NPrintStream out = session.err();
            out.println("missing settings command");
            out.println("type for more help : nuts settings -h");
            throw new NExecutionException(session, NMsg.ofPlain("missing settings command"), NExecutionException.ERROR_1);
        }
        return NExecutionException.SUCCESS;
    }

    public List<NSettingsSubCommand> getSubCommands() {
        if (subCommands == null) {
            subCommands = new ArrayList<>(
                    getSession().extensions().createComponents(NSettingsSubCommand.class, this)
            );
        }
        return subCommands;
    }

}
