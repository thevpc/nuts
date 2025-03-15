/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.which;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.elem.NElements;

import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.runtime.standalone.app.util.NAppUtils;
import net.thevpc.nuts.runtime.standalone.util.ExtraApiUtils;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.exec.local.internal.DefaultInternalNExecutableCommand;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.util.NMsg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author thevpc
 */
public class DefaultNWhichInternalExecutable extends DefaultInternalNExecutableCommand {

    public DefaultNWhichInternalExecutable(NWorkspace workspace,String[] args, NExecCmd execCommand) {
        super(workspace,"which", args, execCommand);
    }

    @Override
    public int execute() {
        NSession session = workspace.currentSession();
        boolean dry = ExtraApiUtils.asBoolean(getExecCommand().getDry());
        if(dry){
            dryExecute();
            return NExecutionException.SUCCESS;
        }
        if (NAppUtils.processHelpOptions(args)) {
            showDefaultHelp();
            return NExecutionException.SUCCESS;
        }
        List<String> commands = new ArrayList<String>();
//        NutsWorkspace ws = session.getWorkspace();
        NCmdLine cmdLine = NCmdLine.of(args);
        while (cmdLine.hasNext()) {
            NArg a = cmdLine.peek().get();
            if (a.isOption()) {
                session.configureLast(cmdLine);
            } else {
                cmdLine.skip();
                commands.add(a.toString());
                commands.addAll(Arrays.asList(cmdLine.toStringArray()));
                cmdLine.skipAll();
            }
        }
        NAssert.requireNonBlank(commands, "commands");
        NTexts factory = NTexts.of();
        for (String arg : commands) {
            NPrintStream out = session.out();
            NElements elem = NElements.of();
            try {
                try (NExecutableInformation p = getExecCommand().copy().clearCommand().configure(false, arg).which()){
                    switch (p.getType()) {
                        case SYSTEM: {
                            if (session.isPlainOut()) {
                                out.println(NMsg.ofC("%s : %s %s",
                                        factory.ofStyled(arg, NTextStyle.primary4()),
                                        factory.ofStyled("system command", NTextStyle.primary6())
                                        , p.getDescription()));

                            } else {
                                NOut.println(
                                        elem.ofObjectBuilder()
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
                                out.println(NMsg.ofC("%s : %s (owner %s ) : %s",
                                        factory.ofStyled(arg, NTextStyle.primary4()),
                                        factory.ofStyled("nuts alias", NTextStyle.primary6()),
                                        p.getId(),
                                        NCmdLine.of(NWorkspace.of().findCommand(p.getName()).getCommand())
                                ));
                            } else {
                                NOut.println(
                                        elem.ofObjectBuilder()
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
                                NId nid = NId.get(arg).get();
                                if (nid != null) {
                                    throw new NNotFoundException(nid);
                                } else {
                                    throw new NNotFoundException(null, NMsg.ofC("artifact not found: %s%s", (arg == null ? "<null>" : arg)));
                                }
                            }
                            if (session.isPlainOut()) {
                                out.println(NMsg.ofC("%s : %s %s",
                                        factory.ofStyled(arg, NTextStyle.primary4()),
                                        factory.ofStyled("artifact", NTextStyle.primary6()),
                                        p.getId()/*, p.getDescription()*/
                                ));
                            } else {
                                NOut.println(
                                        elem.ofObjectBuilder()
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
                                out.println(NMsg.ofC("%s : %s",
                                        factory.ofStyled("internal command", NTextStyle.primary6()),
                                        factory.ofStyled(arg, NTextStyle.primary4())
                                ));
                            } else {
                                NOut.println(
                                        elem.ofObjectBuilder()
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
                                out.println(NMsg.ofC("%s : %s",
                                        factory.ofStyled("unknown command", NTextStyle.primary6()),
                                        factory.ofStyled(arg, NTextStyle.primary4())
                                ));
                            } else {
                                NOut.println(
                                        elem.ofObjectBuilder()
                                                .set("name", arg)
                                                .set("type", "unknown-command")
                                                .build()
                                );
                            }
                            break;
                        }
                    }
                }
                //                boolean showDesc = false;
//                if (showDesc) {
//                    out.printf("\t %s%n", arg/*, p.getDescription()*/);
//                }
            } catch (NNotFoundException ex) {
                if (session.isPlainOut()) {
                    out.println(NMsg.ofC("%s : %s", factory.ofStyled(arg, NTextStyle.primary4()), factory.ofStyled("not found", NTextStyle.error())));
                } else {
                    session.eout().add(
                            elem.ofObjectBuilder()
                                    .set("name", arg)
                                    .set("type", "not-found")
                                    .build()
                    );
                }
            }
        }
        return NExecutionException.SUCCESS;
    }

}
