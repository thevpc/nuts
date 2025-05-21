package net.thevpc.nuts.runtime.standalone.executor.system;

import net.thevpc.nuts.*;

import net.thevpc.nuts.spi.NScopeType;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.NDesktopEnvironmentFamily;
import net.thevpc.nuts.NOsFamily;
import net.thevpc.nuts.io.NNonBlockingInputStream;
import net.thevpc.nuts.util.NRef;
import net.thevpc.nuts.util.*;

import java.io.File;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;

public class NSysExecUtils {
    public static Path sysWhich(String commandName) {
        Path[] p = sysWhichAll(commandName);
        if (p.length > 0) {
            return p[0];
        }
        return null;
    }

    public static Path[] sysWhichAll(String commandName) {
        if (commandName == null || commandName.isEmpty()) {
            return new Path[0];
        }
        List<Path> all = new ArrayList<>();
        String p = NWorkspace.of().getSysEnv("PATH").orNull();
        if (p != null) {
            for (String s : p.split(File.pathSeparator)) {
                try {
                    if (!s.trim().isEmpty()) {
                        Path c = Paths.get(s, commandName);
                        if (Files.isRegularFile(c)) {
                            if (Files.isExecutable(c)) {
                                all.add(c);
                            }
                        }
                    }
                } catch (Exception ex) {
                    //ignore
                }
            }
        }
        return all.toArray(new Path[0]);
    }

    public static PipeRunnable pipe(String name, String cmd, String desc, final NNonBlockingInputStream in, final OutputStream out) {
        return new PipeRunnable(name, cmd, desc, in, out, true);
    }

    public static String resolveRootUserName() {
        NOsFamily sysFamily = NWorkspace.of().getOsFamily();
        switch (sysFamily) {
            case WINDOWS: {
                String s = (String) NApp.of().getProperty("nuts.windows.root-user", NScopeType.SESSION).orNull();
                if (s == null) {
                    s = NWorkspace.of().getConfigProperty("nuts.windows.root-user").flatMap(NLiteral::asString).orNull();
                }
                if (NBlankable.isBlank(s)) {
                    s = "Administrator";
                }
                return s;
            }
            default: {
                return "root";
            }
        }
    }

    public static String resolveRootUserName(NOsFamily sysFamily) {
        switch (sysFamily) {
            case WINDOWS: {
                String s = (String) NApp.of().getProperty("nuts.windows.root-user", NScopeType.SESSION).orNull();
                if (s == null) {
                    s = NWorkspace.of().getConfigProperty("nuts.windows.root-user").flatMap(NLiteral::asString).orNull();
                }
                if (NBlankable.isBlank(s)) {
                    s = "Administrator";
                }
                return s;
            }
            default: {
                return "root";
            }
        }
    }

    public static List<String> buildEffectiveCommandLocal(String[] args,
                                                          NRunAs runAsMode,
                                                          String[] executionOptions
    ) {
        NSession session = NSession.get().get();
        return NSysExecUtils.buildEffectiveCommand(args, runAsMode,
                NWorkspace.of().getDesktopEnvironmentFamilies(),
                n -> {
                    Path path = NSysExecUtils.sysWhich(n);
                    if (path != null) {
                        return path.toString();
                    }
                    return null;
                },
                session.isGui() && NWorkspace.of().isGraphicalDesktopEnvironment(),
                NSysExecUtils.resolveRootUserName(),
                System.getProperty("user.name"),
                executionOptions
        );
    }

    private static List<String> prepareCommand(String[] cmd, Function<String, String[]> patterns) {
        List<String> ret = new ArrayList<>();
        for (String s : cmd) {
            if (s.matches("[$][a-zA-Z]+")) {
                String[] a = patterns.apply(s.substring(1));
                if (a == null) {
                    throw new NIllegalArgumentException(NMsg.ofC("invalid pattern %s", s));
                }
                ret.addAll(Arrays.asList(a));
            }else if (s.matches("[$][{][a-zA-Z]+[}]")) {
                String[] a = patterns.apply(s.substring(2,s.length()-1));
                if (a == null) {
                    throw new NIllegalArgumentException(NMsg.ofC("invalid pattern %s", s));
                }
                ret.addAll(Arrays.asList(a));
            }else{
                ret.add(NMsg.ofV(s,r->{
                    String[] qq = patterns.apply(r);
                    if(qq==null){
                        throw new NIllegalArgumentException(NMsg.ofC("invalid pattern %s", r));
                    }
                    if(qq.length>1){
                        throw new NIllegalArgumentException(NMsg.ofC("invalid pattern %s, toot many values", r));
                    }
                    return qq[0];
                }).toString());
            }
        }
        return ret;
    }

    public static List<String> buildEffectiveCommand(String[] cmd,
                                                     NRunAs runAsMode,
                                                     Set<NDesktopEnvironmentFamily> de,
                                                     Function<String, String> sysWhich,
                                                     Boolean gui,
                                                     String rootName,
                                                     String userName,
                                                     String[] executorOptions
    ) {
        //String runAsEffective = null;
        NOsFamily sysFamily = NWorkspace.of().getOsFamily();
        List<String> command = new ArrayList<>(Arrays.asList(cmd));
        if (runAsMode == null) {
            runAsMode = NRunAs.CURRENT_USER;
        }
        NSession session = NSession.get().get();
        boolean runWithGui = gui != null ? gui : session.isGui() && NWorkspace.of().isGraphicalDesktopEnvironment();
        String rootUserName = rootName != null ? rootName : resolveRootUserName();
        String currentUserName = userName != null ? userName : System.getProperty("user.name");
        if (sysWhich == null) {
            sysWhich = n -> {
                Path path = NSysExecUtils.sysWhich(n);
                if (path != null) {
                    return path.toString();
                }
                return null;
            };
        }
        //optimize mode
        switch (runAsMode.getMode()) {
            case ROOT: {
                if (rootUserName.equals(currentUserName)) {
                    runAsMode = NRunAs.currentUser();
                }
                break;
            }
            case USER: {
                String s = runAsMode.getUser();
                s = s.trim();
                if (currentUserName.equals(s)) {
                    runAsMode = NRunAs.currentUser();
                }
                if (!s.equals(runAsMode.getUser())) {
                    runAsMode = NRunAs.user(s);
                }
                break;
            }
        }
        NRunAs finalRunAsMode = runAsMode;
        Function<String,String[]> cm= s -> {
            switch (s){
                case "user":return new String[]{finalRunAsMode.getMode() == NRunAs.Mode.USER ? finalRunAsMode.getUser() : rootUserName};
                case "command":return command.toArray(new String[0]);
                case "rootUser":return new String[]{rootUserName};
            }
            return null;
        };
        switch (runAsMode.getMode()) {
            case CURRENT_USER: {
                List<String> cc = new ArrayList<>();
                cc.addAll(command);
                return cc;
            }
            case ROOT:
            case USER: {
                List<String> cc = new ArrayList<>();
                switch (sysFamily) {
                    case LINUX:
                    case MACOS:
                    case UNIX: {
                        if (runWithGui) {
                            cc.addAll(prepareCommand(guiPosixSu(de, sysWhich).get(),cm));
                        } else {
                            cc.addAll(prepareCommand(termPosixSu(de, sysWhich).get(),cm));
                        }
                        break;
                    }
                    case WINDOWS: {
                        cc.addAll(prepareCommand(guiWindowsSu(de,sysWhich).get(),cm));
                        break;
                    }
                    default: {
                        throw new NIllegalArgumentException(NMsg.ofC("cannot run as %s on unknown system OS family", finalRunAsMode.getMode() == NRunAs.Mode.USER ? finalRunAsMode.getUser() : rootUserName));
                    }
                }
                return cc;
            }
            case SUDO: {
                List<String> cc = new ArrayList<>();
                switch (sysFamily) {
                    case LINUX:
                    case MACOS:
                    case UNIX: {
                        if (runWithGui) {
                            cc.addAll(prepareCommand(guiPosixSudo(de, sysWhich).get(), cm));
                        } else {
                            cc.addAll(prepareCommand(termPosixSudo(executorOptions, de, sysWhich).get(), cm));
                        }
                        break;
                    }
                    case WINDOWS: {
                        cc.addAll(prepareCommand(guiWindowsSudo(de,sysWhich).get(),cm));
                        break;
                    }
                    default: {
                        throw new NIllegalArgumentException(NMsg.ofC("cannot run sudo %s on unknown system OS family", currentUserName));
                    }
                }
                return cc;
            }
        }
        throw new NIllegalArgumentException(NMsg.ofPlain("cannot run as admin/root on unknown system OS family"));
    }

    private static NOptional<String[]> guiWindowsSudo(Set<NDesktopEnvironmentFamily> de, Function<String, String> sysWhich) {
        return NOptional.of(new String[]{"runas", "/noprofile", "/user:$rootUser","$command"});
    }

    private static NOptional<String[]> guiWindowsSu(Set<NDesktopEnvironmentFamily> de, Function<String, String> sysWhich) {
        return NOptional.of(new String[]{"runas", "/noprofile", "/user:$user","$command"});
    }

    private static NOptional<String[]> termPosixSu(Set<NDesktopEnvironmentFamily> de, Function<String, String> sysWhich) {
        String su = sysWhich.apply("su");
        if (NBlankable.isBlank(su)) {
            return NOptional.ofNamedEmpty("su application");
        }
        return NOptional.of(new String[]{su,"-","$user","-c","$command"});
    }

    private static NOptional<String[]> termPosixSudo(String[] executorOptions, Set<NDesktopEnvironmentFamily> de, Function<String, String> sysWhich){
        NCmdLine cmdLine = NCmdLine.of(executorOptions);
        NRef<Boolean> changePrompt = NRef.of(false);
        NRef<String> newPromptValue = NRef.of("");
        while (cmdLine.hasNext()) {
            NArg ac = cmdLine.peek().get();
            switch (ac.key()) {
                case "--sudo-prompt": {
                    if (ac.getValue().isNull()) {
                        cmdLine.withNextFlag((v, a) -> {
                            if (v) {
                                // --sudo-prompt will reset the prompt to its defaults!
                                changePrompt.set(false);
                                newPromptValue.set(null);
                            } else {
                                // --!sudo-prompt is equivalent to "--!no-sudo-prompt="
                                changePrompt.set(true);
                                newPromptValue.set("");
                            }
                        });
                    } else if (ac.getValue().isString()) {
                        cmdLine.withNextEntry((v, a) -> {
                            changePrompt.set(true);
                            newPromptValue.set(v);
                        });
                    } else {
                        cmdLine.skip();
                    }
                    break;
                }
                default: {
                    cmdLine.skip();
                }
            }
        }
        String su = sysWhich.apply("sudo");
        if (NBlankable.isBlank(su)) {
            return NOptional.ofNamedEmpty("sudo application");
        }
        List<String> rr=new ArrayList<>();
        rr.add(su);
        rr.add("-S");
        if (changePrompt.get()) {
            rr.add("-p");
            rr.add(newPromptValue.get());
        }
        rr.add("$command");
        return NOptional.of(rr.toArray(new String[0]));
    }

    private static NOptional<String[]> guiPosixSu
            (Set<NDesktopEnvironmentFamily> de, Function<String, String> sysWhich) {
        if (de == null) {
            de = NWorkspace.of().getDesktopEnvironmentFamilies();
        }
        String currSu = null;
        currSu = sysWhich.apply("pkexec");
        if (currSu != null) {
            return NOptional.of(new String[]{currSu, "sudo", "-u", "$user", "$command"});
        }
        if (de.contains(NDesktopEnvironmentFamily.KDE)) {
            currSu = sysWhich.apply("kdesu");
            if (currSu != null) {
                return NOptional.of(new String[]{currSu, "-u", "$user", "$command"});
            }
        } else if (de.contains(NDesktopEnvironmentFamily.GNOME)) {
            currSu = sysWhich.apply("gksu");
            if (currSu != null) {
                return NOptional.of(new String[]{currSu, "-u", "$user", "$command"});
            }
        }
        if (currSu == null) {
            currSu = sysWhich.apply("gksu");
            if (currSu != null) {
                return NOptional.of(new String[]{currSu, "-u", "$user", "$command"});
            }
        }
        if (currSu == null) {
            currSu = sysWhich.apply("kdesu");
            if (currSu != null) {
                return NOptional.of(new String[]{currSu, "-u", "$user", "$command"});
            }
        }
        return NOptional.ofNamedEmpty("gui su application (pkexec,kdesu,gksu,...)");
    }

    private static NOptional<String[]> guiPosixSudo
            (Set<NDesktopEnvironmentFamily> de, Function<String, String> sysWhich) {
        if (de == null) {
            de = NWorkspace.of().getDesktopEnvironmentFamilies();
        }
        String currSu = null;
        currSu = sysWhich.apply("pkexec");
        if (currSu != null) {
            return NOptional.of(new String[]{currSu, "$command"});
        }
        if (de.contains(NDesktopEnvironmentFamily.KDE)) {
            currSu = sysWhich.apply("kdesudo");
            if (currSu != null) {
                return NOptional.of(new String[]{currSu, "$command"});
            }
        } else if (de.contains(NDesktopEnvironmentFamily.GNOME)) {
            currSu = sysWhich.apply("gksudo");
            if (currSu != null) {
                return NOptional.of(new String[]{currSu, "$command"});
            }
        }
        if (currSu == null) {
            currSu = sysWhich.apply("gksudo");
            if (currSu != null) {
                return NOptional.of(new String[]{currSu, "$command"});
            }
        }
        if (currSu == null) {
            currSu = sysWhich.apply("kdesudo");
            if (currSu != null) {
                return NOptional.of(new String[]{currSu, "$command"});
            }
        }
        return NOptional.ofNamedEmpty("gui su application (pkexec,kdesudo,gksudo,...)");
    }
}
