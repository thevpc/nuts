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
import net.thevpc.nuts.runtime.standalone.elem.DefaultNutsArrayElementBuilder;
import net.thevpc.nuts.runtime.standalone.io.terminal.AbstractNutsSessionTerminal;
import net.thevpc.nuts.runtime.standalone.util.CoreStringUtils;
import net.thevpc.nuts.runtime.standalone.util.NutsConfigurableHelper;
import net.thevpc.nuts.runtime.standalone.util.collections.NutsPropertiesHolder;

import java.io.InputStream;
import java.time.Instant;
import java.util.*;
import java.util.logging.Filter;
import java.util.logging.Level;

/**
 * Created by vpc on 2/1/17.
 */
public class DefaultNutsSession implements Cloneable, NutsSession {

    //    protected NutsIterableFormat iterFormatHandler = null;
//    protected NutsIterableOutput iterFormat = null;
    protected NutsWorkspace ws = null;
    protected List<String> outputFormatOptions = new ArrayList<>();
    private NutsSessionTerminal terminal;
    private NutsPropertiesHolder properties = new NutsPropertiesHolder();
    private Map<Class, LinkedHashSet<NutsListener>> listeners = new HashMap<>();
    private String dependencySolver;
    private Boolean trace;
    private Boolean bot;
    private String debug;
    private NutsRunAs runAs;

    private NutsExecutionType executionType;
    //    private Boolean force;
    private Boolean dry;
    private Level logTermLevel;
    private Filter logTermFilter;
    private Level logFileLevel;
    private Filter logFileFilter;
    private NutsConfirmationMode confirm = null;
    private NutsContentType outputFormat;
    private NutsArrayElementBuilder eout;
    private NutsFetchStrategy fetchStrategy = null;
    private Boolean cached;
    private Boolean indexed;
    private Boolean transitive;
    private Boolean gui;
    private String progressOptions;
    private String errLinePrefix;
    private String outLinePrefix;
    private Instant expireTime;
    private NutsId appId;
    private String locale;
    private boolean iterableOut;

    public DefaultNutsSession(NutsWorkspace ws) {
        this.ws = new NutsWorkspaceSessionAwareImpl(this, ws);
        copyFrom(NutsSessionUtils.defaultSession(ws).boot().getBootOptions());
    }

    public DefaultNutsSession(NutsWorkspace ws, NutsWorkspaceOptions options) {
        this.ws = new NutsWorkspaceSessionAwareImpl(this, ws);
        copyFrom(options);
    }

    /**
     * configure the current command with the given arguments. This is an
     * override of the {@link NutsCommandLineConfigurable#configure(boolean, java.lang.String...)
     * }
     * to help return a more specific return type;
     *
     * @param args argument to configure with
     * @return {@code this} instance
     */
    @Override
    public Object configure(boolean skipUnsupported, String... args) {
        return NutsConfigurableHelper.configure(this, this, skipUnsupported, args, "nuts-session");
    }

    /**
     * configure the current command with the given arguments.
     *
     * @param skipUnsupported when true, all unsupported options are skipped
     *                        silently
     * @param commandLine     arguments to configure with
     * @return {@code this} instance
     */
    @Override
    public final boolean configure(boolean skipUnsupported, NutsCommandLine commandLine) {
        return NutsConfigurableHelper.configure(this, this, skipUnsupported, commandLine);
    }

    @Override
    public boolean configureFirst(NutsCommandLine cmdLine) {
        NutsArgument a = cmdLine.peek().orNull();
        if (a != null) {
            boolean active = a.isActive();
            switch (a.getStringKey().orElse("")) {
                case "-T":
                case "--output-format-option": {
                    a = cmdLine.nextString().get(this);
                    if (active) {
                        this.addOutputFormatOptions(a.getStringValue().orElse(""));
                    }
                    return true;
                }
                case "-O":
                case "--output-format":
                    a = cmdLine.nextString().get(this);
                    if (active) {
                        String t = a.getStringValue().orElse("");
                        int i = CoreStringUtils.firstIndexOf(t, new char[]{' ', ';', ':', '='});
                        if (i > 0) {
                            this.setOutputFormat(NutsContentType.valueOf(t.substring(0, i).toUpperCase()));
                            this.addOutputFormatOptions(t.substring(i + 1).toUpperCase());
                        } else {
                            this.setOutputFormat(NutsContentType.valueOf(t.toUpperCase()));
                        }
                    }
                    break;
                case "--tson":
                    a = cmdLine.next().get(this);
                    if (active) {
                        this.setOutputFormat(NutsContentType.TSON);
                        if (a.getStringValue() != null) {
                            this.addOutputFormatOptions(a.getStringValue().get(this));
                        }
                    }
                    break;
                case "--yaml":
                    a = cmdLine.next().get(this);
                    if (active) {
                        this.setOutputFormat(NutsContentType.YAML);
                        if (a.getStringValue() != null) {
                            this.addOutputFormatOptions(a.getStringValue().get(this));
                        }
                    }
                    break;
                case "--json": {
                    a = cmdLine.next().get(this);
                    if (active) {
                        this.setOutputFormat(NutsContentType.JSON);
                        this.addOutputFormatOptions(a.getStringValue().orNull());
                    }
                    return true;
                }
                case "--props": {
                    a = cmdLine.next().get(this);
                    if (active) {
                        this.setOutputFormat(NutsContentType.PROPS);
                        this.addOutputFormatOptions(a.getStringValue().orNull());
                    }
                    return true;
                }
                case "--plain": {
                    a = cmdLine.next().get(this);
                    if (active) {
                        this.setOutputFormat(NutsContentType.PLAIN);
                        this.addOutputFormatOptions(a.getStringValue().orNull());
                    }
                    return true;
                }
                case "--table": {
                    a = cmdLine.next().get(this);
                    if (active) {
                        this.setOutputFormat(NutsContentType.TABLE);
                        this.addOutputFormatOptions(a.getStringValue().orNull());
                    }
                    return true;
                }
                case "--tree": {
                    a = cmdLine.next().get(this);
                    if (active) {
                        this.setOutputFormat(NutsContentType.TREE);
                        this.addOutputFormatOptions(a.getStringValue().orNull());
                    }
                    return true;
                }
                case "--xml": {
                    a = cmdLine.next().get(this);
                    if (active) {
                        this.setOutputFormat(NutsContentType.XML);
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
                        this.setConfirm(NutsConfirmationMode.YES);
                    }
                    cmdLine.skip();
                    return true;
                }
                case "--ask": {
                    if (active) {
                        this.setConfirm(NutsConfirmationMode.ASK);
                    }
                    cmdLine.skip();
                    return true;
                }
                case "-n":
                case "--no": {
                    if (active) {
                        this.setConfirm(NutsConfirmationMode.NO);
                    }
                    cmdLine.skip();
                    return true;
                }
                case "--error": {
                    if (active) {
                        this.setConfirm(NutsConfirmationMode.ERROR);
                    }
                    cmdLine.skip();
                    return true;
                }
                case "--trace": {
                    NutsArgument v = cmdLine.nextBoolean().get(this);
                    if (active) {
                        this.setTrace(v.getBooleanValue().get(this));
                    }
                    return true;
                }
                case "--solver": {
                    a = cmdLine.nextString().get(this);
                    if (active) {
                        String s = a.getStringValue().get( this);
                        this.setDependencySolver(s);
                    }
                    break;
                }
                case "--progress": {
                    NutsArgument v = cmdLine.next().get(this);
                    if (active) {
                        String s = a.getStringValue().orNull();
                        if (a.isNegated()) {
                            if (NutsBlankable.isBlank(s)) {
                                s = "false";
                            } else {
                                s = "false," + s;
                            }
                            setProgressOptions(s);
                        } else {
                            if (NutsBlankable.isBlank(s)) {
                                s = "true";
                            } else {
                                s = "true," + s;
                            }
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
                                                NutsValue.of(a.getStringValue().get(this)).asBoolean()
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
                    a = cmdLine.nextString().get(this);
                    if (active) {
                        this.setFetchStrategy(a.getStringValue().flatMap(NutsFetchStrategy::parse).get(this));
                    }
                    return true;
                }
                case "-a":
                case "--anywhere": {
                    a = cmdLine.nextBoolean().get(this);
                    if (active && a.getBooleanValue().get(this)) {
                        this.setFetchStrategy(NutsFetchStrategy.ANYWHERE);
                    }
                    return true;
                }
                case "-F":
                case "--offline": {
                    a = cmdLine.nextBoolean().get(this);
                    if (active && a.getBooleanValue().get(this)) {
                        this.setFetchStrategy(NutsFetchStrategy.OFFLINE);
                    }
                    return true;
                }
                case "--online": {
                    a = cmdLine.nextBoolean().get(this);
                    if (active && a.getBooleanValue().get(this)) {
                        this.setFetchStrategy(NutsFetchStrategy.ONLINE);
                    }
                    return true;
                }
                case "--remote": {
                    a = cmdLine.nextBoolean().get(this);
                    if (active && a.getBooleanValue().get(this)) {
                        this.setFetchStrategy(NutsFetchStrategy.REMOTE);
                    }
                    return true;
                }
                case "-c":
                case "--color": {
                    //if the value is not immediately attached with '=' don't consider
                    a = cmdLine.next().get(this);
                    if (active) {
                        NutsTerminalMode v = a.getStringValue().flatMap(NutsTerminalMode::parse)
                                .ifEmpty(NutsTerminalMode.FORMATTED).get(this);
                        if(v==NutsTerminalMode.DEFAULT){
                            v=NutsTerminalMode.INHERITED;
                        }
                        getTerminal().setOut(getTerminal().out().setMode(v));
                        getTerminal().setErr(getTerminal().err().setMode(v));
                    }
                    return true;
                }
                case "-B":
                case "--bot": {
                    a = cmdLine.nextBoolean().get(this);
                    if (active && a.getBooleanValue().get(this)) {
                        getTerminal().setOut(getTerminal().out().setMode(NutsTerminalMode.FILTERED));
                        getTerminal().setErr(getTerminal().err().setMode(NutsTerminalMode.FILTERED));
                        setProgressOptions("none");
                        setConfirm(NutsConfirmationMode.ERROR);
                        setTrace(false);
//                        setDebug(false);
                        setGui(false);
                    }
                    return true;
                }
                case "--dry":
                case "-D": {
                    a = cmdLine.nextBoolean().get(this);
                    if (active) {
                        setDry(a.getBooleanValue().get(this));
                    }
                    return true;
                }
                case "--out-line-prefix": {
                    a = cmdLine.nextString().get(this);
                    if (active) {
                        this.setOutLinePrefix(a.getStringValue().get(this));
                    }
                    return true;
                }
                case "--err-line-prefix": {
                    a = cmdLine.nextString().get(this);
                    if (active) {
                        this.setErrLinePrefix(a.getStringValue().get(this));
                    }
                    return true;
                }
                case "--line-prefix": {
                    a = cmdLine.nextString().get(this);
                    if (active) {
                        this.setOutLinePrefix(a.getStringValue().get(this));
                        this.setErrLinePrefix(a.getStringValue().get(this));
                    }
                    return true;
                }
                case "--embedded":
                case "-b": {
                    a = cmdLine.nextBoolean().get(this);
                    if (active && a.getBooleanValue().get(this)) {
                        setExecutionType(NutsExecutionType.EMBEDDED);
                    }
                    //ignore
                    break;
                }
                case "--external":
                case "--spawn":
                case "-x": {
                    a = cmdLine.nextBoolean().get(this);
                    if (active && a.getBooleanValue().get(this)) {
                        setExecutionType(NutsExecutionType.SPAWN);
                    }
                    break;
                }
                case "--system": {
                    a = cmdLine.nextBoolean().get(this);
                    if (active && a.getBooleanValue().get(this)) {
                        setExecutionType(NutsExecutionType.SYSTEM);
                    }
                    break;
                }
                case "--current-user": {
                    a = cmdLine.nextBoolean().get(this);
                    if (active && a.getBooleanValue().get(this)) {
                        setRunAs(NutsRunAs.currentUser());
                    }
                    break;
                }
                case "--as-root": {
                    a = cmdLine.nextBoolean().get(this);
                    if (active && a.getBooleanValue().get(this)) {
                        setRunAs(NutsRunAs.root());
                    }
                    break;
                }
                case "--sudo": {
                    a = cmdLine.nextBoolean().get(this);
                    if (active && a.getBooleanValue().get(this)) {
                        setRunAs(NutsRunAs.sudo());
                    }
                    break;
                }
                case "--as-user": {
                    a = cmdLine.nextString().get(this);
                    if (active) {
                        setRunAs(NutsRunAs.user(a.getStringValue().get(this)));
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
            }
        }
        return false;
    }

    //    @Override
    public NutsContentType getPreferredOutputFormat() {
        return this.outputFormat;
    }

    @Override
    public boolean isTrace() {
        boolean b = isBot();
        if (b) {
            return false;
        }
        return (trace != null) ? trace : boot().getBootOptions().getTrace().orElse(true);
    }

    @Override
    public NutsSession setTrace(Boolean trace) {
        this.trace = trace;
        return this;
    }

    @Override
    public boolean isPlainTrace() {
        return isTrace()
                && !isIterableOut()
                && getOutputFormat() == NutsContentType.PLAIN;
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
                && (isBot() || getOutputFormat() != NutsContentType.PLAIN);
    }

    @Override
    public boolean isIterableOut() {
        return iterableOut;
    }

    @Override
    public NutsSession setIterableOut(boolean iterableOut) {
        this.iterableOut = iterableOut;
        return this;
    }

    @Override
    public boolean isStructuredOut() {
        return !isIterableOut()
                && (isBot() || getOutputFormat() != NutsContentType.PLAIN);
    }

    @Override
    public NutsArrayElementBuilder getElemOut() {
        return eout;
    }

    @Override
    public NutsSession setElemOut(NutsArrayElementBuilder eout) {
        this.eout = eout;
        return this;
    }

    @Override
    public boolean isPlainOut() {
        return !isBot() && getOutputFormat() == NutsContentType.PLAIN;
    }

    @Override
    public boolean isBot() {
        if (bot != null) {
            return bot;
        }
        return boot().getBootOptions().getBot().orElse(false);
    }

    @Override
    public Boolean getBot() {
        return bot;
    }

    @Override
    public NutsSession setBot(Boolean bot) {
        this.bot = bot;
        return this;
    }

    @Override
    public boolean isYes() {
        return getConfirm() == NutsConfirmationMode.YES;
    }

    @Override
    public boolean isNo() {
        return getConfirm() == NutsConfirmationMode.NO;
    }

    @Override
    public boolean isAsk() {
        return getConfirm() == NutsConfirmationMode.ASK;
    }

    @Override
    public NutsContentType getOutputFormat(NutsContentType defaultValue) {
        if (this.outputFormat != null) {
            return this.outputFormat;
        }
        NutsContentType o = boot().getBootOptions().getOutputFormat().orNull();
        if (o != null) {
            return o;
        }
        return defaultValue;
    }

    @Override
    public NutsContentType getOutputFormat() {
        return getOutputFormat(NutsContentType.PLAIN);
    }

    @Override
    public NutsSession setOutputFormat(NutsContentType outputFormat) {
        if (outputFormat == null) {
            outputFormat = NutsContentType.PLAIN;
        }
        this.outputFormat = outputFormat;
        return this;
    }

    @Override
    public NutsSession json() {
        return setOutputFormat(NutsContentType.JSON);
    }

    @Override
    public NutsSession plain() {
        return setOutputFormat(NutsContentType.PLAIN);
    }

    @Override
    public NutsSession props() {
        return setOutputFormat(NutsContentType.PROPS);
    }

    @Override
    public NutsSession tree() {
        return setOutputFormat(NutsContentType.TREE);
    }

    @Override
    public NutsSession table() {
        return setOutputFormat(NutsContentType.TABLE);
    }

    @Override
    public NutsSession xml() {
        return setOutputFormat(NutsContentType.XML);
    }

    @Override
    public NutsSession copy() {
        try {
            DefaultNutsSession cloned = (DefaultNutsSession) clone();
            cloned.ws = new NutsWorkspaceSessionAwareImpl(cloned, ws);
            cloned.terminal = terminal == null ? null : NutsSessionTerminal.of(terminal, cloned);
            cloned.properties = properties == null ? null : properties.copy();
            cloned.outputFormatOptions = outputFormatOptions == null ? null : new ArrayList<>(outputFormatOptions);
            cloned.listeners = null;
            if (listeners != null) {
                for (NutsListener listener : getListeners()) {
                    cloned.addListener(listener);
                }
            }
            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new NutsUnsupportedOperationException(this, NutsMessage.cstyle("clone failed for type %s", getClass().getName()), e);
        }
    }

    @Override
    public NutsSession copyFrom(NutsSession other) {
        this.terminal = other.getTerminal();
        Map<String, Object> properties = other.getProperties();
        this.properties.setProperties(properties == null ? null : new LinkedHashMap<>(properties));
        this.listeners.clear();
        for (NutsListener listener : other.getListeners()) {
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

        if (other instanceof DefaultNutsSession) {
            this.outputFormat = ((DefaultNutsSession) other).getPreferredOutputFormat();
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
        this.dependencySolver = other.getDependencySolver();
        return this;
    }

    @Override
    public NutsSession copyFrom(NutsWorkspaceOptions options) {
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
            NutsLogConfig logConfig = options.getLogConfig().orNull();
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
    public NutsId getAppId() {
        return appId;
    }

    @Override
    public NutsSession setAppId(NutsId appId) {
        this.appId = appId;
        return this;
    }

    @Override
    public NutsFetchStrategy getFetchStrategy() {
        if (fetchStrategy != null) {
            return fetchStrategy;
        }
        NutsFetchStrategy wfetchStrategy = boot().getBootOptions().getFetchStrategy().orNull();
        if (wfetchStrategy != null) {
            return wfetchStrategy;
        }
        return NutsFetchStrategy.ONLINE;
    }

    @Override
    public NutsSession setFetchStrategy(NutsFetchStrategy mode) {
        this.fetchStrategy = mode;
        return this;
    }

    @Override
    public NutsSession addListener(NutsListener listener) {
        if (listener != null) {
            boolean ok = false;
            for (Class cls : new Class[]{
                    NutsWorkspaceListener.class,
                    NutsRepositoryListener.class,
                    NutsInstallListener.class,
                    NutsMapListener.class
            }) {
                if (cls.isInstance(listener)) {
                    if (listeners == null) {
                        listeners = new HashMap<>();
                    }
                    LinkedHashSet<NutsListener> li = listeners.get(cls);
                    if (li == null) {
                        li = new LinkedHashSet<>();
                        listeners.put(cls, li);
                    }
                    li.add(listener);
                    ok = true;
                }
            }
            if (!ok) {
                throw new NutsIllegalArgumentException(this, NutsMessage.cstyle("unsupported Listener %s : %s", listener.getClass().getName(), listener));
            }
        }
        return this;
    }

    @Override
    public NutsSession removeListener(NutsListener listener) {
        if (listener != null) {
            if (listeners != null) {
                for (LinkedHashSet<NutsListener> value : listeners.values()) {
                    value.remove(listener);
                }
            }
        }
        return this;
    }

    @Override
    public <T extends NutsListener> List<T> getListeners(Class<T> type) {
        if (listeners != null) {
            LinkedHashSet<NutsListener> tt = listeners.get(type);
            if (tt != null) {
                return new ArrayList<>((Collection) tt);
            }
        }
        return Collections.emptyList();
    }

    @Override
    public List<NutsListener> getListeners() {
        if (listeners == null) {
            return Collections.emptyList();
        }
        LinkedHashSet<NutsListener> all = new LinkedHashSet<>();
        for (LinkedHashSet<NutsListener> value : listeners.values()) {
            all.addAll(value);
        }
        return new ArrayList<>(all);
    }

    @Override
    public NutsSession setProperty(String key, Object value) {
        this.properties.setProperty(key, value);
        return this;
    }

    @Override
    public Map<String, Object> getProperties() {
        return properties.getProperties();
    }

    @Override
    public NutsSession setProperties(Map<String, Object> properties) {
        this.properties.setProperties(properties);
        return this;
    }

    @Override
    public Object getProperty(String key) {
        return properties.getProperty(key);
    }

    //    @Override
//    public NutsSession setAsk(boolean ask) {
//        return setConfirm(ask ? NutsConfirmationMode.ASK : null);
//    }
//    @Override
//    public NutsSession setYes(boolean value) {
//        return setConfirm(value ? NutsConfirmationMode.YES : null);
//    }
//
//    @Override
//    public NutsSession yes(boolean value) {
//        return setYes(value);
//    }
//
//    @Override
//    public NutsSession setNo(boolean value) {
//        return setConfirm(value ? NutsConfirmationMode.NO : null);
//    }
//
//    @Override
//    public NutsSession no(boolean value) {
//        return setNo(value);
//    }
//
//    @Override
//    public NutsSession no() {
//        return no(true);
//    }
//
//    @Override
//    public NutsSession yes() {
//        return yes(true);
//    }
//
//    @Override
//    public NutsSession ask() {
//        return setAsk(true);
//    }
    @Override
    public NutsConfirmationMode getConfirm() {
        NutsConfirmationMode cm = (confirm != null) ? confirm : boot().getBootOptions().getConfirm().orNull();
        if (isBot()) {
            if (cm == null) {
                return NutsConfirmationMode.ERROR;
            }
            switch (cm) {
                case ASK: {
                    return NutsConfirmationMode.ERROR;
                }
            }
            return cm;
        }
        return cm == null ? NutsConfirmationMode.ASK : cm;
    }

    @Override
    public NutsSession setConfirm(NutsConfirmationMode confirm) {
        this.confirm = confirm;
        return this;
    }

    @Override
    public NutsSession addOutputFormatOptions(String... options) {
        if (options != null) {
            for (String option : options) {
                if (!NutsBlankable.isBlank(option)) {
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
    public NutsSession setOutputFormatOptions(String... options) {
        outputFormatOptions.clear();
        return addOutputFormatOptions(options);
    }

    @Override
    public NutsSession setOutputFormatOptions(List<String> options) {
        outputFormatOptions.clear();
        return addOutputFormatOptions(options.toArray(new String[0]));
    }

    @Override
    public NutsPrintStream out() {
        return terminal.out();
    }

    @Override
    public InputStream in() {
        return terminal.in();
    }

    @Override
    public NutsPrintStream err() {
        return terminal.err();
    }

    @Override
    public NutsIterableFormat getIterableOutput() {
        if (!iterableOut) {
            return null;
        }
        return NutsElements.of(this).setContentType(getOutputFormat()).iter(out());
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
    public NutsSessionTerminal getTerminal() {
        return terminal;
    }

    @Override
    public NutsSession setTerminal(NutsSessionTerminal terminal) {
        this.terminal = terminal;
        if (terminal != null) {
            AbstractNutsSessionTerminal a = (AbstractNutsSessionTerminal) terminal;
            if (a.getSession() != this) {
                throw new NutsIllegalArgumentException(this, NutsMessage.cstyle("session mismatch"));
            }
            NutsPrintStream o = a.getOut();
        }
//        this.out0 = (terminal.fout());
//        this.err0 = (terminal.ferr());
//        this.out = out0;
//        this.err = err0;
        return this;
    }

    @Override
    public NutsWorkspace getWorkspace() {
        return ws;
    }

    @Override
    public boolean isTransitive() {
        if (transitive != null) {
            return transitive;
        }
        return boot().getBootOptions().getTransitive().orElse(true);
    }

    @Override
    public NutsSession setTransitive(Boolean value) {
        this.transitive = value;
        return this;
    }

    @Override
    public boolean isCached() {
        if (cached != null) {
            return cached;
        }
        return boot().getBootOptions().getCached().orElse(true);
    }

    @Override
    public NutsSession setCached(Boolean value) {
        this.cached = value;
        return this;
    }

    @Override
    public boolean isIndexed() {
        if (indexed != null) {
            return indexed;
        }
        return boot().getBootOptions().getIndexed().orElse(false);
    }

    @Override
    public NutsSession setIndexed(Boolean value) {
        this.indexed = value;
        return this;
    }

    @Override
    public Instant getExpireTime() {
        return expireTime;
    }

    @Override
    public NutsSession setExpireTime(Instant expireTime) {
        this.expireTime = expireTime;
        return this;
    }

    @Override
    public String getProgressOptions() {
        return progressOptions;
    }

    @Override
    public NutsSession setProgressOptions(String progressOptions) {
        this.progressOptions = progressOptions;
        return this;
    }

    @Override
    public boolean isGui() {
        if (gui != null) {
            return gui;
        }
        return boot().getBootOptions().getGui().orElse(false);
    }

    @Override
    public NutsSession setGui(Boolean gui) {
        this.gui = gui;
        return this;
    }

    @Override
    public String getErrLinePrefix() {
        return errLinePrefix;
    }

    @Override
    public NutsSession setErrLinePrefix(String errLinePrefix) {
        this.errLinePrefix = errLinePrefix;
        return this;
    }

    @Override
    public String getOutLinePrefix() {
        return outLinePrefix;
    }

    @Override
    public NutsSession setOutLinePrefix(String outLinePrefix) {
        this.outLinePrefix = outLinePrefix;
        return this;
    }

    @Override
    public boolean isDry() {
        if (dry == null) {
            return boot().getBootOptions().getDry().orElse(false);
        }
        return dry;
    }

    @Override
    public NutsSession setDry(Boolean dry) {
        this.dry = dry;
        return this;
    }

    @Override
    public Level getLogTermLevel() {
        return logTermLevel;
    }

    @Override
    public NutsSession setLogTermLevel(Level level) {
        this.logTermLevel = level;
        return this;
    }

    @Override
    public Filter getLogTermFilter() {
        return logTermFilter;
    }

    @Override
    public NutsSession setLogFilter(Filter filter) {
        this.logTermFilter = filter;
        return this;
    }

    @Override
    public NutsSession configure(NutsWorkspaceOptions options) {
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
                    if (getTerminal().out().mode() != NutsTerminalMode.FILTERED) {
                        getTerminal().setOut(getTerminal().out().setMode(NutsTerminalMode.FILTERED));
                    }
                    if (getTerminal().err().mode() != NutsTerminalMode.FILTERED) {
                        getTerminal().setErr(getTerminal().err().setMode(NutsTerminalMode.FILTERED));
                    }
                }
            }
            if (options.getTransitive().isPresent()) {
                this.setTransitive(options.getTransitive().orNull());
            }
            if (options.getTerminalMode().isPresent() && NutsTerminalMode.DEFAULT != options.getTerminalMode().orNull()) {
                getTerminal().setOut(
                        getTerminal().getOut().setMode(options.getTerminalMode().orNull())
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
    public NutsSession setLogFileLevel(Level logFileLevel) {
        this.logFileLevel = logFileLevel;
        return this;
    }

    @Override
    public Filter getLogFileFilter() {
        return logFileFilter;
    }

    @Override
    public NutsSession setLogFileFilter(Filter logFileFilter) {
        this.logFileFilter = logFileFilter;
        return this;
    }

    @Override
    public NutsArrayElementBuilder eout() {
        if (eout == null) {
            eout = new DefaultNutsArrayElementBuilder(this);
        }
        return eout;
    }

    @Override
    public NutsSession flush() {
        NutsArrayElementBuilder e = eout();
        if (e.size() > 0) {
            out().printlnf(e.build());
            e.clear();
        }
        out().flush();
        return this;
    }

    @Override
    public NutsExecutionType getExecutionType() {
        if (executionType != null) {
            return executionType;
        }
        return boot().getBootOptions().getExecutionType().orElse(NutsExecutionType.SPAWN);
    }

    @Override
    public NutsSession setExecutionType(NutsExecutionType executionType) {
        this.executionType = executionType;
        return this;
    }

    @Override
    public String getDebug() {
        if (debug == null) {
            return boot().getBootOptions().getDebug().orNull();
        }
        return debug;
    }

    @Override
    public NutsSession setDebug(String debug) {
        this.debug = debug;
        return this;
    }

    @Override
    public String getLocale() {
        if (locale == null) {
            return boot().getBootOptions().getLocale().orNull();
        }
        return locale;
    }

    @Override
    public NutsSession setLocale(String locale) {
        this.locale = locale;
        return this;
    }

    public NutsRunAs getRunAs() {
        if (runAs != null) {
            return runAs;
        }
        NutsRunAs r = boot().getBootOptions().getRunAs().orNull();
        if (r != null) {
            return r;
        }
        return NutsRunAs.currentUser();
    }

    public NutsSession setRunAs(NutsRunAs runAs) {
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
    public NutsSearchCommand search() {
        return getWorkspace().search();
    }

    @Override
    public NutsFetchCommand fetch() {
        return getWorkspace().fetch();
    }

    @Override
    public NutsDeployCommand deploy() {
        return getWorkspace().deploy();
    }

    @Override
    public NutsUndeployCommand undeploy() {
        return getWorkspace().undeploy();
    }

    @Override
    public NutsExecCommand exec() {
        return getWorkspace().exec();
    }

    @Override
    public NutsInstallCommand install() {
        return getWorkspace().install();
    }

    @Override
    public NutsUninstallCommand uninstall() {
        return getWorkspace().uninstall();
    }

    @Override
    public NutsUpdateCommand update() {
        return getWorkspace().update();
    }

    @Override
    public NutsPushCommand push() {
        return getWorkspace().push();
    }

    @Override
    public NutsUpdateStatisticsCommand updateStatistics() {
        return getWorkspace().updateStatistics();
    }

    @Override
    public NutsWorkspaceExtensionManager extensions() {
        return getWorkspace().extensions();
    }

    @Override
    public NutsWorkspaceConfigManager config() {
        return getWorkspace().config();
    }

    @Override
    public NutsRepositoryManager repos() {
        return getWorkspace().repos();
    }

    @Override
    public NutsWorkspaceSecurityManager security() {
        return getWorkspace().security();
    }


    @Override
    public NutsWorkspaceEventManager events() {
        return getWorkspace().events();
    }

    public NutsInfoCommand info() {
        return getWorkspace().info();
    }

    @Override
    public NutsImportManager imports() {
        return getWorkspace().imports();
    }

    @Override
    public NutsCustomCommandManager commands() {
        return getWorkspace().commands();
    }

    @Override
    public NutsWorkspaceLocationManager locations() {
        return getWorkspace().locations();
    }

    @Override
    public NutsWorkspaceEnvManager env() {
        return getWorkspace().env();
    }

    @Override
    public NutsBootManager boot() {
        return getWorkspace().boot();
    }

    @Override
    public String getDependencySolver() {
        return dependencySolver;
    }

    @Override
    public NutsSession setDependencySolver(String dependencySolver) {
        this.dependencySolver = dependencySolver;
        return this;
    }

    private void parseLogLevel(NutsCommandLine cmdLine, boolean enabled) {
        NutsArgument a = cmdLine.peek().get(this);
        switch (a.getStringKey().orElse("")) {
            //these options are just ignored!
//            case "--log-file-size": {
//                a = cmdLine.nextString();
//                String v = a.getStringValue().get(session);
//                if (enabled) {
//                    Integer fileSize = NutsApiUtils.parseFileSizeInBytes(v, 1024 * 1024, null, null);
//                    if (fileSize == null) {
//                        if (NutsBlankable.isBlank(v)) {
//                            throw new NutsBootException(NutsMessage.cstyle("invalid file size : %s", v));
//                        }
//                    } else {
//                        //always in mega
//                        fileSize = fileSize / (1024 * 1024);
//                        if (fileSize <= 0) {
//                            throw new NutsBootException(NutsMessage.cstyle("invalid file size : %s < 1Mb", v));
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
                            NutsUtilStrings.parseLogLevel(id.substring("--log-file-".length())).ifEmpty(null).get(this));
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
                    this.setLogTermLevel(NutsUtilStrings.parseLogLevel(id.substring("--log-term-".length())).ifEmpty(null).get(this));
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
                    Level lvl = NutsUtilStrings.parseLogLevel(id.substring("--log-".length())).ifEmpty(null).get(this);
                    this.setLogTermLevel(lvl);
                    this.setLogFileLevel(lvl);
                }
                break;
            }
        }
    }

    @Override
    public void configureLast(NutsCommandLine commandLine) {
        if (!configureFirst(commandLine)) {
            commandLine.throwUnexpectedArgument(this);
        }
    }
}
