/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.which;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NutsArgument;
import net.thevpc.nuts.cmdline.NutsCommandLine;
import net.thevpc.nuts.elem.NutsElements;
import net.thevpc.nuts.io.NutsPrintStream;
import net.thevpc.nuts.runtime.standalone.app.util.NutsAppUtils;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.exec.DefaultInternalNutsExecutableCommand;
import net.thevpc.nuts.text.NutsTextStyle;
import net.thevpc.nuts.text.NutsTexts;
import net.thevpc.nuts.util.NutsUtils;

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
        NutsSession session = getSession();
        if (NutsAppUtils.processHelpOptions(args, session)) {
            showDefaultHelp();
            return;
        }
        List<String> commands = new ArrayList<String>();
//        NutsWorkspace ws = getSession().getWorkspace();
        NutsCommandLine commandLine = NutsCommandLine.of(args);
        while (commandLine.hasNext()) {
            NutsArgument a = commandLine.peek().get(session);
            if (a.isOption()) {
                session.configureLast(commandLine);
            } else {
                commandLine.skip();
                commands.add(a.toString());
                commands.addAll(Arrays.asList(commandLine.toStringArray()));
                commandLine.skipAll();
            }
        }
        NutsUtils.requireNonBlank(commands,session,"commands");
        NutsTexts factory = NutsTexts.of(session);
        for (String arg : commands) {
            NutsPrintStream out = session.out();
            NutsElements elem = NutsElements.of(session);
            try {
                NutsExecutableInformation p = execCommand.copy().setSession(session).clearCommand().configure(false, arg).which();
                //                boolean showDesc = false;
                switch (p.getType()) {
                    case SYSTEM: {
                        if (session.isPlainOut()) {
                            out.printf("%s : %s %s%n",
                                    factory.ofStyled(arg, NutsTextStyle.primary4()),
                                    factory.ofStyled("system command", NutsTextStyle.primary6())
                                    , p.getDescription());

                        } else {
                            session.out().printlnf(
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
                        if (session.isPlainOut()) {
                            out.printf("%s : %s (owner %s ) : %s%n",
                                    factory.ofStyled(arg, NutsTextStyle.primary4()),
                                    factory.ofStyled("nuts alias", NutsTextStyle.primary6()),
                                    p.getId(),
                                    NutsCommandLine.of(session.commands().findCommand(p.getName()).getCommand())
                            );
                        } else {
                            session.out().printlnf(
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
                            NutsId nid = NutsId.of(arg).get(session);
                            if (nid != null) {
                                throw new NutsNotFoundException(session, nid);
                            } else {
                                throw new NutsNotFoundException(session, null, NutsMessage.ofCstyle("artifact not found: %s%s", (arg == null ? "<null>" : arg)));
                            }
                        }
                        if (session.isPlainOut()) {
                            out.printf("%s : %s %s%n",
                                    factory.ofStyled(arg, NutsTextStyle.primary4()),
                                    factory.ofStyled("artifact", NutsTextStyle.primary6()),
                                    p.getId()/*, p.getDescription()*/
                            );
                        } else {
                            session.out().printlnf(
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
                        if (session.isPlainOut()) {
                            out.printf("%s : %s %n",
                                    factory.ofStyled("internal command", NutsTextStyle.primary6()),
                                    factory.ofStyled(arg, NutsTextStyle.primary4())
                            );
                        } else {
                            session.out().printlnf(
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
                        if (session.isPlainOut()) {
                            out.printf("%s : %s %n",
                                    factory.ofStyled("unknown command", NutsTextStyle.primary6()),
                                    factory.ofStyled(arg, NutsTextStyle.primary4())
                            );
                        } else {
                            session.out().printlnf(
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
                if (session.isPlainOut()) {
                    out.printf("%s : %s%n", factory.ofStyled(arg, NutsTextStyle.primary4()), factory.ofStyled("not found", NutsTextStyle.error()));
                } else {
                    NutsElements e = elem;
                    session.eout().add(
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
