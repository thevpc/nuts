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
 *
 * Copyright (C) 2016-2020 thevpc
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <br>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <br>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.toolbox.nsh.cmds;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import net.vpc.app.nuts.NutsArgument;
import net.vpc.app.nuts.NutsExecutionException;
import net.vpc.app.nuts.NutsSingleton;
import net.vpc.app.nuts.toolbox.nsh.SimpleNshBuiltin;
import net.vpc.common.javashell.JShellContext;
import net.vpc.app.nuts.NutsCommandLine;

/**
 * Created by vpc on 1/7/17.
 */
@NutsSingleton
public class SourceCommand extends SimpleNshBuiltin {

    public SourceCommand() {
        super("source", DEFAULT_SUPPORT);
    }

    private static class Options {

        List<String> files = new ArrayList<>();
    }

    @Override
    protected Object createOptions() {
        return new Options();
    }

    @Override
    protected boolean configureFirst(NutsCommandLine commandLine, SimpleNshCommandContext context) {
        Options options = context.getOptions();
        final NutsArgument a = commandLine.peek();
        if (!a.isOption()) {
            options.files.add(a.getString());
            return true;
        }
        return false;
    }

    @Override
    protected void createResult(NutsCommandLine commandLine, SimpleNshCommandContext context) {
        Options options = context.getOptions();
        final String[] paths = context.getExecutionContext().getGlobalContext().vars().get("PATH", "").split(":|;");
        List<String> goodFiles = new ArrayList<>();
        for (String file : options.files) {
            if (!file.contains("/")) {
                for (String path : paths) {
                    if (new File(path, file).isFile()) {
                        file = new File(path, file).getPath();
                        break;
                    }
                }
                if (!new File(file).isFile()) {
                    if (new File(context.getRootContext().getCwd(), file).isFile()) {
                        file = new File(context.getRootContext().getCwd(), file).getPath();
                    }
                }
                if (!new File(file).isFile()) {
                    throw new NutsExecutionException(context.getWorkspace(), "File not found " + file, 1);
                }
                goodFiles.add(file);
            }
        }
        JShellContext c2 = context.getShell().createContext(context.getRootContext());
        c2.setArgs(context.getArgs());
        int a = 0;
        for (String goodFile : goodFiles) {
            context.getShell().executeFile(goodFile, c2, false);
        }
    }

}
