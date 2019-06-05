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
import net.vpc.app.nuts.toolbox.nsh.AbstractNshBuiltin;
import net.vpc.app.nuts.toolbox.nsh.NutsCommandContext;
import net.vpc.common.io.URLUtils;
import net.vpc.common.strings.StringUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.vpc.app.nuts.NutsCommand;
import net.vpc.app.nuts.NutsArgument;

/**
 * Created by vpc on 1/7/17.
 */
public class WgetCommand extends AbstractNshBuiltin {

    public WgetCommand() {
        super("wget", DEFAULT_SUPPORT);
    }

    private static class Options {

        String outputDocument = null;
    }

    public void exec(String[] args, NutsCommandContext context) {
        NutsCommand cmdLine = cmdLine(args, context);
        Options options = new Options();
        List<String> files = new ArrayList<>();
        NutsArgument a;
        while (cmdLine.hasNext()) {
            if (context.configureFirst(cmdLine)) {
                //
            } else if (cmdLine.next("-O", "--output-document") != null) {
                options.outputDocument = cmdLine.requireNonOption().next().getString();
            } else {
                files.add(cmdLine.requireNonOption().next().getString());
            }
        }
        if (files.isEmpty()) {
            throw new NutsExecutionException(context.getWorkspace(), "wget: Missing Files", 2);
        }
        for (String file : files) {
            download(file, options.outputDocument, context);
        }
    }

    protected void download(String path, String output, NutsCommandContext context) {
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
        Path file = context.getWorkspace().io().path(context.getGlobalContext().getAbsolutePath(StringUtils.isBlank(output2) ? urlName : output2));
        context.getWorkspace().io()
                .copy().from(path).to(file).setTerminalProvider(context.getSession())
                .monitorable().run();
    }
}
