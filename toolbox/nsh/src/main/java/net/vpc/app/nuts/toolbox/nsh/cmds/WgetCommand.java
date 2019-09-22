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

import net.vpc.app.nuts.NutsExecutionException;
import net.vpc.app.nuts.NutsSingleton;
import net.vpc.common.io.URLUtils;
import net.vpc.common.strings.StringUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import net.vpc.app.nuts.toolbox.nsh.NshExecutionContext;
import net.vpc.app.nuts.NutsCommandLine;
import net.vpc.app.nuts.toolbox.nsh.SimpleNshBuiltin;

/**
 * Created by vpc on 1/7/17.
 */
@NutsSingleton
public class WgetCommand extends SimpleNshBuiltin {

    public WgetCommand() {
        super("wget", DEFAULT_SUPPORT);
    }

    private static class Options {

        String outputDocument = null;
        List<String> files = new ArrayList<>();
    }

    @Override
    protected Object createOptions() {
        return new Options();
    }

    @Override
    protected boolean configureFirst(NutsCommandLine commandLine, SimpleNshCommandContext context) {
        Options options = context.getOptions();
        if (commandLine.next("-O", "--output-document") != null) {
            options.outputDocument = commandLine.requireNonOption().next().getString();
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
            throw new NutsExecutionException(context.getWorkspace(), "wget: Missing Files", 2);
        }
        for (String file : options.files) {
            download(file, options.outputDocument, context.getExecutionContext());
        }
    }

    protected void download(String path, String output, NshExecutionContext context) {
        String output2 = output;
        URL url;
        try {
            url = new URL(path);
        } catch (MalformedURLException ex) {
            throw new NutsExecutionException(context.getWorkspace(), ex.getMessage(), ex, 100);
        }
        String urlName = URLUtils.getURLName(url);
        if (!StringUtils.isBlank(output2)) {
            output2 = output2.replace("{}", urlName);
        }
        Path file = Paths.get(context.getGlobalContext().getAbsolutePath(StringUtils.isBlank(output2) ? urlName : output2));
        context.getWorkspace().io()
                .copy()
                .session(context.getSession())
                .from(path).to(file).setSession(context.getSession())
                .logProgress().run();
    }
}
