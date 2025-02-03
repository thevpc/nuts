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
package net.thevpc.nuts.toolbox.nsh.cmds;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.cmdline.NCmdLineAutoComplete;
import net.thevpc.nuts.io.NMemoryPrintStream;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.toolbox.nsh.autocomplete.NCommandAutoCompleteComponent;
import net.thevpc.nuts.toolbox.nsh.eval.NShellExecutionContext;
import net.thevpc.nuts.toolbox.nsh.util.bundles._IOUtils;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NMsg;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Arrays;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author thevpc
 */
public abstract class NShellBuiltinBase implements NShellBuiltin {
    private static final Logger LOG = Logger.getLogger(NShellBuiltinBase.class.getName());
    private final String name;
    private final int supportLevel;
    private String help;
    private boolean enabled = true;

    private Supplier<Object> optionsSupplier;

    public NShellBuiltinBase(String name, int supportLevel, Class<?> optionsSupplier) {
        this.name = name;
        this.supportLevel = supportLevel;
        this.optionsSupplier = ()->createOptions(optionsSupplier);
    }

    public NShellBuiltinBase(String name, Class<?> optionsSupplier) {
        this.name = name;
        this.supportLevel = NConstants.Support.DEFAULT_SUPPORT;
        this.optionsSupplier = ()->createOptions(optionsSupplier);
    }
    public NShellBuiltinBase(String name, int supportLevel, Supplier<?> optionsSupplier) {
        this.name = name;
        this.supportLevel = supportLevel;
        this.optionsSupplier = (Supplier)optionsSupplier;
    }
    public NShellBuiltinBase(String name, Supplier<?> optionsSupplier) {
        this.name = name;
        this.supportLevel = NConstants.Support.DEFAULT_SUPPORT;
        this.optionsSupplier = (Supplier)optionsSupplier;
    }

    protected static Object createOptions(Class<?> optionsClass) {
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

    protected abstract boolean nextOption(NArg arg, NCmdLine cmdLine, NShellExecutionContext context);
    protected abstract boolean nextNonOption(NArg arg, NCmdLine cmdLine, NShellExecutionContext context);

    protected abstract void main(NCmdLine cmdLine, NShellExecutionContext context);

    protected void validate(NCmdLine cmdLine, NShellExecutionContext context) {

    }

    protected void init(NCmdLine cmdLine, NShellExecutionContext context) {

    }

    @Override
    public int getSupportLevel(NSupportLevelContext param) {
        return supportLevel;
    }

    @Override
    public void autoComplete(NShellExecutionContext context, NCmdLineAutoComplete autoComplete) {
        NCmdLineAutoComplete oldAutoComplete = context.getShellContext().getAutoComplete();
        context.getShellContext().setAutoComplete(autoComplete);
        try {
            if (autoComplete == null) {
                throw new NIllegalArgumentException(NMsg.ofPlain("missing auto-complete"));
            }
            NCommandAutoCompleteComponent best = NWorkspace.get().get().extensions().createServiceLoader(NCommandAutoCompleteComponent.class, NShellBuiltin.class, NCommandAutoCompleteComponent.class.getClassLoader())
                    .loadBest(NShellBuiltinBase.this);
            if (best != null) {
                best.autoComplete(this, context);
            } else {
                String[] args = autoComplete.getWords().toArray(new String[0]);
                try {
                    exec(args, context);
                } catch (Exception ex) {
                    //ignore
                }
            }
        } finally {
            context.getShellContext().setAutoComplete(oldAutoComplete);
        }
    }

    @Override
    public String getHelp() {
        if (help == null) {
            try {
                String replaced = getClass().getPackage().getName().replace('.', '/');
                URL resource = getClass().getResource(
                        "/" + replaced + "/"
                                + getName() + ".ntf");
                if (resource != null) {
                    help = _IOUtils.loadString(resource);
                }
            } catch (Exception e) {
                LOG.log(Level.CONFIG, "Unable to load help for " + getName(), e);
            }
            if (help == null) {
                help = "```error no help found for command " + getName() + "```";
            }
        }
        return help;
    }

    @Override
    public String getName() {
        return name;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public String getHelpHeader() {
        String h = getHelp();
        BufferedReader r = new BufferedReader(new StringReader(h));
        while (true) {
            String line = null;
            try {
                line = r.readLine();
            } catch (IOException e) {
                //
            }
            if (line == null) {
                break;
            }
            if (!NBlankable.isBlank(line)) {
                return line;
            }
        }
        return "No help";
    }

    protected void throwExecutionException(Object errObject, int errorCode, NSession session) {
        session = session.copy();
        NPrintStream printStream = NMemoryPrintStream.of();
        if (errObject != null) {
            printStream.print(errObject);
        } else {
            printStream.println(NMsg.ofC("%s: command failed with code %s", getName(), errorCode));
        }
        throw new NExecutionException(NMsg.ofNtf(printStream.toString()), errorCode);
    }

    public final void exec(String[] args, NShellExecutionContext context) {
        context.getSession().runWith(()->{
            try {
                boolean conf = false;
                int maxLoops = 1000;
                boolean robustMode = false;
                NSession session = context.getSession();
                NCmdLine cmdLine = NCmdLine.of(args).setCommandName(getName())
                        .setAutoComplete(context.getShellContext().getAutoComplete());
                context.setOptions(optionsSupplier==null?null:optionsSupplier.get());
                init(cmdLine, context);
                while (cmdLine.hasNext()) {
                    NArg arg = cmdLine.peek().get();
                    if (robustMode) {
                        String[] before = cmdLine.toStringArray();
                        if(arg.isOption()){
                            if (!this.nextOption(arg, cmdLine, context)) {
                                context.configureLast(cmdLine);
                            } else {
                                conf = true;
                            }
                        }else{
                            if (!this.nextNonOption(arg, cmdLine, context)) {
                                context.configureLast(cmdLine);
                            } else {
                                conf = true;
                            }
                        }
                        String[] after = cmdLine.toStringArray();
                        if (Arrays.equals(before, after)) {
                            throw new IllegalStateException("bad implementation of configureFirst in class " + getClass().getName() + "."
                                    + " Commandline is not consumed; perhaps missing skip() class."
                                    + " args = " + Arrays.toString(after));
                        }
                    } else {
                        if(arg.isOption()){
                            if (!this.nextOption(arg, cmdLine, context)) {
                                context.configureLast(cmdLine);
                            } else {
                                conf = true;
                            }
                        }else{
                            if (!this.nextNonOption(arg, cmdLine, context)) {
                                context.configureLast(cmdLine);
                            } else {
                                conf = true;
                            }
                        }
                    }
                    maxLoops--;
                    if (maxLoops < 0) {
                        robustMode = true;
                    }
                }
                this.validate(cmdLine, context);
                if (cmdLine.isAutoCompleteMode()) {
                    return;
                }
                if (context.isAskHelp()) {
                    NOut.println(NText.of(getHelp()));
                    return;
                }
                if (context.isAskVersion()) {
                    NOut.println(NId.getForClass(getClass()).get().getVersion());
                    return;
                }
                main(cmdLine, context);
            } catch (NExecutionException ex) {
                throw ex;
            } catch (NException ex) {
                throw new NExecutionException(ex.getFormattedMessage(), ex, NExecutionException.ERROR_255);
            } catch (Exception ex) {
                throw new NExecutionException(
                        NMsg.ofNtf(NText.of(ex).toString())
                        , ex, NExecutionException.ERROR_255);
            }
        });
    }

    protected NCmdLine cmdLine(String[] args, NShellExecutionContext context) {
        NSession session = context.getSession();
        return NCmdLine.of(args)
                .setAutoComplete(context.getShellContext().getAutoComplete())
                .setCommandName(getName());
    }
}
