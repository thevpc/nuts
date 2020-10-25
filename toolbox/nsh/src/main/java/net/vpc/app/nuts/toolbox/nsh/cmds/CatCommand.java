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
 * Copyright (C) 2016-2020 thevpc
 * <br>
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * <br>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <br>
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.toolbox.nsh.cmds;

import net.vpc.app.nuts.NutsSingleton;
import net.vpc.common.io.IOUtils;
import net.vpc.common.strings.StringUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import net.vpc.app.nuts.NutsArgument;
import net.vpc.app.nuts.NutsExecutionException;
import net.vpc.app.nuts.NutsCommandLine;
import net.vpc.app.nuts.toolbox.nsh.SimpleNshBuiltin;

/**
 * Created by vpc on 1/7/17.
 */
@NutsSingleton
public class CatCommand extends SimpleNshBuiltin {

    public CatCommand() {
        super("cat", DEFAULT_SUPPORT);
    }

    private static class Options {

        boolean n = false;
        boolean T = false;
        boolean E = false;
        List<File> files = new ArrayList<>();
        long currentNumber;
    }

    @Override
    protected Object createOptions() {
        return new Options();
    }

    @Override
    protected boolean configureFirst(NutsCommandLine commandLine, SimpleNshCommandContext context) {
        Options options = context.getOptions();
        NutsArgument a;

        if (commandLine.next("-") != null) {
            options.files.add(null);
            return true;
        } else if ((a = commandLine.next("-n", "--number")) != null) {
            options.n = a.getBooleanValue();
            return true;
        } else if ((a = commandLine.next("-t", "--show-tabs")) != null) {
            options.T = a.getBooleanValue();
            return true;
        } else if ((a = commandLine.next("-E", "--show-ends")) != null) {
            options.E = a.getBooleanValue();
            return true;
        } else if (!commandLine.peek().isOption()) {
            String path = commandLine.next().getString();
            File file = new File(context.getRootContext().getAbsolutePath(path));
            options.files.add(file);
            return true;
        }
        return false;
    }

    @Override
    protected void createResult(NutsCommandLine commandLine, SimpleNshCommandContext context) {
        Options options = context.getOptions();
        if (options.files.isEmpty()) {
            options.files.add(null);
        }
        PrintStream out = context.out();
        try {
            if (options.n || options.T || options.E) {
                options.currentNumber = 1;
                //text mode
                for (File f : options.files) {
                    boolean close = false;
                    InputStream in = null;
                    if (f == null) {
                        in = context.in();
                    } else {
                        in = new FileInputStream(f);
                        close = true;
                    }
                    try {
                        catText(in, out, options, context);
                    } finally {
                        if (close) {
                            in.close();
                        }
                    }
                }
            } else {
                for (File f : options.files) {
                    if (f == null) {
                        IOUtils.copy(context.in(), out, 4096 * 2);
                    } else {
                        IOUtils.copy(f, out, 4096 * 2);
                    }
                }
            }
        } catch (IOException ex) {
            throw new NutsExecutionException(context.getWorkspace(), ex.getMessage(), ex, 100);
        }
    }

    private void catText(InputStream in, OutputStream os, Options options, SimpleNshCommandContext context) throws IOException {
        PrintStream out = null;
        if (os instanceof PrintStream) {
            out = (PrintStream) os;
        } else {
            out = new PrintStream(os);
        }
        out = context.getWorkspace().io().term().getTerminalFormat().prepare(out);
        try {
            //do not close!!
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String line = null;
            while ((line = reader.readLine()) != null) {
                if (options.n) {
                    out.print(StringUtils.alignRight(String.valueOf(options.currentNumber), 6));
                    out.print("  ");
                }
                if (options.T) {
                    line = line.replace("\t", "^I");
                }
                out.print(line);
                if (options.E) {
                    out.println("$");
                }
                out.println();
                options.currentNumber++;
            }
        } finally {
            out.flush();
        }
    }
}
