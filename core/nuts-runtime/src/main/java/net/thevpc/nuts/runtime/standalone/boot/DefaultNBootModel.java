/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . It's based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
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
 * <br> ====================================================================
 */
package net.thevpc.nuts.runtime.standalone.boot;

import net.thevpc.nuts.*;

import net.thevpc.nuts.NBootOptions;
import net.thevpc.nuts.NWorkspaceTerminalOptions;
import net.thevpc.nuts.cmdline.DefaultNArg;

import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.io.NSystemTerminal;
import net.thevpc.nuts.runtime.optional.jansi.OptionalJansi;
import net.thevpc.nuts.io.NullNPrintStream;
import net.thevpc.nuts.runtime.standalone.io.terminal.*;
import net.thevpc.nuts.io.NullOutputStream;
import net.thevpc.nuts.runtime.standalone.session.DefaultNSession;
import net.thevpc.nuts.runtime.standalone.util.CorePlatformUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NativeImageHelper;
import net.thevpc.nuts.runtime.standalone.workspace.config.NWorkspaceModel;
import net.thevpc.nuts.spi.NDefaultTerminalSpec;
import net.thevpc.nuts.spi.NSystemTerminalBase;
import net.thevpc.nuts.spi.NTerminalSpec;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.log.NLogOp;
import net.thevpc.nuts.log.NLogVerb;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NLiteral;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.NOsFamily;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Level;

/**
 * @author thevpc
 */
public class DefaultNBootModel implements NBootModel {

    public NPrintStream nullOut;
    public OutputStream nullOutputStream;
    protected NWorkspace workspace;
    protected boolean firstBoot;
    protected boolean initializing;
    protected NBootOptions bOptions;
    protected NBootOptions effOptions;
    protected NSession bootSession;
    private Map<String, NLiteral> customBootOptions;
    private NWorkspaceTerminalOptions bootTerminal;
    private NLog LOG;
    private NSystemTerminalRef systemTerminal;
    private NWorkspaceModel workspaceModel;

    public DefaultNBootModel(NWorkspace workspace, NWorkspaceModel workspaceModel,NBootOptions bOption0) {
        this.workspace = workspace;
        this.workspaceModel = workspaceModel;
        this.bOptions = bOption0.readOnly();
        this.effOptions = bOption0.readOnly();
        this.bootSession = new DefaultNSession(workspace,null)
                .setAll(effOptions);
    }

    public void init() {
        this.initializing = true;
        NativeImageHelper.prepare(this.workspace);
        this.bootTerminal = detectAnsiTerminalSupport(NOsFamily.getCurrent(), effOptions, true);
        workspaceModel.uuid = effOptions.getUuid().orNull();
        workspaceModel.name = Paths.get(effOptions.getWorkspace().get()).getFileName().toString();
        DefaultSystemTerminal sys = new DefaultSystemTerminal(workspace, new DefaultNSystemTerminalBaseBoot(this));
        this.systemTerminal = new NSystemTerminalRef(getWorkspace(), NutsSystemTerminal_of_NutsSystemTerminalBase(sys));
        this.bootSession.setTerminal(new DefaultNTerminalFromSystem(workspace, this.systemTerminal));
        this.nullOut = NullNPrintStream.INSTANCE;
        this.nullOutputStream = NullOutputStream.INSTANCE;
    }

    public static NWorkspaceTerminalOptions detectAnsiTerminalSupport(NOsFamily os, NBootOptions bOption, boolean boot) {
        List<String> flags = new ArrayList<>();
        boolean tty = false;
        boolean customOut = false;
        boolean customErr = false;
        boolean customIn = false;
//        if (/*!boot &&*/ CoreAnsiTermHelper.isXTerm(session)) {
//            flags.add("tty");
//            tty = true;
//        }
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
        if (!customOut) {
            if (System.console() != null) {
                flags.add("tty");
                tty = true;
            } else {
                if (OptionalJansi.isAvailable()) {
                    if (OptionalJansi.isatty(1)) {
                        flags.add("tty");
                        tty = true;
                    }
                }
            }
        }
        boolean supportsAnsi = false;
        boolean acceptAnsi = false;
        boolean denyAnsi = false;
        if (bOption.getTerminalMode().isPresent()) {
            switch (bOption.getTerminalMode().get()) {
                case FORMATTED:
                case ANSI: {
                    acceptAnsi = true;
                    break;
                }
                case FILTERED: {
                    denyAnsi = true;
                    break;
                }
            }
        }
        switch (os) {
            case LINUX:
            case MACOS:
            case UNIX: {
                if (tty) {
                    supportsAnsi = true;
                }
                break;
            }
            case WINDOWS: {
                if (CorePlatformUtils.IS_CYGWIN || CorePlatformUtils.IS_MINGW_XTERM) {
                    if (CorePlatformUtils.IS_CYGWIN) {
                        if (tty) {
                            supportsAnsi = true;
                        }
                        flags.add("cygwin");
                    }
                    if (CorePlatformUtils.IS_MINGW_XTERM) {
                        if (tty) {
                            supportsAnsi = true;
                        }
                        flags.add("mingw");
                    }
                }
                break;
            }
        }
        if (!denyAnsi) {
            OptionalJansi.fillAnsiFlags(flags);
            if (!flags.contains("ansi") && !flags.contains("raw")) {
                if (supportsAnsi) {
                    flags.add("ansi");
                } else {
                    flags.add("raw");
                }
            }
        } else {
            if (!flags.contains("ansi") && !flags.contains("raw")) {
                flags.add("raw");
            }
        }
        return new NWorkspaceTerminalOptions(stdIn, stdOut, stdErr, flags.toArray(new String[0]));
    }

//    private static String ansiFlag(boolean defaultValue, NWorkspaceOptions bOption) {
//        if (bOption.getTerminalMode().isPresent()) {
//            switch (bOption.getTerminalMode().get()) {
//                case FORMATTED:
//                case ANSI:
//                    return "ansi";
//                case FILTERED:
//                    return "raw";
//                case INHERITED:
//                    return defaultValue ? "ansi" : "raw";
//            }
//        }
//        return defaultValue ? "ansi" : "raw";
//    }

    public void onInitializeWorkspace() {
        this.bootTerminal = detectAnsiTerminalSupport(NOsFamily.getCurrent(), effOptions, false);
    }

    public void setSystemTerminal(NSystemTerminalBase terminal) {
        this.systemTerminal.setBase(terminal);
    }

    public NSystemTerminal createSystemTerminal(NTerminalSpec spec) {
        NSystemTerminalBase termb = NExtensions.of()
                .createComponent(NSystemTerminalBase.class, spec).get();
        return NutsSystemTerminal_of_NutsSystemTerminalBase(termb);
    }

    protected NLogOp _LOGOP() {
        return _LOG().with();
    }

    protected NLog _LOG() {
        return NLog.of(DefaultNBootModel.class);
    }

    public void enableRichTerm() {
        NSystemTerminal st = getSystemTerminal();
        if (st.isAutoCompleteSupported()) {
            //that's ok
        } else {
            NId extId = NId.get("net.thevpc.nuts.ext:next-term#" + workspace.getApiVersion()).get();
            if (!NExtensions.of().isExcludedExtension(extId.toString(), NWorkspace.of().getBootOptions().toWorkspaceOptions())) {
                NExtensions extensions = NExtensions.of();
                extensions.loadExtension(extId);
                NSystemTerminal systemTerminal = createSystemTerminal(
                        new NDefaultTerminalSpec()
                                .setAutoComplete(true)
                );
                setSystemTerminal(systemTerminal);
                if (getSystemTerminal().isAutoCompleteSupported()) {
                    _LOGOP().level(Level.FINE).verb(NLogVerb.SUCCESS)
                            .log(NMsg.ofPlain("enable rich terminal"));
                } else {
                    _LOGOP().level(Level.FINE).verb(NLogVerb.FAIL)
                            .log(NMsg.ofPlain("unable to enable rich terminal"));
                }
            } else {
                _LOGOP().level(Level.FINE).verb(NLogVerb.WARNING)
                        .log(NMsg.ofPlain("enableRichTerm discarded; next-term is excluded."));
            }
        }
    }

    private NSystemTerminal NutsSystemTerminal_of_NutsSystemTerminalBase(NSystemTerminalBase terminal) {
        if (terminal == null) {
            throw new NIllegalArgumentException(NMsg.ofPlain("missing terminal"));
        }
        NSystemTerminal syst;
        if ((terminal instanceof NSystemTerminal)) {
            syst = (NSystemTerminal) terminal;
        } else {
            try {
                syst = new DefaultSystemTerminal(workspace, terminal);
                //NSessionUtils.setSession(syst, session);
            } catch (Exception ex) {
                _LOGOP().level(Level.FINEST).verb(NLogVerb.WARNING)
                        .log(NMsg.ofC("unable to create system terminal : %s", ex));
                DefaultNSystemTerminalBase b = new DefaultNSystemTerminalBase(workspace);
                syst = new DefaultSystemTerminal(workspace, b);
                //NSessionUtils.setSession(syst, session);
            }
        }
        return syst;
    }

    public NBootOptions getBootEffectiveOptions() {
        return effOptions;
    }

    public NBootOptions getBootUserOptions() {
        return bOptions;
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

    public NPrintStream nullPrintStream() {
        return nullOut;
        //return createPrintStream(NullOutputStream.INSTANCE, NutsTerminalMode.FILTERED, session);
    }

    public OutputStream nullOutputStream() {
        return nullOutputStream;
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

    public NOptional<NLiteral> getCustomBootOption(String... names) {
        for (String name : names) {
            NLiteral r = getCustomBootOptions().get(name);
            if (r != null) {
                return NOptional.of(r);
            }
        }
        return NOptional.ofNamedEmpty("options " + Arrays.asList(names));
    }

    public NOptional<NLiteral> getCustomBootOption(String name) {
        NLiteral r = getCustomBootOptions().get(name);
        return NOptional.ofNamed(r, "option " + name);
    }

    public Map<String, NLiteral> getCustomBootOptions() {
        if (customBootOptions == null) {
            customBootOptions = new LinkedHashMap<>();
            List<String> properties = bOptions.getCustomOptions().orNull();
            if (properties != null) {
                for (String property : properties) {
                    if (property != null) {
                        DefaultNArg a = new DefaultNArg(property);
                        if (a.isActive()) {
                            String key = a.getKey().asString().orElse("");
                            String v = a.getStringValue().orElse(null);
                            if(a.isEnabled()) {
                                this.customBootOptions.put(key, NLiteral.of(v));
                            }else if(NBlankable.isBlank(v)){
                                this.customBootOptions.put(key, NLiteral.of(false));
                            }else{
                                NOptional<Boolean> b = NLiteral.of(v).asBoolean();
                                if(b.isPresent()){
                                    this.customBootOptions.put(key, NLiteral.of(!b.get()));
                                }else{
                                    // this is a bad example,
                                    // ---!helpful=4
                                    // so wil propagate ! to the value
                                    this.customBootOptions.put(key, NLiteral.of("!"+v));
                                }
                            }
                        }
                    }
                }
            }
        }
        return customBootOptions;
    }

}
