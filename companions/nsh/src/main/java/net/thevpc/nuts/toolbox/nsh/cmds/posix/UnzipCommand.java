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
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License"); 
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
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
import net.thevpc.nuts.spi.NScopeType;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.toolbox.nsh.cmds.NShellBuiltinDefault;
import net.thevpc.nuts.toolbox.nsh.eval.NShellExecutionContext;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NMsg;

import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by vpc on 1/7/17.
 */
@NComponentScope(NScopeType.WORKSPACE)
public class UnzipCommand extends NShellBuiltinDefault {

    public UnzipCommand() {
        super("unzip", NConstants.Support.DEFAULT_SUPPORT, Options.class);
    }

    @Override
    protected boolean nextOption(NArg arg, NCmdLine cmdLine, NShellExecutionContext context) {
        Options options = context.getOptions();
        NSession session = context.getSession();
        NArg a;
        String mode = "zip";
        while (cmdLine.hasNext()) {
            switch (mode) {
                case "zip": {
                    if ((a = cmdLine.nextFlag("-l").orNull()) != null) {
                        options.l = a.getBooleanValue().get();
                    } else if ((a = cmdLine.nextEntry("-d").orNull()) != null) {
                        options.dir = a.getStringValue().get();
                    } else if (!cmdLine.isNextOption()) {
                        String s = cmdLine.next().get().toString();
                        if (options.zfiles.isEmpty() || s.toLowerCase().endsWith(".zip")) {
                            options.zfiles.add(s);
                        } else {
                            options.internFiles.add(s);
                            mode = "internFiles";
                        }
                    } else {
                        cmdLine.throwUnexpectedArgument();
                    }
                    break;
                }
                case "internFiles": {
                    if ((a = cmdLine.nextFlag("-l").orNull()) != null) {
                        options.l = a.getBooleanValue().get();
                    } else if ((a = cmdLine.nextEntry("-d").orNull()) != null) {
                        options.dir = a.getStringValue().get();
                    } else if ((a = cmdLine.nextEntry("-x").orNull()) != null) {
                        options.xFiles.add(a.getStringValue().get());
                        mode = "xFiles";
                    } else if (!cmdLine.isNextOption()) {
                        options.xFiles.add(cmdLine.next().get().toString());
                    } else {
                        cmdLine.throwUnexpectedArgument();
                    }
                    break;
                }
                case "xFiles": {
                    if ((a = cmdLine.nextFlag("-l").orNull()) != null) {
                        options.l = a.getBooleanValue().get();
                    } else if ((a = cmdLine.nextEntry("-d").orNull()) != null) {
                        options.dir = a.getStringValue().get();
                    } else if (!cmdLine.isNextOption()) {
                        options.xFiles.add(cmdLine.next().get().toString());
                    } else {
                        cmdLine.throwUnexpectedArgument();
                    }
                    break;
                }
                default: {
                    cmdLine.throwUnexpectedArgument();
                }
            }
        }
        return true;
    }

    @Override
    protected void main(NCmdLine cmdLine, NShellExecutionContext context) {
        Options options = context.getOptions();
        NSession session = context.getSession();
        if (options.zfiles.isEmpty()) {
            cmdLine.throwMissingArgument();
        }
        for (String path : options.zfiles) {
            NPath file = NPath.of(path).toAbsolute(context.getDirectory());
            try {
                if (options.l) {
                    NUncompress.of()
                            .from(file)
                            .visit(new NUncompressVisitor() {
                                @Override
                                public boolean visitFolder(String path) {
                                    return true;
                                }

                                @Override
                                public boolean visitFile(String path, InputStream inputStream) {
                                    NOut.print(NMsg.ofStyledPath(path));
                                    return true;
                                }
                            }).run();
                } else {
                    String dir = options.dir;
                    if (NBlankable.isBlank(dir)) {
                        dir = context.getDirectory();
                    }
                    dir = context.getAbsolutePath(dir);
                    NUncompress.of()
                            .from(file)
                            .to(NPath.of(dir))
                            .setSkipRoot(options.skipRoot)
                            .run();
                }
            } catch (UncheckedIOException | NIOException ex) {
                throw new NExecutionException(NMsg.ofC("%s", ex), ex, NExecutionException.ERROR_1);
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
    protected boolean nextNonOption(NArg arg, NCmdLine cmdLine, NShellExecutionContext context) {
        return nextOption(arg, cmdLine, context);
    }
}
