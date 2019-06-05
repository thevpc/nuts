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
import net.vpc.app.nuts.NutsSession;
import net.vpc.app.nuts.NutsWorkspace;

/**
 *
 * @author vpc
 */
public abstract class SimpleNshCommand extends AbstractNshBuiltin {

    public SimpleNshCommand(String name, int supportLevel) {
        super(name, supportLevel);
    }

    public static class SimpleNshCommandContext {

        private NutsCommandContext context;
        private String[] args;
        private Object options;
        private int exitCode = 0;
        private Object outObject;
        private Object errObject;
        private boolean err;

        public SimpleNshCommandContext(String[] args, NutsCommandContext context, Object configObject) {
            this.context = context;
            this.options = configObject;
            this.args = args;
        }

        public String[] getArgs() {
            return args;
        }

        public <T> T getResult() {
            return (T) (err ? errObject : outObject);
        }

        public Object getOutObject() {
            return outObject;
        }

        public void setOutObject(Object outObject) {
            this.outObject = outObject;
        }

        public Object getErrObject() {
            return errObject;
        }

        public void setErrObject(Object errObject) {
            this.errObject = errObject;
        }

        public int getExitCode() {
            return exitCode;
        }

        public void setExitCode(int exitCode) {
            this.exitCode = exitCode;
        }

        public NutsCommandContext getCommandContext() {
            return context;
        }

        public NutsJavaShell getShell() {
            return context.getShell();
        }

        public NutsSession getSession() {
            return context.getSession();
        }

        public <T> T getOptions() {
            return (T) options;
        }

        public PrintStream out() {
            return err ? context.err() : context.out();
        }

        public SimpleNshCommandContext setErr(boolean err) {
            this.err = err;
            return this;
        }

        public PrintStream err() {
            return context.err();
        }

        public void printObject(Object any) {
            if (err) {
                context.printErrObject(any);
            } else {
                context.printOutObject(any);
            }
        }

        public NutsWorkspace getWorkspace() {
            return context.getWorkspace();
        }

        public NutsShellContext getGlobalContext() {
            return context.getGlobalContext();
        }
    }

    protected abstract Object createOptions();

    protected abstract boolean configureFirst(NutsCommand commandLine, SimpleNshCommandContext context);

    protected abstract void createResult(NutsCommand commandLine, SimpleNshCommandContext context);

    @Override
    public final void exec(String[] args, NutsCommandContext context){
        boolean conf = false;
        int maxLoops = 1000;
        boolean robustMode = false;
        NutsCommand commandLine = context.getWorkspace().parser().parseCommand(args);
        SimpleNshCommandContext context2 = new SimpleNshCommandContext(args, context, createOptions());
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
            return;
        }
        createResult(commandLine, context2);
        final Object outObject = context2.getOutObject();
        if (outObject != null) {
            printObject(outObject, context2.setErr(false));
        }
        final Object errObject = context2.getErrObject();
        if (errObject != null) {
            printObject(outObject, context2.setErr(true));
        }
    }

    protected void printObject(Object result, SimpleNshCommandContext context) {
        NutsSession session = context.getCommandContext().getSession();
        if (session.isIncrementalTrace()) {
            //already processed
        } else {
            switch (session.getOutputFormat()) {
                case PLAIN: {
                    printObjectPlain(context);
                    break;
                }
                default: {
                    printObject0(context);
                }
            }
        }
    }

    protected void printObjectPlain(SimpleNshCommandContext context) {
        printObject0(context);
    }

    protected void printObject0(SimpleNshCommandContext context) {
        context.printObject(context.getResult());
    }

}
