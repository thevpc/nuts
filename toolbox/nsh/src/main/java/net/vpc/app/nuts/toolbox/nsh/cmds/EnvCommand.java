/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.toolbox.nsh.cmds;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.vpc.app.nuts.NutsCommand;
import net.vpc.app.nuts.NutsArgument;
import net.vpc.app.nuts.NutsExecCommand;
import net.vpc.app.nuts.NutsExecutionType;
import net.vpc.app.nuts.toolbox.nsh.SimpleNshCommand;

/**
 * Created by vpc on 1/7/17.
 */
public class EnvCommand extends SimpleNshCommand {

    public EnvCommand() {
        super("env", DEFAULT_SUPPORT);
    }

    public static class Options {

        int readStatus = 0;
        LinkedHashMap<String, String> newEnv = new LinkedHashMap<>();
        List<String> command = new ArrayList<String>();
        Set<String> unsetVers = new HashSet<String>();
        boolean sort = true;
        boolean ignoreEnvironment = false;
        String dir = null;
        NutsExecutionType executionType = null;
    }

    @Override
    protected Object createOptions() {
        return new Options();
    }

    @Override
    protected boolean configureFirst(NutsCommand commandLine, SimpleNshCommandContext context) {
        Options options = context.getOptions();
        NutsArgument a = commandLine.peek();
        switch (options.readStatus) {
            case 0: {
                switch (a.getKey().getString()) {
                    case "--sort": {
                        options.sort = (commandLine.nextBoolean().getValue().getBoolean());
                        return true;
                    }
                    case "--external":
                    case "--spawn":
                    case "-x": {
                        commandLine.skip();
                        options.executionType = (NutsExecutionType.SPAWN);
                        return true;
                    }
                    case "--embedded":
                    case "-b": {
                        commandLine.skip();
                        options.executionType = (NutsExecutionType.EMBEDDED);
                        return true;
                    }
                    case "--native":
                    case "--syscall":
                    case "-n": {
                        commandLine.skip();
                        options.executionType = (NutsExecutionType.SYSCALL);
                        return true;
                    }
                    case "-C":
                    case "--chdir": {
                        options.dir = commandLine.nextString().getValue().getString();
                        return true;
                    }
                    case "-u":
                    case "--unset": {
                        options.unsetVers.add(commandLine.nextString().getValue().getString());
                        return true;
                    }
                    case "-i":
                    case "--ignore-environment": {
                        options.ignoreEnvironment = (commandLine.nextBoolean().getValue().getBoolean());
                        return true;
                    }
                    case "-": {
                        commandLine.skip();
                        options.readStatus = 1;
                        return true;
                    }
                    default: {
                        if (a.isKeyValue()) {
                            options.newEnv.put(a.getKey().getString(), a.getValue().getString());
                            commandLine.skip();
                            options.readStatus = 1;
                            return true;
                        }else if (a.isOption()) {
                            return false;
                        } else {
                            options.command.add(a.getString());
                            commandLine.skip();
                            options.readStatus = 2;
                            return true;
                        }
                    }
                }
            }
            case 1: {
                if (a.isKeyValue()) {
                    options.newEnv.put(a.getKey().getString(), a.getValue().getString());
                } else {
                    options.command.add(a.getString());
                    options.readStatus = 2;
                }
                commandLine.skip();
                return true;
            }
            case 2: {
                options.command.add(a.getString());
                commandLine.skip();
                return true;
            }
        }
        return false;
    }

    @Override
    protected void createResult(NutsCommand commandLine, SimpleNshCommandContext context) {
        Options options = context.getOptions();
        if (options.sort) {
            context.getSession().addOutputFormatOptions("--sort");
        }
        LinkedHashMap<String, String> env = new LinkedHashMap<>();
        if (!options.ignoreEnvironment) {
            env.putAll((Map) context.getGlobalContext().vars().getAll());
        }
        for (String v : options.unsetVers) {
            env.remove(v);
        }
        env.putAll(options.newEnv);
        if (options.command.isEmpty()) {
            context.setOutObject(env);
        } else {
            final NutsExecCommand e = context.getWorkspace().exec().command(options.command)
                    .env(env)
                    .failFast();
            if (options.dir != null) {
                e.setDirectory(options.dir);
            }
            if (options.executionType != null) {
                e.setExecutionType(options.executionType);
            }
            e.run();
        }
    }

}
