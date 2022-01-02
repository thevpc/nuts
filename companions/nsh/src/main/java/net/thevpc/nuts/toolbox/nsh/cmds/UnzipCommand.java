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
        NutsArgument a;
        if ((a = commandLine.nextBoolean("-l")) != null) {
            options.l = a.getBooleanValue();
            return true;
        } else if ((a = commandLine.nextString("-d")) != null) {
            options.dir = a.getValue().getString();
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
    protected void execBuiltin(NutsCommandLine commandLine, JShellExecutionContext context) {
        Options options = context.getOptions();
        if (options.files.isEmpty()) {
            commandLine.required();
        }
        NutsSession session = context.getSession();
        for (String path : options.files) {
            NutsPath file = NutsPath.of(path, session).toAbsolute(context.getShellContext().getCwd());
            try {
                if (options.l) {
                    NutsUncompress.of(session)
                            .from(file)
                            .visit(new NutsIOUncompressVisitor() {
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
                        dir = context.getShellContext().getCwd();
                    }
                    dir = context.getShellContext().getAbsolutePath(dir);
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
        List<String> files = new ArrayList<>();
    }
}
