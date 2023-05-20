package net.thevpc.nuts.runtime.standalone.executor.system;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NNonBlockingInputStream;
import net.thevpc.nuts.util.NStringUtils;

import java.io.File;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
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
        String p = System.getenv("PATH");
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

    public static PipeRunnable pipe(String name, String cmd, String desc, final NNonBlockingInputStream in, final OutputStream out, NSession session) {
        return new PipeRunnable(name, cmd, desc, in, out, true, session);
    }

    public static String resolveRootUserName(NSession session) {
        NOsFamily sysFamily = NEnvs.of(session).getOsFamily();
        switch (sysFamily) {
            case WINDOWS: {
                String s = (String) session.getProperty("nuts.windows.root-user");
                if (s == null) {
                    s = NConfigs.of(session).getConfigProperty("nuts.windows.root-user").flatMap(NLiteral::asString).orNull();
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

    public static String resolveRootUserName(NOsFamily sysFamily, NSession session) {
        switch (sysFamily) {
            case WINDOWS: {
                String s = (String) session.getProperty("nuts.windows.root-user");
                if (s == null) {
                    s = NConfigs.of(session).getConfigProperty("nuts.windows.root-user").flatMap(NLiteral::asString).orNull();
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
                                                          NSession session
    ) {
        return NSysExecUtils.buildEffectiveCommand(args, runAsMode,
                NEnvs.of(session).getDesktopEnvironmentFamilies(),
                n -> {
                    Path path = NSysExecUtils.sysWhich(n);
                    if (path != null) {
                        return path.toString();
                    }
                    return null;
                },
                session.isGui() && NEnvs.of(session).isGraphicalDesktopEnvironment(),
                NSysExecUtils.resolveRootUserName(session),
                System.getProperty("user.name"),
                session);
    }

    public static List<String> buildEffectiveCommand(String[] cmd,
                                                     NRunAs runAsMode,
                                                     Set<NDesktopEnvironmentFamily> de,
                                                     Function<String, String> sysWhich,
                                                     Boolean gui,
                                                     String rootName,
                                                     String userName,
                                                     NSession session
    ) {
        //String runAsEffective = null;
        NOsFamily sysFamily = NEnvs.of(session).getOsFamily();
        List<String> command = new ArrayList<>(Arrays.asList(cmd));
        if (runAsMode == null) {
            runAsMode = NRunAs.CURRENT_USER;
        }
        boolean runWithGui = gui != null ? gui : session.isGui() && NEnvs.of(session).isGraphicalDesktopEnvironment();
        String rootUserName = rootName != null ? rootName : resolveRootUserName(session);
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

//        switch (runAsMode.getMode()) {
//            case CURRENT_USER: {
//                break;
//            }
//            case USER: {
//                if (NutsBlankable.isBlank(runAsUser)
//                        || System.getProperty("user.name").equals(runAsUser.trim())
//                ) {
//                    runAsMode = NutsExecutionType.SYSTEM;
//                    runAsEffective = null;
//                } else {
//                    runAsEffective = runAsUser.trim();
//                }
//                break;
//            }
//            case SYSTEM_ROOT: {
//                switch (sysFamily) {
//                    case LINUX:
//                    case MACOS:
//                    case UNIX:
//                    case WINDOWS: {
//                        runAsEffective = rootUserName;
//                        break;
//                    }
//                    default: {
//                        throw new NutsIllegalArgumentException(session, NMsg.plain("cannot run as admin/root on unknown system OS family"));
//                    }
//                }
//                if (System.getProperty("user.name").equals(runAsEffective.trim())) {
//                    runAsMode = NutsExecutionType.SYSTEM_USER.SYSTEM;
//                    runAsEffective = null;
//                }
//                break;
//            }
//            case SYSTEM_SUDO: {
//                runAsEffective = null;
//                break;
//            }
//        }
        switch (runAsMode.getMode()) {
            case CURRENT_USER: {
                List<String> cc = new ArrayList<>();
                cc.addAll(command);
                return cc;
            }
            case ROOT:
            case USER: {
                String runAsEffective = runAsMode.getMode() == NRunAs.Mode.USER ? runAsMode.getUser() : rootUserName;
                List<String> cc = new ArrayList<>();
                switch (sysFamily) {
                    case LINUX:
                    case MACOS:
                    case UNIX: {
                        if (runWithGui) {
                            String currSu = guiSu(de, sysWhich, session).get();
                            cc.add(currSu);
                            cc.add(runAsEffective);
                        } else {
                            String su = sysWhich.apply("su");
                            if (NBlankable.isBlank(su)) {
                                throw new NIllegalArgumentException(session, NMsg.ofPlain("unable to resolve su application"));
                            }
                            cc.add(su);
                            cc.add(runAsEffective);
                            cc.add("-c");
                        }
                        break;
                    }
                    case WINDOWS: {
                        cc.addAll(Arrays.asList("runas", "/noprofile", "/user:" + runAsEffective));
                        break;
                    }
                    default: {
                        throw new NIllegalArgumentException(session, NMsg.ofC("cannot run as %s on unknown system OS family", runAsEffective));
                    }
                }
                cc.addAll(command);
                return cc;
            }
            case SUDO: {
                List<String> cc = new ArrayList<>();
                switch (sysFamily) {
                    case LINUX:
                    case MACOS:
                    case UNIX: {
                        if (runWithGui) {
                            String currSu = guiSudo(de, sysWhich, session).get();
                            cc.add(currSu);
                        } else {
                            String su = sysWhich.apply("sudo");
                            if (NBlankable.isBlank(su)) {
                                throw new NIllegalArgumentException(session, NMsg.ofPlain("unable to resolve sudo application"));
                            }
                            cc.add(su);
                            cc.add("-S");
                            //cc.add("-p");
                            //cc.add("");
                        }
                        break;
                    }
                    case WINDOWS: {
                        cc.addAll(Arrays.asList("runas", "/noprofile", "/user:" + rootUserName));
                        break;
                    }
                    default: {
                        throw new NIllegalArgumentException(session, NMsg.ofC("cannot run sudo %s on unknown system OS family", currentUserName));
                    }
                }
                cc.addAll(command);
                return cc;
            }
        }
        throw new NIllegalArgumentException(session, NMsg.ofPlain("cannot run as admin/root on unknown system OS family"));
    }

    private static NOptional<String> guiSu(Set<NDesktopEnvironmentFamily> de, Function<String, String> sysWhich, NSession session) {
        if (de == null) {
            de = NEnvs.of(session).getDesktopEnvironmentFamilies();
        }
        String currSu = null;
        if (de.contains(NDesktopEnvironmentFamily.KDE)) {
            String kdesu = sysWhich.apply("kdesu");
            if (kdesu != null) {
                currSu = kdesu;
            }
        } else if (de.contains(NDesktopEnvironmentFamily.GNOME)) {
            String gksu = sysWhich.apply("gksu");
            if (gksu != null) {
                currSu = gksu;
            }
        }
        if (currSu == null) {
            String gksu = sysWhich.apply("gksu");
            if (gksu != null) {
                currSu = gksu;
            }
        }
        if (currSu == null) {
            String kdesu = sysWhich.apply("kdesu");
            if (kdesu != null) {
                currSu = kdesu;
            }
        }
        if (currSu == null) {
            return NOptional.ofNamedEmpty("gui su application (kdesu,gksu,...)");
        }
        return NOptional.of(currSu);
    }

    private static NOptional<String> guiSudo(Set<NDesktopEnvironmentFamily> de, Function<String, String> sysWhich, NSession session) {
        if (de == null) {
            de = NEnvs.of(session).getDesktopEnvironmentFamilies();
        }
        String currSu = null;
        if (de.contains(NDesktopEnvironmentFamily.KDE)) {
            String kdesu = sysWhich.apply("kdesudo");
            if (kdesu != null) {
                currSu = kdesu;
            }
        } else if (de.contains(NDesktopEnvironmentFamily.GNOME)) {
            String gksu = sysWhich.apply("gksudo");
            if (gksu != null) {
                currSu = gksu;
            }
        }
        if (currSu == null) {
            String gksu = sysWhich.apply("gksudo");
            if (gksu != null) {
                currSu = gksu;
            }
        }
        if (currSu == null) {
            String kdesu = sysWhich.apply("kdesudo");
            if (kdesu != null) {
                currSu = kdesu;
            }
        }
        if (currSu == null) {
            return NOptional.ofNamedEmpty("gui su application (kdesudo,gksudo,...)");
        }
        return NOptional.of(currSu);
    }
}
