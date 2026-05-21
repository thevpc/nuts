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
package net.thevpc.nuts.runtime.standalone.session;

import net.thevpc.nuts.core.*;

import net.thevpc.nuts.app.NApp;
import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.command.NExecutionException;
import net.thevpc.nuts.command.NExecutionType;
import net.thevpc.nuts.command.NFetchStrategy;
import net.thevpc.nuts.command.NInstallListener;
import net.thevpc.nuts.concurrent.NCallable;
import net.thevpc.nuts.concurrent.NScopedValue;
import net.thevpc.nuts.elem.NElementWriter;
import net.thevpc.nuts.core.NRepositoryListener;
import net.thevpc.nuts.runtime.standalone.util.CoreNUtils;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.cmdline.*;
import net.thevpc.nuts.elem.NArrayElementBuilder;
import net.thevpc.nuts.text.NContentType;
import net.thevpc.nuts.text.NIterableFormat;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.io.NTerminal;
import net.thevpc.nuts.io.NTerminalMode;
import net.thevpc.nuts.log.NLogConfig;
import net.thevpc.nuts.log.NLogUtils;
import net.thevpc.nuts.internal.NScopedWorkspace;
import net.thevpc.nuts.runtime.standalone.elem.builder.DefaultNArrayElementBuilder;
import net.thevpc.nuts.runtime.standalone.xtra.time.ProgressOptions;
import net.thevpc.nuts.runtime.standalone.io.terminal.AbstractNTerminal;
import net.thevpc.nuts.runtime.standalone.util.NPropertiesHolder;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.spi.NScopeType;
import net.thevpc.nuts.util.*;

import java.io.InputStream;
import java.time.Instant;
import java.util.*;
import java.util.function.Supplier;
import java.util.logging.Level;

/**
 * Created by vpc on 2/1/17.
 */
public class DefaultNSession implements Cloneable, NSession, NCopiable {
    protected NWorkspace workspace = null;
    protected List<String> outputFormatOptions = new ArrayList<>();
    private NTerminal terminal;
    private NPropertiesHolder properties = new NPropertiesHolder();
    private Map<Class, LinkedHashSet<NListener>> listeners = new HashMap<>();
    private String dependencySolver;
    private Boolean trace;
    private Boolean bot;
    private Boolean previewRepo;
    private String debug;
    private NRunAs runAs;

    private NExecutionType executionType;
    //    private Boolean force;
    private Boolean dry;
    private Boolean showStacktrace;
    private Level logTermLevel;
    private Level logFileLevel;
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

    public DefaultNSession(NWorkspace workspace) {
        this.workspace = workspace;
        copyFrom(NWorkspace.of().bootOptions().toWorkspaceOptions());
    }

    public DefaultNSession(NWorkspace workspace, NWorkspaceOptions options) {
        this.workspace = workspace;
        if (options != null) {
            copyFrom(options);
        }
    }


//    public NPropertiesHolder getSharedProperties() {
//        return sharedProperties;
//    }
//
//    public NPropertiesHolder getTransitiveProperties() {
//        return transitiveProperties;
//    }
//
//    public NPropertiesHolder getRefProperties() {
//        return refProperties;
//    }

    @Override
    public void runWith(Runnable runnable) {
        if (runnable != null) {
            NScopedWorkspace.runWith(workspace, () -> {
                NScopedValue<NSession> nSessions = NWorkspaceExt.of().sessionScopes();
                nSessions.runWith(DefaultNSession.this, runnable);
            });
        }
    }

    @Override
    public <T> T callWith(NCallable<T> callable) {
        if (callable != null) {
            return NScopedWorkspace.callWith(workspace, () -> {
                NScopedValue<NSession> nSessions = NWorkspaceExt.of().sessionScopes();
                return nSessions.callWith(DefaultNSession.this, callable);
            });
        }
        return null;
    }

    @Override
    public void close() {

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
        NId appId = NApp.of().id().orNull();
        String appName = appId == null ? "app" : appId.artifactId();
        return NCmdLineConfigurable.configure(this, skipUnsupported, args, appName);
    }

    @Override
    public boolean configureFirst(NCmdLine cmdLine) {
        NArg a = cmdLine.peek().orNull();
        if (a != null) {
            boolean active = a.isUncommented();
            switch (a.key()) {
                case "-T":
                case "--output-format-option": {
                    a = cmdLine.nextEntry().get();
                    if (active) {
                        this.addOutputFormatOptions(a.getStringValue().orElse(""));
                    }
                    return true;
                }
                case "-O":
                case "--output-format":
                    a = cmdLine.nextEntry().get();
                    if (active) {
                        String t = a.getStringValue().orElse("");
                        int i = NStringUtils.firstIndexOf(t, new char[]{' ', ';', ':', '='});
                        if (i > 0) {
                            this.outputFormat(NContentType.valueOf(t.substring(0, i).toUpperCase()));
                            this.addOutputFormatOptions(t.substring(i + 1).toUpperCase());
                        } else {
                            this.outputFormat(NContentType.valueOf(t.toUpperCase()));
                        }
                    }
                    break;
                case "--tson":
                    a = cmdLine.next().get();
                    if (active) {
                        this.outputFormat(NContentType.TSON);
                        this.addOutputFormatOptions(a.getStringValue().orNull());
                    }
                    break;
                case "--yaml":
                    a = cmdLine.next().get();
                    if (active) {
                        this.outputFormat(NContentType.YAML);
                        this.addOutputFormatOptions(a.getStringValue().orNull());
                    }
                    break;
                case "--json": {
                    a = cmdLine.next().get();
                    if (active) {
                        this.outputFormat(NContentType.JSON);
                        this.addOutputFormatOptions(a.getStringValue().orNull());
                    }
                    return true;
                }
                case "--props": {
                    a = cmdLine.next().get();
                    if (active) {
                        this.outputFormat(NContentType.PROPS);
                        this.addOutputFormatOptions(a.getStringValue().orNull());
                    }
                    return true;
                }
                case "--plain": {
                    a = cmdLine.next().get();
                    if (active) {
                        this.outputFormat(NContentType.PLAIN);
                        this.addOutputFormatOptions(a.getStringValue().orNull());
                    }
                    return true;
                }
                case "--table": {
                    a = cmdLine.next().get();
                    if (active) {
                        this.outputFormat(NContentType.TABLE);
                        this.addOutputFormatOptions(a.getStringValue().orNull());
                    }
                    return true;
                }
                case "--tree": {
                    a = cmdLine.next().get();
                    if (active) {
                        this.outputFormat(NContentType.TREE);
                        this.addOutputFormatOptions(a.getStringValue().orNull());
                    }
                    return true;
                }
                case "--xml": {
                    a = cmdLine.next().get();
                    if (active) {
                        this.outputFormat(NContentType.XML);
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
                        this.confirm(NConfirmationMode.YES);
                    }
                    cmdLine.skip();
                    return true;
                }
                case "--ask": {
                    if (active) {
                        this.confirm(NConfirmationMode.ASK);
                    }
                    cmdLine.skip();
                    return true;
                }
                case "-n":
                case "--no": {
                    if (active) {
                        this.confirm(NConfirmationMode.NO);
                    }
                    cmdLine.skip();
                    return true;
                }
                case "--error": {
                    if (active) {
                        this.confirm(NConfirmationMode.ERROR);
                    }
                    cmdLine.skip();
                    return true;
                }
                case "--trace": {
                    NArg v = cmdLine.nextFlag().get();
                    if (active) {
                        this.trace(v.getBooleanValue().get());
                    }
                    return true;
                }
                case "--solver": {
                    a = cmdLine.nextEntry().get();
                    if (active) {
                        String s = a.getStringValue().get();
                        this.dependencySolver(s);
                    }
                    break;
                }
                case "--progress": {
                    NArg v = cmdLine.next().get();
                    if (active) {
                        String s = a.getStringValue().orNull();
                        if (a.isNegated()) {
                            if (NBlankable.isBlank(s)) {
                                s = "false";
                            } else {
                                s = "false," + s;
                            }
                            progressOptions(s);
                        } else {
                            progressOptions(s);
                        }
                        this.progressOptions(s);
                    }
                    return true;
                }
                case "--debug": {
                    a = cmdLine.next().get();
                    if (active) {
                        if (a.getStringValue().isBlank()) {
                            this.debug(String.valueOf(a.isEnabled()));
                        } else {
                            if (a.isNegated()) {
                                this.debug(
                                        String.valueOf(!NLiteral.of(a.getStringValue().get()).asBoolean()
                                                .onEmpty(true).orElse(false)
                                        ));
                            } else {
                                this.debug(a.getStringValue().get());
                            }
                        }
                    }
                    return true;
                }
                case "-f":
                case "--fetch": {
                    a = cmdLine.nextEntry().get();
                    if (active) {
                        this.fetchStrategy(a.getStringValue().flatMap(NFetchStrategy::parse).get());
                    }
                    return true;
                }
                case "-a":
                case "--anywhere": {
                    a = cmdLine.nextFlag().get();
                    if (active && a.getBooleanValue().get()) {
                        this.fetchStrategy(NFetchStrategy.ANYWHERE);
                    }
                    return true;
                }
                case "-F":
                case "--offline": {
                    a = cmdLine.nextFlag().get();
                    if (active && a.getBooleanValue().get()) {
                        this.fetchStrategy(NFetchStrategy.OFFLINE);
                    }
                    return true;
                }
                case "--online": {
                    a = cmdLine.nextFlag().get();
                    if (active && a.getBooleanValue().get()) {
                        this.fetchStrategy(NFetchStrategy.ONLINE);
                    }
                    return true;
                }
                case "--remote": {
                    a = cmdLine.nextFlag().get();
                    if (active && a.getBooleanValue().get()) {
                        this.fetchStrategy(NFetchStrategy.REMOTE);
                    }
                    return true;
                }
                case "-c":
                case "--color": {
                    //if the value is not immediately attached with '=' don't consider
                    a = cmdLine.next().get();
                    if (active) {
                        NTerminalMode v = null;
                        if (a.isFlagOption()) {
                            if (a.isNegated()) {
                                v = NTerminalMode.INHERITED;
                            } else {
                                v = NTerminalMode.FORMATTED;
                            }
                        } else {
                            v = a.getStringValue().flatMap(NTerminalMode::parse)
                                    .onEmpty(NTerminalMode.FORMATTED).get();
                            if (v == NTerminalMode.DEFAULT) {
                                v = NTerminalMode.INHERITED;
                            }
                        }
                        terminal().out(terminal().out().terminalMode(v));
                        terminal().err(terminal().err().terminalMode(v));
                    }
                    return true;
                }
                case "-B":
                case "--bot": {
                    a = cmdLine.nextFlag().get();
                    if (active) {
                        bot(a.getBooleanValue().get());
                        if (isBot()) {
                            terminal().out(terminal().out().terminalMode(NTerminalMode.FILTERED));
                            terminal().err(terminal().err().terminalMode(NTerminalMode.FILTERED));
                            //setProgressOptions("none");
                            //setConfirm(NConfirmationMode.ERROR);
                            //setTrace(false);
                            //setGui(false);
                        }
                    }
                    return true;
                }
                case "-U":
                case "--preview-repo": {
                    a = cmdLine.nextFlag().get();
                    if (active) {
                        previewRepo(a.getBooleanValue().get());
                    }
                    return true;
                }
                case "--dry":
                case "-D": {
                    a = cmdLine.nextFlag().get();
                    if (active) {
                        dry(a.getBooleanValue().get());
                    }
                    return true;
                }
                case "--out-line-prefix": {
                    a = cmdLine.nextEntry().get();
                    if (active) {
                        this.outLinePrefix(a.getStringValue().get());
                    }
                    return true;
                }
                case "--err-line-prefix": {
                    a = cmdLine.nextEntry().get();
                    if (active) {
                        this.errLinePrefix(a.getStringValue().get());
                    }
                    return true;
                }
                case "--line-prefix": {
                    a = cmdLine.nextEntry().get();
                    if (active) {
                        this.outLinePrefix(a.getStringValue().get());
                        this.errLinePrefix(a.getStringValue().get());
                    }
                    return true;
                }
                case "--embedded":
                case "-b": {
                    a = cmdLine.nextFlag().get();
                    if (active && a.getBooleanValue().get()) {
                        executionType(NExecutionType.EMBEDDED);
                    }
                    //ignore
                    return true;
                }
                case "--gui": {
                    a = cmdLine.nextFlag().get();
                    if (active) {
                        gui(a.getBooleanValue().get());
                    }
                    //ignore
                    return true;
                }
                case "--external":
                case "--spawn":
                case "-x": {
                    a = cmdLine.nextFlag().get();
                    if (active && a.getBooleanValue().get()) {
                        executionType(NExecutionType.SPAWN);
                    }
                    return true;
                }
                case "--system": {
                    a = cmdLine.nextFlag().get();
                    if (active && a.getBooleanValue().get()) {
                        executionType(NExecutionType.SYSTEM);
                    }
                    return true;
                }
                case "--current-user": {
                    a = cmdLine.nextFlag().get();
                    if (active && a.getBooleanValue().get()) {
                        runAs(NRunAs.currentUser());
                    }
                    return true;
                }
                case "--as-root": {
                    a = cmdLine.nextFlag().get();
                    if (active && a.getBooleanValue().get()) {
                        runAs(NRunAs.root());
                    }
                    return true;
                }
                case "--sudo": {
                    a = cmdLine.nextFlag().get();
                    if (active && a.getBooleanValue().get()) {
                        runAs(NRunAs.sudo());
                    }
                    return true;
                }
                case "--as-user": {
                    a = cmdLine.nextEntry().get();
                    if (active) {
                        runAs(NRunAs.user(a.getStringValue().get()));
                    }
                    return true;
                }
                case "-l":
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
                    return true;
                }
                case "-?":
                case "-h":
                case "--help": {
                    boolean enabled = a.isUncommented();
                    cmdLine.skip();
                    if (enabled) {
                        if (cmdLine.isExecMode()) {
                            NApp.of().printHelp();
                        }
                        cmdLine.skipAll();
                        throw new NExecutionException(NMsg.ofPlain("help"), NExecutionException.SUCCESS);
                    }
                    return true;
                }
                case "--skip-event": {
                    boolean enabled = a.isUncommented();
                    switch (NApp.of().mode()) {
                        case INSTALL:
                        case UNINSTALL:
                        case UPDATE: {
                            if (enabled) {
                                cmdLine.skip();
                                throw new NExecutionException(NMsg.ofPlain("skip-event"), NExecutionException.SUCCESS);
                            }
                        }
                    }
                    return true;
                }
                case "--version": {
                    boolean enabled = a.isUncommented();
                    cmdLine.skip();
                    if (enabled) {
                        if (cmdLine.isExecMode()) {
                            out().println(NId.getForClass(getClass()).get().version());
                            cmdLine.skipAll();
                        }
                        throw new NExecutionException(NMsg.ofPlain("version"), NExecutionException.SUCCESS);
                    }
                    return true;
                }

            }
        }
        return false;
    }

    @Override
    public NOptional<Boolean> getTrace() {
        return NOptional.ofNamed(trace, "trace").withDefault(() -> NWorkspace.of().bootOptions().trace().orElse(true));
    }

    @Override
    public Boolean getTrace(boolean withDefaults) {
        if (!withDefaults) {
            return trace;
        }
        boolean b = isBot();
        if (b) {
            return false;
        }
        return (trace != null) ? trace : false;
    }

    @Override
    public boolean isTrace() {
        return getTrace(true);
    }

    @Override
    public NSession trace(Boolean trace) {
        this.trace = trace;
        return this;
    }

    @Override
    public boolean isPlainTrace() {
        return isTrace()
                && !isIterableOut()
                && outputFormat().orDefault() == NContentType.PLAIN;
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
                && (isBot() || outputFormat().orDefault() != NContentType.PLAIN);
    }

    @Override
    public boolean isIterableOut() {
        return iterableOut;
    }

    @Override
    public NSession iterableOut(boolean iterableOut) {
        this.iterableOut = iterableOut;
        return this;
    }

    @Override
    public boolean isStructuredOut() {
        return !isIterableOut()
                && (isBot() || outputFormat().orDefault() != NContentType.PLAIN);
    }

    @Override
    public NArrayElementBuilder elemOut() {
        return eout;
    }

    @Override
    public NSession elemOut(NArrayElementBuilder eout) {
        this.eout = eout;
        return this;
    }

    @Override
    public boolean isPlainOut() {
        return !isBot() && outputFormat().orDefault() == NContentType.PLAIN;
    }

    @Override
    public NOptional<Boolean> bot() {
        return NOptional.ofNamed(bot, "bot").withDefault(
                () -> NWorkspace.of().bootOptions().bot().orElse(false)
        );
    }

    @Override
    public NOptional<Boolean> previewRepo() {
        return NOptional.ofNamed(previewRepo, "previewRepo").withDefault(
                () -> NWorkspace.of().bootOptions().previewRepo()
                        .orElse(NWorkspaceExt.of().getModel().configModel.getStoredConfigMain().isEnablePreviewRepositories())
        );
    }

    public boolean isPreviewRepo() {
        return previewRepo().orDefault();
    }

    public boolean isBot() {
        return bot().orDefault();
    }

    @Override
    public NSession bot(Boolean bot) {
        this.bot = bot;
        return this;
    }

    @Override
    public NSession previewRepo(Boolean bot) {
        this.previewRepo = bot;
        return this;
    }

    @Override
    public NSession yes() {
        return confirm(NConfirmationMode.YES);
    }

    @Override
    public NSession no() {
        return confirm(NConfirmationMode.NO);
    }

    @Override
    public NSession ask() {
        return confirm(NConfirmationMode.ASK);
    }

    @Override
    public boolean isYes() {
        return confirm().orDefault() == NConfirmationMode.YES;
    }

    @Override
    public boolean isNo() {
        return confirm().orDefault() == NConfirmationMode.NO;
    }

    @Override
    public boolean isAsk() {
        return confirm().orDefault() == NConfirmationMode.ASK;
    }

    @Override
    public NOptional<NContentType> outputFormat() {
        return NOptional.ofNamed(outputFormat, "outputFormat")
                .withDefault(() -> {
                    NContentType o = NWorkspace.of().bootOptions().outputFormat().orNull();
                    if (o != null) {
                        return o;
                    }
                    return NContentType.PLAIN;
                });
    }

    @Override
    public NSession outputFormat(NContentType outputFormat) {
        if (outputFormat == null) {
            outputFormat = NContentType.PLAIN;
        }
        this.outputFormat = outputFormat;
        return this;
    }

    @Override
    public NSession json() {
        return outputFormat(NContentType.JSON);
    }

    @Override
    public NSession plain() {
        return outputFormat(NContentType.PLAIN);
    }

    @Override
    public NSession props() {
        return outputFormat(NContentType.PROPS);
    }

    @Override
    public NSession tree() {
        return outputFormat(NContentType.TREE);
    }

    @Override
    public NSession table() {
        return outputFormat(NContentType.TABLE);
    }

    @Override
    public NSession xml() {
        return outputFormat(NContentType.XML);
    }

    @Override
    public NSession copy() {
        try {
            DefaultNSession cloned = (DefaultNSession) clone();
            cloned.terminal = terminal == null ? null : NTerminal.of(terminal);
            cloned.properties = new NPropertiesHolder();
            for (String s : properties.keySet()) {
                NPropertiesHolder.NScopedPropertyValue v = properties.getScopedValue(s);
                switch (v.getScope()) {
                    case SHARED_SESSION: {
                        cloned.properties.setProperty(s, v.getValue(), NScopeType.SHARED_SESSION);
                        break;
                    }
                    case TRANSITIVE_SESSION: {
                        cloned.properties.setProperty(s, CoreNUtils.copyValue(v.getValue()), NScopeType.TRANSITIVE_SESSION);
                        break;
                    }
                }
            }
            cloned.outputFormatOptions = outputFormatOptions == null ? null : new ArrayList<>(outputFormatOptions);
            cloned.listeners = null;
            if (listeners != null) {
                for (NListener listener : listeners()) {
                    cloned.addListener(listener);
                }
            }
            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new NUnsupportedOperationException(NMsg.ofC("clone failed for type %s", getClass().getName()), e);
        }
    }

    @Override
    public NSession copyFrom(NSession other) {
        //boolean withDefaults = false;
        this.terminal = other.terminal() == null ? null : NTerminal.of(terminal);
        this.terminal = other.terminal();
        for (Map.Entry<String,NPropertiesHolder.NScopedPropertyValue> ee : ((DefaultNSession) other).properties.entrySet()) {
            NPropertiesHolder.NScopedPropertyValue v = ee.getValue();
            switch (v.getScope()) {
                case SHARED_SESSION: {
                    this.properties.setProperty(ee.getKey(), v.getValue(), NScopeType.SHARED_SESSION);
                    break;
                }
                case TRANSITIVE_SESSION: {
                    this.properties.setProperty(ee.getKey(), CoreNUtils.copyValue(v.getValue()), NScopeType.TRANSITIVE_SESSION);
                    break;
                }
            }
        }
        if(this.listeners!=null){
            this.listeners.clear();
        }
        this.listeners.clear();
        for (NListener listener : other.listeners()) {
            addListener(listener);
        }
        this.trace = other.getTrace().orNull();
        this.confirm = other.confirm().orNull();
        this.dry = other.dry().orNull();
        this.gui = other.gui().orNull();
        this.bot = other.bot().orNull();
        this.errLinePrefix = other.errLinePrefix();
        this.outLinePrefix = other.outLinePrefix();
        this.fetchStrategy = other.fetchStrategy().orDefault();
        this.cached = other.cached().orNull();
        this.indexed = other.indexed().orNull();
        this.transitive = other.transitive().orNull();

        this.outputFormat = other.outputFormat().orNull();
        this.iterableOut = other.isIterableOut();
        this.outputFormatOptions.clear();
        this.outputFormatOptions.addAll(other.outputFormatOptions());
        this.progressOptions = other.progressOptions();
        this.logTermLevel = other.logTermLevel();
        this.logFileLevel = other.logFileLevel();
        this.eout = other.eout();
        this.dependencySolver = other.dependencySolver();
        return this;
    }

    @Override
    public NSession copyFrom(NWorkspaceOptions options) {
        if (options != null) {
            this.trace = options.trace().orElse(true);
            this.debug = options.debug().orNull();
            this.progressOptions = options.progressOptions().orNull();
            this.dry = options.dry().orNull();
            this.cached = options.cached().orNull();
            this.indexed = options.indexed().orNull();
            this.gui = options.gui().orNull();
            this.confirm = options.confirm().orNull();
            this.errLinePrefix = options.errLinePrefix().orNull();
            this.outLinePrefix = options.outLinePrefix().orNull();
            this.fetchStrategy = options.fetchStrategy().orNull();
            this.outputFormat = options.outputFormat().orNull();
            this.outputFormatOptions.clear();
            this.outputFormatOptions.addAll(options.outputFormatOptions().orElseGet(Collections::emptyList));
            NLogConfig logConfig = options.logConfig().orNull();
            if (logConfig != null) {
                this.logTermLevel = logConfig.logTermLevel();
                this.logFileLevel = logConfig.logFileLevel();
            }
            this.dependencySolver = options.dependencySolver().orNull();
        }
        return this;
    }

    public NSession copyFrom(NBootOptions options) {
        if (options != null) {
            this.trace = options.trace().orElse(true);
            this.debug = options.debug().orNull();
            this.progressOptions = options.progressOptions().orNull();
            this.dry = options.dry().orNull();
            this.cached = options.cached().orNull();
            this.indexed = options.indexed().orNull();
            this.gui = options.gui().orNull();
            this.confirm = options.confirm().orNull();
            this.errLinePrefix = options.errLinePrefix().orNull();
            this.outLinePrefix = options.outLinePrefix().orNull();
            this.fetchStrategy = options.fetchStrategy().orNull();
            this.outputFormat = options.outputFormat().orNull();
            this.outputFormatOptions.clear();
            this.outputFormatOptions.addAll(options.outputFormatOptions().orElseGet(Collections::emptyList));
            NLogConfig logConfig = options.logConfig().orNull();
            if (logConfig != null) {
                this.logTermLevel = logConfig.logTermLevel();
                this.logFileLevel = logConfig.logFileLevel();
            }
            this.dependencySolver = options.dependencySolver().orNull();
        }
        return this;
    }


    @Override
    public NOptional<NFetchStrategy> fetchStrategy() {
        return NOptional.ofNamed(fetchStrategy, "fetchStrategy")
                .withDefault(() -> {
                    NFetchStrategy wfetchStrategy = NWorkspace.of().bootOptions().fetchStrategy().orNull();
                    if (wfetchStrategy != null) {
                        return wfetchStrategy;
                    }
                    return NFetchStrategy.ONLINE;
                })
                ;
    }

    @Override
    public NSession fetchStrategy(NFetchStrategy mode) {
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
                    NObservableMapListener.class
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
                throw new NIllegalArgumentException(NMsg.ofC("unsupported Listener %s : %s", listener.getClass().getName(), listener));
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
    public List<NListener> listeners() {
        if (listeners == null) {
            return Collections.emptyList();
        }
        LinkedHashSet<NListener> all = new LinkedHashSet<>();
        for (LinkedHashSet<NListener> value : listeners.values()) {
            all.addAll(value);
        }
        return new ArrayList<>(all);
    }

    public NPropertiesHolder getPropertiesHolder() {
        return properties;
    }

//    @Override
//    public Map<String, Object> getProperties() {
//        return properties.toMap();
//    }

//    @Override
//    public Map<String, Object> getProperties(NScopeType scope) {
//        LinkedHashMap<String, Object> a = new LinkedHashMap<>();
//        for (String s : properties.keySet()) {
//            NPropertiesHolder.NScopedPropertyValue v = properties.getScopedValue(s);
//            if (v.getScope()==scope) {
//                a.put(s, v.getValue());
//            }
//        }
//        return a;
//    }

//    @Override
//    public NSession setProperties(NScopeType scope, Map<String, Object> properties) {
//        if (scope == null) {
//            scope = NScopeType.SHARED_SESSION;
//        }
//        switch (scope) {
//            case SESSION: {
//                this.refProperties.setProperties(properties);
//                break;
//            }
//            case SHARED_SESSION: {
//                this.sharedProperties.setProperties(properties);
//                break;
//            }
//            case TRANSITIVE_SESSION: {
//                if (properties != null) {
//                    for (Map.Entry<String, Object> e : properties.entrySet()) {
//                        this.transitiveProperties.setProperty(e.getKey(), CoreNUtils.checkCopiableValue(e.getValue()));
//                    }
//                }
//                break;
//            }
//            case WORKSPACE: {
//                ((NWorkspaceExt) workspace).getModel().properties.setProperties(properties);
//                break;
//            }
//            case PROTOTYPE: {
//                break;
//            }
//            default: {
//                throw new NUnsupportedEnumException(scope);
//            }
//        }
//        return this;
//    }

//    @Override
//    public Object getProperty(String key) {
//        return sharedProperties.getProperty(key);
//    }

    @Override
    public NOptional<NConfirmationMode> confirm() {
        return NOptional.ofNamed(confirm, "confirm")
                .withDefault(() -> {
                    NConfirmationMode cm = NWorkspace.of().bootOptions().confirm().orNull();
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
                });
    }

    @Override
    public NSession confirm(NConfirmationMode confirm) {
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
    public List<String> outputFormatOptions() {
        return outputFormatOptions;
    }

    @Override
    public NSession outputFormatOptions(String... options) {
        outputFormatOptions.clear();
        return addOutputFormatOptions(options);
    }

    @Override
    public NSession outputFormatOptions(List<String> options) {
        outputFormatOptions.clear();
        return addOutputFormatOptions(options.toArray(new String[0]));
    }

    @Override
    public NPrintStream out() {
        return terminal == null ? null : terminal.out();
    }

    @Override
    public InputStream in() {
        return terminal == null ? null : terminal.in();
    }

    @Override
    public NPrintStream err() {
        return terminal == null ? null : terminal.err();
    }

    @Override
    public NIterableFormat iterableOutput() {
        if (!iterableOut) {
            return null;
        }
        return NElementWriter.of().contentType(outputFormat().orDefault()).iter(out());
    }

    @Override
    public NTerminal terminal() {
        return terminal;
    }

    @Override
    public NSession terminal(NTerminal terminal) {
        this.terminal = terminal;
        if (terminal != null) {
            AbstractNTerminal a = (AbstractNTerminal) terminal;
            NPrintStream o = a.out();
        }
//        this.out0 = (terminal.fout());
//        this.err0 = (terminal.ferr());
//        this.out = out0;
//        this.err = err0;
        return this;
    }

    @Override
    public NWorkspace workspace() {
        return workspace;
    }


    @Override
    public NOptional<Boolean> transitive() {
        return NOptional.ofNamed(transitive, "transitive")
                .withDefault(() -> NWorkspace.of().bootOptions().transitive().orElse(true));
    }

    @Override
    public boolean isTransitive() {
        return transitive().orDefault();
    }

    @Override
    public NSession transitive(Boolean value) {
        this.transitive = value;
        return this;
    }

    @Override
    public NOptional<Boolean> cached() {
        return NOptional.ofNamed(cached, "cached")
                .withDefault(() -> NWorkspace.of().bootOptions().cached().orElse(true));
    }

    @Override
    public boolean isCached() {
        return cached().orDefault();
    }

    @Override
    public NSession cached(Boolean value) {
        this.cached = value;
        return this;
    }

    @Override
    public NOptional<Boolean> indexed() {
        return NOptional.ofNamed(indexed, "indexed")
                .withDefault(() -> NWorkspace.of().bootOptions().indexed().orElse(false))
                ;
    }

    @Override
    public boolean isIndexed() {
        return indexed().orDefault();
    }

    @Override
    public NSession indexed(Boolean value) {
        this.indexed = value;
        return this;
    }

    @Override
    public NOptional<Instant> expireTime() {
        return NOptional.ofNamed(expireTime, "expireTime");
    }

    @Override
    public NSession expireTime(Instant expireTime) {
        this.expireTime = expireTime;
        return this;
    }

    @Override
    public String progressOptions() {
        return progressOptions;
    }

    @Override
    public boolean isProgress() {
        if (!isPlainOut() || isBot()) {
            return false;
        }
        //TODO, should we cache this?
        return callWith(() -> {
            NTerminalMode terminalMode = out().terminalMode();
            return ProgressOptions.of().getEnabled().orElse(
                    terminalMode != NTerminalMode.FILTERED
            );
        });
    }

    @Override
    public NSession progressOptions(String progressOptions) {
        this.progressOptions = progressOptions;
        return this;
    }

    @Override
    public NOptional<Boolean> gui() {
        return NOptional.ofNamed(gui, "gui")
                .withDefault(() -> {
                    if (isBot()) {
                        return false;
                    }
                    if (gui != null) {
                        return gui;
                    }
                    return NWorkspace.of().bootOptions().gui().orElse(false);
                });
    }

    @Override
    public boolean isGui() {
        return gui().orDefault();
    }

    @Override
    public NSession gui(Boolean gui) {
        this.gui = gui;
        return this;
    }

    @Override
    public String errLinePrefix() {
        return errLinePrefix;
    }

    @Override
    public NSession errLinePrefix(String errLinePrefix) {
        this.errLinePrefix = errLinePrefix;
        return this;
    }

    @Override
    public String outLinePrefix() {
        return outLinePrefix;
    }

    @Override
    public NSession outLinePrefix(String outLinePrefix) {
        this.outLinePrefix = outLinePrefix;
        return this;
    }

    @Override
    public NOptional<Boolean> dry() {
        return NOptional.ofNamed(dry, "dry").withDefault(() -> NWorkspace.of().bootOptions().dry().orElse(false));
    }

    @Override
    public NOptional<Boolean> showStacktrace() {
        return NOptional.ofNamed(showStacktrace, "showStacktrace")
                .withDefault(() -> NWorkspace.of().bootOptions().showStacktrace().orElse(false));
    }


    @Override
    public boolean isDry() {
        return dry().orDefault();
    }

    @Override
    public NSession dry(Boolean dry) {
        this.dry = dry;
        return this;
    }

    @Override
    public NSession showStacktrace(Boolean showStacktrace) {
        this.showStacktrace = showStacktrace;
        return this;
    }

    @Override
    public Level logTermLevel() {
        return logTermLevel;
    }

    @Override
    public NSession setLogTermLevel(Level level) {
        this.logTermLevel = level;
        return this;
    }

    @Override
    public NSession configure(NWorkspaceOptions options) {
        if (options != null) {
            if (options.cached().isPresent()) {
                this.cached(options.cached().orNull());
            }
            if (options.confirm().isPresent()) {
                this.confirm(options.confirm().orNull());
            }
            if (options.dry().isPresent()) {
                this.dry(options.dry().orNull());
            }
            if (options.outputFormat().isPresent()) {
                this.outputFormat(options.outputFormat().orNull());
            }
            if (options.outputFormatOptions().isPresent()) {
                this.outputFormatOptions(options.outputFormatOptions().orElseGet(Collections::emptyList));
            }
            if (options.errLinePrefix().isPresent()) {
                this.errLinePrefix(options.errLinePrefix().orNull());
            }
            if (options.fetchStrategy().isPresent()) {
                this.fetchStrategy(options.fetchStrategy().orNull());
            }
            if (options.expireTime().isPresent()) {
                this.expireTime(options.expireTime().orNull());
            }
            if (options.gui().isPresent()) {
                this.gui(options.gui().orNull());
            }
            if (options.progressOptions().isPresent()) {
                this.progressOptions(options.progressOptions().orNull());
            }
            if (options.indexed().isPresent()) {
                this.indexed(options.indexed().orElse(true));
            }
            if (options.trace().isPresent()) {
                this.trace(options.trace().orElse(true));
            }
            if (options.bot().isPresent()) {
                boolean wasBot = isBot();
                boolean becomesBot = options.bot().orElse(false);
                this.bot(becomesBot);
                if (/*!wasBot && */becomesBot) {
                    if (terminal().out().terminalMode() != NTerminalMode.FILTERED) {
                        terminal().out(terminal().out().terminalMode(NTerminalMode.FILTERED));
                    }
                    if (terminal().err().terminalMode() != NTerminalMode.FILTERED) {
                        terminal().err(terminal().err().terminalMode(NTerminalMode.FILTERED));
                    }
                }
            }
            if (options.transitive().isPresent()) {
                this.transitive(options.transitive().orNull());
            }
            if (options.terminalMode().isPresent() && NTerminalMode.DEFAULT != options.terminalMode().orNull()) {
                terminal().out(
                        terminal().out().terminalMode(options.terminalMode().orNull())
                );
            }
            if (options.executionType().isPresent()) {
                executionType(options.executionType().orNull());
            }
            if (options.dependencySolver().isPresent()) {
                dependencySolver(options.dependencySolver().orNull());
            }
        }
        return this;
    }

    @Override
    public Level logFileLevel() {
        return logFileLevel;
    }

    @Override
    public NSession logFileLevel(Level logFileLevel) {
        this.logFileLevel = logFileLevel;
        return this;
    }

    @Override
    public NArrayElementBuilder eout() {
        if (eout == null) {
            eout = new DefaultNArrayElementBuilder();
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
    public NOptional<NExecutionType> executionType() {
        return NOptional.ofNamed(executionType, "executionType")
                .withDefault(() -> NWorkspace.of().bootOptions().executionType().orElse(NExecutionType.SPAWN))
                ;
    }

    @Override
    public NSession embedded() {
        return executionType(NExecutionType.EMBEDDED);
    }

    @Override
    public NSession system() {
        return executionType(NExecutionType.SYSTEM);
    }

    @Override
    public NSession spawn() {
        return executionType(NExecutionType.SPAWN);
    }

    @Override
    public NSession executionType(NExecutionType executionType) {
        this.executionType = executionType;
        return this;
    }

    @Override
    public NOptional<String> debug() {
        return NOptional.ofNamed(debug, "debug")
                .withDefault(() -> NWorkspace.of().bootOptions().debug().orNull()
                );
    }

    @Override
    public NSession debug(String debug) {
        this.debug = debug;
        return this;
    }

    @Override
    public NOptional<String> locale() {
        return NOptional.ofNamed(locale, "locale")
                .withDefault(() -> NWorkspace.of().bootOptions().locale().orNull());
    }

    @Override
    public NSession locale(String locale) {
        this.locale = locale;
        return this;
    }

    public NOptional<NRunAs> runAs() {
        return NOptional.ofNamed(runAs, "runAs")
                .withDefault(() -> {
                    NRunAs r = NWorkspace.of().bootOptions().runAs().orNull();
                    if (r != null) {
                        return r;
                    }
                    return NRunAs.currentUser();
                });
    }

    public NSession runAs(NRunAs runAs) {
        this.runAs = runAs;
        return this;
    }

    @Override
    public NSession sudo() {
        return runAs(NRunAs.SUDO);
    }

    @Override
    public NSession root() {
        return runAs(NRunAs.ROOT);
    }

    @Override
    public NSession currentUser() {
        return runAs(NRunAs.CURRENT_USER);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("NSession(");
        NWorkspace ws = workspace();
        sb.append(ws == null ? "null" : ws.location());
        if (properties.size() > 0) {
            sb.append(", properties=").append(properties);
        }
        sb.append(")");
        return sb.toString();
    }

    @Override
    public String dependencySolver() {
        return dependencySolver;
    }

    @Override
    public NSession dependencySolver(String dependencySolver) {
        this.dependencySolver = dependencySolver;
        return this;
    }

    private void parseLogLevel(NCmdLine cmdLine, boolean enabled) {
        NArg a = cmdLine.peek().get();
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
                    String id = a.getKey().asString().get();
                    this.logFileLevel(
                            NLogUtils.parseLogLevel(id.substring("--log-file-".length())).onEmpty(null).get());
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
                    String id = a.getKey().asString().get();
                    this.setLogTermLevel(NLogUtils.parseLogLevel(id.substring("--log-term-".length())).onEmpty(null).get());
                }
                break;
            }

            case "-l":
            case "--verbose":
            {
                cmdLine.skip();
                if (enabled && a.literalValue().asBoolean().orElse(true)) {
                    this.setLogTermLevel(Level.FINEST);
                    this.logFileLevel(Level.FINEST);
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
                    String id = a.getKey().asString().get();
                    Level lvl = NLogUtils.parseLogLevel(id.substring("--log-".length())).onEmpty(null).get();
                    this.setLogTermLevel(lvl);
                    this.logFileLevel(lvl);
                }
                break;
            }
        }
    }


    //    public <T> T getOrComputeRefProperty(String name, Function<NSession, T> supplier) {
//        Object v = getRefProperty(name);
//        if (v != null) {
//            return (T) v;
//        }
//        v = supplier.apply(this);
//        setRefProperty(name, v);
//        return (T) v;
//    }


    @Override
    public boolean isLogTermLevel(Level level) {
        return level != null && this.logTermLevel != null && this.logTermLevel.intValue() <= level.intValue();
    }

    @Override
    public boolean isVerboseTerm() {
        return isLogTermLevel(Level.FINEST);
    }

    @Override
    public boolean isLogFileLevel(Level level) {
        return level != null && this.logFileLevel != null && this.logFileLevel.intValue() <= level.intValue();
    }

    @Override
    public boolean isVerboseFile() {
        return isLogFileLevel(Level.FINEST);
    }

    @Override
    public NSession setProperty(String property, Object value) {
        return setSessionProperty(property, value);
    }

    @Override
    public <T> NSession setProperty(Class<T> property, T value) {
        return setSessionProperty(property == null ? null : property.getName(), value);
    }

    @Override
    public NSession setSessionProperty(String property, Object value) {
        Object old = getPropertiesHolder().setProperty(property, value, NScopeType.SESSION);
        return this;
    }

    @Override
    public NSession setTransitiveProperty(String property, Object value) {
        Object old = getPropertiesHolder().setProperty(property, CoreNUtils.checkCopiableValue(value), NScopeType.TRANSITIVE_SESSION);
        return this;
    }

    @Override
    public NSession setSharedProperty(String property, Object value) {
        Object old = getPropertiesHolder().setProperty(property, value, NScopeType.SHARED_SESSION);
        return this;
    }

    @Override
    public NOptional<Object> getProperty(String property) {
        return getPropertiesHolder().getOptional(property)
                .withDefault(() -> workspace().getProperty(property).orDefault())
                ;
    }

    @Override
    public Map<String, Object> properties() {
        return getPropertiesHolder().toMap();
    }

    @Override
    public <T> NOptional<T> getProperty(Class<T> propertyTypeAndName) {
        return getProperty(propertyTypeAndName == null ? null : propertyTypeAndName.getName()).instanceOf(propertyTypeAndName);
    }

    @Override
    public <T> NSession setTransitiveProperty(Class<T> property, T value) throws NNonCopiableException {
        return setTransitiveProperty(property==null?null:property.getName(), value);
    }

    @Override
    public <T> NSession setSharedProperty(Class<T> property, T value) {
        return setSharedProperty(property==null?null:property.getName(), value);
    }

    @Override
    public <T> T getOrComputeProperty(String property, Supplier<T> supplier) {
        return getOrComputeSessionProperty(property, supplier);
    }

    @Override
    public <T> T getOrComputeSessionProperty(String property, Supplier<T> supplier) {
        return getPropertiesHolder().getOrComputeProperty(property, supplier, NScopeType.SESSION);
    }

    @Override
    public <T> T getOrComputeSharedProperty(String property, Supplier<T> supplier) {
        return getPropertiesHolder().getOrComputeProperty(property, supplier, NScopeType.SHARED_SESSION);
    }

    @Override
    public <T> T getOrComputeTransitiveProperty(String property, Supplier<T> supplier) {
        return getPropertiesHolder().getOrComputeProperty(property,
                supplier == null ? null : () -> CoreNUtils.checkCopiableValue(supplier.get())
                , NScopeType.TRANSITIVE_SESSION);
    }

    @Override
    public <T> T getOrComputeProperty(Class<T> property, Supplier<T> supplier) {
        return getOrComputeSessionProperty(property, supplier);
    }

    @Override
    public <T> T getOrComputeSessionProperty(Class<T> property, Supplier<T> supplier) {
        return getOrComputeSessionProperty(property == null ? null : property.getName(), supplier);
    }

    @Override
    public <T> T getOrComputeSharedProperty(Class<T> property, Supplier<T> supplier) {
        return getOrComputeSharedProperty(property == null ? null : property.getName(), supplier);
    }

    @Override
    public <T> T getOrComputeTransitiveProperty(Class<T> property, Supplier<T> supplier) {
        return getOrComputeTransitiveProperty(property == null ? null : property.getName(), supplier);
    }
}
