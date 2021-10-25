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
 *
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
package net.thevpc.nuts.toolbox.nsh.cmds;

import java.util.ArrayList;
import java.util.List;

import net.thevpc.nuts.NutsArgument;
import net.thevpc.nuts.spi.NutsSingleton;
import net.thevpc.nuts.toolbox.nsh.SimpleNshBuiltin;
import net.thevpc.nuts.NutsCommandLine;

/**
 * Created by vpc on 1/7/17.
 */
@NutsSingleton
public class DirNameCommand extends SimpleNshBuiltin {

    public DirNameCommand() {
        super("dirname", DEFAULT_SUPPORT);
    }

    @Override
    protected Object createOptions() {
        return new Options();
    }

    @Override
    protected boolean configureFirst(NutsCommandLine cmdLine, SimpleNshCommandContext context) {
        Options options = context.getOptions();
        NutsArgument a = cmdLine.peek();
        switch (a.getKey().getString()) {
            case "-z":
            case "--zero": {
                cmdLine.skip();
                options.sep = "\0";
                return true;
            }
            default: {
                if (a.isOption()) {

                } else {
                    options.names.add(cmdLine.next().toString());
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    protected void execBuiltin(NutsCommandLine cmdLine, SimpleNshCommandContext context) {
        Options options = context.getOptions();
        if (options.names.isEmpty()) {
            cmdLine.required();
        }
        List<String> results = new ArrayList<>();
        for (String name : options.names) {
            StringBuilder sb = new StringBuilder(name);
            int lastNameLen = 0;
            while (sb.length() > 0 && sb.charAt(sb.length() - 1) != '/') {
                sb.deleteCharAt(sb.length() - 1);
                lastNameLen++;
            }
            while (sb.length() > 1 && sb.charAt(sb.length() - 1) == '/') {
                sb.deleteCharAt(sb.length() - 1);
            }
            if (lastNameLen == 0) {
                while (sb.length() > 0 && sb.charAt(sb.length() - 1) != '/') {
                    sb.deleteCharAt(sb.length() - 1);
                    lastNameLen++;
                }
                while (sb.length() > 1 && sb.charAt(sb.length() - 1) == '/') {
                    sb.deleteCharAt(sb.length() - 1);
                }
            }
            if (sb.length() == 0) {
                results.add(".");
            } else {
                results.add(sb.toString());
            }
        }
        switch (context.getSession().getOutputFormat()) {
            case PLAIN: {
                for (int i = 0; i < results.size(); i++) {
                    String name = results.get(i);
                    if (i > 0) {
                        context.getSession().out().print(options.sep);
                    }
                    context.getSession().out().print(name);
                }
                break;
            }
            default: {
                context.getSession().out().printlnf(results);
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
