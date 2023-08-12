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
 * <p>
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
package net.thevpc.nuts.toolbox.nsh.cmds.posix;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.io.NCp;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.spi.NComponentScope;
import net.thevpc.nuts.spi.NScopeType;
import net.thevpc.nuts.toolbox.nsh.cmds.NShellBuiltinDefault;
import net.thevpc.nuts.toolbox.nsh.eval.NShellExecutionContext;
import net.thevpc.nuts.toolbox.nsh.util.ShellHelper;
import net.thevpc.nuts.util.NAssert;

import java.util.ArrayList;
import java.util.List;

@NComponentScope(NScopeType.WORKSPACE)
public class CpCommand extends NShellBuiltinDefault {

    public CpCommand() {
        super("cp", NConstants.Support.DEFAULT_SUPPORT, Options.class);
    }


    @Override
    protected boolean onCmdNextNonOption(NArg arg, NCmdLine cmdLine, NShellExecutionContext context) {
        Options options = context.getOptions();
        options.files.add(cmdLine.next().get().toString());
        return true;
    }

    @Override
    protected boolean onCmdNextOption(NArg arg, NCmdLine cmdLine, NShellExecutionContext context) {
        Options options = context.getOptions();
        NSession session = context.getSession();
        switch (cmdLine.peek().get(session).key()) {
            case "--mkdir": {
                cmdLine.withNextFlag((v, a, s) -> options.mkdir = v);
                return true;
            }
            case "-r":
            case "-R":
            case "--recursive": {
                cmdLine.withNextFlag((v, a, s) -> options.recursive = v);
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onCmdExec(NCmdLine cmdLine, NShellExecutionContext context) {
        Options options = context.getOptions();
        NSession session = context.getSession();
        for (String value : options.files) {
            NAssert.requireNonBlank(value, "file path", session);
            options.xfiles.add(NPath.of((value.contains("://") ? value :
                    NPath.of(value, session).toAbsolute(NLocations.of(session).getWorkspaceLocation()).toString()
            ), session));
        }
        if (options.xfiles.size() < 2) {
            throw new NExecutionException(session, NMsg.ofPlain("missing parameters"), NExecutionException.ERROR_2);
        }

        options.sshlistener = new ShellHelper.WsSshListener(session);
        for (int i = 0; i < options.xfiles.size() - 1; i++) {
            copy(options.xfiles.get(i), options.xfiles.get(options.xfiles.size() - 1), options, context);
        }
    }

    public void copy(NPath from, NPath to, Options o, NShellExecutionContext context) {
        NSession session = context.getSession();
        NCp ccp = NCp.of(session)
                .from(from)
                .to(to)
                .setRecursive(o.recursive)
                .setMkdirs(o.mkdir);
        ccp.run();
//        if (from.getProtocol().equals("file") && to.getProtocol().equals("file")) {
//            File from1 = ((JavaXFile) from).getFile();
//            File to1 = ((JavaXFile) to).getFile();
//            if (from1.isFile()) {
//                if (to1.isDirectory() || to.getPath().endsWith("/") || to.getPath().endsWith("\\")) {
//                    to1 = new File(to1, from1.getName());
//                }
//            } else if (to1.isDirectory()) {
//                if (to.getPath().endsWith("/") || to.getPath().endsWith("\\")) {
//                    to1 = new File(to1, from1.getName());
//                }
//            }
//            if (o.mkdir) {
//                FileUtils.createParents(to1);
//            }
//            if(from1.isDirectory()){
//                if(o.recursive) {
//                    copyFolder(from1, to1);
//                }else{
//                    copyFolder(from1, to1);
//                }
//            }
//            if (context.getSession().isPlainTrace()) {
//                context.out().print(NMsg.ofC("[[\\[CP\\]]] %s -> %s\n", from, to);
//            }
//            try {
//                IOUtils.copy(from1, to1);
//            } catch (IOException ex) {
//                throw new UncheckedIOException(ex);
//            }
//        } else if (from.getProtocol().equals("file") && to.getProtocol().equals("ssh")) {
//            SshPath to1 = ((SshXFile) to).getSshPath();
//            String p = to1.getPath();
//            if (p.endsWith("/") || p.endsWith("\\")) {
//                p = p + "/" + FileUtils.getFileName(to1.getPath());
//            }
//
//            try (SShConnection session = new SShConnection(to1.toAddress())
//                    .addListener(o.sshlistener)) {
//                copyLocalToRemote(((JavaXFile) from).getFile(), p, o.mkdir, session);
//            }
//        } else if (from.getProtocol().equals("ssh") && to.getProtocol().equals("file")) {
//            SshPath from1 = ((SshXFile) from).getSshPath();
//            File to1 = ((JavaXFile) to).getFile();
//            if (to1.isDirectory() || to.getPath().endsWith("/") || to.getPath().endsWith("\\")) {
//                to1 = new File(to1, FileUtils.getFileName(from1.getPath()));
//            }
//            try (SShConnection session = new SShConnection(from1.toAddress())
//                    .addListener(o.sshlistener)) {
//                session.copyRemoteToLocal(from1.getPath(), to1.getPath(), o.mkdir);
//            }
//        } else if (from.getProtocol().equals("url") && to.getProtocol().equals("file")) {
//            URL from1 = ((JavaURLXFile) from).getURL();
//            File to1 = ((JavaXFile) to).getFile();
//            if (to1.isDirectory() || to.getPath().endsWith("/") || to.getPath().endsWith("\\")) {
//                to1 = new File(to1, URLUtils.getURLName(from1));
//            }
//            if (o.mkdir) {
//                FileUtils.createParents(to1);
//            }
//            if (context.getSession().isPlainTrace()) {
//                context.out().print(NMsg.ofC("[[\\[CP\\]]] %s -> %s\n", from, to);
//            }
//            try {
//                IOUtils.copy(from1, to1);
//            } catch (IOException ex) {
//                throw new UncheckedIOException(ex);
//            }
//        } else {
//            throw new NutsIllegalArgumentException(context.getSession(), "cp: unsupported protocols " + from + "->" + to);
//        }
    }

    public static class Options {

        boolean mkdir;
        boolean recursive;
        ShellHelper.WsSshListener sshlistener;
        List<String> files = new ArrayList<>();
        List<NPath> xfiles = new ArrayList<>();
    }


}
