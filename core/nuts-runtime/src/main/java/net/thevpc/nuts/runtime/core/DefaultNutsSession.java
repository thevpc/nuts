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
package net.thevpc.nuts.runtime.core;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.sessionaware.NutsWorkspaceSessionAwareImpl;
import net.thevpc.nuts.runtime.core.terminals.AbstractNutsSessionTerminal;
import net.thevpc.nuts.runtime.core.util.CoreStringUtils;
import net.thevpc.nuts.runtime.standalone.util.NutsConfigurableHelper;
import net.thevpc.nuts.runtime.standalone.util.NutsPropertiesHolder;

import java.io.InputStream;
import java.lang.reflect.Array;
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
    private Boolean trace;
    private Boolean bot;
    private Boolean debug;
    private NutsExecutionType executionType;
    //    private Boolean force;
    private Boolean dry;
    private Level logTermLevel;
    private Filter logTermFilter;
    private Level logFileLevel;
    private Filter logFileFilter;
    private NutsConfirmationMode confirm = null;
    private NutsContentType outputFormat;
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
        copyFrom(this.ws.config().options());
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
        NutsArgument a = cmdLine.peek();
        if (a != null) {
            boolean enabled = a.isEnabled();
            switch (a.getStringKey()) {
                case "-T":
                case "--output-format-option": {
                    a = cmdLine.nextString();
                    if (enabled) {
                        this.addOutputFormatOptions(a.getStringValue(""));
                    }
                    return true;
                }
                case "-O":
                case "--output-format":
                    a = cmdLine.nextString();
                    if (enabled) {
                        String t = a.getStringValue("");
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
                    a = cmdLine.next();
                    if (enabled) {
                        this.setOutputFormat(NutsContentType.TSON);
                        if (a.getStringValue() != null) {
                            this.addOutputFormatOptions(a.getStringValue());
                        }
                    }
                    break;
                case "--yaml":
                    a = cmdLine.next();
                    if (enabled) {
                        this.setOutputFormat(NutsContentType.YAML);
                        if (a.getStringValue() != null) {
                            this.addOutputFormatOptions(a.getStringValue());
                        }
                    }
                    break;
                case "--json": {
                    a = cmdLine.next();
                    if (enabled) {
                        this.setOutputFormat(NutsContentType.JSON);
                        if (a.getStringValue() != null) {
                            this.addOutputFormatOptions(a.getStringValue());
                        }
                    }
                    return true;
                }
                case "--props": {
                    a = cmdLine.next();
                    if (enabled) {
                        this.setOutputFormat(NutsContentType.PROPS);
                        if (a.getStringValue() != null) {
                            this.addOutputFormatOptions(a.getStringValue());
                        }
                    }
                    return true;
                }
                case "--plain": {
                    a = cmdLine.next();
                    if (enabled) {
                        this.setOutputFormat(NutsContentType.PLAIN);
                        if (a.getStringValue() != null) {
                            this.addOutputFormatOptions(a.getStringValue());
                        }
                    }
                    return true;
                }
                case "--table": {
                    a = cmdLine.next();
                    if (enabled) {
                        this.setOutputFormat(NutsContentType.TABLE);
                        if (a.getStringValue() != null) {
                            this.addOutputFormatOptions(a.getStringValue());
                        }
                    }
                    return true;
                }
                case "--tree": {
                    a = cmdLine.next();
                    if (enabled) {
                        this.setOutputFormat(NutsContentType.TREE);
                        if (a.getStringValue() != null) {
                            this.addOutputFormatOptions(a.getStringValue());
                        }
                    }
                    return true;
                }
                case "--xml": {
                    a = cmdLine.next();
                    if (enabled) {
                        this.setOutputFormat(NutsContentType.XML);
                        if (a.getStringValue() != null) {
                            this.addOutputFormatOptions(a.getStringValue());
                        }
                    }
                    return true;
                }
//                case "-f":
//                case "--force": {
//                    a = cmdLine.nextBoolean();
//                    if (enabled) {
//                        this.setForce(a.getBooleanValue());
//                    }
//                    return true;
//                }
                case "-y":
                case "--yes": {
                    if (enabled) {
                        this.setConfirm(NutsConfirmationMode.YES);
                    }
                    cmdLine.skip();
                    return true;
                }
                case "--ask": {
                    if (enabled) {
                        this.setConfirm(NutsConfirmationMode.ASK);
                    }
                    cmdLine.skip();
                    return true;
                }
                case "-n":
                case "--no": {
                    if (enabled) {
                        this.setConfirm(NutsConfirmationMode.NO);
                    }
                    cmdLine.skip();
                    return true;
                }
                case "--error": {
                    if (enabled) {
                        this.setConfirm(NutsConfirmationMode.ERROR);
                    }
                    cmdLine.skip();
                    return true;
                }
                case "--trace": {
                    NutsArgument v = cmdLine.nextBoolean();
                    if (enabled) {
                        this.setTrace(v.getBooleanValue());
                    }
                    return true;
                }
                case "--progress": {
                    NutsArgument v = cmdLine.nextString();
                    if (enabled) {
                        this.setProgressOptions(v.getStringValue());
                    }
                    return true;
                }
                case "--no-progress": {
                    a = cmdLine.nextBoolean();
                    if (enabled && a.getBooleanValue()) {
                        this.setProgressOptions("none");
                    }
                    return true;
                }
                case "-f":
                case "--fetch": {
                    a = cmdLine.nextString();
                    if (enabled) {
                        this.setFetchStrategy(NutsFetchStrategy.valueOf(a.getStringValue("").toUpperCase().replace("-", "_")));
                    }
                    return true;
                }
                case "-a":
                case "--anywhere": {
                    a = cmdLine.nextBoolean();
                    if (enabled && a.getBooleanValue()) {
                        this.setFetchStrategy(NutsFetchStrategy.ANYWHERE);
                    }
                    return true;
                }
                case "-F":
                case "--offline": {
                    a = cmdLine.nextBoolean();
                    if (enabled && a.getBooleanValue()) {
                        this.setFetchStrategy(NutsFetchStrategy.OFFLINE);
                    }
                    return true;
                }
                case "--online": {
                    a = cmdLine.nextBoolean();
                    if (enabled && a.getBooleanValue()) {
                        this.setFetchStrategy(NutsFetchStrategy.ONLINE);
                    }
                    return true;
                }
                case "--remote": {
                    a = cmdLine.nextBoolean();
                    if (enabled && a.getBooleanValue()) {
                        this.setFetchStrategy(NutsFetchStrategy.REMOTE);
                    }
                    return true;
                }
                case "-c":
                case "--color": {
                    //if the value is not immediately attached with '=' don't consider
                    a = cmdLine.next();
                    if (enabled) {
                        String v = a.getStringValue("");
                        if (v.isEmpty()) {
                            getTerminal().setOut(getTerminal().out().convertMode(NutsTerminalMode.FORMATTED));
                            getTerminal().setErr(getTerminal().err().convertMode(NutsTerminalMode.FORMATTED));
                        } else {
                            NutsArgument bb = ws.commandLine().createArgument(v);
                            Boolean b = bb.getBoolean(null);
                            if (b != null) {
                                if (b) {
                                    getTerminal().setOut(getTerminal().out().convertMode(NutsTerminalMode.FORMATTED));
                                    getTerminal().setErr(getTerminal().err().convertMode(NutsTerminalMode.FORMATTED));
                                } else {
                                    getTerminal().setOut(getTerminal().out().convertMode(NutsTerminalMode.FILTERED));
                                    getTerminal().setErr(getTerminal().err().convertMode(NutsTerminalMode.FILTERED));
                                }
                            } else {
                                switch (v.toLowerCase()) {
                                    case "formatted": {
                                        getTerminal().setOut(getTerminal().out().convertMode(NutsTerminalMode.FORMATTED));
                                        getTerminal().setErr(getTerminal().err().convertMode(NutsTerminalMode.FORMATTED));
                                        break;
                                    }
                                    case "filtered": {
                                        getTerminal().setOut(getTerminal().out().convertMode(NutsTerminalMode.FILTERED));
                                        getTerminal().setErr(getTerminal().err().convertMode(NutsTerminalMode.FILTERED));
                                        break;
                                    }
                                    case "h":
                                    case "inherited": {
                                        getTerminal().setOut(getTerminal().out().convertMode(NutsTerminalMode.INHERITED));
                                        getTerminal().setErr(getTerminal().err().convertMode(NutsTerminalMode.INHERITED));
                                        break;
                                    }
                                    case "s":
                                    case "auto":
                                    case "system": {
                                        getTerminal().setOut(getTerminal().out().convertMode(NutsTerminalMode.INHERITED));
                                        getTerminal().setErr(getTerminal().err().convertMode(NutsTerminalMode.INHERITED));
                                        break;
                                    }
                                    default: {
                                        cmdLine.pushBack(a);
                                        cmdLine.unexpectedArgument();
                                    }
                                }
                            }
                        }
                    }
                    return true;
                }
                case "-C":
                case "--no-color": {
                    a = cmdLine.nextBoolean();
                    if (enabled) {
                        getTerminal().setOut(getTerminal().out().convertMode(NutsTerminalMode.FILTERED));
                        getTerminal().setErr(getTerminal().err().convertMode(NutsTerminalMode.FILTERED));
                    }
                    return true;
                }
                case "-B":
                case "--bot": {
                    a = cmdLine.nextBoolean();
                    if (enabled && a.getBooleanValue()) {
                        getTerminal().setOut(getTerminal().out().convertMode(NutsTerminalMode.FILTERED));
                        getTerminal().setErr(getTerminal().err().convertMode(NutsTerminalMode.FILTERED));
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
                    a = cmdLine.nextBoolean();
                    if (enabled) {
                        setDry(a.getBooleanValue());
                    }
                    return true;
                }
                case "--out-line-prefix": {
                    a = cmdLine.nextString();
                    if (enabled) {
                        this.setOutLinePrefix(a.getStringValue());
                    }
                    return true;
                }
                case "--err-line-prefix": {
                    a = cmdLine.nextString();
                    if (enabled) {
                        this.setErrLinePrefix(a.getStringValue());
                    }
                    return true;
                }
                case "--line-prefix": {
                    a = cmdLine.nextString();
                    if (enabled) {
                        this.setOutLinePrefix(a.getStringValue());
                        this.setErrLinePrefix(a.getStringValue());
                    }
                    return true;
                }
                case "--embedded":
                case "-b": {
                    a = cmdLine.nextBoolean();
                    if (enabled && a.getBooleanValue()) {
                        setExecutionType(NutsExecutionType.EMBEDDED);
                    }
                    //ignore
                    break;
                }
                case "--external":
                case "--spawn":
                case "-x": {
                    a = cmdLine.nextBoolean();
                    if (enabled && a.getBooleanValue()) {
                        setExecutionType(NutsExecutionType.SPAWN);
                    }
                    break;
                }
                case "--user-cmd": {
                    a = cmdLine.nextBoolean();
                    if (enabled && a.getBooleanValue()) {
                        setExecutionType(NutsExecutionType.USER_CMD);
                    }
                    break;
                }
                case "--root-cmd": {
                    a = cmdLine.nextBoolean();
                    if (enabled && a.getBooleanValue()) {
                        setExecutionType(NutsExecutionType.ROOT_CMD);
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
        return (trace != null) ? trace : ws.config().options().isTrace();
    }

    @Override
    public boolean isPlainTrace() {
        return isTrace()
                && !isIterableOut()
                && getOutputFormat() == NutsContentType.PLAIN;
    }

    @Override
    public NutsSession setIterableOut(boolean iterableOut) {
        this.iterableOut = iterableOut;
        return this;
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
    public boolean isStructuredOut() {
        return !isIterableOut()
                && (isBot() || getOutputFormat() != NutsContentType.PLAIN);
    }

    @Override
    public boolean isPlainOut() {
        return !isBot() && getOutputFormat() == NutsContentType.PLAIN;
    }

    @Override
    public NutsSession setTrace(Boolean trace) {
        this.trace = trace;
        return this;
    }

    @Override
    public NutsSession setBot(Boolean bot) {
        this.bot = bot;
        return this;
    }

    @Override
    public boolean isBot() {
        if (bot != null) {
            return bot;
        }
        return ws.config().options().isBot();
    }

    @Override
    public Boolean getBot() {
        return bot;
    }

    //    @Override
//    public NutsSession setSilent() {
//        return setTrace(false);
//    }
//    @Override
//    public boolean isForce() {
//        if(force==null){
//            return ws.config().options().isForce();
//        }
//        return force;
//    }
//    @Override
//    public NutsSession setForce(Boolean force) {
//        this.force = force;
//        return this;
//    }
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
        NutsContentType o = ws.config().options().getOutputFormat();
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
            cloned.terminal = terminal == null ? null : cloned.ws.term().createTerminal(terminal);
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
            throw new NutsUnsupportedOperationException(this, e);
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
        this.outputFormatOptions.addAll(Arrays.asList(other.getOutputFormatOptions()));
        this.progressOptions = other.getProgressOptions();
        this.logTermLevel = other.getLogTermLevel();
        this.logTermFilter = other.getLogTermFilter();
        this.logFileLevel = other.getLogFileLevel();
        this.logFileFilter = other.getLogFileFilter();
        this.appId = other.getAppId();
        return this;
    }

    @Override
    public NutsSession copyFrom(NutsWorkspaceOptions options) {
        if (options != null) {
            this.trace = options.isTrace();
            this.debug = options.isDebug();
            this.progressOptions = options.getProgressOptions();
            this.dry = options.isDry();
            this.cached = options.isCached();
            this.indexed = options.isIndexed();
            this.gui = options.isGui();
            this.confirm = options.getConfirm();
            this.errLinePrefix = options.getErrLinePrefix();
            this.outLinePrefix = options.getOutLinePrefix();
            this.fetchStrategy = options.getFetchStrategy();
            this.outputFormat = options.getOutputFormat();
            this.outputFormatOptions.clear();
            this.outputFormatOptions.addAll(Arrays.asList(options.getOutputFormatOptions()));
            this.outputFormatOptions.addAll(Arrays.asList(options.getOutputFormatOptions()));
            NutsLogConfig logConfig = options.getLogConfig();
            if (logConfig != null) {
                this.logTermLevel = logConfig.getLogTermLevel();
                this.logTermFilter = logConfig.getLogTermFilter();
                this.logFileLevel = logConfig.getLogFileLevel();
                this.logFileFilter = logConfig.getLogFileFilter();
            }

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
    public NutsSession setFetchStrategy(NutsFetchStrategy mode) {
        this.fetchStrategy = mode;
        return this;
    }

    //    @Override
//    public NutsSession fetchStrategy(NutsFetchStrategy mode) {
//        return setFetchStrategy(mode);
//    }
//    @Override
//    public NutsSession fetchRemote() {
//        return setFetchStrategy(NutsFetchStrategy.REMOTE);
//    }
//
//    @Override
//    public NutsSession fetchOffline() {
//        return setFetchStrategy(NutsFetchStrategy.OFFLINE);
//    }
//
//    @Override
//    public NutsSession fetchOnline() {
//        return setFetchStrategy(NutsFetchStrategy.ONLINE);
//    }
//
//    @Override
//    public NutsSession fetchInstalled() {
//        return setFetchStrategy(NutsFetchStrategy.INSTALLED);
//    }
//
//    @Override
//    public NutsSession fetchAnyWhere() {
//        return setFetchStrategy(NutsFetchStrategy.ANYWHERE);
//    }
    @Override
    public NutsFetchStrategy getFetchStrategy() {
        if (fetchStrategy != null) {
            return fetchStrategy;
        }
        NutsFetchStrategy wfetchStrategy = ws.config().getOptions().getFetchStrategy();
        if (wfetchStrategy != null) {
            return wfetchStrategy;
        }
        return NutsFetchStrategy.ONLINE;
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
    public <T extends NutsListener> T[] getListeners(Class<T> type) {
        if (listeners != null) {
            LinkedHashSet<NutsListener> tt = listeners.get(type);
            if (tt != null) {
                return tt.toArray((T[]) Array.newInstance(type, 0));
            }
        }
        return (T[]) Array.newInstance(type, 0);
    }

    @Override
    public NutsListener[] getListeners() {
        if (listeners == null) {
            return new NutsListener[0];
        }
        LinkedHashSet<NutsListener> all = new LinkedHashSet<>();
        for (LinkedHashSet<NutsListener> value : listeners.values()) {
            all.addAll(value);
        }
        return all.toArray(new NutsListener[0]);
    }

    @Override
    public NutsSession setTerminal(NutsSessionTerminal terminal) {
        this.terminal = terminal;
        if (terminal != null) {
            AbstractNutsSessionTerminal a = (AbstractNutsSessionTerminal) terminal;
            if (a.getSession() != this) {
                throw new NutsIllegalArgumentException(this, NutsMessage.cstyle("session mismatch"));
            }
        }
//        this.out0 = (terminal.fout());
//        this.err0 = (terminal.ferr());
//        this.out = out0;
//        this.err = err0;
        return this;
    }

    @Override
    public NutsSession setProperty(String key, Object value) {
        this.properties.setProperty(key, value);
        return this;
    }

    @Override
    public NutsSession setProperties(Map<String, Object> properties) {
        this.properties.setProperties(properties);
        return this;
    }

    @Override
    public Map<String, Object> getProperties() {
        return properties.getProperties();
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
        NutsConfirmationMode cm = (confirm != null) ? confirm : ws.config().options().getConfirm();
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
    public NutsSession setOutputFormatOptions(String... options) {
        outputFormatOptions.clear();
        return addOutputFormatOptions(options);
    }

    @Override
    public NutsSession addOutputFormatOptions(String... options) {
        if (options != null) {
            for (String option : options) {
                if (!CoreStringUtils.isBlank(option)) {
                    outputFormatOptions.add(option);
                }
            }
        }
        return this;
    }

    @Override
    public String[] getOutputFormatOptions() {
        return outputFormatOptions.toArray(new String[0]);
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
        return getWorkspace().elem().setContentType(getOutputFormat()).iter(out());
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
    public NutsWorkspace getWorkspace() {
        return ws;
    }

    @Override
    public boolean isTransitive() {
        if (transitive != null) {
            return transitive;
        }
        return ws.config().options().isTransitive();
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
        return ws.config().options().isCached();
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
        return ws.config().options().isIndexed();
    }

    @Override
    public NutsSession setIndexed(Boolean value) {
        this.indexed = value;
        return this;
    }

    @Override
    public NutsSession setExpireTime(Instant expireTime) {
        this.expireTime = expireTime;
        return this;
    }

    @Override
    public Instant getExpireTime() {
        return expireTime;
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
    public NutsObjectFormat formatObject(Object any) {
        return getWorkspace().formats().object().setSession(this).setValue(any);
    }

    @Override
    public boolean isGui() {
        if (gui != null) {
            return gui;
        }
        return ws.config().options().isGui();
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
            return ws.config().options().isDry();
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
    public NutsSession setLogLevel(Level level) {
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
            if (options.getCached() != null) {
                this.setCached(options.isCached());
            }
            if (options.getConfirm() != null) {
                this.setConfirm(options.getConfirm());
            }
            if (options.getDry() != null) {
                this.setDry(options.getDry());
            }
            if (options.getOutputFormat() != null) {
                this.setOutputFormat(options.getOutputFormat());
            }
            if (options.getOutputFormatOptions() != null) {
                this.setOutputFormatOptions(options.getOutputFormatOptions());
            }
            if (options.getErrLinePrefix() != null) {
                this.setErrLinePrefix(options.getErrLinePrefix());
            }
            if (options.getFetchStrategy() != null) {
                this.setFetchStrategy(options.getFetchStrategy());
            }
            if (options.getExpireTime() != null) {
                this.setExpireTime(options.getExpireTime());
            }
            if (options.getGui() != null) {
                this.setGui(options.getGui());
            }
            if (options.getProgressOptions() != null) {
                this.setProgressOptions(options.getProgressOptions());
            }
            if (options.getIndexed() != null) {
                this.setIndexed(options.getIndexed());
            }
            if (options.getTrace() != null) {
                this.setTrace(options.getTrace());
            }
            if (options.getBot() != null) {
                boolean wasBot = isBot();
                this.setBot(options.getBot());
                boolean becomesBot = isBot();
                if (!wasBot && becomesBot) {
                    if (getTerminal().out().mode() != NutsTerminalMode.FORMATTED) {
                        getTerminal().setOut(getTerminal().out().convertMode(NutsTerminalMode.FILTERED));
                    }
                    if (getTerminal().err().mode() != NutsTerminalMode.FORMATTED) {
                        getTerminal().setErr(getTerminal().err().convertMode(NutsTerminalMode.FILTERED));
                    }
                }
            }
            if (options.getTransitive() != null) {
                this.setTransitive(options.getTransitive());
            }
            if (options.getTerminalMode() != null) {
                getTerminal().setOut(
                        getTerminal().getOut().convertMode(options.getTerminalMode())
                );
            }
            if (options.getExecutionType() != null) {
                setExecutionType(options.getExecutionType());
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
    public NutsExecutionType getExecutionType() {
        if (executionType != null) {
            return executionType;
        }
        return ws.config().options().getExecutionType();
    }

    @Override
    public NutsSession setExecutionType(NutsExecutionType executionType) {
        this.executionType = executionType;
        return this;
    }

    @Override
    public boolean isDebug() {
        if (debug == null) {
            return ws.config().options().isDebug();
        }
        return debug;
    }

    @Override
    public Boolean getDebug() {
        return debug;
    }

    @Override
    public NutsSession setDebug(Boolean debug) {
        this.debug = debug;
        return this;
    }

    @Override
    public String getLocale() {
        if (locale == null) {
            return ws.config().options().getLocale();
        }
        return locale;
    }

    @Override
    public NutsSession setLocale(String locale) {
        this.locale = locale;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("NutsSession(");
        if (properties.size() > 0) {
            sb.append("properties=").append(properties);
        }
        sb.append(")");
        return sb.toString();
    }
}
