/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
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
package net.thevpc.nuts.toolbox.nsh.cmds.bash;

import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.spi.NComponentScope;
import net.thevpc.nuts.spi.NComponentScopeType;
import net.thevpc.nuts.toolbox.nsh.cmds.NShellBuiltinDefault;
import net.thevpc.nuts.toolbox.nsh.eval.NShellExecutionContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vpc on 1/7/17.
 */
@NComponentScope(NComponentScopeType.WORKSPACE)
public class BaseNameCommand extends NShellBuiltinDefault {

    public BaseNameCommand() {
        super("basename", DEFAULT_SUPPORT, Options.class);
    }

    @Override
    protected boolean onCmdNextOption(NArg arg, NCmdLine cmdLine, NShellExecutionContext context) {
        Options options = context.getOptions();
        NSession session = context.getSession();
        NArg a = cmdLine.peek().get(session);
        switch (a.key()) {
            case "-z":
            case "--zero": {
                cmdLine.skip();
                options.sep = "\0";
                return true;
            }
            case "-a":
            case "--all":
            case "--multi": {
                cmdLine.withNextFlag((v, r, s) -> options.multi = v);
                return true;
            }
            case "-s":
            case "--suffix": {
                cmdLine.withNextEntry((v, r, s) -> {
                    options.suffix = v;
                    options.multi = true;
                });
                return true;
            }
            default: {
                if (a.isOption()) {

                } else {
                    while (!cmdLine.isEmpty()) {
                        NArg n = cmdLine.nextNonOption().get(session);
                        if (options.names.isEmpty()) {
                            options.names.add(n.toString());
                        } else {
                            if (options.multi) {
                                options.names.add(n.toString());
                            } else if (options.names.size() == 1 && options.suffix == null) {
                                options.suffix = n.toString();
                            } else {
                                cmdLine.pushBack(n);
                                cmdLine.throwUnexpectedArgument();
                            }
                        }
                    }
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    protected boolean onCmdNextNonOption(NArg arg, NCmdLine cmdLine, NShellExecutionContext context) {
        return onCmdNextOption(arg, cmdLine, context);
    }

    @Override
    protected void onCmdExec(NCmdLine cmdLine, NShellExecutionContext context) {
        Options options = context.getOptions();
        NSession session = context.getSession();
        if (options.names.isEmpty()) {
            cmdLine.throwMissingArgument();
        }
        List<String> results = new ArrayList<>();
        for (String name : options.names) {
            StringBuilder sb = new StringBuilder(name);
            int lastNameLen = 0;
            while (sb.length() - lastNameLen > 0 && sb.charAt(sb.length() - 1 - lastNameLen) != '/') {
                lastNameLen++;
            }
            if (lastNameLen == 0) {
                while (sb.length() > 1 && sb.charAt(sb.length() - 1) == '/') {
                    sb.deleteCharAt(sb.length() - 1);
                }
                while (sb.length() - lastNameLen > 0 && sb.charAt(sb.length() - 1 - lastNameLen) != '/') {
                    lastNameLen++;
                }
            }
            String basename = (lastNameLen == 0) ? sb.toString() : sb.substring(sb.length() - lastNameLen);
            if (options.suffix != null && basename.endsWith(options.suffix)) {
                basename = basename.substring(0, basename.length() - options.suffix.length());
            }
            results.add(basename);
        }
        switch (session.getOutputFormat()) {
            case PLAIN: {
                for (int i = 0; i < results.size(); i++) {
                    String name = results.get(i);
                    if (i > 0) {
                        session.out().print(options.sep);
                    }
                    session.out().print(name);
                }
                break;
            }
            default: {
                session.out().println(results);
            }
        }
    }

    private static class Options {

        String sep = "\n";
        List<String> names = new ArrayList<>();
        boolean multi = false;
        String suffix = null;
    }


}
