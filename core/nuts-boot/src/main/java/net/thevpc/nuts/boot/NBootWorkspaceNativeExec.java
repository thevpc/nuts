package net.thevpc.nuts.boot;

import net.thevpc.nuts.boot.core.NWorkspaceBase;
import net.thevpc.nuts.boot.internal.cmdline.NBootArg;
import net.thevpc.nuts.boot.internal.cmdline.NBootCmdLine;
import net.thevpc.nuts.boot.internal.cmdline.NBootWorkspaceCmdLineParser;
import net.thevpc.nuts.boot.internal.util.*;

import java.io.InputStream;
import java.time.Instant;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class NBootWorkspaceNativeExec implements NBootWorkspace {
    public final static String COMMAND_PREFIX = "native-exec";
    private final Instant creationTime = Instant.now();
    private Scanner scanner;
    private final NBootLog bLog;
    private final NBootOptionsInfo options;

    private long minTime = 5000;
    private long waitTime = 3000;
    private long maxCount = -1;
    private NBootArguments unparsedOptions;
    private NBootCompleteRequest complete;
    private NBootCompleteCmdlineRequest cmdComplete;

    public NBootWorkspaceNativeExec(NBootArguments unparsedOptions) {
        if (unparsedOptions == null) {
            unparsedOptions = new NBootArguments();
        }
        this.unparsedOptions = unparsedOptions;
        NBootOptionsInfo userOptions = new NBootOptionsInfo();
        userOptions.stdin(unparsedOptions.in());
        userOptions.stdout(unparsedOptions.out());
        userOptions.stderr(unparsedOptions.err());
        userOptions.creationTime(unparsedOptions.startTime());
        InputStream in = userOptions.stdin();
        scanner = new Scanner(in == null ? System.in : in);
        if (unparsedOptions.complete() != null) {
            this.complete = unparsedOptions.complete();
        }
        this.bLog = new NBootLog(userOptions);
        List<String> allArgs = new ArrayList<>();
        if (unparsedOptions.optionArgs() != null) {
            allArgs.addAll(Arrays.asList(unparsedOptions.optionArgs()));
        }
        if (unparsedOptions.appArgs() != null) {
            allArgs.addAll(Arrays.asList(unparsedOptions.appArgs()));
        }
        parseArguments(allArgs.toArray(new String[0]), userOptions);
        if (NBootUtils.firstNonNull(userOptions.skipErrors(), false)) {
            StringBuilder errorMessage = new StringBuilder();
            if (userOptions.errors() != null) {
                for (String s : userOptions.errors()) {
                    errorMessage.append(s).append("\n");
                }
            }
            errorMessage.append(NBootI18n.of("Try 'nuts --help' for more information."));
            bLog.warn(NBootMsg.ofC(NBootI18n.of("Skipped Error : %s"), errorMessage));
        }
        this.options = userOptions.copy();
        this.postInit();
    }

    public NBootArguments bootArguments() {
        return unparsedOptions;
    }

    public NBootOptionsInfo options() {
        return options;
    }


    private void parseArguments(String[] bootArguments, NBootOptionsInfo userOptions) {
        NBootCmdLine cmdLine = new NBootCmdLine(bootArguments)
                .setCommandName("nuts")
                .setExpandSimpleOptions(true)
                .registerSpecialSimpleOption("-version");
        while (cmdLine.hasNext()) {
            if (nextArgument(cmdLine, userOptions) == null) {
                //some error occurred!
                cmdLine.skip();
            }
        }
    }

    private List<NBootArg> nextArgument(NBootCmdLine cmdLine, NBootOptionsInfo options) {
        while (cmdLine.hasNext()) {
            NBootArg a = cmdLine.peek();
            if (a.isOption()) {
                boolean active = a.isActive();
                String k = a.key();
                switch (k) {
                    case "--min-time": {
                        a = cmdLine.nextEntry();
                        if (active) {
                            String sValue = NBootUtils.firstNonNull(a.stringValue(), "");
                            try {
                                this.minTime = NBootUtils.parseTimePeriod(sValue, k);
                            } catch (Exception ex) {
                                NBootWorkspaceHelper.addError(NBootMsg.ofC("%s", ex.getMessage()), options);
                            }
                            break;
                        }
                        return Collections.singletonList(a);
                    }
                    case "--wait-time": {
                        a = cmdLine.nextEntry();
                        if (active) {
                            String sValue = NBootUtils.firstNonNull(a.stringValue(), "");
                            try {
                                this.waitTime = NBootUtils.parseTimePeriod(sValue, k);
                            } catch (Exception ex) {
                                NBootWorkspaceHelper.addError(NBootMsg.ofC("%s", ex.getMessage()), options);
                            }
                            break;
                        }
                        return Collections.singletonList(a);
                    }
                    case "--max-count": {
                        a = cmdLine.nextEntry();
                        if (active) {
                            String sValue = NBootUtils.firstNonNull(a.stringValue(), "");
                            try {
                                this.maxCount = Long.parseLong(sValue);
                            } catch (Exception ex) {
                                NBootWorkspaceHelper.addError(NBootMsg.ofC("%s", ex.getMessage()), options);
                            }
                            break;
                        }
                        return Collections.singletonList(a);
                    }

                    case "--java":
                    case "--boot-java":
                    case "-j": {
                        a = cmdLine.nextEntry();
                        if (active && options != null) {
                            String v = NBootUtils.firstNonNull(a.stringValue(), "");
                            options.javaCommand(v);
                        }
                        return (Collections.singletonList(a));
                    }
                    case "--java-home":
                    case "--boot-java-home": {
                        a = cmdLine.nextEntry();
                        if (active && options != null) {
                            String v = a.stringValue();
                            options.javaCommand(NBootUtils.resolveJavaCommand(v));
                        }
                        return (Collections.singletonList(a));
                    }
                    case "--java-options":
                    case "--boot-java-options":
                    case "-J": {
                        a = cmdLine.nextEntry();
                        String v = NBootUtils.firstNonNull(a.stringValue(), "");
                        if (active && options != null) {
                            options.javaOptions(v);
                        }
                        return (Collections.singletonList(a));
                    }

                    case "--gui": {
                        a = cmdLine.nextFlag();
                        if (active && options != null) {
                            options.gui(a.booleanValue());
                        }
                        return (Collections.singletonList(a));
                    }

                    case "-B":
                    case "--bot": {
                        a = cmdLine.nextFlag();
                        if (active) {
                            if (options != null) {
                                options.bot(a.booleanValue());
                            }
                            return (Collections.singletonList(a));
                        } else {
                            return (Collections.singletonList(a));
                        }
                    }
                    case "-t":
                    case "--trace": {
                        a = cmdLine.nextFlag();
                        if (active) {
                            if (options != null) {
                                options.trace(a.booleanValue());
                            }
                            return (Collections.singletonList(a));
                        } else {
                            return (Collections.singletonList(a));
                        }
                    }
                    case "-P":
                    case "--progress": {
                        a = cmdLine.next();
                        if (active) {
                            if (options != null) {
                                String s = a.stringValue();
                                if (a.isNegated()) {
                                    if (NBootUtils.isBlank(s)) {
                                        s = "false";
                                    } else {
                                        s = "false," + s;
                                    }
                                    options.progressOptions(s);
                                } else {
                                    options.progressOptions(s);
                                }
                            }
                            return (Collections.singletonList(a));
                        } else {
                            return (Collections.singletonList(a));
                        }
                    }
                    case "--dry":
                    case "-D": {
                        a = cmdLine.nextFlag();
                        if (active && options != null) {
                            options.dry(a.booleanValue());
                        }
                        return (Collections.singletonList(a));
                    }
                    case "-d":
                    case "--stacktrace":
                    {
                        a = cmdLine.nextFlag();
                        if (active && options != null) {
                            options.showStacktrace(a.booleanValue());
                        }
                        return (Collections.singletonList(a));
                    }
                    case "--debug": {
                        a = cmdLine.next();
                        if (active) {
                            if (options != null) {
                                if (NBootUtils.isBlank(a.stringValue())) {
                                    options.debug(String.valueOf(a.isEnabled()));
                                } else {
                                    if (a.isNegated()) {
                                        options.debug(String.valueOf(!NBootUtils.parseBoolean(a.stringValue(), true, false)));
                                    } else {
                                        options.debug(a.stringValue());
                                    }
                                }
                            }
                            return (Collections.singletonList(a));
                        } else {
                            return (Collections.singletonList(a));
                        }
                    }

                    case "--verbose":
                    case "-l":

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
                            NBootLogConfig logConfig = options.logConfig();
                            if (logConfig == null) {
                                logConfig = new NBootLogConfig();
                            }
                            NBootArg r = NBootWorkspaceCmdLineParser.parseLogLevel(logConfig, cmdLine, active);
                            options.logConfig(logConfig);
                            return r == null
                                    ? null
                                    : Collections.singletonList(r);
                        } else {
                            return (Collections.singletonList(a));
                        }
                    }

                    case "--output-format-option":
                    case "-T": {
                        if (active) {
                            if (options != null) {
                                options.addOutputFormatOptions(cmdLine.nextEntry().stringValue());
                            }
                            return (Collections.singletonList(a));
                        } else {
                            cmdLine.skip();
                            return (Collections.singletonList(a));
                        }
                    }
                    case "-O":
                    case "--output-format":
                        a = cmdLine.nextEntry();
                        if (active) {
                            if (options != null) {
                                String t = NBootUtils.firstNonNull(a.stringValue(), "");
                                int i = NBootUtils.firstIndexOf(t, new char[]{' ', ';', ':', '='});
                                if (i > 0) {
                                    options.outputFormat((t.substring(0, i).toUpperCase()));
                                    options.addOutputFormatOptions(t.substring(i + 1).toUpperCase());
                                } else {
                                    options.outputFormat((t.toUpperCase()));
                                    options.addOutputFormatOptions("");
                                }
                            }
                            return (Collections.singletonList(a));
                        } else {
                            return (Collections.singletonList(a));
                        }
                    case "--tson":
                        a = cmdLine.next();
                        if (active) {
                            if (options != null) {
                                options.outputFormat("TSON");
                                options.addOutputFormatOptions(NBootUtils.firstNonNull(a.stringValue(), ""));
                            }
                            return (Collections.singletonList(a));
                        } else {
                            return (Collections.singletonList(a));
                        }
                    case "--yaml":
                        a = cmdLine.next();
                        if (active) {
                            if (options != null) {
                                options.outputFormat("YAML");
                                options.addOutputFormatOptions(NBootUtils.firstNonNull(a.stringValue(), ""));
                            }
                            return (Collections.singletonList(a));
                        } else {
                            return (Collections.singletonList(a));
                        }
                    case "--json":
                        a = cmdLine.next();
                        if (active) {
                            if (options != null) {
                                options.outputFormat("JSON");
                                options.addOutputFormatOptions(NBootUtils.firstNonNull(a.stringValue(), ""));
                            }
                            return (Collections.singletonList(a));
                        } else {
                            return (Collections.singletonList(a));
                        }
                    case "--plain":
                        a = cmdLine.next();
                        if (active) {
                            if (options != null) {
                                options.outputFormat("PLAIN");
                                options.addOutputFormatOptions(NBootUtils.firstNonNull(a.stringValue(), ""));
                            }
                            return (Collections.singletonList(a));
                        } else {
                            return (Collections.singletonList(a));
                        }
                    case "--xml":
                        a = cmdLine.next();
                        if (active) {
                            if (options != null) {
                                options.outputFormat("XML");
                                options.addOutputFormatOptions(NBootUtils.firstNonNull(a.stringValue(), ""));
                            }
                            return (Collections.singletonList(a));
                        } else {
                            return (Collections.singletonList(a));
                        }
                    case "--table":
                        a = cmdLine.next();
                        if (active) {
                            if (options != null) {
                                options.outputFormat("TABLE");
                                options.addOutputFormatOptions(NBootUtils.firstNonNull(a.stringValue(), ""));
                            }
                            return (Collections.singletonList(a));
                        } else {
                            return (Collections.singletonList(a));
                        }
                    case "--tree":
                        a = cmdLine.next();
                        if (active) {
                            if (options != null) {
                                options.outputFormat("TREE");
                                options.addOutputFormatOptions(NBootUtils.firstNonNull(a.stringValue(), ""));
                            }
                            return (Collections.singletonList(a));
                        } else {
                            return (Collections.singletonList(a));
                        }
                    case "--props":
                        a = cmdLine.next();
                        if (active) {
                            if (options != null) {
                                options.outputFormat("PROPS");
                                options.addOutputFormatOptions(NBootUtils.firstNonNull(a.stringValue(), ""));
                            }
                        } else {
                            return (Collections.singletonList(a));
                        }
                    case "--yes":
                    case "-y": {
                        a = cmdLine.nextFlag();
                        if (active && a.booleanValue()) {
                            if (options != null) {
                                //explicitConfirm = true;
                                options.confirm("YES");
                            }
                            return (Collections.singletonList(a));
                        } else {
                            return (Collections.singletonList(a));
                        }
                    }
                    case "--no":
                    case "-n": {
                        a = cmdLine.nextFlag();
                        if (active && a.booleanValue()) {
                            if (options != null) {
                                //explicitConfirm = true;
                                options.confirm("NO");
                            }
                            return (Collections.singletonList(a));
                        } else {
                            return (Collections.singletonList(a));
                        }
                    }
                    case "--error": {
                        a = cmdLine.nextFlag();
                        if (active && a.booleanValue()) {
                            if (options != null) {
                                //explicitConfirm = true;
                                options.confirm("ERROR");
                            }
                            return (Collections.singletonList(a));
                        } else {
                            return (Collections.singletonList(a));
                        }
                    }
                    case "--ask": {
                        a = cmdLine.nextFlag();
                        if (active && a.booleanValue()) {
                            if (options != null) {
                                //explicitConfirm = true;
                                options.confirm("ASK");
                            }
                            return (Collections.singletonList(a));
                        } else {
                            return (Collections.singletonList(a));
                        }
                    }
                    case "--open-file": {
                        a = cmdLine.nextFlag();
                        if (active && a.booleanValue()) {
                            if (options != null) {
                                options.executionType("OPEN");
                            }
                            return (Collections.singletonList(a));
                        } else {
                            return (Collections.singletonList(a));
                        }
                    }
                    case "--external":
                    case "--spawn":
                    case "-x": {
                        a = cmdLine.nextFlag();
                        if (active && a.booleanValue()) {
                            if (options != null) {
                                options.executionType("SPAWN");
                            }
                            return (Collections.singletonList(a));
                        } else {
                            return (Collections.singletonList(a));
                        }
                    }
                    case "--user-cmd"://deprecated since 0.8.1
                    case "--system": {
                        a = cmdLine.nextFlag();
                        if (active && a.booleanValue()) {
                            if (options != null) {
                                options.executionType("SYSTEM");
                            }
                            return (Collections.singletonList(a));
                        } else {
                            return (Collections.singletonList(a));
                        }
                    }
                    case "--root-cmd": //deprecated since 0.8.1
                    case "--as-root": {
                        a = cmdLine.nextFlag();
                        if (active && a.booleanValue()) {
                            if (options != null) {
                                options.runAs("ROOT");
                            }
                            return (Collections.singletonList(a));
                        } else {
                            return (Collections.singletonList(a));
                        }
                    }
                    case "--current-user": {
                        a = cmdLine.nextFlag();
                        if (active && a.booleanValue()) {
                            if (options != null) {
                                options.runAs("CURRENT_USER");
                            }
                            return (Collections.singletonList(a));
                        } else {
                            return (Collections.singletonList(a));
                        }
                    }
                    case "--run-as": {
                        a = cmdLine.nextEntry();
                        if (active) {
                            if (options != null) {
                                options.runAs("USER:" + a.stringValue());
                            }
                            return (Collections.singletonList(a));
                        } else {
                            return (Collections.singletonList(a));
                        }
                    }
                    case "--sudo": {
                        a = cmdLine.nextFlag();
                        if (active) {
                            if (options != null) {
                                options.runAs("SUDO");
                            }
                            return (Collections.singletonList(a));
                        } else {
                            return (Collections.singletonList(a));
                        }
                    }
                    //**********************************
                    //*
                    //* Commands
                    //*
                    //**********************************
                    case "-": {
                        if (active) {
                            List<String> newArgs = new ArrayList<>();
                            newArgs.addAll(Arrays.asList(cmdLine.toStringArray()));
                            cmdLine.skipAll();
                            if (options != null) {
                                if (a.value() != null) {
                                    NBootWorkspaceHelper.addError(NBootMsg.ofC(NBootI18n.of("invalid argument for workspace: %s"), a.image()), options);
                                }
                                List<String> applicationArguments = NBootUtils.nonNullStrList(options.applicationArguments());
                                applicationArguments.addAll(newArgs);
                                options.applicationArguments(applicationArguments);
                            }
                            return (newArgs.stream().map(NBootArg::of).collect(Collectors.toList()));
                        } else {
                            List<String> newArgs = new ArrayList<>(Arrays.asList(cmdLine.toStringArray()));
                            cmdLine.skipAll();
                            return (newArgs.stream().map(NBootArg::of).collect(Collectors.toList()));
                        }
                    }

                    case "-version":
                    case "-v":
                    case "--version": {
                        a = cmdLine.nextFlag();
                        if (active) {
                            if (options != null) {
                                options.commandVersion(a.isActive());
                            }
                            return (Collections.singletonList(a));
                        } else {
                            return (Collections.singletonList(a));
                        }
                    }
                    case "--out-line-prefix": {
                        a = cmdLine.nextEntry();
                        if (active) {
                            if (options != null) {
                                options.outLinePrefix(a.stringValue());
                            }
                        }
                    }
                    case "--err-line-prefix": {
                        a = cmdLine.nextEntry();
                        if (active) {
                            if (options != null) {
                                options.errLinePrefix(a.stringValue());
                            }
                            return (Collections.singletonList(a));
                        } else {
                            return (Collections.singletonList(a));
                        }
                    }
                    case "--line-prefix": {
                        a = cmdLine.nextEntry();
                        if (active) {
                            if (options != null) {
                                options.outLinePrefix(a.stringValue());
                                options.errLinePrefix(a.stringValue());
                            }
                            return (Collections.singletonList(a));
                        } else {
                            return (Collections.singletonList(a));
                        }
                    }
                    case "-e":
                    case "--exec": {
                        a = cmdLine.nextFlag();
                        if (active) {
                            List<String> newArgs = new ArrayList<>();
                            newArgs.add(a.toString());
                            if (a.booleanValue()) {
                                while ((a = cmdLine.next()) != null) {
                                    if (a.isOption()) {
                                        if (options != null) {
                                            List<String> executorOptions = options.executorOptions();
                                            if (executorOptions == null) {
                                                executorOptions = new ArrayList<>();
                                            }
                                            executorOptions.add(NBootUtils.firstNonNull(a.image(), ""));
                                            newArgs.add(NBootUtils.firstNonNull(a.image(), ""));
                                            options.executorOptions(executorOptions);
                                        } else {
                                            newArgs.add(NBootUtils.firstNonNull(a.image(), ""));
                                        }
                                    } else {
                                        if (options != null) {
                                            List<String> applicationArguments = NBootUtils.nonNullStrList(options.applicationArguments());
                                            applicationArguments.add(NBootUtils.firstNonNull(a.toString(), ""));
                                            List<String> list = Arrays.asList(cmdLine.toStringArray());
                                            applicationArguments.addAll(list);
                                            newArgs.addAll(list);
                                            cmdLine.skipAll();
                                            options.applicationArguments(applicationArguments);
                                        } else {
                                            newArgs.addAll(Arrays.asList(cmdLine.toStringArray()));
                                            cmdLine.skipAll();
                                        }
                                    }

                                }
                            }
                            return (newArgs.stream().map(NBootArg::of).collect(Collectors.toList()));
                        } else {
                            List<String> newArgs = new ArrayList<>();
                            newArgs.addAll(Arrays.asList(cmdLine.toStringArray()));
                            cmdLine.skipAll();
                            return (newArgs.stream().map(NBootArg::of).collect(Collectors.toList()));
                        }
                    }
                    case "-?":
                    case "--help":
                    case "-h": {
                        a = cmdLine.nextFlag();
                        if (active) {
                            if (options != null) {
                                options.commandHelp(a.booleanValue());
                            }
                            return (Collections.singletonList(a));
                        } else {
                            return (Collections.singletonList(a));
                        }
                    }
                    case "--skip-errors": {
                        a = cmdLine.nextFlag();
                        if (active) {
                            if (options != null) {
                                options.skipErrors(a.booleanValue());
                            }
                            return (Collections.singletonList(a));
                        } else {
                            return (Collections.singletonList(a));
                        }
                    }
                    case "-L":
                    case "--locale": {
                        a = cmdLine.nextEntry();
                        if (active) {
                            if (options != null) {
                                options.locale(a.stringValue());
                            }
                            return (Collections.singletonList(a));
                        } else {
                            return (Collections.singletonList(a));
                        }
                    }
                    //ERRORS
                    default: {
                        if (k.startsWith("---") && k.length() > 3 && k.charAt(3) != '-') {
                            a = cmdLine.next();
                            if (options != null) {
                                List<String> customOptions = options.customOptions();
                                if (customOptions == null) {
                                    customOptions = new ArrayList<>();
                                }
                                customOptions.add(a.toString());
                                options.customOptions(customOptions);
                            }
                            return (Collections.singletonList(a));
                        } else {
                            NBootWorkspaceHelper.addError(NBootMsg.ofC(NBootI18n.of("nuts: invalid option %s"), a.image()), options);
                            throw new NBootException(NBootMsg.ofC(NBootI18n.of("unsupported option %s"), a));
                        }
                    }
                }
            } else {
                List<String> newArgs = new ArrayList<>();
                newArgs.addAll(Arrays.asList(cmdLine.toStringArray()));
                cmdLine.skipAll();
                if (options != null) {
                    List<String> applicationArguments = NBootUtils.nonNullStrList(options.applicationArguments());
                    applicationArguments.addAll(newArgs);
                    options.applicationArguments(applicationArguments);
                }
                return (newArgs.stream().map(NBootArg::of).collect(Collectors.toList()));
            }
        }
        if (cmdLine.isEmpty()) {
            return null;
        }
        throw new NBootException(NBootMsg.ofC(NBootI18n.of("unsupported %s"), cmdLine.peek()));
    }

    private void postInit() {
        if (this.options.creationTime() == null) {
            this.options.creationTime(creationTime);
        }
        if (options.applicationArguments() == null) {
            options.applicationArguments(new ArrayList<>());
        }
        if (options.errors() == null) {
            options.errors(new ArrayList<>());
        }
        this.bLog.setOptions(this.options);
    }

    @Override
    public NBootWorkspace runWorkspace() {
        runWorkspace0();
        return this;
    }


    @Override
    public NWorkspaceBase workspace() {
        return new NWorkspaceBase() {
            @Override
            public void runBootCommand() {
            }

            @Override
            public void completeBootCommand(NBootCompleteCmdlineRequest completeRequest) {

            }
        };
    }


    public void runWorkspace0() {
        if (complete != null) {
            NBootCompleteRequestOrResult r = NBootWorkspaceCmdLineParser.complete(new NBootCompleteCmdlineRequest(complete, Arrays.asList(unparsedOptions.optionArgs())));
            if (r instanceof NBootCompleteResult) {
                NBootContext.context().log.out().println(((NBootCompleteResult) r).format());
                return;
            } else {
                cmdComplete = (NBootCompleteCmdlineRequest) r;
            }
        }
        if (NBootUtils.firstNonNull(options.commandHelp(), false)) {
            NBootWorkspaceHelper.runCommandHelp(options, cmdComplete);
            return;
        } else if (NBootUtils.firstNonNull(options.commandVersion(), false)) {
            NBootWorkspaceHelper.runCommandVersion(null, options, cmdComplete);
            return;
        }

        if (options.applicationArguments().isEmpty()) {
            NBootWorkspaceHelper.addError(NBootMsg.ofPlain(NBootI18n.of("missing command")), options);
        }
        if (!options.errors().isEmpty()) {
            showErrors();
            StringBuilder sb = new StringBuilder();
            sb.append(NBootI18n.of("Unable to run command")).append("\n");
            sb.append(NBootI18n.of("run using options :")).append("\n");
            sb.append(" minTime =").append(minTime).append("\n");
            sb.append(" waitTime=").append(waitTime).append("\n");
            sb.append(" maxCount=").append(maxCount).append("\n");
            sb.append(" cmd     =")
                    .append(options.applicationArguments().stream()
                            .map(x -> "\"" + x + "\"")
                            .collect(Collectors.joining(" "))).append("\n");
            throw new NBootException(NBootMsg.ofC("%s", sb));
        }
        long count = 0;
        while (true) {
            showDebugLine(NBootI18n.of("START COMMAND"));
            long start = System.currentTimeMillis();
            int i = execCommand();
            long end = System.currentTimeMillis();
            showDebugLine(NBootMsg.ofC(NBootI18n.of("END   COMMAND : ret=%s; time=%s"), i, (end - start)).toString());
            if (minTime > 0) {
                if ((end - start) < minTime) {
                    showErrorLine(NBootI18n.of("PROCESS TOO FAST, exit"));
                    throw new NBootException(NBootMsg.ofC(NBootI18n.of("PROCESS TOO FAST, exit with : %s"), i), i);
                }
            }
            if (waitTime > 0) {
                try {
                    showDebugLine(NBootI18n.of("WAITING..."));
                    Thread.sleep(waitTime);
                } catch (InterruptedException ex) {
                    throw new NBootException(NBootMsg.ofC(NBootI18n.of("PROCESS INTERRUPTED, exit with : %s"), i), i);
                }
            }
            count++;
            if (maxCount > 0 && count >= maxCount) {
                return;
            }
        }
    }

    private int execCommand() {
        int i = 1;
        try {
            final Process p;
            ProcessBuilder pb = new ProcessBuilder(options.applicationArguments());
            pb.inheritIO();
            p = pb.start();
            i = p.waitFor();
        } catch (Exception ex) {
            showErrorLine(ex.toString());
        }
        return i;
    }

    private void showDebugLine(String err) {
        NBootLogConfig lc = options.logConfig();
        if (lc != null) {
            Level lvl = lc.logTermLevel();
            if (lvl != null && lvl.intValue() <= Level.FINE.intValue()) {
                bLog.outln(err);
            }
        }
    }

    private void showErrorLine(String err) {
        bLog.errln(err);
    }


    private void showErrors() {
        for (String error : options.errors()) {
            showErrorLine(error);
        }
    }

}
