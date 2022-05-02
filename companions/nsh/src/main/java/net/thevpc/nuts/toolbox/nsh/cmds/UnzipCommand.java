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
import net.thevpc.nuts.io.NutsIOException;
import net.thevpc.nuts.io.NutsUncompressVisitor;
import net.thevpc.nuts.io.NutsPath;
import net.thevpc.nuts.io.NutsUncompress;
import net.thevpc.nuts.spi.NutsComponentScope;
import net.thevpc.nuts.spi.NutsComponentScopeType;
import net.thevpc.nuts.toolbox.nsh.SimpleJShellBuiltin;
import net.thevpc.nuts.toolbox.nsh.jshell.JShellExecutionContext;

import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by vpc on 1/7/17.
 */
@NutsComponentScope(NutsComponentScopeType.WORKSPACE)
public class UnzipCommand extends SimpleJShellBuiltin {

    public UnzipCommand() {
        super("unzip", DEFAULT_SUPPORT,Options.class);
    }

    @Override
    protected boolean configureFirst(NutsCommandLine commandLine, JShellExecutionContext context) {
        Options options = context.getOptions();
        NutsSession session = context.getSession();
        NutsArgument a;
        String mode="zip";
        while(commandLine.hasNext()){
            switch (mode){
                case "zip":{
                    if ((a = commandLine.nextBoolean("-l").orNull()) != null) {
                        options.l = a.getBooleanValue().get(session);
                    } else if ((a = commandLine.nextString("-d").orNull()) != null) {
                        options.dir = a.getStringValue().get(session);
                    } else if (!commandLine.isNextOption()) {
                        String s = commandLine.next().get(session).toString();
                        if(options.zfiles.isEmpty()||s.toLowerCase().endsWith(".zip")) {
                            options.zfiles.add(s);
                        }else{
                            options.internFiles.add(s);
                            mode="internFiles";
                        }
                    }else{
                        commandLine.throwUnexpectedArgument(session);
                    }
                    break;
                }
                case "internFiles":{
                    if ((a = commandLine.nextBoolean("-l").orNull()) != null) {
                        options.l = a.getBooleanValue().get(session);
                    } else if ((a = commandLine.nextString("-d").orNull()) != null) {
                        options.dir = a.getStringValue().get(session);
                    } else if ((a = commandLine.nextString("-x").orNull()) != null) {
                        options.xFiles.add(a.getStringValue().get(session));
                        mode="xFiles";
                    } else if (!commandLine.isNextOption()) {
                        options.xFiles.add(commandLine.next().get(session).toString());
                    }else{
                        commandLine.throwUnexpectedArgument(session);
                    }
                    break;
                }
                case "xFiles":{
                    if ((a = commandLine.nextBoolean("-l").orNull()) != null) {
                        options.l = a.getBooleanValue().get(session);
                    } else if ((a = commandLine.nextString("-d").orNull()) != null) {
                        options.dir = a.getStringValue().get(session);
                    } else if (!commandLine.isNextOption()) {
                        options.xFiles.add(commandLine.next().get(session).toString());
                    }else{
                        commandLine.throwUnexpectedArgument(session);
                    }
                    break;
                }
                default:{
                    commandLine.throwUnexpectedArgument(session);
                }
            }
        }
        return true;
    }

    @Override
    protected void execBuiltin(NutsCommandLine commandLine, JShellExecutionContext context) {
        Options options = context.getOptions();
        NutsSession session = context.getSession();
        if (options.zfiles.isEmpty()) {
            commandLine.throwMissingArgument(session);
        }
        for (String path : options.zfiles) {
            NutsPath file = NutsPath.of(path, session).toAbsolute(context.getCwd());
            try {
                if (options.l) {
                    NutsUncompress.of(session)
                            .from(file)
                            .visit(new NutsUncompressVisitor() {
                                @Override
                                public boolean visitFolder(String path) {
                                    return true;
                                }

                                @Override
                                public boolean visitFile(String path, InputStream inputStream) {
                                    session.out().printf("%s\n", path);
                                    return true;
                                }
                            });
                } else {
                    String dir = options.dir;
                    if (NutsBlankable.isBlank(dir)) {
                        dir = context.getCwd();
                    }
                    dir = context.getAbsolutePath(dir);
                    NutsUncompress.of(session)
                            .from(file)
                            .to(dir)
                            .setSkipRoot(options.skipRoot)
                            .run();
                }
            } catch (UncheckedIOException | NutsIOException ex) {
                throw new NutsExecutionException(session, NutsMessage.cstyle("%s", ex), ex, 1);
            }
        }
    }

    private static class Options {

        boolean l = false;
        boolean skipRoot = false;
        String dir = null;
        List<String> zfiles = new ArrayList<>();
        List<String> internFiles = new ArrayList<>();
        List<String> xFiles = new ArrayList<>();
    }
}
