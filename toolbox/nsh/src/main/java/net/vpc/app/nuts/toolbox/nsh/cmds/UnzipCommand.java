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

import net.vpc.app.nuts.*;
import net.vpc.common.io.InputStreamVisitor;
import net.vpc.common.io.UnzipOptions;
import net.vpc.common.io.ZipUtils;
import net.vpc.common.strings.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;

import net.vpc.app.nuts.toolbox.nsh.NshExecutionContext;
import net.vpc.app.nuts.toolbox.nsh.SimpleNshBuiltin;

/**
 * Created by vpc on 1/7/17.
 */
@NutsSingleton
public class UnzipCommand extends SimpleNshBuiltin {

    public UnzipCommand() {
        super("unzip", DEFAULT_SUPPORT);
    }

    private static class Options {

        boolean l = false;
        boolean skipRoot = false;
        String dir = null;
        List<String> files = new ArrayList<>();
    }

    @Override
    protected Object createOptions() {
        return new Options();
    }

    @Override
    protected boolean configureFirst(NutsCommandLine commandLine, SimpleNshCommandContext context) {
        Options options = context.getOptions();
        NutsArgument a;
        if ((a = commandLine.nextBoolean("-l")) != null) {
            options.l = a.getBooleanValue();
            return true;
        } else if ((a = commandLine.nextString("-d")) != null) {
            options.dir = a.getStringValue();
            return true;
        } else if (!commandLine.peek().isOption()) {
            while (commandLine.hasNext()) {
                options.files.add(commandLine.next().getString());
            }
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
        for (String path : options.files) {
            File file = new File(context.getRootContext().getAbsolutePath(path));
            try {
                if (options.l) {
                    ZipUtils.visitZipFile(file, null, new InputStreamVisitor() {
                        @Override
                        public boolean visit(String path, InputStream inputStream) throws IOException {
                            context.out().printf("%s\n", path);
                            return true;
                        }
                    });
                } else {
                    String dir = options.dir;
                    if (StringUtils.isBlank(dir)) {
                        dir = context.getRootContext().getCwd();
                    }
                    dir = context.getRootContext().getAbsolutePath(dir);
                    ZipUtils.unzip(file.getPath(), dir, new UnzipOptions().setSkipRoot(options.skipRoot));
                }
            } catch (IOException|UncheckedIOException| NutsIOException ex) {
                throw new NutsExecutionException(context.getWorkspace(), ex.getMessage(), ex, 1);
            }
        }
    }
}
