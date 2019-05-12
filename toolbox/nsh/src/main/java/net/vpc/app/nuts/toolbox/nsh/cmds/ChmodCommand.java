/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.toolbox.nsh.cmds;

import net.vpc.app.nuts.NutsExecutionException;
import net.vpc.app.nuts.toolbox.nsh.AbstractNutsCommand;
import net.vpc.app.nuts.toolbox.nsh.NutsCommandContext;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import net.vpc.app.nuts.NutsCommandLine;
import net.vpc.app.nuts.NutsArgument;

/**
 * Created by vpc on 1/7/17.
 */
public class ChmodCommand extends AbstractNutsCommand {


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

    public class Mods {
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

    public void apply(String s, Mods m, int v) throws Exception {
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

    public int exec(String[] args, NutsCommandContext context) throws Exception {
        NutsCommandLine cmdLine = cmdLine(args, context);
        List<File> files = new ArrayList<>();
        Mods m = new Mods();
        NutsArgument a;
        while (cmdLine.hasNext()) {
            String s = cmdLine.read().getString();
            if (context.configure(cmdLine)) {
                //
            }else if (s.startsWith("-")) {
                if (s.equals("-R") || s.equals("--recursive")) {
                    m.recursive = true;
                } else if (isRights(s.substring(1))) {
                    m.user = true;
                    apply(s.substring(1), m, -1);
                } else {
                    throw new NutsExecutionException("chmod: Unsupported option" + s,2);
                }
            } else if (s.startsWith("+")) {
                if (isRights(s.substring(1))) {
                    m.user = true;
                    apply(s.substring(1), m, 1);
                } else {
                    throw new NutsExecutionException("chmod: Unsupported option" + s,2);
                }
            } else if (s.startsWith("u-")) {
                if (isRights(s.substring(2))) {
                    m.user = true;
                    apply(s.substring(2), m, -1);
                } else {
                    throw new NutsExecutionException("chmod: Unsupported option" + s,2);
                }
            } else if (s.startsWith("a-")) {
                if (isRights(s.substring(2))) {
                    m.user = false;
                    apply(s.substring(2), m, -1);
                } else {
                    throw new NutsExecutionException("chmod: Unsupported option" + s,2);
                }
            } else if (s.startsWith("u+")) {
                if (isRights(s.substring(2))) {
                    m.user = true;
                    apply(s.substring(2), m, 1);
                } else {
                    throw new NutsExecutionException("chmod: Unsupported option" + s,2);
                }
            } else if (s.startsWith("a+")) {
                if (isRights(s.substring(2))) {
                    m.user = false;
                    apply(s.substring(2), m, 1);
                } else {
                    throw new NutsExecutionException("chmod: Unsupported option" + s,2);
                }
            } else {
                files.add(new File(context.getShell().getAbsolutePath(s)));
            }
        }
        if (files.isEmpty()) {
            throw new NutsExecutionException("chmod: Missing Expression",2);
        }
        PrintStream out = context.out();
        for (File f : files) {
            chmod(f, m, out);
        }
        return 0;
    }

    protected void chmod(File f, Mods m, PrintStream out) {
        if (m.r == 0 && m.w == 0 && m.x == 0) {
            return;
        }
        if (m.r != 0) {
            if (!f.canRead() &&
                    !f.setReadable(m.r == 1, m.user)
            ) {
                out.printf("Unable to [[" + ((m.r == 1) ? "set" : "unset") + "]] readAll  flag for ==%s==\n", f);
            }
        }
        if (m.w != 0) {
            if (!f.canWrite() && !f.setWritable(m.w == 1, m.user)) {
                out.printf("Unable to [[" + ((m.w == 1) ? "set" : "unset") + "]] write flag for ==%s==\n", f);
            }
        }
        if (m.x != 0) {
            if (!f.canExecute() && !f.setExecutable(m.x == 1, m.user)) {
                out.printf("Unable to [[" + ((m.x == 1) ? "set" : "unset") + "]] exec  flag for ==%s==\n", f);
            }
        }
        if (f.isDirectory()) {
            File[] files = f.listFiles();
            if (files != null) {
                for (File file : files) {
                    chmod(file, m, out);
                }
            }
        }
    }
}
