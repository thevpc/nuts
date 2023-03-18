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
import net.thevpc.nuts.io.NIOException;
import net.thevpc.nuts.io.NUncompressVisitor;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.io.NUncompress;
import net.thevpc.nuts.spi.NComponentScope;
import net.thevpc.nuts.spi.NComponentScopeType;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.toolbox.nsh.cmds.NShellBuiltinDefault;
import net.thevpc.nuts.toolbox.nsh.eval.NShellExecutionContext;

import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by vpc on 1/7/17.
 */
@NComponentScope(NComponentScopeType.WORKSPACE)
public class UnzipCommand extends NShellBuiltinDefault {

    public UnzipCommand() {
        super("unzip", DEFAULT_SUPPORT, Options.class);
    }

    @Override
    protected boolean onCmdNextOption(NArg arg, NCmdLine commandLine, NShellExecutionContext context) {
        Options options = context.getOptions();
        NSession session = context.getSession();
        NArg a;
        String mode = "zip";
        while (commandLine.hasNext()) {
            switch (mode) {
                case "zip": {
                    if ((a = commandLine.nextFlag("-l").orNull()) != null) {
                        options.l = a.getBooleanValue().get(session);
                    } else if ((a = commandLine.nextEntry("-d").orNull()) != null) {
                        options.dir = a.getStringValue().get(session);
                    } else if (!commandLine.isNextOption()) {
                        String s = commandLine.next().get(session).toString();
                        if (options.zfiles.isEmpty() || s.toLowerCase().endsWith(".zip")) {
                            options.zfiles.add(s);
                        } else {
                            options.internFiles.add(s);
                            mode = "internFiles";
                        }
                    } else {
                        commandLine.throwUnexpectedArgument();
                    }
                    break;
                }
                case "internFiles": {
                    if ((a = commandLine.nextFlag("-l").orNull()) != null) {
                        options.l = a.getBooleanValue().get(session);
                    } else if ((a = commandLine.nextEntry("-d").orNull()) != null) {
                        options.dir = a.getStringValue().get(session);
                    } else if ((a = commandLine.nextEntry("-x").orNull()) != null) {
                        options.xFiles.add(a.getStringValue().get(session));
                        mode = "xFiles";
                    } else if (!commandLine.isNextOption()) {
                        options.xFiles.add(commandLine.next().get(session).toString());
                    } else {
                        commandLine.throwUnexpectedArgument();
                    }
                    break;
                }
                case "xFiles": {
                    if ((a = commandLine.nextFlag("-l").orNull()) != null) {
                        options.l = a.getBooleanValue().get(session);
                    } else if ((a = commandLine.nextEntry("-d").orNull()) != null) {
                        options.dir = a.getStringValue().get(session);
                    } else if (!commandLine.isNextOption()) {
                        options.xFiles.add(commandLine.next().get(session).toString());
                    } else {
                        commandLine.throwUnexpectedArgument();
                    }
                    break;
                }
                default: {
                    commandLine.throwUnexpectedArgument();
                }
            }
        }
        return true;
    }

    @Override
    protected void onCmdExec(NCmdLine commandLine, NShellExecutionContext context) {
        Options options = context.getOptions();
        NSession session = context.getSession();
        if (options.zfiles.isEmpty()) {
            commandLine.throwMissingArgument();
        }
        for (String path : options.zfiles) {
            NPath file = NPath.of(path, session).toAbsolute(context.getDirectory());
            try {
                if (options.l) {
                    NUncompress.of(session)
                            .from(file)
                            .visit(new NUncompressVisitor() {
                                @Override
                                public boolean visitFolder(String path) {
                                    return true;
                                }

                                @Override
                                public boolean visitFile(String path, InputStream inputStream) {
                                    session.out().print(NMsg.ofStyled(path, NTextStyle.path()));
                                    return true;
                                }
                            }).run();
                } else {
                    String dir = options.dir;
                    if (NBlankable.isBlank(dir)) {
                        dir = context.getDirectory();
                    }
                    dir = context.getAbsolutePath(dir);
                    NUncompress.of(session)
                            .from(file)
                            .to(NPath.of(dir, session))
                            .setSkipRoot(options.skipRoot)
                            .run();
                }
            } catch (UncheckedIOException | NIOException ex) {
                throw new NExecutionException(session, NMsg.ofC("%s", ex), ex, 1);
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
    @Override
    protected boolean onCmdNextNonOption(NArg arg, NCmdLine commandLine, NShellExecutionContext context) {
        return onCmdNextOption(arg, commandLine, context);
    }
}
