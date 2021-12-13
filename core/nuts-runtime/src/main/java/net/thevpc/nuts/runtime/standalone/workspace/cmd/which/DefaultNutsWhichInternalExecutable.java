/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.which;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.app.util.NutsAppUtils;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.exec.DefaultInternalNutsExecutableCommand;

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
        if (NutsAppUtils.processHelpOptions(args, getSession())) {
            showDefaultHelp();
            return;
        }
        List<String> commands = new ArrayList<String>();
//        NutsWorkspace ws = getSession().getWorkspace();
        NutsCommandLine commandLine = NutsCommandLine.of(args,getSession());
        while (commandLine.hasNext()) {
            NutsArgument a = commandLine.peek();
            if (a.isOption()) {
                getSession().configureLast(commandLine);
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
        NutsTexts factory = NutsTexts.of(getSession());
        for (String arg : commands) {
            NutsPrintStream out = getSession().out();
            NutsElements elem = NutsElements.of(getSession());
            try {
                NutsExecutableInformation p = execCommand.copy().setSession(getSession()).clearCommand().configure(false, arg).which();
                //                boolean showDesc = false;
                switch (p.getType()) {
                    case SYSTEM: {
                        if (getSession().isPlainOut()) {
                            out.printf("%s : %s %s%n",
                                    factory.ofStyled(arg, NutsTextStyle.primary4()),
                                    factory.ofStyled("system command", NutsTextStyle.primary6())
                                    , p.getDescription());

                        } else {
                            getSession().out().printlnf(
                                    elem.ofObject()
                                            .set("name", arg)
                                            .set("type", "system-command")
                                            .set("description", p.getDescription())
                                            .build()
                            );
                        }
                        break;
                    }
                    case ALIAS: {
                        if (getSession().isPlainOut()) {
                            out.printf("%s : %s (owner %s ) : %s%n",
                                    factory.ofStyled(arg, NutsTextStyle.primary4()),
                                    factory.ofStyled("nuts alias", NutsTextStyle.primary6()),
                                    p.getId(),
                                    NutsCommandLine.of(getSession().commands().findCommand(p.getName()).getCommand(),getSession())
                            );
                        } else {
                            getSession().out().printlnf(
                                    elem.ofObject()
                                            .set("name", arg)
                                            .set("type", "alias")
                                            .set("description", p.getDescription())
                                            .set("id", p.getId().toString())
                                            .build()
                            );
                        }
                        break;
                    }
                    case ARTIFACT: {
                        if (p.getId() == null) {
                            NutsId nid = NutsId.of(arg,getSession());
                            if (nid != null) {
                                throw new NutsNotFoundException(getSession(), nid);
                            } else {
                                throw new NutsNotFoundException(getSession(), null, NutsMessage.cstyle("artifact not found: %s%s", (arg == null ? "<null>" : arg)));
                            }
                        }
                        if (getSession().isPlainOut()) {
                            out.printf("%s : %s %s%n",
                                    factory.ofStyled(arg, NutsTextStyle.primary4()),
                                    factory.ofStyled("artifact", NutsTextStyle.primary6()),
                                    p.getId()/*, p.getDescription()*/
                            );
                        } else {
                            getSession().out().printlnf(
                                    elem.ofObject()
                                            .set("name", arg)
                                            .set("type", "artifact")
                                            .set("id", p.getId().toString())
                                            .set("description", p.getDescription())
                                            .build()
                            );
                        }
                        break;
                    }
                    case INTERNAL: {
                        if (getSession().isPlainOut()) {
                            out.printf("%s : %s %n",
                                    factory.ofStyled("internal command", NutsTextStyle.primary6()),
                                    factory.ofStyled(arg, NutsTextStyle.primary4())
                            );
                        } else {
                            getSession().out().printlnf(
                                    elem.ofObject()
                                            .set("name", arg)
                                            .set("type", "internal-command")
                                            .set("description", p.getDescription())
                                            .build()
                            );
                        }
                        break;
                    }
                    case UNKNOWN: {
                        if (getSession().isPlainOut()) {
                            out.printf("%s : %s %n",
                                    factory.ofStyled("unknown command", NutsTextStyle.primary6()),
                                    factory.ofStyled(arg, NutsTextStyle.primary4())
                            );
                        } else {
                            getSession().out().printlnf(
                                    elem.ofObject()
                                            .set("name", arg)
                                            .set("type", "unknown-command")
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
                if (getSession().isPlainOut()) {
                    out.printf("%s : %s%n", factory.ofStyled(arg, NutsTextStyle.primary4()), factory.ofStyled("not found", NutsTextStyle.error()));
                } else {
                    NutsElements e = elem;
                    getSession().eout().add(
                            e.ofObject()
                                    .set("name", arg)
                                    .set("type", "not-found")
                                    .build()
                    );
                }
            }
        }
    }

}
