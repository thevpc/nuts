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

import net.vpc.app.nuts.toolbox.nsh.AbstractNutsCommand;
import net.vpc.app.nuts.toolbox.nsh.NutsCommandContext;
import net.vpc.app.nuts.toolbox.nsh.util.FilePath;
import net.vpc.app.nuts.toolbox.nsh.util.SShConnection;
import net.vpc.common.commandline.CommandLine;
import net.vpc.common.io.FileUtils;
import net.vpc.common.io.IOUtils;
import net.vpc.common.io.RuntimeIOException;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by vpc on 1/7/17.
 * ssh copy credits to Chanaka Lakmal from
 * https://medium.com/ldclakmal/scp-with-java-b7b7dbcdbc85
 */
public class CpCommand extends AbstractNutsCommand {

    public CpCommand() {
        super("cp", DEFAULT_SUPPORT);
    }

    public static class Options {
        String keyPassword = null;
        String keyFilePath = null;
        boolean mkdir;
    }

    public int exec(String[] args, NutsCommandContext context) throws Exception {
        CommandLine cmdLine = cmdLine(args, context);
        List<FilePath> files = new ArrayList<>();
        Options o = new Options();
        while (!cmdLine.isEmpty()) {
            if (cmdLine.isOption()) {
                if (cmdLine.isOption(null, "password")) {
                    o.keyPassword = cmdLine.readValue();
                } else if (cmdLine.isOption(null, "cert")) {
                    o.keyFilePath = cmdLine.readValue();
                } else if (cmdLine.isOption(null, "mkdir")) {
                    o.mkdir = true;
                }
            } else {
                files.add(new FilePath(cmdLine.readValue()));
            }
        }
        if (files.size() < 2) {
            throw new IllegalArgumentException("Missing parameters");
        }
        for (int i = 0; i < files.size() - 1; i++) {
            copy(files.get(i), files.get(files.size() - 1), o);
        }
        return 0;
    }

    public void copy(FilePath from, FilePath to, Options o) {
        if (from.getProtocol().equals("file") && to.getProtocol().equals("file")) {
            File from1 = new File(from.getPath());
            File to1 = new File(to.getPath());
            if (from1.isFile()) {
                if (to1.isDirectory() || to.getPath().endsWith("/") || to.getPath().endsWith("\\")) {
                    to1 = new File(to1, from1.getName());
                }
            } else if (from1.isDirectory()) {
                if (to.getPath().endsWith("/") || to.getPath().endsWith("\\")) {
                    to1 = new File(to1, from1.getName());
                }

            }
            if (o.mkdir) {
                FileUtils.createParents(to1);
            }
            IOUtils.copy(from1, to1);
        } else if (from.getProtocol().equals("file") && to.getProtocol().equals("ssh")) {
            String p = to.getPath();
            if (p.endsWith("/") || p.endsWith("\\")) {
                p = p + "/" + FileUtils.getFileName(from.getPath());
            }

            SShConnection session = new SShConnection(to.getUser(), to.getServer(), to.getPort(), o.keyFilePath, o.keyPassword);
            if (o.mkdir) {
                //session.mkdir;
            }
            copyLocalToRemote(new File(from.getPath()), p, session);
            session.close();
        } else if (from.getProtocol().equals("ssh") && to.getProtocol().equals("file")) {
            File to1 = new File(to.getPath());
            if (to1.isDirectory() || to.getPath().endsWith("/") || to.getPath().endsWith("\\")) {
                to1 = new File(to1, FileUtils.getFileName(from.getPath()));
            }
            SShConnection session = new SShConnection(from.getUser(), from.getServer(), from.getPort(), o.keyFilePath, o.keyPassword);
            if (o.mkdir) {
                FileUtils.createParents(to1);
            }
            session.copyRemoteToLocal(from.getPath(), to1.getPath());
            session.close();
        } else if (from.getProtocol().equals("url") && to.getProtocol().equals("file")) {
            try {
                File to1 = new File(to.getPath());
                if (to1.isDirectory() || to.getPath().endsWith("/") || to.getPath().endsWith("\\")) {
                    to1 = new File(to1, FileUtils.getFileName(from.getPath()));
                }
                if (o.mkdir) {
                    FileUtils.createParents(to1);
                }
                IOUtils.copy(new URL(from.getPath()), to1);
            } catch (MalformedURLException e) {
                throw new RuntimeIOException(e);
            }
        } else {
            throw new RuntimeIOException("Unsupported protocols " + from.getProtocol() + "->" + to.getProtocol());
        }
    }

    private void copyLocalToRemote(File from, String to, SShConnection session) {
        if (from.isDirectory()) {
            session.exec("mkdir -p " + to);
            for (File file : from.listFiles()) {
                copyLocalToRemote(file, to + "/" + file.getName(), session);
            }
        } else if (from.isFile()) {
            session.copyLocalToRemote(from.getPath(), to);
        }
    }


}
