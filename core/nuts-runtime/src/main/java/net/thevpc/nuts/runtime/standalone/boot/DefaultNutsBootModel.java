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
 * Copyright [2020] [thevpc] Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.runtime.standalone.boot;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.optional.jansi.OptionalJansi;
import net.thevpc.nuts.runtime.standalone.event.DefaultNutsWorkspaceEvent;
import net.thevpc.nuts.runtime.standalone.io.printstream.NutsPrintStreamNull;
import net.thevpc.nuts.runtime.standalone.io.terminal.*;
import net.thevpc.nuts.runtime.standalone.session.DefaultNutsSession;
import net.thevpc.nuts.runtime.standalone.session.NutsSessionUtils;
import net.thevpc.nuts.runtime.standalone.util.CorePlatformUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NutsWorkspaceExt;
import net.thevpc.nuts.runtime.standalone.workspace.config.NutsWorkspaceModel;
import net.thevpc.nuts.spi.NutsDefaultTerminalSpec;
import net.thevpc.nuts.spi.NutsSystemTerminalBase;
import net.thevpc.nuts.spi.NutsTerminalSpec;

import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Level;

/**
 * @author thevpc
 */
public class DefaultNutsBootModel implements NutsBootModel {

    public final NutsPrintStream nullOut;
    protected NutsWorkspace workspace;
    protected boolean firstBoot;
    protected boolean initializing;
    protected NutsWorkspaceBootOptions bOptions;
    protected NutsSession bootSession;
    private Map<String, NutsValue> customBootOptions;
    private NutsBootTerminal bootTerminal;
    private NutsLogger LOG;
    private NutsSystemTerminal systemTerminal;

    public DefaultNutsBootModel(NutsWorkspace workspace, NutsWorkspaceBootOptions bOption0) {
        this.workspace = workspace;
        this.initializing = true;
        NutsWorkspaceModel _model = NutsWorkspaceExt.of(workspace).getModel();
        this.bootSession = new DefaultNutsSession(workspace, bOption0);
        this.bOptions = bOption0.readOnly();
        this.bootTerminal = detectAnsiTerminalSupport(NutsOsFamily.getCurrent(), bOptions, true, bootSession);
        _model.uuid = bOptions.getUuid().orNull();
        _model.name = Paths.get(bOptions.getWorkspace().get()).getFileName().toString();
        DefaultSystemTerminal sys = new DefaultSystemTerminal(new DefaultNutsSystemTerminalBaseBoot(this));

        this.systemTerminal = NutsSystemTerminal_of_NutsSystemTerminalBase(sys, bootSession);
        this.bootSession.setTerminal(new DefaultNutsSessionTerminalFromSystem(bootSession, this.systemTerminal));
        this.nullOut = new NutsPrintStreamNull(bootSession);
    }

    public static NutsBootTerminal detectAnsiTerminalSupport(NutsOsFamily os, NutsWorkspaceOptions bOption, boolean boot, NutsSession session) {
        List<String> flags = new ArrayList<>();
        boolean tty = false;
        boolean customOut = false;
        boolean customErr = false;
        boolean customIn = false;
        if (!boot && CoreAnsiTermHelper.isXTerm(session)) {
            flags.add("tty");
            tty = true;
        }
        InputStream stdIn = System.in;
        PrintStream stdOut = System.out;
        PrintStream stdErr = System.err;
        if (bOption.getStdin().isPresent() && bOption.getStdin().get() != System.in) {
            stdIn = bOption.getStdin().orNull();
            flags.add("customIn");
            customIn = true;
        }
        if (bOption.getStdout().isPresent() && bOption.getStdout().get() != System.out) {
            stdOut = bOption.getStdout().orNull();
            flags.add("customOut");
            customOut = true;
        }
        if (bOption.getStderr().isPresent() && bOption.getStderr().get() != System.err) {
            stdErr = bOption.getStderr().orNull();
            flags.add("customErr");
            customErr = true;
        }
        if (System.console() != null) {
            flags.add("console");
        }
        switch (os) {
            case LINUX:
            case MACOS:
            case UNIX: {
                flags.add(ansiFlag(tty, bOption));
                return new NutsBootTerminal(stdIn, stdOut, stdErr, flags.toArray(flags.toArray(new String[0])));
            }
            case WINDOWS: {
                if (CorePlatformUtils.IS_CYGWIN || CorePlatformUtils.IS_MINGW_XTERM) {
                    if (CorePlatformUtils.IS_CYGWIN) {
                        flags.add("cygwin");
                    }
                    if (CorePlatformUtils.IS_MINGW_XTERM) {
                        flags.add("mingw");
                    }
                    flags.add(ansiFlag((!customOut && !customErr), bOption));
                    return new NutsBootTerminal(stdIn, stdOut, stdErr, flags.toArray(new String[0]));
                }
                if (OptionalJansi.isAvailable()) {
                    NutsBootTerminal t = OptionalJansi.resolveStdFd(session, flags);
                    if (t != null) {
                        return t;
                    }
                }
                flags.add(ansiFlag(tty, bOption));
                return new NutsBootTerminal(stdIn, stdOut, stdErr, flags.toArray(new String[0]));
            }
            default: {
                if (OptionalJansi.isAvailable()) {
                    NutsBootTerminal t = OptionalJansi.resolveStdFd(session, flags);
                    if (t != null) {
                        return t;
                    }
                }
                flags.add(ansiFlag(tty, bOption));
                return new NutsBootTerminal(stdIn, stdOut, stdErr, flags.toArray(new String[0]));
            }
        }
    }

    private static String ansiFlag(boolean defaultValue, NutsWorkspaceOptions bOption) {
        if (bOption.getTerminalMode().isPresent()) {
            switch (bOption.getTerminalMode().get()) {
                case FORMATTED:
                case ANSI:
                    return "ansi";
                case FILTERED:
                    return "raw";
                case INHERITED:
                    return defaultValue ? "ansi" : "raw";
            }
        }
        return defaultValue ? "ansi" : "raw";
    }

    public void onInitializeWorkspace() {
        this.bootTerminal = detectAnsiTerminalSupport(NutsOsFamily.getCurrent(), bOptions.getUserOptions().get(), false, bootSession);
    }

    public void setSystemTerminal(NutsSystemTerminalBase terminal, NutsSession session) {
        if (terminal == null) {
            throw new NutsExtensionNotFoundException(session, NutsSystemTerminalBase.class, null);
        }
        NutsSystemTerminal syst = NutsSystemTerminal_of_NutsSystemTerminalBase(terminal, session);
        NutsSystemTerminal old = this.systemTerminal;
        this.systemTerminal = syst;

        if (old != this.systemTerminal) {
            NutsWorkspaceEvent event = null;
            if (session != null) {
                for (NutsWorkspaceListener workspaceListener : session.events().getWorkspaceListeners()) {
                    if (event == null) {
                        event = new DefaultNutsWorkspaceEvent(session, null, "systemTerminal", old, this.systemTerminal);
                    }
                    workspaceListener.onUpdateProperty(event);
                }
            }
        }
    }

    public NutsSystemTerminal createSystemTerminal(NutsTerminalSpec spec, NutsSession session) {
        NutsSystemTerminalBase termb = session.extensions()
                .setSession(session)
                .createSupported(NutsSystemTerminalBase.class, true, spec);
        return NutsSystemTerminal_of_NutsSystemTerminalBase(termb, session);
    }

    protected NutsLoggerOp _LOGOP(NutsSession session) {
        return _LOG(session).with().session(session);
    }

    protected NutsLogger _LOG(NutsSession session) {
        if (LOG == null) {
            LOG = NutsLogger.of(DefaultNutsBootModel.class, session);
        }
        return LOG;
    }

    public void enableRichTerm(NutsSession session) {
        NutsSystemTerminal st = getSystemTerminal();
        if (st.isAutoCompleteSupported()) {
            //that's ok
        } else {
            NutsId extId = NutsId.of("net.thevpc.nuts.ext:next-term#" + session.getWorkspace().getApiVersion()).get(session);
            if (!session.config().isExcludedExtension(extId.toString(), session.boot().getBootOptions())) {
                NutsWorkspaceExtensionManager extensions = session.extensions();
                extensions.setSession(session).loadExtension(extId);
                NutsSystemTerminal systemTerminal = createSystemTerminal(
                        new NutsDefaultTerminalSpec()
                                .setAutoComplete(true),
                        session
                );
                setSystemTerminal(systemTerminal, session);
                if (getSystemTerminal().isAutoCompleteSupported()) {
                    _LOGOP(session).level(Level.FINE).verb(NutsLogVerb.SUCCESS)
                            .log(NutsMessage.jstyle("enable rich terminal"));
                } else {
                    _LOGOP(session).level(Level.FINE).verb(NutsLogVerb.FAIL)
                            .log(NutsMessage.jstyle("unable to enable rich terminal"));
                }
            } else {
                _LOGOP(session).level(Level.FINE).verb(NutsLogVerb.WARNING)
                        .log(NutsMessage.jstyle("enableRichTerm discarded; next-term is excluded."));
            }
        }
    }

    private NutsSystemTerminal NutsSystemTerminal_of_NutsSystemTerminalBase(NutsSystemTerminalBase terminal, NutsSession session) {
        if (terminal == null) {
            throw new NutsExtensionNotFoundException(session, NutsSystemTerminalBase.class, "SystemTerminalBase");
        }
        NutsSystemTerminal syst;
        if ((terminal instanceof NutsSystemTerminal)) {
            syst = (NutsSystemTerminal) terminal;
        } else {
            try {
                syst = new DefaultSystemTerminal(terminal);
                NutsSessionUtils.setSession(syst, session);
            } catch (Exception ex) {
                _LOGOP(session).level(Level.FINEST).verb(NutsLogVerb.WARNING)
                        .log(NutsMessage.jstyle("unable to create system terminal : {0}", ex));
                DefaultNutsSystemTerminalBase b = new DefaultNutsSystemTerminalBase();
                NutsSessionUtils.setSession(b, session);
                syst = new DefaultSystemTerminal(b);
                NutsSessionUtils.setSession(syst, session);
            }
        }
        return syst;
    }

    public NutsWorkspaceBootOptions getBootEffectiveOptions() {
        return bOptions;
    }

    public NutsWorkspaceOptions getBootUserOptions() {
        return bOptions.getUserOptions().get();
    }

    public NutsBootTerminal getBootTerminal() {
        return bootTerminal;
    }

    @Override
    public boolean isInitializing() {
        return initializing;
    }

    public DefaultNutsBootModel setInitializing(boolean initializing) {
        this.initializing = initializing;
        return this;
    }

    public boolean isFirstBoot() {
        return firstBoot;
    }

    public DefaultNutsBootModel setFirstBoot(boolean firstBoot) {
        this.firstBoot = firstBoot;
        return this;
    }

    public NutsPrintStream nullPrintStream() {
        return nullOut;
        //return createPrintStream(NullOutputStream.INSTANCE, NutsTerminalMode.FILTERED, session);
    }

    public NutsSystemTerminal getSystemTerminal() {
        return systemTerminal;
    }

    public NutsSession bootSession() {
        return bootSession;
    }

    public NutsWorkspace getWorkspace() {
        return workspace;
    }

    public NutsSession getBootSession() {
        return bootSession;
    }

    public NutsOptional<NutsValue> getCustomBootOption(String... names) {
        for (String name : names) {
            NutsValue r = getCustomBootOptions().get(name);
            if (r != null) {
                return NutsOptional.of(r);
            }
        }
        return NutsOptional.ofEmpty(session -> NutsMessage.cstyle("option not found : %s", Arrays.asList(names)));
    }

    public NutsOptional<NutsValue> getCustomBootOption(String name) {
        NutsValue r = getCustomBootOptions().get(name);
        return NutsOptional.of(r, session -> NutsMessage.cstyle("option not found : %s", name));
    }

    public Map<String, NutsValue> getCustomBootOptions() {
        if (customBootOptions == null) {
            customBootOptions = new LinkedHashMap<>();
            List<String> properties = bOptions.getUserOptions().get().getCustomOptions().orNull();
            if (properties != null) {
                for (String property : properties) {
                    if (property != null) {
                        DefaultNutsArgument a = new DefaultNutsArgument(property);
                        if (a.isActive()) {
                            String key = a.getKey().asString().orElse("");
                            this.customBootOptions.put(key, NutsValue.of(a.getStringValue().orElse(null)));
                        }
                    }
                }
            }
        }
        return customBootOptions;
    }

}
