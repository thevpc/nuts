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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Arrays;

import net.thevpc.nuts.toolbox.nsh.bundles.jshell.JShell;
import net.thevpc.nuts.toolbox.nsh.bundles.jshell.JShellContext;
import net.thevpc.nuts.toolbox.nsh.bundles.jshell.JShellExecutionContext;
import net.thevpc.nuts.toolbox.nsh.bundles.jshell.JShellFileContext;
import net.thevpc.nuts.*;

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
        private int exitCode = 0;
        private Object outObject;
        private Object errObject;
        private boolean err;
        private boolean outObjectNewLine;
        private boolean errObjectNewLine;

        public SimpleNshCommandContext(String[] args, JShellExecutionContext context, Object configObject) {
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

        public boolean isErr() {
            return err;
        }
        public NutsPrintStream out() {
            return isErr() ? context.err() : context.out();
        }

        public InputStream in() {
            return context.in();
        }

        public SimpleNshCommandContext setErr(boolean err) {
            this.err = err;
            return this;
        }

        public NutsPrintStream err() {
            return context.err();
        }

        public void printObject(Object any) {
            printObject(any,null);
        }

        public void printObject(Object any,NutsSession session) {
            if(session==null){
                session=context.getSession();
            }
            NutsObjectFormat objstream = session.getWorkspace().formats().object(any);
            if (err) {
                if (errObjectNewLine) {
                    objstream.println(session.err());
                } else {
                    objstream.print(session.err());
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

        public JShellFileContext getGlobalContext() {
            return context.getGlobalContext();
        }

        public JShellContext getRootContext() {
            return context.getNutsShellContext();
        }

    }

    protected abstract Object createOptions();

    protected abstract boolean configureFirst(NutsCommandLine commandLine, SimpleNshCommandContext context);

    protected void prepareOptions(NutsCommandLine commandLine, SimpleNshCommandContext context) {

    }

    protected abstract void createResult(NutsCommandLine commandLine, SimpleNshCommandContext context);

    @Override
    public int execImpl(String[] args, JShellExecutionContext context) {
        boolean conf = false;
        int maxLoops = 1000;
        boolean robustMode = false;
        NutsCommandLine commandLine = context.getWorkspace().commandLine().create(args).setCommandName(getName());
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
        prepareOptions(commandLine, context2);
        createResult(commandLine, context2);
        if(context2.getExitCode()==0){
            final Object outObject = context2.getOutObject();
            if (outObject != null) {
                printObject(context2.setErr(false), null);
            }
            final Object errObject = context2.getErrObject();
            if (errObject != null) {
                printObject(context2.setErr(true), null);
            }
        }else{
            NutsSession session = context.getSession().copy();
            NutsPrintStream printStream = session.getWorkspace().io().createMemoryPrintStream();
            session.setTerminal(session.getWorkspace().term().createTerminal(
                    new ByteArrayInputStream(new byte[0]),
                    printStream,
                    printStream
            ));
            final Object errObject = context2.getErrObject();
            if (errObject != null) {
                if(context.getSession().isPlainOut()){
                    printObject(context2.setErr(true), session);
                }
            }
            throw new NutsExecutionException(context.getSession(),NutsMessage.formatted(printStream.toString()), context2.getExitCode());
        }
        return 0;
    }

    protected void initCommandLine(NutsCommandLine commandLine) {

    }

    protected void printObject(SimpleNshCommandContext context, NutsSession session) {
        if(session==null) {
            session = context.getExecutionContext().getSession();
        }
        if (session.isIterableTrace()) {
            //already processed
        } else {
            switch (session.getOutputFormat()) {
                case PLAIN: {
                    printPlainObject(context, session);
                    break;
                }
                default: {
                    context.printObject(context.getResult(),session);
                }
            }
        }
    }

    protected void printPlainObject(SimpleNshCommandContext context, NutsSession session) {
        context.printObject(context.getResult(),session);
    }

}
