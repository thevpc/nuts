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
package net.thevpc.nuts.runtime.standalone.session;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.*;
import net.thevpc.nuts.elem.NArrayElementBuilder;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.format.NIterableFormat;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.io.NSessionTerminal;
import net.thevpc.nuts.io.NTerminalMode;
import net.thevpc.nuts.runtime.standalone.app.cmdline.NCmdLineUtils;
import net.thevpc.nuts.runtime.standalone.elem.DefaultNArrayElementBuilder;
import net.thevpc.nuts.runtime.standalone.extension.DefaultNExtensions;
import net.thevpc.nuts.runtime.standalone.io.progress.ProgressOptions;
import net.thevpc.nuts.runtime.standalone.io.terminal.AbstractNSessionTerminal;
import net.thevpc.nuts.runtime.standalone.util.CoreStringUtils;
import net.thevpc.nuts.runtime.standalone.util.collections.NPropertiesHolder;
import net.thevpc.nuts.runtime.standalone.util.jclass.JavaClassUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTextTransformConfig;
import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.util.*;

import java.io.InputStream;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.logging.Filter;
import java.util.logging.Level;

/**
 * Created by vpc on 2/1/17.
 */
public class DefaultNSession implements Cloneable, NSession {

    //    protected NutsIterableFormat iterFormatHandler = null;
//    protected NutsIterableOutput iterFormat = null;
    protected NWorkspace ws = null;
    protected List<String> outputFormatOptions = new ArrayList<>();
    private NSessionTerminal terminal;
    private NPropertiesHolder properties = new NPropertiesHolder();
    private NPropertiesHolder refProperties = new NPropertiesHolder();
    private Map<Class, LinkedHashSet<NListener>> listeners = new HashMap<>();
    private String dependencySolver;
    private Boolean trace;
    private Boolean bot;
    private String debug;
    private NRunAs runAs;

    private NExecutionType executionType;
    //    private Boolean force;
    private Boolean dry;
    private Level logTermLevel;
    private Filter logTermFilter;
    private Level logFileLevel;
    private Filter logFileFilter;
    private NConfirmationMode confirm = null;
    private NContentType outputFormat;
    private NArrayElementBuilder eout;
    private NFetchStrategy fetchStrategy = null;
    private Boolean cached;
    private Boolean indexed;
    private Boolean transitive;
    private Boolean gui;
    private String progressOptions;
    private String errLinePrefix;
    private String outLinePrefix;
    private Instant expireTime;
    //private NId appId;
    private String locale;
    private boolean iterableOut;
    private Class appClass;
    private final NPath[] appFolders = new NPath[NStoreType.values().length];
    private final NPath[] appSharedFolders = new NPath[NStoreType.values().length];
    /**
     * auto complete info for "auto-complete" mode
     */
    private NCmdLineAutoComplete appAutoComplete;
    private NId appId;
    private NClock appStartTime;
    private List<String> appArgs;
    private NApplicationMode appMode = NApplicationMode.RUN;
    private NAppStoreLocationResolver appStoreLocationResolver;
    /**
     * previous parse for "update" mode
     */
    private NVersion appPreviousVersion;
    private List<String> appModeArgs = new ArrayList<>();

    public DefaultNSession(NWorkspace ws) {
        this.ws = new NWorkspaceSessionAwareImpl(this, ws);
        setAll(NBootManager.of(NSessionUtils.defaultSession(ws)).getBootOptions());
    }

    public DefaultNSession(NWorkspace ws, NWorkspaceOptions options) {
        this.ws = new NWorkspaceSessionAwareImpl(this, ws);
        setAll(options);
    }


    /**
     * configure the current command with the given arguments. This is an
     * override of the {@link NCmdLineConfigurable#configure(boolean, java.lang.String...)
     * }
     * to help return a more specific return type;
     *
     * @param args argument to configure with
     * @return {@code this} instance
     */
    @Override
    public final NSession configure(boolean skipUnsupported, String... args) {
        NId appId = getAppId();
        String appName = appId == null ? "app" : appId.getArtifactId();
        return NCmdLineConfigurable.configure(this, skipUnsupported, args,appName,this);
    }

    @Override
    public boolean configureFirst(NCmdLine cmdLine) {
        NArg a = cmdLine.peek().orNull();
        if (a != null) {
            boolean active = a.isActive();
            switch (a.key()) {
                case "-T":
                case "--output-format-option": {
                    a = cmdLine.nextEntry().get(this);
                    if (active) {
                        this.addOutputFormatOptions(a.getStringValue().orElse(""));
                    }
                    return true;
                }
                case "-O":
                case "--output-format":
                    a = cmdLine.nextEntry().get(this);
                    if (active) {
                        String t = a.getStringValue().orElse("");
                        int i = CoreStringUtils.firstIndexOf(t, new char[]{' ', ';', ':', '='});
                        if (i > 0) {
                            this.setOutputFormat(NContentType.valueOf(t.substring(0, i).toUpperCase()));
                            this.addOutputFormatOptions(t.substring(i + 1).toUpperCase());
                        } else {
                            this.setOutputFormat(NContentType.valueOf(t.toUpperCase()));
                        }
                    }
                    break;
                case "--tson":
                    a = cmdLine.next().get(this);
                    if (active) {
                        this.setOutputFormat(NContentType.TSON);
                        if (a.getStringValue() != null) {
                            this.addOutputFormatOptions(a.getStringValue().get(this));
                        }
                    }
                    break;
                case "--yaml":
                    a = cmdLine.next().get(this);
                    if (active) {
                        this.setOutputFormat(NContentType.YAML);
                        if (a.getStringValue() != null) {
                            this.addOutputFormatOptions(a.getStringValue().get(this));
                        }
                    }
                    break;
                case "--json": {
                    a = cmdLine.next().get(this);
                    if (active) {
                        this.setOutputFormat(NContentType.JSON);
                        this.addOutputFormatOptions(a.getStringValue().orNull());
                    }
                    return true;
                }
                case "--props": {
                    a = cmdLine.next().get(this);
                    if (active) {
                        this.setOutputFormat(NContentType.PROPS);
                        this.addOutputFormatOptions(a.getStringValue().orNull());
                    }
                    return true;
                }
                case "--plain": {
                    a = cmdLine.next().get(this);
                    if (active) {
                        this.setOutputFormat(NContentType.PLAIN);
                        this.addOutputFormatOptions(a.getStringValue().orNull());
                    }
                    return true;
                }
                case "--table": {
                    a = cmdLine.next().get(this);
                    if (active) {
                        this.setOutputFormat(NContentType.TABLE);
                        this.addOutputFormatOptions(a.getStringValue().orNull());
                    }
                    return true;
                }
                case "--tree": {
                    a = cmdLine.next().get(this);
                    if (active) {
                        this.setOutputFormat(NContentType.TREE);
                        this.addOutputFormatOptions(a.getStringValue().orNull());
                    }
                    return true;
                }
                case "--xml": {
                    a = cmdLine.next().get(this);
                    if (active) {
                        this.setOutputFormat(NContentType.XML);
                        this.addOutputFormatOptions(a.getStringValue().orNull());
                    }
                    return true;
                }
//                case "-f":
//                case "--force": {
//                    a = cmdLine.nextBoolean();
//                    if (active) {
//                        this.setForce(a.getBooleanValue());
//                    }
//                    return true;
//                }
                case "-y":
                case "--yes": {
                    if (active) {
                        this.setConfirm(NConfirmationMode.YES);
                    }
                    cmdLine.skip();
                    return true;
                }
                case "--ask": {
                    if (active) {
                        this.setConfirm(NConfirmationMode.ASK);
                    }
                    cmdLine.skip();
                    return true;
                }
                case "-n":
                case "--no": {
                    if (active) {
                        this.setConfirm(NConfirmationMode.NO);
                    }
                    cmdLine.skip();
                    return true;
                }
                case "--error": {
                    if (active) {
                        this.setConfirm(NConfirmationMode.ERROR);
                    }
                    cmdLine.skip();
                    return true;
                }
                case "--trace": {
                    NArg v = cmdLine.nextFlag().get(this);
                    if (active) {
                        this.setTrace(v.getBooleanValue().get(this));
                    }
                    return true;
                }
                case "--solver": {
                    a = cmdLine.nextEntry().get(this);
                    if (active) {
                        String s = a.getStringValue().get(this);
                        this.setDependencySolver(s);
                    }
                    break;
                }
                case "--progress": {
                    NArg v = cmdLine.next().get(this);
                    if (active) {
                        String s = a.getStringValue().orNull();
                        if (a.isNegated()) {
                            if (NBlankable.isBlank(s)) {
                                s = "false";
                            } else {
                                s = "false," + s;
                            }
                            setProgressOptions(s);
                        } else {
                            setProgressOptions(s);
                        }
                        this.setProgressOptions(s);
                    }
                    return true;
                }
                case "--debug": {
                    a = cmdLine.next().get(this);
                    if (active) {
                        if (a.getStringValue().isBlank()) {
                            this.setDebug(String.valueOf(a.isEnabled()));
                        } else {
                            if (a.isNegated()) {
                                this.setDebug(
                                        String.valueOf(!
                                                NLiteral.of(a.getStringValue().get(this)).asBoolean()
                                                        .ifEmpty(true).orElse(false)
                                        ));
                            } else {
                                this.setDebug(a.getStringValue().get(this));
                            }
                        }
                    }
                    return true;
                }
                case "-f":
                case "--fetch": {
                    a = cmdLine.nextEntry().get(this);
                    if (active) {
                        this.setFetchStrategy(a.getStringValue().flatMap(NFetchStrategy::parse).get(this));
                    }
                    return true;
                }
                case "-a":
                case "--anywhere": {
                    a = cmdLine.nextFlag().get(this);
                    if (active && a.getBooleanValue().get(this)) {
                        this.setFetchStrategy(NFetchStrategy.ANYWHERE);
                    }
                    return true;
                }
                case "-F":
                case "--offline": {
                    a = cmdLine.nextFlag().get(this);
                    if (active && a.getBooleanValue().get(this)) {
                        this.setFetchStrategy(NFetchStrategy.OFFLINE);
                    }
                    return true;
                }
                case "--online": {
                    a = cmdLine.nextFlag().get(this);
                    if (active && a.getBooleanValue().get(this)) {
                        this.setFetchStrategy(NFetchStrategy.ONLINE);
                    }
                    return true;
                }
                case "--remote": {
                    a = cmdLine.nextFlag().get(this);
                    if (active && a.getBooleanValue().get(this)) {
                        this.setFetchStrategy(NFetchStrategy.REMOTE);
                    }
                    return true;
                }
                case "-c":
                case "--color": {
                    //if the value is not immediately attached with '=' don't consider
                    a = cmdLine.next().get(this);
                    if (active) {
                        NTerminalMode v = a.getStringValue().flatMap(NTerminalMode::parse)
                                .ifEmpty(NTerminalMode.FORMATTED).get(this);
                        if (v == NTerminalMode.DEFAULT) {
                            v = NTerminalMode.INHERITED;
                        }
                        getTerminal().setOut(getTerminal().out().setTerminalMode(v));
                        getTerminal().setErr(getTerminal().err().setTerminalMode(v));
                    }
                    return true;
                }
                case "-B":
                case "--bot": {
                    a = cmdLine.nextFlag().get(this);
                    if (active && a.getBooleanValue().get(this)) {
                        getTerminal().setOut(getTerminal().out().setTerminalMode(NTerminalMode.FILTERED));
                        getTerminal().setErr(getTerminal().err().setTerminalMode(NTerminalMode.FILTERED));
                        setProgressOptions("none");
                        setConfirm(NConfirmationMode.ERROR);
                        setTrace(false);
//                        setDebug(false);
                        setGui(false);
                    }
                    return true;
                }
                case "--dry":
                case "-D": {
                    a = cmdLine.nextFlag().get(this);
                    if (active) {
                        setDry(a.getBooleanValue().get(this));
                    }
                    return true;
                }
                case "--out-line-prefix": {
                    a = cmdLine.nextEntry().get(this);
                    if (active) {
                        this.setOutLinePrefix(a.getStringValue().get(this));
                    }
                    return true;
                }
                case "--err-line-prefix": {
                    a = cmdLine.nextEntry().get(this);
                    if (active) {
                        this.setErrLinePrefix(a.getStringValue().get(this));
                    }
                    return true;
                }
                case "--line-prefix": {
                    a = cmdLine.nextEntry().get(this);
                    if (active) {
                        this.setOutLinePrefix(a.getStringValue().get(this));
                        this.setErrLinePrefix(a.getStringValue().get(this));
                    }
                    return true;
                }
                case "--embedded":
                case "-b": {
                    a = cmdLine.nextFlag().get(this);
                    if (active && a.getBooleanValue().get(this)) {
                        setExecutionType(NExecutionType.EMBEDDED);
                    }
                    //ignore
                    break;
                }
                case "--external":
                case "--spawn":
                case "-x": {
                    a = cmdLine.nextFlag().get(this);
                    if (active && a.getBooleanValue().get(this)) {
                        setExecutionType(NExecutionType.SPAWN);
                    }
                    break;
                }
                case "--system": {
                    a = cmdLine.nextFlag().get(this);
                    if (active && a.getBooleanValue().get(this)) {
                        setExecutionType(NExecutionType.SYSTEM);
                    }
                    break;
                }
                case "--current-user": {
                    a = cmdLine.nextFlag().get(this);
                    if (active && a.getBooleanValue().get(this)) {
                        setRunAs(NRunAs.currentUser());
                    }
                    break;
                }
                case "--as-root": {
                    a = cmdLine.nextFlag().get(this);
                    if (active && a.getBooleanValue().get(this)) {
                        setRunAs(NRunAs.root());
                    }
                    break;
                }
                case "--sudo": {
                    a = cmdLine.nextFlag().get(this);
                    if (active && a.getBooleanValue().get(this)) {
                        setRunAs(NRunAs.sudo());
                    }
                    break;
                }
                case "--as-user": {
                    a = cmdLine.nextEntry().get(this);
                    if (active) {
                        setRunAs(NRunAs.user(a.getStringValue().get(this)));
                    }
                    break;
                }
                case "--verbose":

                case "--log-verbose":
                case "--log-finest":
                case "--log-finer":
                case "--log-fine":
                case "--log-info":
                case "--log-warning":
                case "--log-severe":
                case "--log-config":
                case "--log-all":
                case "--log-off":

                case "--log-term-verbose":
                case "--log-term-finest":
                case "--log-term-finer":
                case "--log-term-fine":
                case "--log-term-info":
                case "--log-term-warning":
                case "--log-term-severe":
                case "--log-term-config":
                case "--log-term-all":
                case "--log-term-off":

                case "--log-file-verbose":
                case "--log-file-finest":
                case "--log-file-finer":
                case "--log-file-fine":
                case "--log-file-info":
                case "--log-file-warning":
                case "--log-file-severe":
                case "--log-file-config":
                case "--log-file-all":
                case "--log-file-off":

                case "--log-file-size":
                case "--log-file-name":
                case "--log-file-base":
                case "--log-file-count": {
                    if (active) {
                        parseLogLevel(cmdLine, active);
                    }
                    break;
                }
                case "-?":
                case "-h":
                case "--help": {
                    boolean enabled = a.isActive();
                    cmdLine.skip();
                    if (enabled) {
                        if (cmdLine.isExecMode()) {
                            printAppHelp();
                        }
                        cmdLine.skipAll();
                        throw new NExecutionException(this, NMsg.ofPlain("help"), NExecutionException.SUCCESS);
                    }
                    break;
                }
                case "--skip-event": {
                    boolean enabled = a.isActive();
                    switch (getAppMode()) {
                        case INSTALL:
                        case UNINSTALL:
                        case UPDATE: {
                            if (enabled) {
                                cmdLine.skip();
                                throw new NExecutionException(this, NMsg.ofPlain("skip-event"), NExecutionException.SUCCESS);
                            }
                        }
                    }
                    return true;
                }
                case "--version": {
                    boolean enabled = a.isActive();
                    cmdLine.skip();
                    if (enabled) {
                        if (cmdLine.isExecMode()) {
                            out().println(NIdResolver.of(this).resolveId(getClass()).getVersion());
                            cmdLine.skipAll();
                        }
                        throw new NExecutionException(this, NMsg.ofPlain("version"), NExecutionException.SUCCESS);
                    }
                    return true;
                }

            }
        }
        return false;
    }


    //    @Override
    public NContentType getPreferredOutputFormat() {
        return this.outputFormat;
    }

    @Override
    public boolean isTrace() {
        boolean b = isBot();
        if (b) {
            return false;
        }
        return (trace != null) ? trace : NBootManager.of(this).getBootOptions().getTrace().orElse(true);
    }

    @Override
    public NSession setTrace(Boolean trace) {
        this.trace = trace;
        return this;
    }

    @Override
    public boolean isPlainTrace() {
        return isTrace()
                && !isIterableOut()
                && getOutputFormat() == NContentType.PLAIN;
    }

    @Override
    public boolean isIterableTrace() {
        return isTrace()
                && isIterableOut();
    }

    @Override
    public boolean isStructuredTrace() {
        return isTrace()
                && !isIterableOut()
                && (isBot() || getOutputFormat() != NContentType.PLAIN);
    }

    @Override
    public boolean isIterableOut() {
        return iterableOut;
    }

    @Override
    public NSession setIterableOut(boolean iterableOut) {
        this.iterableOut = iterableOut;
        return this;
    }

    @Override
    public boolean isStructuredOut() {
        return !isIterableOut()
                && (isBot() || getOutputFormat() != NContentType.PLAIN);
    }

    @Override
    public NArrayElementBuilder getElemOut() {
        return eout;
    }

    @Override
    public NSession setElemOut(NArrayElementBuilder eout) {
        this.eout = eout;
        return this;
    }

    @Override
    public boolean isPlainOut() {
        return !isBot() && getOutputFormat() == NContentType.PLAIN;
    }

    @Override
    public boolean isBot() {
        if (bot != null) {
            return bot;
        }
        return NBootManager.of(this).getBootOptions().getBot().orElse(false);
    }

    @Override
    public Boolean getBot() {
        return bot;
    }

    @Override
    public NSession setBot(Boolean bot) {
        this.bot = bot;
        return this;
    }

    @Override
    public boolean isYes() {
        return getConfirm() == NConfirmationMode.YES;
    }

    @Override
    public boolean isNo() {
        return getConfirm() == NConfirmationMode.NO;
    }

    @Override
    public boolean isAsk() {
        return getConfirm() == NConfirmationMode.ASK;
    }

    @Override
    public NContentType getOutputFormat() {
        if (this.outputFormat != null) {
            return this.outputFormat;
        }
        NContentType o = NBootManager.of(this).getBootOptions().getOutputFormat().orNull();
        if (o != null) {
            return o;
        }
        return NContentType.PLAIN;
    }

    @Override
    public NSession setOutputFormat(NContentType outputFormat) {
        if (outputFormat == null) {
            outputFormat = NContentType.PLAIN;
        }
        this.outputFormat = outputFormat;
        return this;
    }

    @Override
    public NSession json() {
        return setOutputFormat(NContentType.JSON);
    }

    @Override
    public NSession plain() {
        return setOutputFormat(NContentType.PLAIN);
    }

    @Override
    public NSession props() {
        return setOutputFormat(NContentType.PROPS);
    }

    @Override
    public NSession tree() {
        return setOutputFormat(NContentType.TREE);
    }

    @Override
    public NSession table() {
        return setOutputFormat(NContentType.TABLE);
    }

    @Override
    public NSession xml() {
        return setOutputFormat(NContentType.XML);
    }

    @Override
    public NSession copy() {
        try {
            DefaultNSession cloned = (DefaultNSession) clone();
            cloned.ws = new NWorkspaceSessionAwareImpl(cloned, ws);
            cloned.terminal = terminal == null ? null : NSessionTerminal.of(terminal, cloned);
            cloned.properties = properties == null ? null : properties.copy();
            cloned.refProperties = new NPropertiesHolder();
            cloned.outputFormatOptions = outputFormatOptions == null ? null : new ArrayList<>(outputFormatOptions);
            cloned.listeners = null;

            cloned.appClass = this.getAppClass();
            NStoreType[] values = NStoreType.values();
            for (int i = 0; i < values.length; i++) {
                NStoreType value = values[i];
                cloned.appFolders[i] = this.getAppFolder(value);
            }
            for (int i = 0; i < values.length; i++) {
                NStoreType value = values[i];
                cloned.appSharedFolders[i] = this.getAppSharedFolder(value);
            }
            cloned.appAutoComplete = this.getAppAutoComplete();
            cloned.appStartTime = this.getAppStartTime();
            cloned.appArgs = this.getAppArguments() == null ? null : new ArrayList<>(this.getAppArguments());
            cloned.appMode = this.getAppMode();
            cloned.appStoreLocationResolver = this.getAppStoreLocationResolver();
            this.appPreviousVersion = this.getAppPreviousVersion();
            cloned.appModeArgs = this.getAppModeArguments() == null ? null : new ArrayList<>(this.getAppModeArguments());


            if (listeners != null) {
                for (NListener listener : getListeners()) {
                    cloned.addListener(listener);
                }
            }
            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new NUnsupportedOperationException(this, NMsg.ofC("clone failed for type %s", getClass().getName()), e);
        }
    }

    @Override
    public NSession setAll(NSession other) {
        this.terminal = other.getTerminal();
        Map<String, Object> properties = other.getProperties();
        this.properties.setProperties(properties == null ? null : new LinkedHashMap<>(properties));
        this.listeners.clear();
        for (NListener listener : other.getListeners()) {
            addListener(listener);
        }
        this.trace = other.isTrace();
//        this.force = other.isForce();
        this.confirm = other.getConfirm();
        this.dry = other.isDry();
        this.gui = other.isGui();
        this.errLinePrefix = other.getErrLinePrefix();
        this.outLinePrefix = other.getOutLinePrefix();
        this.fetchStrategy = other.getFetchStrategy();
        this.fetchStrategy = other.getFetchStrategy();
        this.cached = other.isCached();
        this.indexed = other.isIndexed();
        this.transitive = other.isTransitive();

        if (other instanceof DefaultNSession) {
            this.outputFormat = ((DefaultNSession) other).getPreferredOutputFormat();
        } else {
            this.outputFormat = other.getOutputFormat();
        }
        this.iterableOut = other.isIterableOut();
        this.outputFormatOptions.clear();
        this.outputFormatOptions.addAll(other.getOutputFormatOptions());
        this.progressOptions = other.getProgressOptions();
        this.logTermLevel = other.getLogTermLevel();
        this.logTermFilter = other.getLogTermFilter();
        this.logFileLevel = other.getLogFileLevel();
        this.logFileFilter = other.getLogFileFilter();
        this.eout = other.eout();
        this.appId = other.getAppId();
        this.appClass = other.getAppClass();
        NStoreType[] values = NStoreType.values();
        for (int i = 0; i < values.length; i++) {
            NStoreType value = values[i];
            this.appFolders[i] = other.getAppFolder(value);
        }
        for (int i = 0; i < values.length; i++) {
            NStoreType value = values[i];
            this.appSharedFolders[i] = other.getAppSharedFolder(value);
        }
        this.appAutoComplete = other.getAppAutoComplete();
        this.appStartTime = other.getAppStartTime();
        this.appArgs = other.getAppArguments() == null ? null : new ArrayList<>(other.getAppArguments());
        this.appMode = other.getAppMode();
        this.appStoreLocationResolver = other.getAppStoreLocationResolver();
        this.appPreviousVersion = other.getAppPreviousVersion();
        this.appModeArgs = other.getAppModeArguments() == null ? null : new ArrayList<>(other.getAppModeArguments());
        this.dependencySolver = other.getDependencySolver();
        return this;
    }

    @Override
    public NSession setAll(NWorkspaceOptions options) {
        if (options != null) {
            this.trace = options.getTrace().orElse(true);
            this.debug = options.getDebug().orNull();
            this.progressOptions = options.getProgressOptions().orNull();
            this.dry = options.getDry().orNull();
            this.cached = options.getCached().orNull();
            this.indexed = options.getIndexed().orNull();
            this.gui = options.getGui().orNull();
            this.confirm = options.getConfirm().orNull();
            this.errLinePrefix = options.getErrLinePrefix().orNull();
            this.outLinePrefix = options.getOutLinePrefix().orNull();
            this.fetchStrategy = options.getFetchStrategy().orNull();
            this.outputFormat = options.getOutputFormat().orNull();
            this.outputFormatOptions.clear();
            this.outputFormatOptions.addAll(options.getOutputFormatOptions().orElseGet(Collections::emptyList));
            NLogConfig logConfig = options.getLogConfig().orNull();
            if (logConfig != null) {
                this.logTermLevel = logConfig.getLogTermLevel();
                this.logTermFilter = logConfig.getLogTermFilter();
                this.logFileLevel = logConfig.getLogFileLevel();
                this.logFileFilter = logConfig.getLogFileFilter();
            }
            this.dependencySolver = options.getDependencySolver().orNull();
        }
        return this;
    }


    @Override
    public NId getAppId() {
        return this.appId;
    }

    @Override
    public NFetchStrategy getFetchStrategy() {
        if (fetchStrategy != null) {
            return fetchStrategy;
        }
        NFetchStrategy wfetchStrategy = NBootManager.of(this).getBootOptions().getFetchStrategy().orNull();
        if (wfetchStrategy != null) {
            return wfetchStrategy;
        }
        return NFetchStrategy.ONLINE;
    }

    @Override
    public NSession setFetchStrategy(NFetchStrategy mode) {
        this.fetchStrategy = mode;
        return this;
    }

    @Override
    public NSession addListener(NListener listener) {
        if (listener != null) {
            boolean ok = false;
            for (Class cls : new Class[]{
                    NWorkspaceListener.class,
                    NRepositoryListener.class,
                    NInstallListener.class,
                    NMapListener.class
            }) {
                if (cls.isInstance(listener)) {
                    if (listeners == null) {
                        listeners = new HashMap<>();
                    }
                    LinkedHashSet<NListener> li = listeners.get(cls);
                    if (li == null) {
                        li = new LinkedHashSet<>();
                        listeners.put(cls, li);
                    }
                    li.add(listener);
                    ok = true;
                }
            }
            if (!ok) {
                throw new NIllegalArgumentException(this, NMsg.ofC("unsupported Listener %s : %s", listener.getClass().getName(), listener));
            }
        }
        return this;
    }

    @Override
    public NSession removeListener(NListener listener) {
        if (listener != null) {
            if (listeners != null) {
                for (LinkedHashSet<NListener> value : listeners.values()) {
                    value.remove(listener);
                }
            }
        }
        return this;
    }

    @Override
    public <T extends NListener> List<T> getListeners(Class<T> type) {
        if (listeners != null) {
            LinkedHashSet<NListener> tt = listeners.get(type);
            if (tt != null) {
                return new ArrayList<>((Collection) tt);
            }
        }
        return Collections.emptyList();
    }

    @Override
    public List<NListener> getListeners() {
        if (listeners == null) {
            return Collections.emptyList();
        }
        LinkedHashSet<NListener> all = new LinkedHashSet<>();
        for (LinkedHashSet<NListener> value : listeners.values()) {
            all.addAll(value);
        }
        return new ArrayList<>(all);
    }

    @Override
    public NSession setProperty(String key, Object value) {
        this.properties.setProperty(key, value);
        return this;
    }

    @Override
    public Map<String, Object> getProperties() {
        return properties.getProperties();
    }

    @Override
    public NSession setProperties(Map<String, Object> properties) {
        this.properties.setProperties(properties);
        return this;
    }

    @Override
    public Object getProperty(String key) {
        return properties.getProperty(key);
    }


    @Override
    public NSession setRefProperty(String key, Object value) {
        this.refProperties.setProperty(key, value);
        return this;
    }

    @Override
    public Map<String, Object> getRefProperties() {
        return refProperties.getProperties();
    }

    @Override
    public NSession setRefProperties(Map<String, Object> properties) {
        this.refProperties.setProperties(properties);
        return this;
    }

    @Override
    public Object getRefProperty(String key) {
        return refProperties.getProperty(key);
    }

    @Override
    public Object getWorkspaceProperty(String key) {
        return ((NWorkspaceExt) ws).getModel().properties.getProperty(key);
    }

    @Override
    public NConfirmationMode getConfirm() {
        NConfirmationMode cm = (confirm != null) ? confirm : NBootManager.of(this).getBootOptions().getConfirm().orNull();
        if (isBot()) {
            if (cm == null) {
                return NConfirmationMode.ERROR;
            }
            switch (cm) {
                case ASK: {
                    return NConfirmationMode.ERROR;
                }
            }
            return cm;
        }
        return cm == null ? NConfirmationMode.ASK : cm;
    }

    @Override
    public NSession setConfirm(NConfirmationMode confirm) {
        this.confirm = confirm;
        return this;
    }

    @Override
    public NSession addOutputFormatOptions(String... options) {
        if (options != null) {
            for (String option : options) {
                if (!NBlankable.isBlank(option)) {
                    outputFormatOptions.add(option);
                }
            }
        }
        return this;
    }

    @Override
    public List<String> getOutputFormatOptions() {
        return outputFormatOptions;
    }

    @Override
    public NSession setOutputFormatOptions(String... options) {
        outputFormatOptions.clear();
        return addOutputFormatOptions(options);
    }

    @Override
    public NSession setOutputFormatOptions(List<String> options) {
        outputFormatOptions.clear();
        return addOutputFormatOptions(options.toArray(new String[0]));
    }

    @Override
    public NPrintStream out() {
        return terminal.out();
    }

    @Override
    public InputStream in() {
        return terminal.in();
    }

    @Override
    public NPrintStream err() {
        return terminal.err();
    }

    @Override
    public NIterableFormat getIterableOutput() {
        if (!iterableOut) {
            return null;
        }
        return NElements.of(this).setContentType(getOutputFormat()).iter(out());
//        if (iterFormatHandler == null) {
//            return null;
//        }
//        if (iterFormat == null) {
//            iterFormat = new CustomNutsIncrementalOutputFormat(ws, iterFormatHandler);
//            iterFormat.setSession(this);
//        }
//        return iterFormat;
    }

    @Override
    public NSessionTerminal getTerminal() {
        return terminal;
    }

    @Override
    public NSession setTerminal(NSessionTerminal terminal) {
        this.terminal = terminal;
        if (terminal != null) {
            AbstractNSessionTerminal a = (AbstractNSessionTerminal) terminal;
            if (a.getSession() != this) {
                throw new NIllegalArgumentException(this, NMsg.ofPlain("session mismatch"));
            }
            NPrintStream o = a.getOut();
        }
//        this.out0 = (terminal.fout());
//        this.err0 = (terminal.ferr());
//        this.out = out0;
//        this.err = err0;
        return this;
    }

    @Override
    public NWorkspace getWorkspace() {
        return ws;
    }

    @Override
    public boolean isTransitive() {
        if (transitive != null) {
            return transitive;
        }
        return NBootManager.of(this).getBootOptions().getTransitive().orElse(true);
    }

    @Override
    public NSession setTransitive(Boolean value) {
        this.transitive = value;
        return this;
    }

    @Override
    public boolean isCached() {
        if (cached != null) {
            return cached;
        }
        return NBootManager.of(this).getBootOptions().getCached().orElse(true);
    }

    @Override
    public NSession setCached(Boolean value) {
        this.cached = value;
        return this;
    }

    @Override
    public boolean isIndexed() {
        if (indexed != null) {
            return indexed;
        }
        return NBootManager.of(this).getBootOptions().getIndexed().orElse(false);
    }

    @Override
    public NSession setIndexed(Boolean value) {
        this.indexed = value;
        return this;
    }

    @Override
    public Instant getExpireTime() {
        return expireTime;
    }

    @Override
    public NSession setExpireTime(Instant expireTime) {
        this.expireTime = expireTime;
        return this;
    }

    @Override
    public String getProgressOptions() {
        return progressOptions;
    }

    @Override
    public boolean isProgress() {
        if (!isPlainOut() || isBot()) {
            return false;
        }
        //TODO, should we cache this?
        return ProgressOptions.of(this).isEnabled();
    }

    @Override
    public NSession setProgressOptions(String progressOptions) {
        this.progressOptions = progressOptions;
        return this;
    }

    @Override
    public boolean isGui() {
        if (gui != null) {
            return gui;
        }
        return NBootManager.of(this).getBootOptions().getGui().orElse(false);
    }

    @Override
    public NSession setGui(Boolean gui) {
        this.gui = gui;
        return this;
    }

    @Override
    public String getErrLinePrefix() {
        return errLinePrefix;
    }

    @Override
    public NSession setErrLinePrefix(String errLinePrefix) {
        this.errLinePrefix = errLinePrefix;
        return this;
    }

    @Override
    public String getOutLinePrefix() {
        return outLinePrefix;
    }

    @Override
    public NSession setOutLinePrefix(String outLinePrefix) {
        this.outLinePrefix = outLinePrefix;
        return this;
    }

    @Override
    public boolean isDry() {
        if (dry == null) {
            return NBootManager.of(this).getBootOptions().getDry().orElse(false);
        }
        return dry;
    }

    @Override
    public NSession setDry(Boolean dry) {
        this.dry = dry;
        return this;
    }

    @Override
    public Level getLogTermLevel() {
        return logTermLevel;
    }

    @Override
    public NSession setLogTermLevel(Level level) {
        this.logTermLevel = level;
        return this;
    }

    @Override
    public Filter getLogTermFilter() {
        return logTermFilter;
    }

    @Override
    public NSession setLogFilter(Filter filter) {
        this.logTermFilter = filter;
        return this;
    }

    @Override
    public NSession configure(NWorkspaceOptions options) {
        if (options != null) {
            if (options.getCached().isPresent()) {
                this.setCached(options.getCached().orNull());
            }
            if (options.getConfirm().isPresent()) {
                this.setConfirm(options.getConfirm().orNull());
            }
            if (options.getDry().isPresent()) {
                this.setDry(options.getDry().orNull());
            }
            if (options.getOutputFormat().isPresent()) {
                this.setOutputFormat(options.getOutputFormat().orNull());
            }
            if (options.getOutputFormatOptions().isPresent()) {
                this.setOutputFormatOptions(options.getOutputFormatOptions().orElseGet(Collections::emptyList));
            }
            if (options.getErrLinePrefix().isPresent()) {
                this.setErrLinePrefix(options.getErrLinePrefix().orNull());
            }
            if (options.getFetchStrategy().isPresent()) {
                this.setFetchStrategy(options.getFetchStrategy().orNull());
            }
            if (options.getExpireTime().isPresent()) {
                this.setExpireTime(options.getExpireTime().orNull());
            }
            if (options.getGui().isPresent()) {
                this.setGui(options.getGui().orNull());
            }
            if (options.getProgressOptions().isPresent()) {
                this.setProgressOptions(options.getProgressOptions().orNull());
            }
            if (options.getIndexed().isPresent()) {
                this.setIndexed(options.getIndexed().orElse(true));
            }
            if (options.getTrace().isPresent()) {
                this.setTrace(options.getTrace().orElse(true));
            }
            if (options.getBot().isPresent()) {
                boolean wasBot = isBot();
                boolean becomesBot = options.getBot().orElse(false);
                this.setBot(becomesBot);
                if (/*!wasBot && */becomesBot) {
                    if (getTerminal().out().getTerminalMode() != NTerminalMode.FILTERED) {
                        getTerminal().setOut(getTerminal().out().setTerminalMode(NTerminalMode.FILTERED));
                    }
                    if (getTerminal().err().getTerminalMode() != NTerminalMode.FILTERED) {
                        getTerminal().setErr(getTerminal().err().setTerminalMode(NTerminalMode.FILTERED));
                    }
                }
            }
            if (options.getTransitive().isPresent()) {
                this.setTransitive(options.getTransitive().orNull());
            }
            if (options.getTerminalMode().isPresent() && NTerminalMode.DEFAULT != options.getTerminalMode().orNull()) {
                getTerminal().setOut(
                        getTerminal().getOut().setTerminalMode(options.getTerminalMode().orNull())
                );
            }
            if (options.getExecutionType().isPresent()) {
                setExecutionType(options.getExecutionType().orNull());
            }
            if (options.getDependencySolver().isPresent()) {
                setDependencySolver(options.getDependencySolver().orNull());
            }
        }
        return this;
    }

    @Override
    public Level getLogFileLevel() {
        return logFileLevel;
    }

    @Override
    public NSession setLogFileLevel(Level logFileLevel) {
        this.logFileLevel = logFileLevel;
        return this;
    }

    @Override
    public Filter getLogFileFilter() {
        return logFileFilter;
    }

    @Override
    public NSession setLogFileFilter(Filter logFileFilter) {
        this.logFileFilter = logFileFilter;
        return this;
    }

    @Override
    public NArrayElementBuilder eout() {
        if (eout == null) {
            eout = new DefaultNArrayElementBuilder(this);
        }
        return eout;
    }

    @Override
    public NSession flush() {
        NArrayElementBuilder e = eout();
        if (e.size() > 0) {
            out().println(e.build());
            e.clear();
        }
        out().flush();
        return this;
    }

    @Override
    public NExecutionType getExecutionType() {
        if (executionType != null) {
            return executionType;
        }
        return NBootManager.of(this).getBootOptions().getExecutionType().orElse(NExecutionType.SPAWN);
    }

    @Override
    public NSession setExecutionType(NExecutionType executionType) {
        this.executionType = executionType;
        return this;
    }

    @Override
    public String getDebug() {
        if (debug == null) {
            return NBootManager.of(this).getBootOptions().getDebug().orNull();
        }
        return debug;
    }

    @Override
    public NSession setDebug(String debug) {
        this.debug = debug;
        return this;
    }

    @Override
    public String getLocale() {
        if (locale == null) {
            return NBootManager.of(this).getBootOptions().getLocale().orNull();
        }
        return locale;
    }

    @Override
    public NSession setLocale(String locale) {
        this.locale = locale;
        return this;
    }

    public NRunAs getRunAs() {
        if (runAs != null) {
            return runAs;
        }
        NRunAs r = NBootManager.of(this).getBootOptions().getRunAs().orNull();
        if (r != null) {
            return r;
        }
        return NRunAs.currentUser();
    }

    public NSession setRunAs(NRunAs runAs) {
        this.runAs = runAs;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("NutsSession(");
        sb.append(getWorkspace().getLocation());
        if (properties.size() > 0) {
            sb.append(", properties=").append(properties);
        }
        sb.append(")");
        return sb.toString();
    }

    @Override
    public NExtensions extensions() {
        return new DefaultNExtensions(this);
    }

    @Override
    public String getDependencySolver() {
        return dependencySolver;
    }

    @Override
    public NSession setDependencySolver(String dependencySolver) {
        this.dependencySolver = dependencySolver;
        return this;
    }

    private void parseLogLevel(NCmdLine cmdLine, boolean enabled) {
        NArg a = cmdLine.peek().get(this);
        switch (a.key()) {
            //these options are just ignored!
//            case "--log-file-size": {
//                a = cmdLine.nextString();
//                String v = a.getStringValue().get(session);
//                if (enabled) {
//                    Integer fileSize = NutsApiUtils.parseFileSizeInBytes(v, 1024 * 1024, null, null);
//                    if (fileSize == null) {
//                        if (NutsBlankable.isBlank(v)) {
//                            throw new NutsBootException(NMsg.ofC("invalid file size : %s", v));
//                        }
//                    } else {
//                        //always in mega
//                        fileSize = fileSize / (1024 * 1024);
//                        if (fileSize <= 0) {
//                            throw new NutsBootException(NMsg.ofC("invalid file size : %s < 1Mb", v));
//                        }
//                    }
//                    if (fileSize != null) {
//                        this.setLogFileSize(fileSize);
//                    }
//                }
//                break;
//            }
//
//            case "--log-file-count": {
//                a = cmdLine.nextString();
//                if (enabled) {
//                    this.setLogFileCount(a.getValue().getInt());
//                }
//                break;
//            }
//
//            case "--log-file-name": {
//                a = cmdLine.nextString();
//                String v = a.getStringValue().get(session);
//                if (enabled) {
//                    this.setLogFileName(v);
//                }
//                break;
//            }
//
//            case "--log-file-base": {
//                a = cmdLine.nextString();
//                String v = a.getStringValue().get(session);
//                if (enabled) {
//                    this.setLogFileBase(v);
//                }
//                break;
//            }
            case "--log-file-verbose":
            case "--log-file-finest":
            case "--log-file-finer":
            case "--log-file-fine":
            case "--log-file-info":
            case "--log-file-warning":
            case "--log-file-config":
            case "--log-file-severe":
            case "--log-file-all":
            case "--log-file-off": {
                cmdLine.skip();
                if (enabled) {
                    String id = a.getKey().asString().get(this);
                    this.setLogFileLevel(
                            NLogUtils.parseLogLevel(id.substring("--log-file-".length())).ifEmpty(null).get(this));
                }
                break;
            }

            case "--log-term-verbose":
            case "--log-term-finest":
            case "--log-term-finer":
            case "--log-term-fine":
            case "--log-term-info":
            case "--log-term-warning":
            case "--log-term-config":
            case "--log-term-severe":
            case "--log-term-all":
            case "--log-term-off": {
                cmdLine.skip();
                if (enabled) {
                    String id = a.getKey().asString().get(this);
                    this.setLogTermLevel(NLogUtils.parseLogLevel(id.substring("--log-term-".length())).ifEmpty(null).get(this));
                }
                break;
            }

            case "--verbose": {
                cmdLine.skip();
                if (enabled && a.getValue().asBoolean().orElse(true)) {
                    this.setLogTermLevel(Level.FINEST);
                    this.setLogFileLevel(Level.FINEST);
                }
                break;
            }
            case "--log-verbose":
            case "--log-finest":
            case "--log-finer":
            case "--log-fine":
            case "--log-info":
            case "--log-warning":
            case "--log-config":
            case "--log-severe":
            case "--log-all":
            case "--log-off": {
                cmdLine.skip();
                if (enabled) {
                    String id = a.getKey().asString().get(this);
                    Level lvl = NLogUtils.parseLogLevel(id.substring("--log-".length())).ifEmpty(null).get(this);
                    this.setLogTermLevel(lvl);
                    this.setLogFileLevel(lvl);
                }
                break;
            }
        }
    }

    public <T> T getOrComputeWorkspaceProperty(String name, Function<NSession, T> supplier) {
        Object v = getWorkspaceProperty(name);
        if (v != null) {
            return (T) v;
        }
        v = supplier.apply(this);
        setRefProperty(name, v);
        return (T) v;
    }

    public <T> T getOrComputeRefProperty(String name, Function<NSession, T> supplier) {
        Object v = getRefProperty(name);
        if (v != null) {
            return (T) v;
        }
        v = supplier.apply(this);
        setRefProperty(name, v);
        return (T) v;
    }

    @Override
    public <T> T getOrComputeProperty(String name, Function<NSession, T> supplier) {
        Object v = getProperty(name);
        if (v != null) {
            return (T) v;
        }
        v = supplier.apply(this);
        setRefProperty(name, v);
        return (T) v;
    }

    @Override
    public NSession prepareApplication(String[] args0, Class appClass, String storeId, NClock startTime) {
        List<String> args = new ArrayList<>();
        if (args0 != null) {
            for (String s : args0) {
                if (s == null) {
                    s = "";
                }
                args.add(s);
            }
        }
        this.appStartTime = startTime == null ? NClock.now() : startTime;
        int wordIndex = -1;
        if (args.size() > 0 && args.get(0).startsWith("--nuts-exec-mode=")) {
            NCmdLine execModeCommand = NCmdLine.parseDefault(
                    args.get(0).substring(args.get(0).indexOf('=') + 1)).get(this);
            if (execModeCommand.hasNext()) {
                NArg a = execModeCommand.next().get(this);
                switch (a.key()) {
                    case "auto-complete": {
                        this.appMode = NApplicationMode.AUTO_COMPLETE;
                        if (execModeCommand.hasNext()) {
                            wordIndex = execModeCommand.next().get(this).asInt().get(this);
                        }
                        this.appModeArgs = execModeCommand.toStringList();
                        execModeCommand.skipAll();
                        break;
                    }
                    case "install": {
                        this.appMode = NApplicationMode.INSTALL;
                        this.appModeArgs = execModeCommand.toStringList();
                        execModeCommand.skipAll();
                        break;
                    }
                    case "uninstall": {
                        this.appMode = NApplicationMode.UNINSTALL;
                        this.appModeArgs = execModeCommand.toStringList();
                        execModeCommand.skipAll();
                        break;
                    }
                    case "update": {
                        this.appMode = NApplicationMode.UPDATE;
                        if (execModeCommand.hasNext()) {
                            this.appPreviousVersion = NVersion.of(execModeCommand.next().flatMap(NLiteral::asString).get(this)).get(this);
                        }
                        this.appModeArgs = execModeCommand.toStringList();
                        execModeCommand.skipAll();
                        break;
                    }
                    default: {
                        throw new NExecutionException(this, NMsg.ofC("Unsupported nuts-exec-mode : %s", args.get(0)), NExecutionException.ERROR_255);
                    }
                }
            }
            args = args.subList(1, args.size());
        }
        NId _appId = (NId) NApplications.getSharedMap().get("nuts.embedded.application.id");
        if (_appId != null) {
            //("=== Inherited "+_appId);
        } else {
            _appId = NIdResolver.of(this).resolveId(appClass);
        }
        if (_appId == null) {
            throw new NExecutionException(this, NMsg.ofC("invalid Nuts Application (%s). Id cannot be resolved", appClass.getName()), NExecutionException.ERROR_255);
        }
        this.appArgs = (args);
        this.appId = (_appId);
        this.appClass = appClass == null ? null : JavaClassUtils.unwrapCGLib(appClass);
        NLocations locations = NLocations.of(this);
        for (NStoreType folder : NStoreType.values()) {
            setAppFolder(folder, locations.getStoreLocation(this.appId, folder));
            setAppSharedFolder(folder, locations.getStoreLocation(this.appId.builder().setVersion("SHARED").build(), folder));
        }
        if (this.appMode == NApplicationMode.AUTO_COMPLETE) {
            //TODO fix me
//            this.workspace.term().setSession(session).getSystemTerminal()
//                    .setMode(NutsTerminalMode.FILTERED);
            if (wordIndex < 0) {
                wordIndex = args.size();
            }
            this.appAutoComplete = new AppCmdLineAutoComplete(this, args, wordIndex, out());
        } else {
            this.appAutoComplete = null;
        }
        return this;
    }


    @Override
    public NApplicationMode getAppMode() {
        return this.appMode;
    }

    @Override
    public List<String> getAppModeArguments() {
        return this.appModeArgs;
    }

    @Override
    public NCmdLineAutoComplete getAppAutoComplete() {
        return this.appAutoComplete;
    }


    @Override
    public NOptional<NText> getAppHelp() {
        NText h = null;
        try {
            h = NWorkspaceExt.of(getWorkspace()).resolveDefaultHelp(getAppClass(), this);
        } catch (Exception ex) {
            //
        }
        if (h != null) {
            try {
                h = NTexts.of(this).transform(h, new NTextTransformConfig()
                        .setProcessTitleNumbers(true)
                        .setNormalize(true)
                        .setFlatten(true)
                );
            } catch (Exception ex) {
                //
                return NOptional.ofNamedError("application help", ex);
            }
        }
        return NOptional.ofNamed(h, "application help");
    }

    @Override
    public void printAppHelp() {
        NText h = NWorkspaceExt.of(getWorkspace()).resolveDefaultHelp(getAppClass(), this);
        h = NTexts.of(this).transform(h, new NTextTransformConfig()
                .setProcessTitleNumbers(true)
                .setNormalize(true)
                .setFlatten(true)
        );
        if (h == null) {
            this.out().println(NMsg.ofC("Help is %s.", NMsg.ofStyled("missing", NTextStyle.error())));
        } else {
            this.out().println(h);
        }
        //need flush if the help is syntactically incorrect
        this.out().flush();
    }

    @Override
    public Class getAppClass() {
        return this.appClass;
    }

    @Override
    public NPath getAppBinFolder() {
        return getAppFolder(NStoreType.BIN);
    }

    @Override
    public NPath getAppConfFolder() {
        return getAppFolder(NStoreType.CONF);
    }

    @Override
    public NPath getAppLogFolder() {
        return getAppFolder(NStoreType.LOG);
    }

    @Override
    public NPath getAppTempFolder() {
        return getAppFolder(NStoreType.TEMP);
    }

    @Override
    public NPath getAppVarFolder() {
        return getAppFolder(NStoreType.VAR);
    }

    @Override
    public NPath getAppLibFolder() {
        return getAppFolder(NStoreType.LIB);
    }

    @Override
    public NPath getAppRunFolder() {
        return getAppFolder(NStoreType.RUN);
    }

    @Override
    public NPath getAppCacheFolder() {
        return getAppFolder(NStoreType.CACHE);
    }

    @Override
    public NPath getAppVersionFolder(NStoreType location, String version) {
        if (version == null
                || version.isEmpty()
                || version.equalsIgnoreCase("current")
                || version.equals(getAppId().getVersion().getValue())) {
            return getAppFolder(location);
        }
        NId newId = this.getAppId().builder().setVersion(version).build();
        if (this.appStoreLocationResolver != null) {
            NPath r = this.appStoreLocationResolver.getStoreLocation(newId, location);
            if (r != null) {
                return r;
            }
        }
        return NLocations.of(this).getStoreLocation(newId, location);
    }

    @Override
    public NPath getAppSharedAppsFolder() {
        return getAppSharedFolder(NStoreType.BIN);
    }

    @Override
    public NPath getAppSharedConfFolder() {
        return getAppSharedFolder(NStoreType.CONF);
    }

    @Override
    public NPath getAppSharedLogFolder() {
        return getAppSharedFolder(NStoreType.LOG);
    }

    @Override
    public NPath getAppSharedTempFolder() {
        return getAppSharedFolder(NStoreType.TEMP);
    }

    @Override
    public NPath getAppSharedVarFolder() {
        return getAppSharedFolder(NStoreType.VAR);
    }

    @Override
    public NPath getAppSharedLibFolder() {
        return getAppSharedFolder(NStoreType.LIB);
    }

    @Override
    public NPath getAppSharedRunFolder() {
        return getAppSharedFolder(NStoreType.RUN);
    }

    @Override
    public NPath getAppSharedFolder(NStoreType location) {
        return this.appSharedFolders[location.ordinal()];
    }

    @Override
    public NVersion getAppVersion() {
        return this.appId == null ? null : this.appId.getVersion();
    }

    @Override
    public List<String> getAppArguments() {
        return this.appArgs;
    }

    @Override
    public NClock getAppStartTime() {
        return this.appStartTime;
    }

    @Override
    public NVersion getAppPreviousVersion() {
        return this.appPreviousVersion;
    }

    @Override
    public NCmdLine getAppCommandLine() {
        return NCmdLine.of(getAppArguments())
                .setCommandName(getAppId().getArtifactId())
                .setAutoComplete(getAppAutoComplete())
                .setSession(this);
    }

    @Override
    public void processAppCommandLine(NCmdLineProcessor commandLineProcessor) {
        getAppCommandLine().process(commandLineProcessor, new DefaultNCmdLineContext(this));
    }

    @Override
    public NPath getAppFolder(NStoreType location) {
        return this.appFolders[location.ordinal()];
    }

    @Override
    public boolean isAppExecMode() {
        return getAppAutoComplete() == null;
    }

    @Override
    public NAppStoreLocationResolver getAppStoreLocationResolver() {
        return this.appStoreLocationResolver;
    }

    @Override
    public NSession setAppVersionStoreLocationSupplier(NAppStoreLocationResolver appVersionStoreLocationSupplier) {
        this.appStoreLocationResolver = appVersionStoreLocationSupplier;
        return this;
    }

    @Override
    public NSession setAppMode(NApplicationMode mode) {
        this.appMode = mode;
        return this;
    }

    @Override
    public NSession setAppModeArgs(List<String> modeArgs) {
        this.appModeArgs = modeArgs;
        return this;
    }


    @Override
    public NSession setAppFolder(NStoreType location, NPath folder) {
        this.appFolders[location.ordinal()] = folder;
        return this;
    }

    @Override
    public NSession setAppSharedFolder(NStoreType location, NPath folder) {
        this.appSharedFolders[location.ordinal()] = folder;
        return this;
    }

    //    @Override
    public NSession setAppId(NId appId) {
        this.appId = appId;
        return this;
    }

    //    @Override
    @Override
    public NSession setAppArguments(List<String> args) {
        this.appArgs = args;
        return this;
    }

    @Override
    public NSession setAppArguments(String[] args) {
        this.appArgs = new ArrayList<>(Arrays.asList(args));
        return this;
    }

    @Override
    public NSession setAppStartTime(NClock startTime) {
        this.appStartTime = startTime;
        return this;
    }

    @Override
    public NSession setAppPreviousVersion(NVersion previousVersion) {
        this.appPreviousVersion = previousVersion;
        return this;
    }

    private static class AppCmdLineAutoComplete extends NCmdLineAutoCompleteBase {

        private final ArrayList<String> words;
        private final NPrintStream out0;
        private final NSession session;
        private final int wordIndex;

        public AppCmdLineAutoComplete(NSession session, List<String> args, int wordIndex, NPrintStream out0) {
            this.session = session;
            words = new ArrayList<>(args);
            this.wordIndex = wordIndex;
            this.out0 = out0;
        }

        @Override
        public NSession getSession() {
            return session;
        }

        @Override
        public String getLine() {
            return NCmdLine.of(getWords()).toString();
        }

        @Override
        public List<String> getWords() {
            return words;
        }

        @Override
        public int getCurrentWordIndex() {
            return wordIndex;
        }

        @Override
        protected NArgCandidate addCandidatesImpl(NArgCandidate value) {
            NArgCandidate c = super.addCandidatesImpl(value);
            String v = value.getValue();
            if (v == null) {
                throw new NExecutionException(session, NMsg.ofPlain("candidate cannot be null"), NExecutionException.ERROR_2);
            }
            String d = value.getDisplay();
            if (Objects.equals(v, d) || d == null) {
                out0.println(NMsg.ofC("%s", AUTO_COMPLETE_CANDIDATE_PREFIX + NCmdLineUtils.escapeArgument(v)));
            } else {
                out0.println(NMsg.ofC("%s", AUTO_COMPLETE_CANDIDATE_PREFIX + NCmdLineUtils.escapeArgument(v) + " " + NCmdLineUtils.escapeArgument(d)));
            }
            return c;
        }
    }
}
