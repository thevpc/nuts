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

import net.thevpc.nuts.*;
import net.thevpc.common.io.IOUtils;
import net.thevpc.common.strings.StringUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import net.thevpc.nuts.toolbox.nsh.SimpleNshBuiltin;

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
            throw new NutsExecutionException(context.getSession(), ex.getMessage(), ex, 100);
        }
    }

    private void catText(InputStream in, OutputStream os, Options options, SimpleNshCommandContext context) throws IOException {
        NutsPrintStream out = context.getWorkspace().io().createPrintStream(os);
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
