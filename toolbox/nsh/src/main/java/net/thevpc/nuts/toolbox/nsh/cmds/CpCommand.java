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
import net.thevpc.nuts.toolbox.nsh.util.ShellHelper;
import net.thevpc.common.io.URLUtils;
import net.thevpc.common.ssh.SShConnection;
import net.thevpc.common.io.FileUtils;
import net.thevpc.common.io.IOUtils;
import net.thevpc.common.ssh.SshPath;
import net.thevpc.common.ssh.SshXFile;
import net.thevpc.common.strings.StringUtils;
import net.thevpc.common.xfile.JavaURLXFile;
import net.thevpc.common.xfile.JavaXFile;
import net.thevpc.common.xfile.XFile;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import net.thevpc.nuts.toolbox.nsh.NshExecutionContext;
import net.thevpc.nuts.toolbox.nsh.SimpleNshBuiltin;

/**
 * Created by vpc on 1/7/17. ssh copy credits to Chanaka Lakmal from
 * https://medium.com/ldclakmal/scp-with-java-b7b7dbcdbc85
 */
@NutsSingleton
public class CpCommand extends SimpleNshBuiltin {

    public CpCommand() {
        super("cp", DEFAULT_SUPPORT);
    }

    public static class Options {

        boolean mkdir;
        ShellHelper.WsSshListener sshlistener;
        List<String> files = new ArrayList<>();
        List<XFile> xfiles = new ArrayList<>();
    }

    @Override
    protected Object createOptions() {
        return new Options();
    }

    @Override
    protected boolean configureFirst(NutsCommandLine commandLine, SimpleNshCommandContext context) {
        Options options = context.getOptions();
        NutsArgument a;
        if ((a = commandLine.nextBoolean("--mkdir")) != null) {
            options.mkdir = a.getBooleanValue();
            return true;
        } else if (commandLine.peek().isNonOption()) {
            options.files.add(commandLine.next().getString());
            return true;
        }
        return false;
    }

    @Override
    protected void createResult(NutsCommandLine commandLine, SimpleNshCommandContext context) {
        Options options = context.getOptions();
        for (String value : options.files) {
            if (StringUtils.isBlank(value)) {
                throw new NutsExecutionException(context.getWorkspace(), "Empty File Path", 2);
            }
            options.xfiles.add(XFile.of(value.contains("://") ? value : context.getWorkspace().io().expandPath(value)));
        }
        if (options.xfiles.size() < 2) {
            throw new NutsExecutionException(context.getWorkspace(), "Missing parameters", 2);
        }

        options.sshlistener = new ShellHelper.WsSshListener(context.getSession());
        for (int i = 0; i < options.xfiles.size() - 1; i++) {
            copy(options.xfiles.get(i), options.xfiles.get(options.xfiles.size() - 1), options, context.getExecutionContext());
        }
    }

    public void copy(XFile from, XFile to, Options o, NshExecutionContext context) {
        if (from.getProtocol().equals("file") && to.getProtocol().equals("file")) {
            File from1 = ((JavaXFile) from).getFile();
            File to1 = ((JavaXFile) to).getFile();
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
            if (context.getSession().isPlainTrace()) {
                context.out().printf("[[\\[CP\\]]] %s -> %s\n", from, to);
            }
            try {
                IOUtils.copy(from1, to1);
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        } else if (from.getProtocol().equals("file") && to.getProtocol().equals("ssh")) {
            SshPath to1 = ((SshXFile) to).getSshPath();
            String p = to1.getPath();
            if (p.endsWith("/") || p.endsWith("\\")) {
                p = p + "/" + FileUtils.getFileName(to1.getPath());
            }

            try (SShConnection session = new SShConnection(to1.toAddress())
                    .addListener(o.sshlistener)) {
                copyLocalToRemote(((JavaXFile) from).getFile(), p, o.mkdir, session);
            }
        } else if (from.getProtocol().equals("ssh") && to.getProtocol().equals("file")) {
            SshPath from1 = ((SshXFile) from).getSshPath();
            File to1 = ((JavaXFile) to).getFile();
            if (to1.isDirectory() || to.getPath().endsWith("/") || to.getPath().endsWith("\\")) {
                to1 = new File(to1, FileUtils.getFileName(from1.getPath()));
            }
            try (SShConnection session = new SShConnection(from1.toAddress())
                    .addListener(o.sshlistener)) {
                session.copyRemoteToLocal(from1.getPath(), to1.getPath(), o.mkdir);
            }
        } else if (from.getProtocol().equals("url") && to.getProtocol().equals("file")) {
            URL from1 = ((JavaURLXFile) from).getURL();
            File to1 = ((JavaXFile) to).getFile();
            if (to1.isDirectory() || to.getPath().endsWith("/") || to.getPath().endsWith("\\")) {
                to1 = new File(to1, URLUtils.getURLName(from1));
            }
            if (o.mkdir) {
                FileUtils.createParents(to1);
            }
            if (context.getSession().isPlainTrace()) {
                context.out().printf("[[\\[CP\\]]] %s -> %s\n", from, to);
            }
            try {
                IOUtils.copy(from1, to1);
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        } else {
            throw new NutsIllegalArgumentException(context.getWorkspace(), "cp: Unsupported protocols " + from + "->" + to);
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