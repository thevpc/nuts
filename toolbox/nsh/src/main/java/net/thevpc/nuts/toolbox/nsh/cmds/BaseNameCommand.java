/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
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
import java.util.List;
import net.thevpc.nuts.NutsArgument;
import net.thevpc.nuts.NutsSingleton;
import net.thevpc.nuts.toolbox.nsh.SimpleNshBuiltin;
import net.thevpc.nuts.NutsCommandLine;

/**
 * Created by vpc on 1/7/17.
 */
@NutsSingleton
public class BaseNameCommand extends SimpleNshBuiltin {

    public BaseNameCommand() {
        super("basename", DEFAULT_SUPPORT);
    }

    private static class Options {

        String sep = "\n";
        List<String> names = new ArrayList<>();
        boolean multi = false;
        String suffix = null;
    }

    @Override
    protected Object createOptions() {
        return new Options();
    }

    @Override
    protected boolean configureFirst(NutsCommandLine cmdLine, SimpleNshCommandContext context) {
        Options options = context.getOptions();
        NutsArgument a = cmdLine.peek();
        switch (a.getStringKey()) {
            case "-z":
            case "--zero": {
                cmdLine.skip();
                options.sep = "\0";
                return true;
            }
            case "-a":
            case "--all":
            case "--multi": {
                options.multi = cmdLine.nextBoolean().getBooleanValue();
                return true;
            }
            case "-s":
            case "--suffix": {
                options.suffix = cmdLine.nextString().getStringValue();
                options.multi = true;
                return true;
            }
            default: {
                if (a.isOption()) {

                } else {
                    while (!cmdLine.isEmpty()) {
                        NutsArgument n = cmdLine.nextNonOption();
                        if (options.names.isEmpty()) {
                            options.names.add(n.toString());
                        } else {
                            if (options.multi) {
                                options.names.add(n.toString());
                            } else if (options.names.size() == 1 && options.suffix == null) {
                                options.suffix = n.toString();
                            } else {
                                cmdLine.pushBack(n);
                                cmdLine.unexpectedArgument();
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
    protected void createResult(NutsCommandLine commandLine, SimpleNshCommandContext context) {
        Options options = context.getOptions();
        if (options.names.isEmpty()) {
            commandLine.required();
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
        context.setPrintlnOutObject(results);
    }

    @Override
    protected void printPlainObject(SimpleNshCommandContext context) {
        List<String> results = context.getResult();
        Options options = context.getOptions();
        for (String name : results) {
            context.out().print(name);
            context.out().print(options.sep);
        }
    }

}