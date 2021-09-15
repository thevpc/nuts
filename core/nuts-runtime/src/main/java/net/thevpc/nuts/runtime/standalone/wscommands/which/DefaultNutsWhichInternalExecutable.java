/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.wscommands.which;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.util.CoreNutsUtils;
import net.thevpc.nuts.runtime.standalone.wscommands.exec.DefaultInternalNutsExecutableCommand;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author thevpc
 */
public class DefaultNutsWhichInternalExecutable extends DefaultInternalNutsExecutableCommand {

    private final NutsExecCommand execCommand;

    public DefaultNutsWhichInternalExecutable(String[] args, NutsSession session, NutsExecCommand execCommand) {
        super("which", args, session);
        this.execCommand = execCommand;
    }

    @Override
    public void execute() {
        if (CoreNutsUtils.isIncludesHelpOption(args)) {
            showDefaultHelp();
            return;
        }
        List<String> commands = new ArrayList<String>();
        NutsWorkspace ws = getSession().getWorkspace();
        NutsCommandLine commandLine = ws.commandLine().create(args);
        while (commandLine.hasNext()) {
            NutsArgument a = commandLine.peek();
            if (a.isOption()) {
                switch (a.getKey().getString()) {
                    case "--help": {
                        commandLine.skipAll();
                        showDefaultHelp();
                        return;
                    }
                    default: {
                        getSession().configureLast(commandLine);
                    }
                }
            } else {
                commandLine.skip();
                commands.add(a.toString());
                commands.addAll(Arrays.asList(commandLine.toStringArray()));
                commandLine.skipAll();
            }
        }
        if (commands.isEmpty()) {
            throw new NutsIllegalArgumentException(getSession(), NutsMessage.cstyle("which: missing commands"));
        }
        NutsTextManager factory = ws.text();
        for (String arg : this.args) {
            NutsPrintStream out = getSession().out();
            try {
                NutsExecutableInformation p = execCommand.copy().setSession(getSession()).clearCommand().configure(false, arg).which();
                NutsElementFormat e = getSession().getWorkspace().elem();
//                boolean showDesc = false;
                switch (p.getType()) {
                    case SYSTEM: {
                        if(getSession().isPlainOut()){
                            out.printf("%s : %s %s%n",
                                    factory.forStyled(arg, NutsTextStyle.primary4()),
                                    factory.forStyled("system command", NutsTextStyle.primary6())
                                    , p.getDescription());

                        }else {
                            getSession().eout().add(
                                    e.forObject()
                                            .set("name",arg)
                                            .set("type","system-command")
                                            .set("description",p.getDescription())
                                            .build()
                            );
                        }
                        break;
                    }
                    case ALIAS: {
                        if(getSession().isPlainOut()){
                            out.printf("%s : %s (owner %s ) : %s%n",
                                    factory.forStyled(arg, NutsTextStyle.primary4()),
                                    factory.forStyled("nuts alias", NutsTextStyle.primary6()),
                                    p.getId(),
                                    ws.commandLine().create(ws.commands().findCommand(p.getName()).getCommand())
                            );
                        }else {
                            getSession().eout().add(
                                    e.forObject()
                                            .set("name",arg)
                                            .set("type","alias")
                                            .set("description",p.getDescription())
                                            .set("id",p.getId().toString())
                                            .build()
                            );
                        }
                        break;
                    }
                    case ARTIFACT: {
                        if (p.getId() == null) {
                            NutsId nid = getSession().getWorkspace().id().parser().setLenient(true).parse(arg);
                            if(nid!=null) {
                                throw new NutsNotFoundException(getSession(), nid);
                            }else{
                                throw new NutsNotFoundException(getSession(), null,NutsMessage.cstyle("artifact not found: %s%s", (arg == null ? "<null>" : arg)));
                            }
                        }
                        if(getSession().isPlainOut()){
                            out.printf("%s : %s %s%n",
                                    factory.forStyled(arg, NutsTextStyle.primary4()),
                                    factory.forStyled("artifact", NutsTextStyle.primary6()),
                                    p.getId()/*, p.getDescription()*/
                            );
                        }else {
                            getSession().eout().add(
                                    e.forObject()
                                            .set("name",arg)
                                            .set("type","artifact")
                                            .set("id",p.getId().toString())
                                            .set("description",p.getDescription())
                                            .build()
                            );
                        }
                        break;
                    }
                    case INTERNAL: {
                        if(getSession().isPlainOut()){
                            out.printf("%s : %s %n",
                                    factory.forStyled("internal command", NutsTextStyle.primary6()),
                                    factory.forStyled(arg, NutsTextStyle.primary4())
                            );
                        }else {
                            getSession().eout().add(
                                    e.forObject()
                                            .set("name",arg)
                                            .set("type","internal-command")
                                            .set("description",p.getDescription())
                                            .build()
                            );
                        }
                        break;
                    }
                }
//                if (showDesc) {
//                    out.printf("\t %s%n", arg/*, p.getDescription()*/);
//                }
            } catch (NutsNotFoundException ex) {
                if(getSession().isPlainOut()){
                    out.printf("%s : %s%n", factory.forStyled(arg,NutsTextStyle.primary4()),factory.forStyled("not found",NutsTextStyle.error()));
                }else {
                    NutsElementFormat e = getSession().getWorkspace().elem();
                    getSession().eout().add(
                            e.forObject()
                                    .set("name",arg)
                                    .set("type","not-found")
                                    .build()
                    );
                }
            }
        }
    }

}
