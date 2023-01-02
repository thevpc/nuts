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
import net.thevpc.nuts.boot.NBootOptions;
import net.thevpc.nuts.cmdline.DefaultNArg;
import net.thevpc.nuts.io.NOutStream;
import net.thevpc.nuts.io.NSystemTerminal;
import net.thevpc.nuts.runtime.optional.jansi.OptionalJansi;
import net.thevpc.nuts.runtime.standalone.event.DefaultNWorkspaceEvent;
import net.thevpc.nuts.runtime.standalone.io.printstream.NOutStreamNull;
import net.thevpc.nuts.runtime.standalone.io.terminal.*;
import net.thevpc.nuts.runtime.standalone.session.DefaultNSession;
import net.thevpc.nuts.runtime.standalone.session.NSessionUtils;
import net.thevpc.nuts.runtime.standalone.util.CorePlatformUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.runtime.standalone.workspace.config.NWorkspaceModel;
import net.thevpc.nuts.spi.NDefaultTerminalSpec;
import net.thevpc.nuts.spi.NSystemTerminalBase;
import net.thevpc.nuts.spi.NTerminalSpec;
import net.thevpc.nuts.util.NLogger;
import net.thevpc.nuts.util.NLoggerOp;
import net.thevpc.nuts.util.NLoggerVerb;

import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Level;

/**
 * @author thevpc
 */
public class DefaultNBootModel implements NBootModel {

    public NOutStream nullOut;
    protected NWorkspace workspace;
    protected boolean firstBoot;
    protected boolean initializing;
    protected NBootOptions bOptions;
    protected NSession bootSession;
    private Map<String, NValue> customBootOptions;
    private NWorkspaceTerminalOptions bootTerminal;
    private NLogger LOG;
    private NSystemTerminal systemTerminal;

    public DefaultNBootModel(NWorkspace workspace) {
        this.workspace = workspace;
    }

    public void init(NBootOptions bOption0) {
        this.initializing = true;
        NWorkspaceModel _model = NWorkspaceExt.of(workspace).getModel();
        this.bootSession = new DefaultNSession(workspace, bOption0);
        this.bOptions = bOption0.readOnly();
        this.bootTerminal = detectAnsiTerminalSupport(NOsFamily.getCurrent(), bOptions, true, bootSession);
        _model.uuid = bOptions.getUuid().orNull();
        _model.name = Paths.get(bOptions.getWorkspace().get()).getFileName().toString();
        DefaultSystemTerminal sys = new DefaultSystemTerminal(new DefaultNSystemTerminalBaseBoot(this));

        this.systemTerminal = NutsSystemTerminal_of_NutsSystemTerminalBase(sys, bootSession);
        this.bootSession.setTerminal(new DefaultNSessionTerminalFromSystem(bootSession, this.systemTerminal));
        this.nullOut = new NOutStreamNull(bootSession);
    }

    public static NWorkspaceTerminalOptions detectAnsiTerminalSupport(NOsFamily os, NWorkspaceOptions bOption, boolean boot, NSession session) {
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
                return new NWorkspaceTerminalOptions(stdIn, stdOut, stdErr, flags.toArray(flags.toArray(new String[0])));
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
                    return new NWorkspaceTerminalOptions(stdIn, stdOut, stdErr, flags.toArray(new String[0]));
                }
                if (OptionalJansi.isAvailable()) {
                    NWorkspaceTerminalOptions t = OptionalJansi.resolveStdFd(session, flags);
                    if (t != null) {
                        return t;
                    }
                }
                flags.add(ansiFlag(tty, bOption));
                return new NWorkspaceTerminalOptions(stdIn, stdOut, stdErr, flags.toArray(new String[0]));
            }
            default: {
                if (OptionalJansi.isAvailable()) {
                    NWorkspaceTerminalOptions t = OptionalJansi.resolveStdFd(session, flags);
                    if (t != null) {
                        return t;
                    }
                }
                flags.add(ansiFlag(tty, bOption));
                return new NWorkspaceTerminalOptions(stdIn, stdOut, stdErr, flags.toArray(new String[0]));
            }
        }
    }

    private static String ansiFlag(boolean defaultValue, NWorkspaceOptions bOption) {
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
        this.bootTerminal = detectAnsiTerminalSupport(NOsFamily.getCurrent(), bOptions.getUserOptions().get(), false, bootSession);
    }

    public void setSystemTerminal(NSystemTerminalBase terminal, NSession session) {
        if (terminal == null) {
            throw new NExtensionNotFoundException(session, NSystemTerminalBase.class, null);
        }
        NSystemTerminal syst = NutsSystemTerminal_of_NutsSystemTerminalBase(terminal, session);
        NSystemTerminal old = this.systemTerminal;
        this.systemTerminal = syst;

        if (old != this.systemTerminal) {
            NWorkspaceEvent event = null;
            if (session != null) {
                for (NWorkspaceListener workspaceListener : NEvents.of(session).getWorkspaceListeners()) {
                    if (event == null) {
                        event = new DefaultNWorkspaceEvent(session, null, "systemTerminal", old, this.systemTerminal);
                    }
                    workspaceListener.onUpdateProperty(event);
                }
            }
        }
    }

    public NSystemTerminal createSystemTerminal(NTerminalSpec spec, NSession session) {
        NSystemTerminalBase termb = session.extensions()
                .setSession(session)
                .createSupported(NSystemTerminalBase.class, true, spec);
        return NutsSystemTerminal_of_NutsSystemTerminalBase(termb, session);
    }

    protected NLoggerOp _LOGOP(NSession session) {
        return _LOG(session).with().session(session);
    }

    protected NLogger _LOG(NSession session) {
        if (LOG == null) {
            LOG = NLogger.of(DefaultNBootModel.class, session);
        }
        return LOG;
    }

    public void enableRichTerm(NSession session) {
        NSystemTerminal st = getSystemTerminal();
        if (st.isAutoCompleteSupported()) {
            //that's ok
        } else {
            NId extId = NId.of("net.thevpc.nuts.ext:next-term#" + session.getWorkspace().getApiVersion()).get(session);
            if (!NConfigs.of(session).isExcludedExtension(extId.toString(), NBootManager.of(session).getBootOptions())) {
                NExtensions extensions = session.extensions();
                extensions.setSession(session).loadExtension(extId);
                NSystemTerminal systemTerminal = createSystemTerminal(
                        new NDefaultTerminalSpec()
                                .setAutoComplete(true),
                        session
                );
                setSystemTerminal(systemTerminal, session);
                if (getSystemTerminal().isAutoCompleteSupported()) {
                    _LOGOP(session).level(Level.FINE).verb(NLoggerVerb.SUCCESS)
                            .log(NMsg.ofPlain("enable rich terminal"));
                } else {
                    _LOGOP(session).level(Level.FINE).verb(NLoggerVerb.FAIL)
                            .log(NMsg.ofPlain("unable to enable rich terminal"));
                }
            } else {
                _LOGOP(session).level(Level.FINE).verb(NLoggerVerb.WARNING)
                        .log(NMsg.ofPlain("enableRichTerm discarded; next-term is excluded."));
            }
        }
    }

    private NSystemTerminal NutsSystemTerminal_of_NutsSystemTerminalBase(NSystemTerminalBase terminal, NSession session) {
        if (terminal == null) {
            throw new NExtensionNotFoundException(session, NSystemTerminalBase.class, "SystemTerminalBase");
        }
        NSystemTerminal syst;
        if ((terminal instanceof NSystemTerminal)) {
            syst = (NSystemTerminal) terminal;
        } else {
            try {
                syst = new DefaultSystemTerminal(terminal);
                NSessionUtils.setSession(syst, session);
            } catch (Exception ex) {
                _LOGOP(session).level(Level.FINEST).verb(NLoggerVerb.WARNING)
                        .log(NMsg.ofJstyle("unable to create system terminal : {0}", ex));
                DefaultNSystemTerminalBase b = new DefaultNSystemTerminalBase();
                NSessionUtils.setSession(b, session);
                syst = new DefaultSystemTerminal(b);
                NSessionUtils.setSession(syst, session);
            }
        }
        return syst;
    }

    public NBootOptions getBootEffectiveOptions() {
        return bOptions;
    }

    public NWorkspaceOptions getBootUserOptions() {
        return bOptions.getUserOptions().get();
    }

    public NWorkspaceTerminalOptions getBootTerminal() {
        return bootTerminal;
    }

    @Override
    public boolean isInitializing() {
        return initializing;
    }

    public DefaultNBootModel setInitializing(boolean initializing) {
        this.initializing = initializing;
        return this;
    }

    public boolean isFirstBoot() {
        return firstBoot;
    }

    public DefaultNBootModel setFirstBoot(boolean firstBoot) {
        this.firstBoot = firstBoot;
        return this;
    }

    public NOutStream nullPrintStream() {
        return nullOut;
        //return createPrintStream(NullOutputStream.INSTANCE, NutsTerminalMode.FILTERED, session);
    }

    public NSystemTerminal getSystemTerminal() {
        return systemTerminal;
    }

    public NSession bootSession() {
        return bootSession;
    }

    public NWorkspace getWorkspace() {
        return workspace;
    }

    public NSession getBootSession() {
        return bootSession;
    }

    public NOptional<NValue> getCustomBootOption(String... names) {
        for (String name : names) {
            NValue r = getCustomBootOptions().get(name);
            if (r != null) {
                return NOptional.of(r);
            }
        }
        return NOptional.ofNamedEmpty("options "+ Arrays.asList(names));
    }

    public NOptional<NValue> getCustomBootOption(String name) {
        NValue r = getCustomBootOptions().get(name);
        return NOptional.ofNamed(r, "option " + name);
    }

    public Map<String, NValue> getCustomBootOptions() {
        if (customBootOptions == null) {
            customBootOptions = new LinkedHashMap<>();
            List<String> properties = bOptions.getUserOptions().get().getCustomOptions().orNull();
            if (properties != null) {
                for (String property : properties) {
                    if (property != null) {
                        DefaultNArg a = new DefaultNArg(property);
                        if (a.isActive()) {
                            String key = a.getKey().asString().orElse("");
                            this.customBootOptions.put(key, NValue.of(a.getStringValue().orElse(null)));
                        }
                    }
                }
            }
        }
        return customBootOptions;
    }

}
