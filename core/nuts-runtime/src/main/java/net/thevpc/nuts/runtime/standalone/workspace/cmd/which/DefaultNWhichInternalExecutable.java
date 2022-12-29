/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.which;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NArgument;
import net.thevpc.nuts.cmdline.NCommandLine;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.io.NStream;
import net.thevpc.nuts.runtime.standalone.app.util.NAppUtils;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.exec.DefaultInternalNExecutableCommand;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.util.NUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author thevpc
 */
public class DefaultNWhichInternalExecutable extends DefaultInternalNExecutableCommand {

    private final NExecCommand execCommand;

    public DefaultNWhichInternalExecutable(String[] args, NSession session, NExecCommand execCommand) {
        super("which", args, session);
        this.execCommand = execCommand;
    }

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
        List<String> commands = new ArrayList<String>();
//        NutsWorkspace ws = getSession().getWorkspace();
        NCommandLine commandLine = NCommandLine.of(args);
        while (commandLine.hasNext()) {
            NArgument a = commandLine.peek().get(session);
            if (a.isOption()) {
                session.configureLast(commandLine);
            } else {
                commandLine.skip();
                commands.add(a.toString());
                commands.addAll(Arrays.asList(commandLine.toStringArray()));
                commandLine.skipAll();
            }
        }
        NUtils.requireNonBlank(commands, "commands", session);
        NTexts factory = NTexts.of(session);
        for (String arg : commands) {
            NStream out = session.out();
            NElements elem = NElements.of(session);
            try {
                NExecutableInformation p = execCommand.copy().setSession(session).clearCommand().configure(false, arg).which();
                //                boolean showDesc = false;
                switch (p.getType()) {
                    case SYSTEM: {
                        if (session.isPlainOut()) {
                            out.printf("%s : %s %s%n",
                                    factory.ofStyled(arg, NTextStyle.primary4()),
                                    factory.ofStyled("system command", NTextStyle.primary6())
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
                                    factory.ofStyled(arg, NTextStyle.primary4()),
                                    factory.ofStyled("nuts alias", NTextStyle.primary6()),
                                    p.getId(),
                                    NCommandLine.of(session.commands().findCommand(p.getName()).getCommand())
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
                            NId nid = NId.of(arg).get(session);
                            if (nid != null) {
                                throw new NNotFoundException(session, nid);
                            } else {
                                throw new NNotFoundException(session, null, NMsg.ofCstyle("artifact not found: %s%s", (arg == null ? "<null>" : arg)));
                            }
                        }
                        if (session.isPlainOut()) {
                            out.printf("%s : %s %s%n",
                                    factory.ofStyled(arg, NTextStyle.primary4()),
                                    factory.ofStyled("artifact", NTextStyle.primary6()),
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
                                    factory.ofStyled("internal command", NTextStyle.primary6()),
                                    factory.ofStyled(arg, NTextStyle.primary4())
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
                                    factory.ofStyled("unknown command", NTextStyle.primary6()),
                                    factory.ofStyled(arg, NTextStyle.primary4())
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
            } catch (NNotFoundException ex) {
                if (session.isPlainOut()) {
                    out.printf("%s : %s%n", factory.ofStyled(arg, NTextStyle.primary4()), factory.ofStyled("not found", NTextStyle.error()));
                } else {
                    NElements e = elem;
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
