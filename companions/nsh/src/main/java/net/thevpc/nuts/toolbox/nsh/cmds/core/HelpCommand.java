/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 * <p>
 * Copyright [2020] [thevpc]
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain a
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
 */
package net.thevpc.nuts.toolbox.nsh.cmds.core;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.io.NTerminalMode;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.toolbox.nsh.cmds.NShellBuiltinCore;
import net.thevpc.nuts.toolbox.nsh.cmds.NShellBuiltin;
import net.thevpc.nuts.toolbox.nsh.eval.NShellExecutionContext;
import net.thevpc.nuts.toolbox.nsh.util.bundles._StringUtils;
import net.thevpc.nuts.toolbox.nsh.options.CommandNonOption;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

/**
 * Created by vpc on 1/7/17.
 */
public class HelpCommand extends NShellBuiltinCore {

    public HelpCommand() {
        super("help", DEFAULT_SUPPORT, Options.class);
    }

    @Override
    protected boolean onCmdNextOption(NArg arg, NCmdLine cmdLine, NShellExecutionContext context) {
        NSession session = context.getSession();
        Options options = context.getOptions();
        if (cmdLine.next("--ntf").isPresent()) {
            options.code = true;
            return true;
        } else if (cmdLine.peek().get(session).isNonOption()) {
            options.commandNames.add(
                    cmdLine.nextNonOption(new CommandNonOption("command", context.getShellContext()))
                            .get().asString().get(session));
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected boolean onCmdNextNonOption(NArg arg, NCmdLine cmdLine, NShellExecutionContext context) {
        NSession session = context.getSession();
        Options options = context.getOptions();
        if (cmdLine.next("--ntf").isPresent()) {
            options.code = true;
            return true;
        } else if (cmdLine.peek().get(session).isNonOption()) {
            options.commandNames.add(
                    cmdLine.nextNonOption(new CommandNonOption("command", context.getShellContext()))
                            .get().asString().get(session));
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected void onCmdExec(NCmdLine cmdLine, NShellExecutionContext context) {
        NSession session = context.getSession();
        Options options = context.getOptions();
        if (options.code) {
            session.getTerminal().setOut(
                    session.getTerminal().out().setTerminalMode(NTerminalMode.INHERITED)
            );
        }
        final NTexts text = NTexts.of(session);
        Function<String, String> ss = options.code ? new Function<String, String>() {
            @Override
            public String apply(String t) {
                return text.ofPlain(t).toString();
            }
        } : x -> x;
        if (cmdLine.isExecMode()) {
            if (options.commandNames.isEmpty()) {
                NText n=null;
                try {
                    if (context.getSession() != null) {
                        n = context.getSession().getAppHelp().orNull();
                    }
                } catch (Exception ex) {
                    //
                }
//                NPath p;
//                if (!NBlankable.isBlank(packageName) && !NBlankable.isBlank(serviceName)) {
//                    p = NPath.of("classpath:/" + packageName.replace('.', '/') + "/" + serviceName + ".ntf", HelpCommand.class.getClassLoader(), session);
//                } else {
//                    p = NPath.of("classpath:/net/thevpc/nuts/toolbox/nsh.ntf", HelpCommand.class.getClassLoader(), session);
//                }
//                NText n = text.parser().parse(p);
//                n = text.transform(n, new NTextTransformConfig().setProcessAll(true)
//                        .setCurrentDir(p.getParent())
//                        .setImportClassLoader(getClass().getClassLoader())
//                );
                String helpText = (n == null ? "no help found" : n.toString());
                context.out().println(ss.apply(helpText));
                context.out().println(NTexts.of(session).ofStyled("AVAILABLE COMMANDS ARE:", NTextStyle.primary1()));
                NShellBuiltin[] commands = context.builtins().getAll();
                Arrays.sort(commands, new Comparator<NShellBuiltin>() {
                    @Override
                    public int compare(NShellBuiltin o1, NShellBuiltin o2) {
                        return o1.getName().compareTo(o2.getName());
                    }
                });
                int max = 1;
                for (NShellBuiltin cmd : commands) {
                    int x = cmd.getName().length();
                    if (x > max) {
                        max = x;
                    }
                }
                for (NShellBuiltin cmd : commands) {
                    context.out().print(NMsg.ofC("%s : ", text.ofStyled(_StringUtils.formatLeft(cmd.getName(), max), NTextStyle.primary4())));
                    context.out().println(ss.apply(cmd.getHelpHeader())); //formatted
                }
            } else {
                int x = 0;
                for (String commandName : options.commandNames) {
                    NShellBuiltin command1 = context.builtins().find(commandName);
                    if (command1 == null) {
                        context.err().println(NMsg.ofC("command not found : %s", text.ofStyled(commandName, NTextStyle.error())));
                        x = 1;
                    } else {
                        String help = command1.getHelp();
                        context.out().print(NMsg.ofC("%s : %s\f", text.ofStyled("COMMAND", NTextStyle.primary4()), "commandName"));
                        context.out().println(ss.apply(help));
                    }
                }
                throwExecutionException("error", x, session);
            }
        }
    }

    private static class Options {
        boolean code = false;
        List<String> commandNames = new ArrayList<>();
    }

}
