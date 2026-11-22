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

final class DefaultNBootOptionsBuilderIdentityState {
    private final DefaultNBootOptionsBuilder owner;

    DefaultNBootOptionsBuilderIdentityState(DefaultNBootOptionsBuilder owner) {
        this.owner = owner;
    }

    List<String> outputFormatOptions;
    List<String> customOptions;
    NVersion apiVersion;
    NId runtimeId;
    String javaCommand;
    String javaOptions;
    String workspace;
    String outLinePrefix;
    String errLinePrefix;
    String name;
    String userName;
    Boolean sharedInstance;
    char[] credentials;
    String progressOptions;
    String dependencySolver;
    Instant creationTime;
    String debug;
    Boolean inherited;
    String locale;
    String theme;
    String uuid;

    public NOptional<String> uuid() {
        return NOptional.of(uuid);
    }

    public DefaultNBootOptionsBuilder uuid(String uuid) {
        this.uuid = NStringUtils.stripToNull(uuid);
        return owner;
    }

    public NOptional<NVersion> apiVersion() {
        return NOptional.ofNamed(apiVersion, "apiVersion");
    }

    public NBootOptionsBuilder apiVersion(NVersion apiVersion) {
        this.apiVersion = apiVersion;
        return owner;
    }

    public NOptional<Instant> creationTime() {
        return NOptional.ofNamed(creationTime, "creationTime");
    }

    public NBootOptionsBuilder creationTime(Instant creationTime) {
        this.creationTime = creationTime;
        return owner;
    }

    public NOptional<String> javaCommand() {
        return NOptional.ofNamed(javaCommand, "javaCommand");
    }

    public NBootOptionsBuilder javaCommand(String javaCommand) {
        this.javaCommand = javaCommand;
        return owner;
    }

    public NOptional<String> javaOptions() {
        return NOptional.ofNamed(javaOptions, "javaOptions");
    }

    public NBootOptionsBuilder javaOptions(String javaOptions) {
        this.javaOptions = javaOptions;
        return owner;
    }

    public NOptional<String> name() {
        return NOptional.ofNamed(name, "name");
    }

    public NBootOptionsBuilder name(String workspaceName) {
        this.name = workspaceName;
        return owner;
    }

    public NOptional<List<String>> outputFormatOptions() {
        return NOptional.ofNamed(outputFormatOptions, "outputFormatOptions");
    }

    public NBootOptionsBuilder outputFormatOptions(List<String> options) {
        if (options != null) {
            if (outputFormatOptions == null) {
                outputFormatOptions = new ArrayList<>();
            }
            this.outputFormatOptions.clear();
            return addOutputFormatOptions(NReservedLangUtils.nonNullList(options).toArray(new String[0]));
        } else {
            this.outputFormatOptions = null;
        }
        return owner;
    }

    public NOptional<char[]> credential() {
        return NOptional.ofNamed(credentials, "credentials");
    }

    public NBootOptionsBuilder credential(char[] credentials) {
        this.credentials = credentials;
        return owner;
    }

    public NOptional<NId> runtimeId() {
        return NOptional.ofNamed(runtimeId, "runtimeId");
    }

    public NBootOptionsBuilder runtimeId(NId runtimeId) {
        this.runtimeId = runtimeId;
        return owner;
    }

    public NOptional<String> userName() {
        return NOptional.ofNamed(userName, "userName");
    }

    public NOptional<String> workspace() {
        return NOptional.ofNamed(workspace, "workspace");
    }

    public NBootOptionsBuilder workspace(String workspace) {
        this.workspace = workspace;
        return owner;
    }

    public NOptional<String> debug() {
        return NOptional.ofNamed(debug, "debug");
    }

    public NBootOptionsBuilder debug(String debug) {
        this.debug = debug;
        return owner;
    }

    public NOptional<Boolean> inherited() {
        return NOptional.ofNamed(inherited, "inherited");
    }

    public NBootOptionsBuilder inherited(Boolean inherited) {
        this.inherited = inherited;
        return owner;
    }

    public NOptional<String> outLinePrefix() {
        return NOptional.ofNamed(outLinePrefix, "outLinePrefix");
    }

    public NBootOptionsBuilder outLinePrefix(String outLinePrefix) {
        this.outLinePrefix = outLinePrefix;
        return owner;
    }

    public NOptional<String> errLinePrefix() {
        return NOptional.ofNamed(errLinePrefix, "errLinePrefix");
    }

    public NBootOptionsBuilder errLinePrefix(String errLinePrefix) {
        this.errLinePrefix = errLinePrefix;
        return owner;
    }

    public NOptional<String> progressOptions() {
        return NOptional.ofNamed(progressOptions, "progressOptions");
    }

    public NBootOptionsBuilder progressOptions(String progressOptions) {
        this.progressOptions = progressOptions;
        return owner;
    }

    public NBootOptionsBuilder customOptions(List<String> properties) {
        this.customOptions = properties;
        return owner;
    }

    public NOptional<String> locale() {
        return NOptional.ofNamed(locale, "locale");
    }

    public NBootOptionsBuilder locale(String locale) {
        this.locale = locale;
        return owner;
    }

    public NOptional<String> theme() {
        return NOptional.ofNamed(theme, "theme");
    }

    public NBootOptionsBuilder theme(String theme) {
        this.theme = theme;
        return owner;
    }

    public NOptional<Boolean> sharedInstance() {
        return NOptional.ofNamed(sharedInstance, "sharedInstance");
    }

    public NBootOptionsBuilder sharedInstance(Boolean sharedInstance) {
        this.sharedInstance = sharedInstance;
        return owner;
    }

    public NBootOptionsBuilder userName(String username) {
        this.userName = username;
        return owner;
    }

    public NBootOptionsBuilder addOutputFormatOptions(String... options) {
        if (options != null) {
            for (String option : options) {
                if (option != null) {
                    option = NStringUtils.strip(option);
                    if (!option.isEmpty()) {
                        if (outputFormatOptions == null) {
                            outputFormatOptions = new ArrayList<>();
                        }
                        outputFormatOptions.add(option);
                    }
                }
            }
        }
        return owner;
    }

    public NOptional<String> dependencySolver() {
        return NOptional.ofNamed(dependencySolver, "dependencySolver");
    }

    public NBootOptionsBuilder dependencySolver(String dependencySolver) {
        this.dependencySolver = dependencySolver;
        return owner;
    }

    public NOptional<List<String>> customOptions() {
        return NOptional.ofNamed(customOptions, "customOptions");
    }

    public NOptional<List<NArg>> customOptionArgs() {
        return NOptional.ofNamed(customOptions == null ? null : customOptions.stream().map(x -> NArg.of(x)).collect(Collectors.toList()), "customOptions");
    }

    public NOptional<NArg> customOptionArg(String key) {
        return NOptional.ofNamedOptional(customOptions().orElse(new ArrayList<>()).stream().map(x -> NArg.of(x))
                .filter(x -> Objects.equals(x.getStringKey().orNull(), key))
                .findFirst(), key);
    }

    public NOptional<String> customOption(String key) {
        return NOptional.ofNamedOptional(customOptions().orElse(new ArrayList<>()).stream().map(x -> NArg.of(x))
                .filter(x -> Objects.equals(x.getStringKey().orNull(), key))
                .map(x->x.image())
                .findFirst(), key);
    }
}
