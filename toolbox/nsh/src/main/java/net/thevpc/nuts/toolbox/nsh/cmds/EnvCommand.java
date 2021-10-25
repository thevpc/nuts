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
 *
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
package net.thevpc.nuts.toolbox.nsh.cmds;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import net.thevpc.nuts.*;
import net.thevpc.nuts.spi.NutsSingleton;
import net.thevpc.nuts.toolbox.nsh.SimpleNshBuiltin;

/**
 * Created by vpc on 1/7/17.
 */
@NutsSingleton
public class EnvCommand extends SimpleNshBuiltin {

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
        NutsRunAs runAs = null;
    }

    @Override
    protected Object createOptions() {
        return new Options();
    }

    @Override
    protected boolean configureFirst(NutsCommandLine commandLine, SimpleNshCommandContext context) {
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
                    case "--system":{
                        commandLine.skip();
                        options.executionType = (NutsExecutionType.SYSTEM);
                        return true;
                    }
                    case "--current-user":{
                        commandLine.skip();
                        options.runAs = NutsRunAs.currentUser();
                        return true;
                    }
                    case "--as-root":{
                        commandLine.skip();
                        options.runAs = NutsRunAs.root();
                        return true;
                    }
                    case "--sudo":{
                        commandLine.skip();
                        options.runAs = NutsRunAs.sudo();
                        return true;
                    }
                    case "--as-user":{
                        a = commandLine.nextString();
                        options.runAs = NutsRunAs.user(a.getValue().getString());
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
                        } else if (a.isOption()) {
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
    protected void execBuiltin(NutsCommandLine commandLine, SimpleNshCommandContext context) {
        Options options = context.getOptions();
        if (options.sort) {
            context.getSession().addOutputFormatOptions("--sort");
        }
        SortedMap<String, String> env = new TreeMap<>();
        if (!options.ignoreEnvironment) {
            env.putAll((Map) context.getRootContext().vars().getAll());
        }
        for (String v : options.unsetVers) {
            env.remove(v);
        }
        env.putAll(options.newEnv);
        if (options.command.isEmpty()) {
            context.getSession().out().printlnf(env);
        } else {
            final NutsExecCommand e = context.getSession().exec().addCommand(options.command)
                    .setEnv(env)
                    .setFailFast(true);
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
