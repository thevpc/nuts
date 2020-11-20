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

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Arrays;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.NutsWorkspace;
import net.thevpc.nuts.NutsCommandLine;
import net.thevpc.nuts.NutsObjectFormat;

/**
 *
 * @author vpc
 */
public abstract class SimpleNshBuiltin extends AbstractNshBuiltin {

    public SimpleNshBuiltin(String name, int supportLevel) {
        super(name, supportLevel);
    }

    public static class SimpleNshCommandContext {

        private NshExecutionContext context;
        private String[] args;
        private Object options;
        private int exitCode = 0;
        private Object outObject;
        private Object errObject;
        private boolean err;
        private boolean outObjectNewLine;
        private boolean errObjectNewLine;

        public SimpleNshCommandContext(String[] args, NshExecutionContext context, Object configObject) {
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

        public void setPrintOutObject(Object outObject) {
            this.outObject = outObject;
            this.outObjectNewLine = false;
        }

        public void setPrintErrObject(Object outObject) {
            this.errObject = outObject;
            this.errObjectNewLine = false;
        }

        public void setPrintlnOutObject(Object outObject) {
            this.outObject = outObject;
            this.outObjectNewLine = true;
        }

        public void setPrintlnErrObject(Object outObject) {
            this.errObject = outObject;
            this.errObjectNewLine = true;
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

        public NshExecutionContext getExecutionContext() {
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

        public InputStream in() {
            return context.in();
        }

        public SimpleNshCommandContext setErr(boolean err) {
            this.err = err;
            return this;
        }

        public PrintStream err() {
            return context.err();
        }

        public void printObject(Object any) {
            NutsObjectFormat objstream = context.getSession().formatObject(any);
            if (err) {
                if (errObjectNewLine) {
                    objstream.println(context.getSession().err());
                } else {
                    objstream.print(context.getSession().err());
                }
            } else {
                if (outObjectNewLine) {
                    objstream.println();
                } else {
                    objstream.print();
                }
            }
        }

        public NutsWorkspace getWorkspace() {
            return context.getWorkspace();
        }

        public String getCwd() {
            return context.getGlobalContext().getCwd();
        }

        public NutsShellContext getRootContext() {
            return context.getGlobalContext();
        }
    }

    protected abstract Object createOptions();

    protected abstract boolean configureFirst(NutsCommandLine commandLine, SimpleNshCommandContext context);

    protected void prepareOptions(NutsCommandLine commandLine, SimpleNshCommandContext context) {

    }

    protected abstract void createResult(NutsCommandLine commandLine, SimpleNshCommandContext context);

    @Override
    public final void exec(String[] args, NshExecutionContext context) {
        boolean conf = false;
        int maxLoops = 1000;
        boolean robustMode = false;
        NutsCommandLine commandLine = context.getWorkspace().commandLine().create(args);
        SimpleNshCommandContext context2 = new SimpleNshCommandContext(args, context, createOptions());
        while (commandLine.hasNext()) {
            if (robustMode) {
                String[] before = commandLine.toStringArray();
                if (!this.configureFirst(commandLine, context2)) {
                    if (!context.configureFirst(commandLine)) {
                        commandLine.unexpectedArgument();
                    }
                } else {
                    conf = true;
                }
                String[] after = commandLine.toStringArray();
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
        prepareOptions(commandLine, context2);
        createResult(commandLine, context2);
        final Object outObject = context2.getOutObject();
        if (outObject != null) {
            printObject(context2.setErr(false));
        }
        final Object errObject = context2.getErrObject();
        if (errObject != null) {
            printObject(context2.setErr(true));
        }
    }

    protected void printObject(SimpleNshCommandContext context) {
        NutsSession session = context.getExecutionContext().getSession();
        if (session.isIterableTrace()) {
            //already processed
        } else {
            switch (session.getOutputFormat()) {
                case PLAIN: {
                    printPlainObject(context);
                    break;
                }
                default: {
                    context.printObject(context.getResult());
                }
            }
        }
    }

    protected void printPlainObject(SimpleNshCommandContext context) {
        context.printObject(context.getResult());
    }

}