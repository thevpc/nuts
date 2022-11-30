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
package net.thevpc.nuts.toolbox.nsh.cmds;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NutsArgument;
import net.thevpc.nuts.cmdline.NutsCommandLine;
import net.thevpc.nuts.io.NutsCp;
import net.thevpc.nuts.io.NutsPath;
import net.thevpc.nuts.spi.NutsComponentScope;
import net.thevpc.nuts.spi.NutsComponentScopeType;
import net.thevpc.nuts.toolbox.nsh.SimpleJShellBuiltin;
import net.thevpc.nuts.toolbox.nsh.jshell.JShellExecutionContext;
import net.thevpc.nuts.toolbox.nsh.util.ShellHelper;
import net.thevpc.nuts.util.NutsUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vpc on 1/7/17. ssh copy credits to Chanaka Lakmal from
 * https://medium.com/ldclakmal/scp-with-java-b7b7dbcdbc85
 */
@NutsComponentScope(NutsComponentScopeType.WORKSPACE)
public class CpCommand extends SimpleJShellBuiltin {

    public CpCommand() {
        super("cp", DEFAULT_SUPPORT,Options.class);
    }


    @Override
    protected boolean configureFirst(NutsCommandLine commandLine, JShellExecutionContext context) {
        Options options = context.getOptions();
        NutsSession session = context.getSession();
        switch (commandLine.peek().get(session).key()) {
            case "--mkdir": {
                commandLine.withNextBoolean((v, a, s) -> options.mkdir=v,session);
                return true;
            }
            case "-r":
            case "-R":
            case "--recursive": {
                commandLine.withNextBoolean((v, a, s) -> options.recursive=v,session);
                return true;
            }
            default: {
                if (commandLine.peek().get(session).isNonOption()) {
                    commandLine.withNextString((v, a, s) -> options.files.add(v),session);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    protected void execBuiltin(NutsCommandLine commandLine, JShellExecutionContext context) {
        Options options = context.getOptions();
        NutsSession session = context.getSession();
        for (String value : options.files) {
            NutsUtils.requireNonBlank(value, "file path", session);
            options.xfiles.add(NutsPath.of((value.contains("://") ? value :
                    NutsPath.of(value, session).toAbsolute(session.locations().getWorkspaceLocation()).toString()
            ), session));
        }
        if (options.xfiles.size() < 2) {
            throw new NutsExecutionException(session, NutsMessage.ofPlain("missing parameters"), 2);
        }

        options.sshlistener = new ShellHelper.WsSshListener(session);
        for (int i = 0; i < options.xfiles.size() - 1; i++) {
            copy(options.xfiles.get(i), options.xfiles.get(options.xfiles.size() - 1), options, context);
        }
    }

    public void copy(NutsPath from, NutsPath to, Options o, JShellExecutionContext context) {
        NutsSession session = context.getSession();
        NutsCp ccp = NutsCp.of(session)
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
//                context.out().printf("[[\\[CP\\]]] %s -> %s\n", from, to);
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
//                context.out().printf("[[\\[CP\\]]] %s -> %s\n", from, to);
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
        List<NutsPath> xfiles = new ArrayList<>();
    }

//    private void copyFolder(File from1, File to1) {
//        try {
//            Files.walk(from1.toPath())
//                    .forEach(source -> {
//                        Path destination = Paths.get(to1.getPath(), source.toString()
//                                .substring(from1.getPath().length()));
//                        if(Files.isDirectory(source)){
//                            destination.toFile().mkdirs();
//                        }else {
//                            FileUtils.createParents(destination.toFile());
//                            try {
//                                Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
//                            } catch (IOException e) {
//                                throw new UncheckedIOException(e);
//                            }
//                        }
//                    });
//        } catch (IOException e) {
//            throw new UncheckedIOException(e);
//        }
//    }

//    private void copyLocalToRemote(File from, String to, boolean mkdir, SShConnection session) {
//        if (from.isDirectory()) {
//            if (mkdir) {
//                session.mkdir(to, true);
//            }
//            for (File file : from.listFiles()) {
//                copyLocalToRemote(file, to + "/" + file.getName(), mkdir, session);
//            }
//        } else if (from.isFile()) {
////            String p = FileUtils.getFileParentPath(to);
////            if (p != null) {
////                session.mkdir(p, true);
////            }
//            session.copyLocalToRemote(from.getPath(), to, mkdir);
//        }
//    }

}
