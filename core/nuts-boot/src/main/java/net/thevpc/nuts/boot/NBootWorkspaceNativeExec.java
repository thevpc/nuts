package net.thevpc.nuts.boot;

import net.thevpc.nuts.NI18n;
import net.thevpc.nuts.NWorkspaceBase;
import net.thevpc.nuts.boot.reserved.cmdline.NBootArg;
import net.thevpc.nuts.boot.reserved.cmdline.NBootCmdLine;
import net.thevpc.nuts.boot.reserved.cmdline.NBootWorkspaceCmdLineParser;
import net.thevpc.nuts.boot.reserved.util.*;

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

    public NBootWorkspaceNativeExec(NBootArguments unparsedOptions) {
        if (unparsedOptions == null) {
            unparsedOptions = new NBootArguments();
        }
        this.unparsedOptions = unparsedOptions;
        NBootOptionsInfo userOptions = new NBootOptionsInfo();
        userOptions.setStdin(unparsedOptions.getIn());
        userOptions.setStdout(unparsedOptions.getOut());
        userOptions.setStderr(unparsedOptions.getErr());
        userOptions.setCreationTime(unparsedOptions.getStartTime());
        InputStream in = userOptions.getStdin();
        scanner = new Scanner(in == null ? System.in : in);
        this.bLog = new NBootLog(userOptions);
        List<String> allArgs = new ArrayList<>();
        if (unparsedOptions.getOptionArgs() != null) {
            allArgs.addAll(Arrays.asList(unparsedOptions.getOptionArgs()));
        }
        if (unparsedOptions.getAppArgs() != null) {
            allArgs.addAll(Arrays.asList(unparsedOptions.getAppArgs()));
        }
        parseArguments(allArgs.toArray(new String[0]), userOptions);
        if (NBootUtils.firstNonNull(userOptions.getSkipErrors(), false)) {
            StringBuilder errorMessage = new StringBuilder();
            if (userOptions.getErrors() != null) {
                for (String s : userOptions.getErrors()) {
                    errorMessage.append(s).append("\n");
                }
            }
            errorMessage.append(NI18n.of("Try 'nuts --help' for more information."));
            bLog.warn(NBootMsg.ofC(NI18n.of("Skipped Error : %s"), errorMessage));
        }
        this.options = userOptions.copy();
        this.postInit();
    }

    public NBootArguments getBootArguments() {
        return unparsedOptions;
    }

    public NBootOptionsInfo getOptions() {
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
                            String sValue = NBootUtils.firstNonNull(a.getStringValue(), "");
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
                            String sValue = NBootUtils.firstNonNull(a.getStringValue(), "");
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
                            String sValue = NBootUtils.firstNonNull(a.getStringValue(), "");
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
                            String v = NBootUtils.firstNonNull(a.getStringValue(), "");
                            options.setJavaCommand(v);
                        }
                        return (Collections.singletonList(a));
                    }
                    case "--java-home":
                    case "--boot-java-home": {
                        a = cmdLine.nextEntry();
                        if (active && options != null) {
                            String v = a.getStringValue();
                            options.setJavaCommand(NBootUtils.resolveJavaCommand(v));
                        }
                        return (Collections.singletonList(a));
                    }
                    case "--java-options":
                    case "--boot-java-options":
                    case "-J": {
                        a = cmdLine.nextEntry();
                        String v = NBootUtils.firstNonNull(a.getStringValue(), "");
                        if (active && options != null) {
                            options.setJavaOptions(v);
                        }
                        return (Collections.singletonList(a));
                    }

                    case "--gui": {
                        a = cmdLine.nextFlag();
                        if (active && options != null) {
                            options.setGui(a.getBooleanValue());
                        }
                        return (Collections.singletonList(a));
                    }

                    case "-B":
                    case "--bot": {
                        a = cmdLine.nextFlag();
                        if (active) {
                            if (options != null) {
                                options.setBot(a.getBooleanValue());
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
                                options.setTrace(a.getBooleanValue());
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
                                String s = a.getStringValue();
                                if (a.isNegated()) {
                                    if (NBootUtils.isBlank(s)) {
                                        s = "false";
                                    } else {
                                        s = "false," + s;
                                    }
                                    options.setProgressOptions(s);
                                } else {
                                    options.setProgressOptions(s);
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
                            options.setDry(a.getBooleanValue());
                        }
                        return (Collections.singletonList(a));
                    }
                    case "--stacktrace": {
                        a = cmdLine.nextFlag();
                        if (active && options != null) {
                            options.setShowStacktrace(a.getBooleanValue());
                        }
                        return (Collections.singletonList(a));
                    }
                    case "--debug": {
                        a = cmdLine.next();
                        if (active) {
                            if (options != null) {
                                if (NBootUtils.isBlank(a.getStringValue())) {
                                    options.setDebug(String.valueOf(a.isEnabled()));
                                } else {
                                    if (a.isNegated()) {
                                        options.setDebug(String.valueOf(!NBootUtils.parseBoolean(a.getStringValue(), true, false)));
                                    } else {
                                        options.setDebug(a.getStringValue());
                                    }
                                }
                            }
                            return (Collections.singletonList(a));
                        } else {
                            return (Collections.singletonList(a));
                        }
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
                            NBootLogConfig logConfig = options.getLogConfig();
                            if (logConfig == null) {
                                logConfig = new NBootLogConfig();
                            }
                            NBootArg r = NBootWorkspaceCmdLineParser.parseLogLevel(logConfig, cmdLine, active);
                            options.setLogConfig(logConfig);
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
                                options.addOutputFormatOptions(cmdLine.nextEntry().getStringValue());
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
                                String t = NBootUtils.firstNonNull(a.getStringValue(), "");
                                int i = NBootUtils.firstIndexOf(t, new char[]{' ', ';', ':', '='});
                                if (i > 0) {
                                    options.setOutputFormat((t.substring(0, i).toUpperCase()));
                                    options.addOutputFormatOptions(t.substring(i + 1).toUpperCase());
                                } else {
                                    options.setOutputFormat((t.toUpperCase()));
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
                                options.setOutputFormat("TSON");
                                options.addOutputFormatOptions(NBootUtils.firstNonNull(a.getStringValue(), ""));
                            }
                            return (Collections.singletonList(a));
                        } else {
                            return (Collections.singletonList(a));
                        }
                    case "--yaml":
                        a = cmdLine.next();
                        if (active) {
                            if (options != null) {
                                options.setOutputFormat("YAML");
                                options.addOutputFormatOptions(NBootUtils.firstNonNull(a.getStringValue(), ""));
                            }
                            return (Collections.singletonList(a));
                        } else {
                            return (Collections.singletonList(a));
                        }
                    case "--json":
                        a = cmdLine.next();
                        if (active) {
                            if (options != null) {
                                options.setOutputFormat("JSON");
                                options.addOutputFormatOptions(NBootUtils.firstNonNull(a.getStringValue(), ""));
                            }
                            return (Collections.singletonList(a));
                        } else {
                            return (Collections.singletonList(a));
                        }
                    case "--plain":
                        a = cmdLine.next();
                        if (active) {
                            if (options != null) {
                                options.setOutputFormat("PLAIN");
                                options.addOutputFormatOptions(NBootUtils.firstNonNull(a.getStringValue(), ""));
                            }
                            return (Collections.singletonList(a));
                        } else {
                            return (Collections.singletonList(a));
                        }
                    case "--xml":
                        a = cmdLine.next();
                        if (active) {
                            if (options != null) {
                                options.setOutputFormat("XML");
                                options.addOutputFormatOptions(NBootUtils.firstNonNull(a.getStringValue(), ""));
                            }
                            return (Collections.singletonList(a));
                        } else {
                            return (Collections.singletonList(a));
                        }
                    case "--table":
                        a = cmdLine.next();
                        if (active) {
                            if (options != null) {
                                options.setOutputFormat("TABLE");
                                options.addOutputFormatOptions(NBootUtils.firstNonNull(a.getStringValue(), ""));
                            }
                            return (Collections.singletonList(a));
                        } else {
                            return (Collections.singletonList(a));
                        }
                    case "--tree":
                        a = cmdLine.next();
                        if (active) {
                            if (options != null) {
                                options.setOutputFormat("TREE");
                                options.addOutputFormatOptions(NBootUtils.firstNonNull(a.getStringValue(), ""));
                            }
                            return (Collections.singletonList(a));
                        } else {
                            return (Collections.singletonList(a));
                        }
                    case "--props":
                        a = cmdLine.next();
                        if (active) {
                            if (options != null) {
                                options.setOutputFormat("PROPS");
                                options.addOutputFormatOptions(NBootUtils.firstNonNull(a.getStringValue(), ""));
                            }
                        } else {
                            return (Collections.singletonList(a));
                        }
                    case "--yes":
                    case "-y": {
                        a = cmdLine.nextFlag();
                        if (active && a.getBooleanValue()) {
                            if (options != null) {
                                //explicitConfirm = true;
                                options.setConfirm("YES");
                            }
                            return (Collections.singletonList(a));
                        } else {
                            return (Collections.singletonList(a));
                        }
                    }
                    case "--no":
                    case "-n": {
                        a = cmdLine.nextFlag();
                        if (active && a.getBooleanValue()) {
                            if (options != null) {
                                //explicitConfirm = true;
                                options.setConfirm("NO");
                            }
                            return (Collections.singletonList(a));
                        } else {
                            return (Collections.singletonList(a));
                        }
                    }
                    case "--error": {
                        a = cmdLine.nextFlag();
                        if (active && a.getBooleanValue()) {
                            if (options != null) {
                                //explicitConfirm = true;
                                options.setConfirm("ERROR");
                            }
                            return (Collections.singletonList(a));
                        } else {
                            return (Collections.singletonList(a));
                        }
                    }
                    case "--ask": {
                        a = cmdLine.nextFlag();
                        if (active && a.getBooleanValue()) {
                            if (options != null) {
                                //explicitConfirm = true;
                                options.setConfirm("ASK");
                            }
                            return (Collections.singletonList(a));
                        } else {
                            return (Collections.singletonList(a));
                        }
                    }
                    case "--open-file": {
                        a = cmdLine.nextFlag();
                        if (active && a.getBooleanValue()) {
                            if (options != null) {
                                options.setExecutionType("OPEN");
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
                        if (active && a.getBooleanValue()) {
                            if (options != null) {
                                options.setExecutionType("SPAWN");
                            }
                            return (Collections.singletonList(a));
                        } else {
                            return (Collections.singletonList(a));
                        }
                    }
                    case "--user-cmd"://deprecated since 0.8.1
                    case "--system": {
                        a = cmdLine.nextFlag();
                        if (active && a.getBooleanValue()) {
                            if (options != null) {
                                options.setExecutionType("SYSTEM");
                            }
                            return (Collections.singletonList(a));
                        } else {
                            return (Collections.singletonList(a));
                        }
                    }
                    case "--root-cmd": //deprecated since 0.8.1
                    case "--as-root": {
                        a = cmdLine.nextFlag();
                        if (active && a.getBooleanValue()) {
                            if (options != null) {
                                options.setRunAs("ROOT");
                            }
                            return (Collections.singletonList(a));
                        } else {
                            return (Collections.singletonList(a));
                        }
                    }
                    case "--current-user": {
                        a = cmdLine.nextFlag();
                        if (active && a.getBooleanValue()) {
                            if (options != null) {
                                options.setRunAs("CURRENT_USER");
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
                                options.setRunAs("USER:" + a.getStringValue());
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
                                options.setRunAs("SUDO");
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
                                if (a.getValue() != null) {
                                    NBootWorkspaceHelper.addError(NBootMsg.ofC(NI18n.of("invalid argument for workspace: %s"), a.getImage()), options);
                                }
                                List<String> applicationArguments = NBootUtils.nonNullStrList(options.getApplicationArguments());
                                applicationArguments.addAll(newArgs);
                                options.setApplicationArguments(applicationArguments);
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
                                options.setCommandVersion(a.isActive());
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
                                options.setOutLinePrefix(a.getStringValue());
                            }
                        }
                    }
                    case "--err-line-prefix": {
                        a = cmdLine.nextEntry();
                        if (active) {
                            if (options != null) {
                                options.setErrLinePrefix(a.getStringValue());
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
                                options.setOutLinePrefix(a.getStringValue());
                                options.setErrLinePrefix(a.getStringValue());
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
                            if (a.getBooleanValue()) {
                                while ((a = cmdLine.next()) != null) {
                                    if (a.isOption()) {
                                        if (options != null) {
                                            List<String> executorOptions = options.getExecutorOptions();
                                            if (executorOptions == null) {
                                                executorOptions = new ArrayList<>();
                                            }
                                            executorOptions.add(NBootUtils.firstNonNull(a.getImage(), ""));
                                            newArgs.add(NBootUtils.firstNonNull(a.getImage(), ""));
                                            options.setExecutorOptions(executorOptions);
                                        } else {
                                            newArgs.add(NBootUtils.firstNonNull(a.getImage(), ""));
                                        }
                                    } else {
                                        if (options != null) {
                                            List<String> applicationArguments = NBootUtils.nonNullStrList(options.getApplicationArguments());
                                            applicationArguments.add(NBootUtils.firstNonNull(a.toString(), ""));
                                            List<String> list = Arrays.asList(cmdLine.toStringArray());
                                            applicationArguments.addAll(list);
                                            newArgs.addAll(list);
                                            cmdLine.skipAll();
                                            options.setApplicationArguments(applicationArguments);
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
                                options.setCommandHelp(a.getBooleanValue());
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
                                options.setSkipErrors(a.getBooleanValue());
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
                                options.setLocale(a.getStringValue());
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
                                List<String> customOptions = options.getCustomOptions();
                                if (customOptions == null) {
                                    customOptions = new ArrayList<>();
                                }
                                customOptions.add(a.toString());
                                options.setCustomOptions(customOptions);
                            }
                            return (Collections.singletonList(a));
                        } else {
                            NBootWorkspaceHelper.addError(NBootMsg.ofC(NI18n.of("nuts: invalid option %s"), a.getImage()), options);
                            throw new NBootException(NBootMsg.ofC(NI18n.of("unsupported option %s"), a));
                        }
                    }
                }
            } else {
                List<String> newArgs = new ArrayList<>();
                newArgs.addAll(Arrays.asList(cmdLine.toStringArray()));
                cmdLine.skipAll();
                if (options != null) {
                    List<String> applicationArguments = NBootUtils.nonNullStrList(options.getApplicationArguments());
                    applicationArguments.addAll(newArgs);
                    options.setApplicationArguments(applicationArguments);
                }
                return (newArgs.stream().map(NBootArg::of).collect(Collectors.toList()));
            }
        }
        if (cmdLine.isEmpty()) {
            return null;
        }
        throw new NBootException(NBootMsg.ofC(NI18n.of("unsupported %s"), cmdLine.peek()));
    }

    private void postInit() {
        if (this.options.getCreationTime() == null) {
            this.options.setCreationTime(creationTime);
        }
        if (options.getApplicationArguments() == null) {
            options.setApplicationArguments(new ArrayList<>());
        }
        if (options.getErrors() == null) {
            options.setErrors(new ArrayList<>());
        }
        this.bLog.setOptions(this.options);
    }

    @Override
    public NBootWorkspace runWorkspace() {
        runWorkspace0();
        return this;
    }


    @Override
    public NWorkspaceBase getWorkspace() {
        return new NWorkspaceBase() {
            @Override
            public void runBootCommand() {
            }
        };
    }


    public void runWorkspace0() {
        if (NBootUtils.firstNonNull(options.getCommandHelp(), false)) {
            NBootWorkspaceHelper.runCommandHelp(options);
            return;
        } else if (NBootUtils.firstNonNull(options.getCommandVersion(), false)) {
            NBootWorkspaceHelper.runCommandVersion(null, options);
            return;
        }

        if (options.getApplicationArguments().isEmpty()) {
            NBootWorkspaceHelper.addError(NBootMsg.ofPlain(NI18n.of("missing command")), options);
        }
        if (!options.getErrors().isEmpty()) {
            showErrors();
            StringBuilder sb = new StringBuilder();
            sb.append(NI18n.of("Unable to run command")).append("\n");
            sb.append(NI18n.of("run using options :")).append("\n");
            sb.append(" minTime =").append(minTime).append("\n");
            sb.append(" waitTime=").append(waitTime).append("\n");
            sb.append(" maxCount=").append(maxCount).append("\n");
            sb.append(" cmd     =")
                    .append(options.getApplicationArguments().stream()
                            .map(x -> "\"" + x + "\"")
                            .collect(Collectors.joining(" "))).append("\n");
            throw new NBootException(NBootMsg.ofC("%s", sb));
        }
        long count = 0;
        while (true) {
            showDebugLine(NI18n.of("START COMMAND"));
            long start = System.currentTimeMillis();
            int i = execCommand();
            long end = System.currentTimeMillis();
            showDebugLine(NBootMsg.ofC(NI18n.of("END   COMMAND : ret=%s; time=%s"), i, (end - start)).toString());
            if (minTime > 0) {
                if ((end - start) < minTime) {
                    showErrorLine(NI18n.of("PROCESS TOO FAST, exit"));
                    throw new NBootException(NBootMsg.ofC(NI18n.of("PROCESS TOO FAST, exit with : %s"), i), i);
                }
            }
            if (waitTime > 0) {
                try {
                    showDebugLine(NI18n.of("WAITING..."));
                    Thread.sleep(waitTime);
                } catch (InterruptedException ex) {
                    throw new NBootException(NBootMsg.ofC(NI18n.of("PROCESS INTERRUPTED, exit with : %s"), i), i);
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
            ProcessBuilder pb = new ProcessBuilder(options.getApplicationArguments());
            pb.inheritIO();
            p = pb.start();
            i = p.waitFor();
        } catch (Exception ex) {
            showErrorLine(ex.toString());
        }
        return i;
    }

    private void showDebugLine(String err) {
        NBootLogConfig lc = options.getLogConfig();
        if (lc != null) {
            Level lvl = lc.getLogTermLevel();
            if (lvl != null && lvl.intValue() <= Level.FINE.intValue()) {
                bLog.outln(err);
            }
        }
    }

    private void showErrorLine(String err) {
        bLog.errln(err);
    }


    private void showErrors() {
        for (String error : options.getErrors()) {
            showErrorLine(error);
        }
    }

}
