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
package net.thevpc.nuts.runtime.standalone;

import java.io.InputStream;
import java.io.PrintStream;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.util.common.CoreStringUtils;
import net.thevpc.nuts.runtime.standalone.util.NutsConfigurableHelper;
import net.thevpc.nuts.runtime.standalone.util.NutsPropertiesHolder;
import net.thevpc.nuts.runtime.standalone.util.common.CoreCommonUtils;

import java.lang.reflect.Array;
import java.time.Instant;
import java.util.*;

import net.thevpc.nuts.runtime.standalone.format.CustomNutsIncrementalOutputFormat;

/**
 * Created by vpc on 2/1/17.
 */
public class DefaultNutsSession implements Cloneable, NutsSession {

    private NutsSessionTerminal terminal;
    private NutsPropertiesHolder properties = new NutsPropertiesHolder();
    private Map<Class, LinkedHashSet<NutsListener>> listeners = new HashMap<>();
    private Boolean trace;
//    private Boolean force;
    private Boolean dry;
    private NutsConfirmationMode confirm = null;
    private NutsContentType outputFormat;
    protected NutsIterableFormat iterFormatHandler = null;
    protected NutsIterableOutput iterFormat = null;
    protected NutsWorkspace ws = null;
    protected List<String> outputFormatOptions = new ArrayList<>();
    private NutsFetchStrategy fetchStrategy = null;
    private Boolean cached;
    private Boolean indexed;
    private Boolean transitive;
    private Boolean gui;
    private String progressOptions;
    private String errLinePrefix;
    private String outLinePrefix;
    private Instant expireTime;

    public DefaultNutsSession(NutsWorkspace ws) {
        this.ws = ws;
        copyFrom(ws.config().options());
    }

    @Override
    public NutsSession copyFrom(NutsWorkspaceOptions options) {
        if(options!=null){
            this.trace = options.isTrace();
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
        }
        return this;
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
        this.iterFormat = null;
        this.outputFormatOptions.clear();
        this.outputFormatOptions.addAll(Arrays.asList(other.getOutputFormatOptions()));
        this.progressOptions = other.getProgressOptions();
        return this;
    }

    @Override
    public NutsSession copy() {
        try {
            DefaultNutsSession cloned = (DefaultNutsSession) clone();
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
            throw new NutsUnsupportedOperationException(ws, e);
        }
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
                throw new IllegalArgumentException("Unsupported Listener " + listener.getClass().getName() + " : " + listener);
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

    @Override
    public NutsSession setProperty(String key, Object value) {
        this.properties.setProperty(key, value);
        return this;
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
    public NutsSession setTerminal(NutsSessionTerminal terminal) {
        this.terminal = terminal;
//        this.out0 = (terminal.fout());
//        this.err0 = (terminal.ferr());
//        this.out = out0;
//        this.err = err0;
        return this;
    }

    @Override
    public NutsIterableFormat getIterableFormat() {
        return iterFormatHandler;
    }

    @Override
    public NutsIterableOutput getIterableOutput() {
        if (iterFormatHandler == null) {
            return null;
        }
        if (iterFormat == null) {
            iterFormat = new CustomNutsIncrementalOutputFormat(ws, iterFormatHandler);
            iterFormat.setSession(this);
        }
        return iterFormat;
    }

    @Override
    public NutsSession setIterableFormat(NutsIterableFormat f) {
        if (f == null) {
            this.iterFormatHandler = null;
        } else {
            this.iterFormatHandler = f;
            this.setOutputFormat(f.getOutputFormat());
        }
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
    public NutsSession props() {
        return setOutputFormat(NutsContentType.PROPS);
    }

    @Override
    public NutsSession setOutputFormat(NutsContentType outputFormat) {
        if (outputFormat == null) {
            outputFormat = NutsContentType.PLAIN;
        }
        this.outputFormat = outputFormat;
        return this;
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
        return NutsConfigurableHelper.configure(this, ws, skipUnsupported, commandLine);
    }

    /**
     * configure the current command with the given arguments. This is an
     * override of the {@link NutsConfigurable#configure(boolean, java.lang.String...) }
     * to help return a more specific return type;
     *
     * @param args argument to configure with
     * @return {@code this} instance
     */
    @Override
    public Object configure(boolean skipUnsupported, String... args) {
        return NutsConfigurableHelper.configure(this, ws, skipUnsupported, args, "nuts-session");
    }

    @Override
    public boolean configureFirst(NutsCommandLine cmdLine) {
        NutsArgument a = cmdLine.peek();
        if (a != null) {
            boolean enabled = a.isEnabled();
            switch (a.getStringKey()) {
                case "--output-format": {
                    a = cmdLine.nextString();
                    if (enabled) {
                        NutsContentType outf = CoreCommonUtils.parseEnumString(a.getStringValue(), NutsContentType.class, false);
                        this.setOutputFormat(outf);
                    }
                    return true;
                }
                case "-T":
                case "--output-format-option": {
                    a = cmdLine.nextString();
                    if (enabled) {
                        this.addOutputFormatOptions(a.getStringValue(""));
                    }
                    return true;
                }
                case "--json": {
                    a = cmdLine.next();
                    if (enabled) {
                        this.setOutputFormat(NutsContentType.JSON);
                        this.addOutputFormatOptions(a.getStringValue(""));
                    }
                    return true;
                }
                case "--props": {
                    a = cmdLine.next();
                    if (enabled) {
                        this.setOutputFormat(NutsContentType.PROPS);
                        this.addOutputFormatOptions(a.getStringValue(""));
                    }
                    return true;
                }
                case "--plain": {
                    a = cmdLine.next();
                    if (enabled) {
                        this.setOutputFormat(NutsContentType.PLAIN);
                        this.addOutputFormatOptions(a.getStringValue(""));
                    }
                    return true;
                }
                case "--table": {
                    a = cmdLine.next();
                    if (enabled) {
                        this.setOutputFormat(NutsContentType.TABLE);
                        this.addOutputFormatOptions(a.getStringValue(""));
                    }
                    return true;
                }
                case "--tree": {
                    a = cmdLine.next();
                    if (enabled) {
                        this.setOutputFormat(NutsContentType.TREE);
                        this.addOutputFormatOptions(a.getStringValue(""));
                    }
                    return true;
                }
                case "--xml": {
                    a = cmdLine.next();
                    if (enabled) {
                        this.setOutputFormat(NutsContentType.XML);
                        this.addOutputFormatOptions(a.getStringValue(""));
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
                    NutsArgument v = cmdLine.nextString();
                    if (enabled) {
                        this.setFetchStrategy(NutsFetchStrategy.valueOf(a.getStringValue().toUpperCase().replace("-", "_")));
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
                case "--color":
                    {
                    //if the value is not imediately attatched with '=' don't consider
                    a = cmdLine.next();
                    if (enabled) {
                        String v = a.getStringValue("");
                        if (v.isEmpty()) {
                            getTerminal().setMode(NutsTerminalMode.FORMATTED);
                        } else {
                            NutsArgument bb = ws.commandLine().createArgument(v);
                            Boolean b = bb.getBoolean(null);
                            if (b != null) {
                                if (b) {
                                    getTerminal().setMode(NutsTerminalMode.FORMATTED);

                                } else {
                                    getTerminal().setMode(NutsTerminalMode.FILTERED);
                                }
                            } else {
                                switch (v.toLowerCase()) {
                                    case "formatted": {
                                        getTerminal().setMode(NutsTerminalMode.FORMATTED);
                                        break;
                                    }
                                    case "filtered": {
                                        getTerminal().setMode(NutsTerminalMode.FILTERED);
                                        break;
                                    }
                                    case "h":
                                    case "inherited": {
                                        getTerminal().setMode(NutsTerminalMode.INHERITED);
                                        break;
                                    }
                                    case "s":
                                    case "auto":
                                    case "system": {
                                        getTerminal().setMode(null);
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
                        getTerminal().setMode(NutsTerminalMode.FILTERED);
                    }
                    return true;
                }
                case "--bot": {
                    a = cmdLine.nextBoolean();
                    if (enabled && a.getBooleanValue()) {
                        getTerminal().setMode(NutsTerminalMode.FILTERED);
                        setProgressOptions("none");
                        setConfirm(NutsConfirmationMode.ERROR);
                        setTrace(false);
//                        setDebug(false);
                        setGui(false);
                    }
                    return true;
                }
                case "--dry":
                case "-D":
                    {
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
            }
        }
        return false;
    }

    //    @Override
    public NutsContentType getPreferredOutputFormat() {
        return this.outputFormat;
    }

    @Override
    public NutsContentType getOutputFormat(NutsContentType defaultValue) {
        NutsIterableFormat f = getIterableFormat();
        if (f != null) {
            NutsContentType o = f.getOutputFormat();
            if (o != null) {
                return o;
            }
        }
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
    public boolean isTrace() {
        if(trace==null){
            return ws.config().options().isTrace();
        }
        return trace;
    }

    @Override
    public boolean isPlainTrace() {
        return isTrace()
                && !isIterableOut()
                && getOutputFormat() == NutsContentType.PLAIN;
    }

    @Override
    public boolean isPlainOut() {
        return getOutputFormat() == NutsContentType.PLAIN;
    }

    @Override
    public boolean isStructuredOut() {
        return !isIterableOut()
                && getOutputFormat() != NutsContentType.PLAIN;
    }

    @Override
    public boolean isIterableOut() {
        return getIterableFormat() != null;
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
                && getOutputFormat() != NutsContentType.PLAIN;
    }

    @Override
    public NutsSession setTrace(Boolean trace) {
        this.trace = trace;
        return this;
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
        if (confirm != null) {
            return confirm;
        }
        NutsConfirmationMode c = ws.config().options().getConfirm();
        if (c != null) {
            return c;
        }
        return NutsConfirmationMode.ASK;
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
    public String toString() {
        StringBuilder sb = new StringBuilder("NutsSession(");
        if (properties.size() > 0) {
            sb.append("properties=").append(properties);
        }
        sb.append(")");
        return sb.toString();
    }

    @Override
    public PrintStream out() {
        return terminal.out();
    }

    @Override
    public InputStream in() {
        return terminal.in();
    }

    @Override
    public PrintStream err() {
        return terminal.err();
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
    public Instant getExpireTime() {
        return expireTime;
    }

    @Override
    public NutsSession setExpireTime(Instant expireTime) {
        this.expireTime = expireTime;
        return this;
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
        if(dry==null){
            return ws.config().options().isDry();
        }
        return dry;
    }

    @Override
    public NutsSession setDry(boolean dry) {
        this.dry = dry;
        return this;
    }
}
