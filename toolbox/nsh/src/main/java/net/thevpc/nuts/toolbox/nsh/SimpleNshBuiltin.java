/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
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
package net.thevpc.nuts.toolbox.nsh;

import net.thevpc.nuts.*;
import net.thevpc.nuts.toolbox.nsh.bundles.jshell.JShell;
import net.thevpc.nuts.toolbox.nsh.bundles.jshell.JShellContext;
import net.thevpc.nuts.toolbox.nsh.bundles.jshell.JShellExecutionContext;

import java.io.InputStream;
import java.util.Arrays;

/**
 *
 * @author thevpc
 */
public abstract class SimpleNshBuiltin extends AbstractNshBuiltin {

    public SimpleNshBuiltin(String name, int supportLevel) {
        super(name, supportLevel);
    }

    public static class SimpleNshCommandContext {

        private JShellExecutionContext context;
        private String[] args;
        private Object options;

        public SimpleNshCommandContext(String[] args, JShellExecutionContext context, Object configObject) {
            this.context = context;
            this.options = configObject;
            this.args = args;
        }

        public String[] getArgs() {
            return args;
        }

        public JShellExecutionContext getExecutionContext() {
            return context;
        }

        public JShell getShell() {
            return context.getShell();
        }

        public NutsSession getSession() {
            return context.getSession();
        }

        public <T> T getOptions() {
            return (T) options;
        }

        public InputStream in() {
            return context.in();
        }

        public NutsPrintStream err() {
            return context.err();
        }

        public NutsWorkspace getWorkspace() {
            return context.getWorkspace();
        }

        public String getCwd() {
            return context.getShellContext().getCwd();
        }

        public JShellContext getGlobalContext() {
            return context.getShellContext();
        }

        public JShellContext getRootContext() {
            return context.getShellContext();
        }

    }

    protected abstract Object createOptions();

    protected abstract boolean configureFirst(NutsCommandLine commandLine, SimpleNshCommandContext context);

    protected abstract void execBuiltin(NutsCommandLine commandLine, SimpleNshCommandContext context);

    @Override
    public int execImpl(String[] args, JShellExecutionContext context) {
        boolean conf = false;
        int maxLoops = 1000;
        boolean robustMode = false;
        NutsSession session = context.getSession();
        NutsCommandLine commandLine = NutsCommandLine.of(args,session).setCommandName(getName());
        initCommandLine(commandLine);
        SimpleNshCommandContext context2 = new SimpleNshCommandContext(args, context, createOptions());
        while (commandLine.hasNext()) {
            if (robustMode) {
                String[] before = commandLine.toStringArray();
                if (!this.configureFirst(commandLine, context2)) {
                    context.configureLast(commandLine);
                } else {
                    conf = true;
                }
                String[] after = commandLine.toStringArray();
                if (Arrays.equals(before, after)) {
                    throw new IllegalStateException("bad implementation of configureFirst in class " + getClass().getName() + "."
                            + " Commandline is not consumed; perhaps missing skip() class."
                            + " args = " + Arrays.toString(after));
                }
            } else {
                if (!this.configureFirst(commandLine, context2)) {
                    context.configureLast(commandLine);
                } else {
                    conf = true;
                }
            }
            maxLoops--;
            if (maxLoops < 0) {
                robustMode = true;
            }
        }
        if (commandLine.isAutoCompleteMode()) {
            return 0;
        }
        execBuiltin(commandLine, context2);
        return 0;
    }


    protected void initCommandLine(NutsCommandLine commandLine) {

    }

}
