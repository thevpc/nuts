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
package net.thevpc.nuts.toolbox.nsh.cmds.bash;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.spi.NComponentScope;
import net.thevpc.nuts.spi.NScopeType;
import net.thevpc.nuts.toolbox.nsh.cmds.NShellBuiltinDefault;
import net.thevpc.nuts.toolbox.nsh.eval.NShellExecutionContext;
import net.thevpc.nuts.util.NBlankable;

import java.util.*;

/**
 * Created by vpc on 1/7/17.
 */
@NComponentScope(NScopeType.WORKSPACE)
public class EnvCommand extends NShellBuiltinDefault {

    public EnvCommand() {
        super("env", NConstants.Support.DEFAULT_SUPPORT, Options.class);
    }

    @Override
    protected boolean onCmdNextOption(NArg arg, NCmdLine cmdLine, NShellExecutionContext context) {
        Options options = context.getOptions();
        NSession session = context.getSession();
        NArg a = cmdLine.peek().get(session);
        switch (options.readStatus) {
            case 0: {
                switch (a.key()) {
                    case "--sort": {
                        cmdLine.withNextFlag((v, r, s) -> options.sort = v);
                        return true;
                    }
                    case "--external":
                    case "--spawn":
                    case "-x": {
                        cmdLine.withNextTrueFlag((v, r, s) -> options.executionType = NExecutionType.SPAWN);
                        return true;
                    }
                    case "--embedded":
                    case "-b": {
                        cmdLine.withNextTrueFlag((v, r, s) -> options.executionType = NExecutionType.EMBEDDED);
                        return true;
                    }
                    case "--system": {
                        cmdLine.withNextTrueFlag((v, r, s) -> options.executionType = NExecutionType.SYSTEM);
                        return true;
                    }
                    case "--current-user": {
                        cmdLine.withNextTrueFlag((v, r, s) -> options.runAs = NRunAs.currentUser());
                        return true;
                    }
                    case "--as-root": {
                        cmdLine.withNextTrueFlag((v, r, s) -> options.runAs = NRunAs.root());
                        return true;
                    }
                    case "--sudo": {
                        cmdLine.withNextTrueFlag((v, r, s) -> options.runAs = NRunAs.sudo());
                        return true;
                    }
                    case "--as-user": {
                        cmdLine.withNextEntry((v, r, s) -> options.runAs = NRunAs.user(v));
                        return true;
                    }
                    case "-C":
                    case "--chdir": {
                        cmdLine.withNextEntry((v, r, s) -> options.dir = v);
                        return true;
                    }
                    case "-u":
                    case "--unset": {
                        cmdLine.withNextEntry((v, r, s) -> options.unsetVers.add(v));
                        return true;
                    }
                    case "-i":
                    case "--ignore-environment": {
                        cmdLine.withNextFlag((v, r, s) -> options.ignoreEnvironment = v);
                        return true;
                    }
                    case "-": {
                        cmdLine.skip();
                        options.readStatus = 1;
                        return true;
                    }
                    default: {
                        if (a.isKeyValue()) {
                            options.newEnv.put(a.key(), a.getStringValue().get(session));
                            cmdLine.skip();
                            options.readStatus = 1;
                            return true;
                        } else if (a.isOption()) {
                            return false;
                        } else {
                            options.command.add(a.asString().get(session));
                            cmdLine.skip();
                            options.readStatus = 2;
                            return true;
                        }
                    }
                }
            }
            case 1: {
                if (a.isKeyValue()) {
                    options.newEnv.put(a.key(), a.getStringValue().get(session));
                } else {
                    options.command.add(a.asString().get(session));
                    options.readStatus = 2;
                }
                cmdLine.skip();
                return true;
            }
            case 2: {
                options.command.add(a.asString().get(session));
                cmdLine.skip();
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onCmdExec(NCmdLine cmdLine, NShellExecutionContext context) {
        Options options = context.getOptions();
        if (options.sort) {
            context.getSession().addOutputFormatOptions("--sort");
        }
        SortedMap<String, String> env = new TreeMap<>();
        if (!options.ignoreEnvironment) {
            env.putAll((Map) context.vars().getAll());
        }
        for (String v : options.unsetVers) {
            env.remove(v);
        }
        env.putAll(options.newEnv);
        if (options.command.isEmpty()) {
            if (context.getSession().isPlainOut()) {
                for (Map.Entry<String, String> e : env.entrySet()) {
                    context.getSession().out().println(e.getKey() + "=" + e.getValue());
                }
            } else {
                context.getSession().out().println(env);
            }
        } else {
            final NExecCmd e = NExecCmd.of(context.getSession()).addCommand(options.command)
                    .setEnv(env)
                    .failFast();
            if (!NBlankable.isBlank(options.dir)) {
                e.setDirectory(NPath.of(options.dir, context.getSession()));
            }
            if (options.executionType != null) {
                e.setExecutionType(options.executionType);
            }
            e.run();
        }
    }

    public static class Options {

        int readStatus = 0;
        LinkedHashMap<String, String> newEnv = new LinkedHashMap<>();
        List<String> command = new ArrayList<String>();
        Set<String> unsetVers = new HashSet<String>();
        boolean sort = true;
        boolean ignoreEnvironment = false;
        String dir = null;
        NExecutionType executionType = null;
        NRunAs runAs = null;
    }

    @Override
    protected boolean onCmdNextNonOption(NArg arg, NCmdLine cmdLine, NShellExecutionContext context) {
        return onCmdNextOption(arg, cmdLine, context);
    }
}
