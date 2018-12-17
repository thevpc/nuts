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
import net.vpc.app.nuts.toolbox.nsh.util.ShellHelper;
import net.vpc.common.io.URLUtils;
import net.vpc.common.ssh.SShConnection;
import net.vpc.common.commandline.Argument;
import net.vpc.common.commandline.CommandLine;
import net.vpc.common.io.FileUtils;
import net.vpc.common.io.IOUtils;
import net.vpc.common.io.RuntimeIOException;
import net.vpc.common.ssh.SshPath;
import net.vpc.common.strings.StringUtils;

import java.io.File;
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
        boolean noColors;
        boolean mkdir;
        boolean verbose;
        ShellHelper.WsSshListener sshlistener;
    }

    public int exec(String[] args, NutsCommandContext context) throws Exception {
        CommandLine cmdLine = cmdLine(args, context);
        List<FilePath> files = new ArrayList<>();
        Options o = new Options();
        Argument a;
        while (cmdLine.hasNext()) {
            if (context.configure(cmdLine)) {
                //
            }else if ((a = cmdLine.readBooleanOption("--mkdir")) != null) {
                    o.mkdir = a.getBooleanValue();
                } else if ((a = cmdLine.readBooleanOption("--no-colors")) != null) {
                    o.noColors = a.getBooleanValue();
                } else if ((a = cmdLine.readBooleanOption("--verbose")) != null) {
                    o.verbose = a.getBooleanValue();
            } else {
                String value = cmdLine.readNonOption().getExpression();
                if (StringUtils.isEmpty(value)) {
                    throw new IllegalArgumentException("Empty File Path");
                }
                files.add(FilePath.of(value,context.getShell().getCwd()));
            }
        }
        if (files.size() < 2) {
            throw new IllegalArgumentException("Missing parameters");
        }
        o.sshlistener = o.verbose ? new ShellHelper.WsSshListener(context.getWorkspace(),context.getSession()) : null;
        for (int i = 0; i < files.size() - 1; i++) {
            copy(files.get(i), files.get(files.size() - 1), o,context);
        }
        return 0;
    }

    public void copy(FilePath from, FilePath to, Options o, NutsCommandContext context) {
        if (from.getProtocol().equals("file") && to.getProtocol().equals("file")) {
            File from1 = ((FilePath.LocalFilePath) from).getFile();
            File to1 = ((FilePath.LocalFilePath) to).getFile();
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
            context.out().printf("[[\\[CP\\]]] %s -> %s%\n",from ,to);
            IOUtils.copy(from1, to1);
        } else if (from.getProtocol().equals("file") && to.getProtocol().equals("ssh")) {
            SshPath to1 = ((FilePath.SshFilePath) to).getSshPath();
            String p = to1.getPath();
            if (p.endsWith("/") || p.endsWith("\\")) {
                p = p + "/" + FileUtils.getFileName(to1.getPath());
            }

            try (SShConnection session = new SShConnection(to1.toAddress())
                    .addListener(o.sshlistener)
            ) {
                copyLocalToRemote(((FilePath.LocalFilePath) from).getFile(), p, o.mkdir, session);
            }
        } else if (from.getProtocol().equals("ssh") && to.getProtocol().equals("file")) {
            SshPath from1 = ((FilePath.SshFilePath) from).getSshPath();
            File to1 = ((FilePath.LocalFilePath) to).getFile();
            if (to1.isDirectory() || to.getPath().endsWith("/") || to.getPath().endsWith("\\")) {
                to1 = new File(to1, FileUtils.getFileName(from1.getPath()));
            }
            try (SShConnection session = new SShConnection(from1.toAddress())
                    .addListener(o.sshlistener)
            ) {
                session.copyRemoteToLocal(from1.getPath(), to1.getPath(), o.mkdir);
            }
        } else if (from.getProtocol().equals("url") && to.getProtocol().equals("file")) {
            URL from1 = ((FilePath.URLFilePath) from).getURL();
            File to1 = ((FilePath.LocalFilePath) to).getFile();
            if (to1.isDirectory() || to.getPath().endsWith("/") || to.getPath().endsWith("\\")) {
                to1 = new File(to1, URLUtils.getURLName(from1));
            }
            if (o.mkdir) {
                FileUtils.createParents(to1);
            }
            context.out().printf("[[\\[CP\\]]] %s -> %s%\n",from ,to);
            IOUtils.copy(from1, to1);
        } else {
            throw new RuntimeIOException("cp: Unsupported protocols " + from + "->" + to);
        }
    }

    private void copyLocalToRemote(File from, String to, boolean mkdir, SShConnection session) {
        if (from.isDirectory()) {
            if (mkdir) {
                session.mkdir(to, true);
            }
            for (File file : from.listFiles()) {
                copyLocalToRemote(file, to + "/" + file.getName(), mkdir, session);
            }
        } else if (from.isFile()) {
//            String p = FileUtils.getFileParentPath(to);
//            if (p != null) {
//                session.mkdir(p, true);
//            }
            session.copyLocalToRemote(from.getPath(), to, mkdir);
        }
    }

}
