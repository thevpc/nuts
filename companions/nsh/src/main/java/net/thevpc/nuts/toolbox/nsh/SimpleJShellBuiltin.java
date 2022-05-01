/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
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
package net.thevpc.nuts.toolbox.nsh;

import net.thevpc.nuts.*;
import net.thevpc.nuts.toolbox.nsh.jshell.JShellExecutionContext;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.function.Supplier;

/**
 * @author thevpc
 */
public abstract class SimpleJShellBuiltin extends AbstractJShellBuiltin {
    private Supplier<Object> optionsSupplier;

    public SimpleJShellBuiltin(String name, int supportLevel, Class optionsSupplier) {
        super(name, supportLevel);
        this.optionsSupplier = ()->createOptions(optionsSupplier);
    }
    public SimpleJShellBuiltin(String name, Class optionsSupplier) {
        super(name, DEFAULT_SUPPORT);
        this.optionsSupplier = ()->createOptions(optionsSupplier);
    }
    public SimpleJShellBuiltin(String name, int supportLevel, Supplier optionsSupplier) {
        super(name, supportLevel);
        this.optionsSupplier = optionsSupplier;
    }
    public SimpleJShellBuiltin(String name, Supplier optionsSupplier) {
        super(name, DEFAULT_SUPPORT);
        this.optionsSupplier = optionsSupplier;
    }

    protected static Object createOptions(Class optionsClass) {
        if (optionsClass == null) {
            return null;
        }
        try {
            Constructor c = optionsClass.getDeclaredConstructor();
            c.setAccessible(true);
            return c.newInstance();
        } catch (InstantiationException
                | NoSuchMethodException
                | IllegalAccessException
                | InvocationTargetException
                e) {
            throw new IllegalArgumentException(e);
        }
    }

    protected abstract boolean configureFirst(NutsCommandLine commandLine, JShellExecutionContext context);

    protected abstract void execBuiltin(NutsCommandLine commandLine, JShellExecutionContext context);

    @Override
    public void execImpl(String[] args, JShellExecutionContext context) {
        boolean conf = false;
        int maxLoops = 1000;
        boolean robustMode = false;
        NutsSession session = context.getSession();
        NutsCommandLine commandLine = NutsCommandLine.of(args).setCommandName(getName())
                .setAutoComplete(context.getShellContext().getAutoComplete());
        initCommandLine(commandLine, context);
        context.setOptions(optionsSupplier==null?null:optionsSupplier.get());
        while (commandLine.hasNext()) {
            if (robustMode) {
                String[] before = commandLine.toStringArray();
                if (!this.configureFirst(commandLine, context)) {
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
                if (!this.configureFirst(commandLine, context)) {
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
            return;
        }
        if (context.isAskHelp()) {
            session.out().printlnf(NutsString.of(getHelp(), session));
            return;
        }
        if (context.isAskVersion()) {
            session.out().printlnf(NutsIdResolver.of(session).resolveId(getClass()).getVersion().toString());
            return;
        }
        execBuiltin(commandLine, context);
    }

    protected void initCommandLine(NutsCommandLine commandLine, JShellExecutionContext context) {

    }

//    public static class SimpleNshCommandContext {
//
//        private JShellExecutionContext context;
//        private String[] args;
//        private Object options;
//
//        public SimpleNshCommandContext(String[] args, JShellExecutionContext context, Object configObject) {
//            this.context = context;
//            this.options = configObject;
//            this.args = args;
//        }
//
//        public String[] getArgs() {
//            return args;
//        }
//
//        public JShellContext getShellContext() {
//            return getExecutionContext().getShellContext();
//        }
//
//        public JShellExecutionContext getExecutionContext() {
//            return context;
//        }
//
//        public JShell getShell() {
//            return context.getShell();
//        }
//
//        public NutsSession getSession() {
//            return context.getSession();
//        }
//
//        public <T> T getOptions() {
//            return (T) options;
//        }
//
//        public InputStream in() {
//            return context.in();
//        }
//
//        public NutsPrintStream err() {
//            return context.err();
//        }
//
//        public NutsWorkspace getWorkspace() {
//            return context.getWorkspace();
//        }
//
//        public String getCwd() {
//            return context.getCwd();
//        }
//
//        public JShellContext getGlobalContext() {
//            return context.getShellContext();
//        }
//
//        public JShellContext getRootContext() {
//            return context.getShellContext();
//        }
//
//        public NutsPrintStream out() {
//            return getExecutionContext().out();
//        }
//    }

}
