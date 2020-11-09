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

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.thevpc.nuts.NutsArgument;
import net.thevpc.nuts.NutsCommandLine;
import net.thevpc.nuts.NutsSingleton;
import net.thevpc.nuts.toolbox.nsh.SimpleNshBuiltin;

/**
 * Created by vpc on 1/7/17.
 */
@NutsSingleton
public class ChmodCommand extends SimpleNshBuiltin {

    public ChmodCommand() {
        super("chmod", DEFAULT_SUPPORT);
    }

    private boolean isRights(String r) {
        for (char c : r.toCharArray()) {
            switch (c) {
                case 'r':
                case 'w':
                case 'x': {
                    break;
                }
                default: {
                    return false;
                }
            }
        }
        return true;
    }

    private static class Mods {

        int x = 0;
        int w = 0;
        int r = 0;
        boolean user = false;
        boolean recursive = false;

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            if (user) {
                sb.append("u");
            } else {
                sb.append("a");
            }
            if (r > 0) {
                sb.append("+r");
            } else if (r < 0) {
                sb.append("-r");
            }
            if (w > 0) {
                sb.append("+w");
            } else if (w < 0) {
                sb.append("-w");
            }
            if (x > 0) {
                sb.append("+x");
            } else if (x < 0) {
                sb.append("-x");
            }
            if (recursive) {
                sb.append(" [recursive]");
            }
            return sb.toString();
        }
    }

    public void apply(String s, Mods m, int v) {
        for (char c : s.toCharArray()) {
            switch (c) {
                case 'r': {
                    m.r = v;
                    break;
                }
                case 'w': {
                    m.w = v;
                    break;
                }
                case 'x': {
                    m.x = v;
                    break;
                }
            }
        }
    }

    private static class Options {

        List<File> files = new ArrayList<>();
        Mods m = new Mods();
    }

    @Override
    protected Object createOptions() {
        return new Options();
    }

    @Override
    protected boolean configureFirst(NutsCommandLine commandLine, SimpleNshCommandContext context) {
        Options options = context.getOptions();
        //invert processing order!
        if (context.getExecutionContext().configureFirst(commandLine)) {
            return true;
        }
        NutsArgument a = commandLine.peek();
        String s = a.getString();
        if (s.startsWith("-")) {
            if (s.equals("-R") || s.equals("--recursive")) {
                commandLine.skip();
                options.m.recursive = true;
                return true;
            } else if (isRights(s.substring(1))) {
                commandLine.skip();
                options.m.user = true;
                apply(s.substring(1), options.m, -1);
                return true;
            }
        } else if (s.startsWith("+")) {
            if (isRights(s.substring(1))) {
                commandLine.skip();
                options.m.user = true;
                apply(s.substring(1), options.m, 1);
                return true;
            }
        } else if (s.startsWith("u-")) {
            if (isRights(s.substring(2))) {
                commandLine.skip();
                options.m.user = true;
                apply(s.substring(2), options.m, -1);
                return true;
            }
        } else if (s.startsWith("a-")) {
            if (isRights(s.substring(2))) {
                commandLine.skip();
                options.m.user = false;
                apply(s.substring(2), options.m, -1);
                return true;
            }
        } else if (s.startsWith("u+")) {
            if (isRights(s.substring(2))) {
                commandLine.skip();
                options.m.user = true;
                apply(s.substring(2), options.m, 1);
                return true;
            }
        } else if (s.startsWith("a+")) {
            if (isRights(s.substring(2))) {
                commandLine.skip();
                options.m.user = false;
                apply(s.substring(2), options.m, 1);
                return true;
            }
        } else if (!a.isOption()) {
            commandLine.skip();
            options.files.add(new File(context.getRootContext().getAbsolutePath(s)));
            return true;
        }
        return false;
    }

    @Override
    protected void createResult(NutsCommandLine commandLine, SimpleNshCommandContext context) {
        Options options = context.getOptions();
        if (options.files.isEmpty()) {
            commandLine.required();
        }
        LinkedHashMap<String, String> errors = new LinkedHashMap<>();
        for (File f : options.files) {
            chmod(f, options.m, errors);
        }
        if (errors.isEmpty()) {
            errors = null;
        }
        context.setErrObject(errors);
    }

    private void chmod(File f, Mods m, Map<String, String> errors) {
        if (m.r == 0 && m.w == 0 && m.x == 0) {
            return;
        }
        if (m.r != 0) {
            if (!f.canRead()
                    && !f.setReadable(m.r == 1, m.user)) {
                errors.put(f.getPath(), "Unable to [[" + ((m.r == 1) ? "set" : "unset") + "]] read  flag");
            }
        }
        if (m.w != 0) {
            if (!f.canWrite() && !f.setWritable(m.w == 1, m.user)) {
                errors.put(f.getPath(), "Unable to [[" + ((m.r == 1) ? "set" : "unset") + "]] write  flag");
            }
        }
        if (m.x != 0) {
            if (!f.canExecute() && !f.setExecutable(m.x == 1, m.user)) {
                errors.put(f.getPath(), "Unable to [[" + ((m.r == 1) ? "set" : "unset") + "]] exec  flag");
            }
        }
        if (f.isDirectory()) {
            File[] files = f.listFiles();
            if (files != null) {
                for (File file : files) {
                    chmod(file, m, errors);
                }
            }
        }
    }
}
