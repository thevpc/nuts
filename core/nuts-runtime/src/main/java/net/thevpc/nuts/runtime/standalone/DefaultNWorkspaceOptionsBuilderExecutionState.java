package net.thevpc.nuts.runtime.standalone;

import net.thevpc.nuts.*;
import net.thevpc.nuts.artifact.*;
import net.thevpc.nuts.app.*;
import net.thevpc.nuts.boot.*;
import net.thevpc.nuts.cmdline.*;
import net.thevpc.nuts.command.*;
import net.thevpc.nuts.core.*;
import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.internal.*;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.log.*;
import net.thevpc.nuts.platform.*;
import net.thevpc.nuts.reflect.*;
import net.thevpc.nuts.security.*;
import net.thevpc.nuts.spi.*;
import net.thevpc.nuts.text.*;
import net.thevpc.nuts.util.*;
import java.io.*;
import java.net.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;
import java.util.stream.Collectors;

final class DefaultNWorkspaceOptionsBuilderExecutionState {
    private final DefaultNWorkspaceOptionsBuilder owner;

    DefaultNWorkspaceOptionsBuilderExecutionState(DefaultNWorkspaceOptionsBuilder owner) {
        this.owner = owner;
    }

    NTerminalMode terminalMode;
    Boolean readOnly;
    Boolean trace;
    NLogConfig logConfig;
    NConfirmationMode confirm;
    NContentType outputFormat;
    List<String> applicationArguments;
    NOpenMode openMode;
    Boolean dry;
    Boolean force;
    Boolean showStacktrace;
    Supplier<ClassLoader> classLoaderSupplier;
    List<String> executorOptions;
    Boolean recover;
    Boolean reset;
    Boolean resetHard;
    Boolean commandVersion;
    Boolean commandHelp;
    NExecutionType executionType;
    NRunAs runAs;
    NFetchStrategy fetchStrategy;
    Boolean cached;
    Boolean indexed;
    Boolean transitive;
    Boolean bot;
    Boolean previewRepo;
    InputStream stdin;
    PrintStream stdout;
    PrintStream stderr;
    ExecutorService executorService;
    Instant expireTime;
    List<NMsg> errors;
    Boolean skipErrors;
    Boolean initLaunchers;
    Boolean initScripts;
    Boolean initPlatforms;
    Boolean initJava;
    Boolean localMaven;
    NIsolationLevel isolationLevel;
    NSupportMode desktopLauncher;
    NSupportMode menuLauncher;
    NSupportMode userLauncher;

    public NOptional<NSupportMode> desktopLauncher() {
        return NOptional.ofNamed(desktopLauncher, "desktopLauncher");
    }

    public NOptional<NSupportMode> menuLauncher() {
        return NOptional.ofNamed(menuLauncher, "menuLauncher");
    }

    public NOptional<NSupportMode> userLauncher() {
        return NOptional.ofNamed(userLauncher, "userLauncher");
    }

    public NWorkspaceOptionsBuilder initLaunchers(Boolean initLaunchers) {
        this.initLaunchers = initLaunchers;
        return owner;
    }

    public NWorkspaceOptionsBuilder initScripts(Boolean initScripts) {
        this.initScripts = initScripts;
        return owner;
    }

    public NWorkspaceOptionsBuilder initPlatforms(Boolean initPlatforms) {
        this.initPlatforms = initPlatforms;
        return owner;
    }

    public NWorkspaceOptionsBuilder localMaven(Boolean localMaven) {
        this.localMaven = localMaven;
        return owner;
    }

    public NWorkspaceOptionsBuilder initJava(Boolean initJava) {
        this.initJava = initJava;
        return owner;
    }

    public NWorkspaceOptionsBuilder isolationLevel(NIsolationLevel isolationLevel) {
        this.isolationLevel = isolationLevel;
        return owner;
    }

    public NWorkspaceOptionsBuilder desktopLauncher(NSupportMode desktopLauncher) {
        this.desktopLauncher = desktopLauncher;
        return owner;
    }

    public NWorkspaceOptionsBuilder menuLauncher(NSupportMode menuLauncher) {
        this.menuLauncher = menuLauncher;
        return owner;
    }

    public NWorkspaceOptionsBuilder userLauncher(NSupportMode userLauncher) {
        this.userLauncher = userLauncher;
        return owner;
    }

    public NOptional<List<String>> applicationArguments() {
        return NOptional.ofNamed(applicationArguments, "applicationArguments");
    }

    public NWorkspaceOptionsBuilder applicationArguments(List<String> applicationArguments) {
        this.applicationArguments = applicationArguments;
        return owner;
    }

    public NOptional<Supplier<ClassLoader>> classLoaderSupplier() {
        return NOptional.ofNamed(classLoaderSupplier, "classLoaderSupplier");
    }

    public NWorkspaceOptionsBuilder classLoaderSupplier(Supplier<ClassLoader> provider) {
        this.classLoaderSupplier = provider;
        return owner;
    }

    public NOptional<NConfirmationMode> confirm() {
        return NOptional.ofNamed(confirm, "confirm");
    }

    public NWorkspaceOptionsBuilder confirm(NConfirmationMode confirm) {
        this.confirm = confirm;
        return owner;
    }

    public NOptional<Boolean> dry() {
        return NOptional.ofNamed(dry, "dry");
    }

    public NOptional<Boolean> force() {
        return NOptional.ofNamed(force, "force");
    }

    public NOptional<Boolean> showStacktrace() {
        return NOptional.ofNamed(showStacktrace, "showStacktrace");
    }

    public NWorkspaceOptionsBuilder dry(Boolean dry) {
        this.dry = dry;
        return owner;
    }

    public NWorkspaceOptionsBuilder force(Boolean force) {
        this.force = force;
        return owner;
    }

    public NWorkspaceOptionsBuilder showStacktrace(Boolean showStacktrace) {
        this.showStacktrace = showStacktrace;
        return owner;
    }

    public NOptional<NExecutionType> executionType() {
        return NOptional.ofNamed(executionType, "executionType");
    }

    public NWorkspaceOptionsBuilder executionType(NExecutionType executionType) {
        this.executionType = executionType;
        return owner;
    }

    public NOptional<NRunAs> runAs() {
        return NOptional.ofNamed(runAs, "runAs");
    }

    /**
     * set runAsUser
     *
     * @param runAs new value
     * @return {@code this} instance
     */
    public NWorkspaceOptionsBuilder runAs(NRunAs runAs) {
        this.runAs = runAs;
        return owner;
    }

    public NOptional<List<String>> executorOptions() {
        return NOptional.ofNamed(executorOptions, "executorOptions");
    }

    public NWorkspaceOptionsBuilder executorOptions(List<String> executorOptions) {
        this.executorOptions = executorOptions;
        return owner;
    }

    public NOptional<NLogConfig> logConfig() {
        return NOptional.ofNamed(logConfig, "logConfig");
    }

    public NWorkspaceOptionsBuilder logConfig(NLogConfig logConfig) {
        this.logConfig = logConfig == null ? null : logConfig.copy();
        return owner;
    }

    public NOptional<NOpenMode> openMode() {
        return NOptional.ofNamed(openMode, "openMode");
    }

    public NWorkspaceOptionsBuilder openMode(NOpenMode openMode) {
        this.openMode = openMode;
        return owner;
    }

    public NOptional<NContentType> outputFormat() {
        return NOptional.ofNamed(outputFormat, "outputFormat");
    }

    public NWorkspaceOptionsBuilder outputFormat(NContentType outputFormat) {
        this.outputFormat = outputFormat;
        return owner;
    }

    public NOptional<NTerminalMode> terminalMode() {
        return NOptional.ofNamed(terminalMode, "terminalMode");
    }

    public NWorkspaceOptionsBuilder terminalMode(NTerminalMode terminalMode) {
        this.terminalMode = terminalMode;
        return owner;
    }

    public NOptional<Boolean> readOnly() {
        return NOptional.ofNamed(readOnly, "readOnly");
    }

    public NWorkspaceOptionsBuilder readOnly(Boolean readOnly) {
        this.readOnly = readOnly;
        return owner;
    }

    public NOptional<Boolean> recover() {
        return NOptional.ofNamed(recover, "recover");
    }

    public NWorkspaceOptionsBuilder recover(Boolean recover) {
        this.recover = recover;
        return owner;
    }

    public NOptional<Boolean> reset() {
        return NOptional.ofNamed(reset, "reset");
    }

    public NWorkspaceOptionsBuilder reset(Boolean reset) {
        this.reset = reset;
        return owner;
    }

    public NOptional<Boolean> resetHard() {
        return NOptional.ofNamed(resetHard, "resetHard");
    }

    public NWorkspaceOptionsBuilder resetHard(Boolean resetHard) {
        this.resetHard = resetHard;
        return owner;
    }

    public NOptional<Boolean> commandVersion() {
        return NOptional.ofNamed(commandVersion, "commandVersion");
    }

    public NWorkspaceOptionsBuilder commandVersion(Boolean version) {
        this.commandVersion = version;
        return owner;
    }

    public NOptional<Boolean> commandHelp() {
        return NOptional.ofNamed(commandHelp, "commandHelp");
    }

    public NWorkspaceOptionsBuilder commandHelp(Boolean help) {
        this.commandHelp = help;
        return owner;
    }

    public NOptional<Boolean> trace() {
        return NOptional.ofNamed(trace, "trace");
    }

    public NWorkspaceOptionsBuilder trace(Boolean trace) {
        this.trace = trace;
        return owner;
    }

    public NOptional<Boolean> cached() {
        return NOptional.ofNamed(cached, "cached");
    }

    public NWorkspaceOptionsBuilder cached(Boolean cached) {
        this.cached = cached;
        return owner;
    }

    public NOptional<Boolean> indexed() {
        return NOptional.ofNamed(indexed, "indexed");
    }

    public NWorkspaceOptionsBuilder indexed(Boolean indexed) {
        this.indexed = indexed;
        return owner;
    }

    public NOptional<Boolean> transitive() {
        return NOptional.ofNamed(transitive, "transitive");
    }

    public NWorkspaceOptionsBuilder transitive(Boolean transitive) {
        this.transitive = transitive;
        return owner;
    }

    public NOptional<Boolean> bot() {
        return NOptional.ofNamed(bot, "bot");
    }

    public NWorkspaceOptionsBuilder bot(Boolean bot) {
        this.bot = bot;
        return owner;
    }

    public NOptional<Boolean> previewRepo() {
        return NOptional.ofNamed(previewRepo, "previewRepo");
    }

    public NWorkspaceOptionsBuilder previewRepo(Boolean bot) {
        this.previewRepo = bot;
        return owner;
    }

    public NOptional<NFetchStrategy> fetchStrategy() {
        return NOptional.ofNamed(fetchStrategy, "fetchStrategy");
    }

    public NWorkspaceOptionsBuilder fetchStrategy(NFetchStrategy fetchStrategy) {
        this.fetchStrategy = fetchStrategy;
        return owner;
    }

    public NOptional<InputStream> stdin() {
        return NOptional.ofNamed(stdin, "stdin");
    }

    public NWorkspaceOptionsBuilder stdin(InputStream stdin) {
        this.stdin = stdin;
        return owner;
    }

    public NOptional<PrintStream> stdout() {
        return NOptional.ofNamed(stdout, "stdout");
    }

    public NWorkspaceOptionsBuilder stdout(PrintStream stdout) {
        this.stdout = stdout;
        return owner;
    }

    public NOptional<PrintStream> stderr() {
        return NOptional.ofNamed(stderr, "stderr");
    }

    public NWorkspaceOptionsBuilder stderr(PrintStream stderr) {
        this.stderr = stderr;
        return owner;
    }

    public NOptional<ExecutorService> executorService() {
        return NOptional.ofNamed(executorService, "executorService");
    }

    public NWorkspaceOptionsBuilder executorService(ExecutorService executorService) {
        this.executorService = executorService;
        return owner;
    }

    public NOptional<Instant> expireTime() {
        return NOptional.ofNamed(expireTime, "expireTime");
    }

    public NWorkspaceOptionsBuilder expireTime(Instant expireTime) {
        this.expireTime = expireTime;
        return owner;
    }

    public NOptional<Boolean> skipErrors() {
        return NOptional.ofNamed(skipErrors, "skipErrors");
    }

    public NWorkspaceOptionsBuilder skipErrors(Boolean value) {
        this.skipErrors = value;
        return owner;
    }

    public NOptional<List<NMsg>> errors() {
        return NOptional.ofNamed(errors, "errors");
    }

    public NWorkspaceOptionsBuilder errors(List<NMsg> errors) {
        this.errors = errors;
        return owner;
    }

    public NOptional<NIsolationLevel> isolationLevel() {
        return NOptional.ofNamed(isolationLevel, "isolationLevel");
    }

    public NOptional<Boolean> initLaunchers() {
        return NOptional.ofNamed(initLaunchers, "initLaunchers");
    }

    public NOptional<Boolean> initScripts() {
        return NOptional.ofNamed(initScripts, "initScripts");
    }

    public NOptional<Boolean> initPlatforms() {
        return NOptional.ofNamed(initPlatforms, "initPlatforms");
    }

    public NOptional<Boolean> localMaven() {
        return NOptional.ofNamed(localMaven, "localMaven");
    }

    public NOptional<Boolean> initJava() {
        return NOptional.ofNamed(initJava, "initJava");
    }

    public NWorkspaceOptionsBuilder unsetRuntimeOptions() {
        commandHelp(null);
        commandVersion(null);
        openMode(null);
        executionType(null);
        runAs(null);
        reset(null);
        recover(null);
        dry(null);
        showStacktrace(null);
        executorOptions(null);
        applicationArguments(null);
        return owner;
    }
}
