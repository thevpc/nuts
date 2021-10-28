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

import net.thevpc.nuts.NutsTextCode;
import net.thevpc.nuts.NutsUtilStrings;
import net.thevpc.nuts.spi.NutsSingleton;
import net.thevpc.nuts.toolbox.nsh.SimpleNshBuiltin;
import net.thevpc.nuts.NutsCommandLine;

/**
 * Created by vpc on 1/7/17.
 */
@NutsSingleton
public class EchoCommand extends SimpleNshBuiltin {

    public EchoCommand() {
        super("echo", DEFAULT_SUPPORT);
    }

    @Override
    protected Object createOptions() {
        return new Options();
    }

    @Override
    protected boolean configureFirst(NutsCommandLine commandLine, SimpleNshCommandContext context) {
        Options options = context.getOptions();
        switch (commandLine.peek().getKey().getString()) {
            case "-n": {
                options.newLine = !commandLine.nextBoolean().getValue().getBoolean();
                return true;
            }
            case "-p":
            case "--plain": {
                options.highlighter = null;
                return true;
            }
            case "-H":
            case "--highlighter": {
                options.highlighter = NutsUtilStrings.trim(commandLine.nextString().getValue().getString());
                return true;
            }
            default: {
                if (commandLine.peek().isNonOption()) {
                    while (commandLine.hasNext()) {
                        if (options.tokensCount > 0) {
                            options.message.append(" ");
                        }
                        options.message.append(commandLine.next().toString());
                        options.tokensCount++;
                    }
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    protected void execBuiltin(NutsCommandLine commandLine, SimpleNshCommandContext context) {
        Options options = context.getOptions();
        Object ns = null;
        if (options.highlighter == null || "text".equalsIgnoreCase(options.highlighter) || "plain".equalsIgnoreCase(options.highlighter)) {
            ns = options.message.toString();
        } else {
            NutsTextCode c = context.getSession().text().ofCode(options.highlighter, options.message.toString());
            ns = c.highlight(context.getSession());
        }
        if (options.newLine) {
            context.getSession().out().printlnf(ns);
        } else {
            context.getSession().out().printf(ns);
        }
    }

    private static class Options {

        boolean newLine = true;
        String highlighter = null;
        boolean first = true;
        StringBuilder message = new StringBuilder();
        int tokensCount = 0;
    }
}
