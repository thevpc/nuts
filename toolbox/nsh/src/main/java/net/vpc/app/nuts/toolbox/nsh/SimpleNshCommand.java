/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 *
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * Copyright (C) 2016-2017 Taha BEN SALAH
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.toolbox.nsh;

import java.io.PrintStream;
import java.util.Arrays;
import net.vpc.app.nuts.NutsCommand;
import net.vpc.app.nuts.NutsOutputFormat;
import net.vpc.app.nuts.NutsWorkspace;

/**
 *
 * @author vpc
 */
public abstract class SimpleNshCommand extends AbstractNshCommand {

    public SimpleNshCommand(String name, int supportLevel) {
        super(name, supportLevel);
    }

    public static class SimpleNshCommandContext {

        private NutsCommandContext context;
        private Object configObject;

        public SimpleNshCommandContext(NutsCommandContext context, Object configObject) {
            this.context = context;
            this.configObject = configObject;
        }

        public NutsCommandContext getContext() {
            return context;
        }

        public NutsJavaShell getShell() {
            return context.getShell();
        }
        
        public <T> T getConfigObject() {
            return (T) configObject;
        }

        public PrintStream out() {
            return context.out();
        }

        public PrintStream err() {
            return context.err();
        }

        public void printObject(Object any) {
            context.printObject(any);
        }

        public NutsWorkspace getWorkspace() {
            return context.getWorkspace();
        }
        public NutsConsoleContext consoleContext() {
            return context.shellContext();
        }
    }

    protected abstract Object createConfiguration();

    protected abstract boolean configureFirst(NutsCommand commandLine, SimpleNshCommandContext context);

    protected abstract Object createResult(SimpleNshCommandContext context);

    @Override
    public int exec(String[] args, NutsCommandContext context) throws Exception {
        boolean conf = false;
        int maxLoops = 1000;
        boolean robustMode = false;
        NutsCommand commandLine = context.getWorkspace().parser().parseCommand(args);
        SimpleNshCommandContext context2 = new SimpleNshCommandContext(context, createConfiguration());
        while (commandLine.hasNext()) {
            if (robustMode) {
                String[] before = commandLine.toArray();
                if (!this.configureFirst(commandLine, context2)) {
                    if (!context.configureFirst(commandLine)) {
                        commandLine.unexpectedArgument();
                    }
                } else {
                    conf = true;
                }
                String[] after = commandLine.toArray();
                if (Arrays.equals(before, after)) {
                    throw new IllegalStateException("Bad implementation of configureFirst in class " + getClass().getName() + "."
                            + " Commandline is not consumed. Perhaps missing skip() class."
                            + " args = " + Arrays.toString(after));
                }
            } else {
                if (!this.configureFirst(commandLine, context2)) {
                    if (!context.configureFirst(commandLine)) {
                        commandLine.unexpectedArgument();
                    }
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
        Object result = createResult(context2);
        context.printObject(result);
        return 0;
    }

    protected void printObject(Object result, SimpleNshCommandContext context) {
        switch (context.getContext().getSession().getOutputFormat(NutsOutputFormat.PLAIN)) {
            case PLAIN: {
                printObjectPlain(result, context);
                break;
            }
            default: {
                context.printObject(result);
            }
        }
    }

    protected void printObjectPlain(Object result, SimpleNshCommandContext context) {
        context.printObject(result);
    }

}
