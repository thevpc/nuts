package net.thevpc.nuts.toolbox.nsh.options;

import net.thevpc.nuts.NSession;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.toolbox.nsh.err.NShellException;
import net.thevpc.nuts.toolbox.nsh.nshell.NShellOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DefaultNShellOptionsParser implements NShellOptionsParser {

    public DefaultNShellOptionsParser() {
    }

    protected NShellOptions createOptions() {
        return new NShellOptions();
    }

    @Override
    public NShellOptions parse(String[] args) {
        NShellOptions options = createOptions();
        List<String> args0 = new ArrayList<>(Arrays.asList(args));
        while (!args0.isEmpty()) {
            parseNextArgument(args0, options);
        }
        postParse(options);
        return options;
    }

    protected void postParse(NShellOptions options) {
        if (options.isInteractive() ||
                (options.getFiles().isEmpty()
                        && !options.isStdInAndPos()
                        && !options.isCommand())
        ) {
            options.setEffectiveInteractive(true);
        }
    }

    protected void parseNextArgument(List<String> args, NShellOptions options) {
        String arg = args.get(0);
        switch (arg) {
            case "-?":
            case "--help": {
                args.remove(0);
                options.setVersion(true);
                break;
            }
            case "--version": {
                args.remove(0);
                options.setHelp(true);
                break;
            }
            case "-v":
            case "--verbose": {
                args.remove(0);
                options.setVerbose(true);
                break;
            }
            case "-x": {
                args.remove(0);
                options.setXtrace(true);
                break;
            }
            case "-i": {
                args.remove(0);
                options.setInteractive(true);
                break;
            }
            case "-s": {
                args.remove(0);
                options.setStdInAndPos(true);
                break;
            }
            case "-r": {
                args.remove(0);
                options.setRestricted(true);
                break;
            }
            case "-l":
            case "--login": {
                args.remove(0);
                options.setLogin(true);
                break;
            }
            case "-D":
            case "--dump-strings": {
                args.remove(0);
                options.setDumpStrings(true);
                break;
            }
            case "--dump-po-strings": {
                args.remove(0);
                options.setDumpPoStrings(true);
                break;
            }
            case "--noediting": {
                args.remove(0);
                options.setNoEditing(true);
                break;
            }
            case "--noprofile": {
                args.remove(0);
                options.setNoProfile(true);
                break;
            }
            case "--norc": {
                args.remove(0);
                options.setNoRc(true);
                break;
            }
            case "--posix": {
                args.remove(0);
                options.setPosix(true);
                break;
            }
            case "--bash": {
                args.remove(0);
                options.setBash(true);
                break;
            }
            case "-c": {
                args.remove(0);
                if (!args.isEmpty()) {
                    options.setServiceName(args.get(0));
                }
                options.setCommand(true);
                options.setCommandArgs(Arrays.asList(args.toArray(new String[0])));
                args.clear();
                break;
            }
            case "--rcfile":
            case "--init-file": {
                args.remove(0);
                if (!args.isEmpty()) {
                    options.setRcFile(args.remove(0));
                }
                break;
            }
            case "--restricted": {
                args.remove(0);
                options.setRestricted(true);
                break;
            }
            case "--startup-script": {
                args.remove(0);
                if (!args.isEmpty()) {
                    options.setStartupScript(args.remove(0));
                }
                break;
            }
            case "--shutdown-script": {
                args.remove(0);
                if (!args.isEmpty()) {
                    options.setShutdownScript(args.remove(0));
                }
                break;
            }
            case "--": {
                args.remove(0);
                if (options.isStdInAndPos()) {
                    options.getCommandArgs().addAll(Arrays.asList(args.toArray(new String[0])));
                    args.clear();
                } else {
                    options.getFiles().addAll(Arrays.asList(args.toArray(new String[0])));
                    args.clear();
                }
                break;
            }
            case "-": {
                args.remove(0);
                options.setLogin(true);
                if (options.isStdInAndPos()) {
                    options.getCommandArgs().addAll(Arrays.asList(args.toArray(new String[0])));
                    args.clear();
                } else {
                    options.getFiles().addAll(Arrays.asList(args.toArray(new String[0])));
                    args.clear();
                }
                break;
            }
            default: {
                if (arg.startsWith("-")) {
                    parseUnsupportedNextArgument(args, options);
                } else {
                    options.getFiles().add(args.remove(0));
                    options.getCommandArgs().addAll(Arrays.asList(args.toArray(new String[0])));
                    args.clear();
                    options.setExitAfterProcessingLines(true);
                }
                break;
            }
        }

    }

    protected void parseUnsupportedNextArgument(List<String> args, NShellOptions options) {
        NCmdLine a = NCmdLine.of(args);
        if (NSession.of().configureFirst(a)) {
            //replace remaining...
            args.clear();
            args.addAll(Arrays.asList(a.toStringArray()));
        } else {
            throw new NShellException(NMsg.ofC("unsupported option %s", args.get(0)), 1);
        }
    }

}
