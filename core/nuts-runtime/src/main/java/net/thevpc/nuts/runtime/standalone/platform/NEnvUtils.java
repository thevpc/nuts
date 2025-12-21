package net.thevpc.nuts.runtime.standalone.platform;

import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.artifact.NIdBuilder;
import net.thevpc.nuts.command.NExec;
import net.thevpc.nuts.core.NWorkspace;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.platform.NDesktopEnvironmentFamily;
import net.thevpc.nuts.platform.NDesktopIntegrationItem;
import net.thevpc.nuts.platform.NEnv;
import net.thevpc.nuts.platform.NShellFamily;
import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NStringUtils;
import net.thevpc.nuts.util.NSupportMode;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class NEnvUtils {
    public static final Pattern UNIX_USER_DIRS_PATTERN = Pattern.compile("^\\s*(?<k>[A-Z_]+)\\s*=\\s*(?<v>.*)$");

    public static NId[] getDesktopEnvironmentsXDGOrEmpty(NEnv env) {
        String _XDG_SESSION_DESKTOP = env.getEnv("XDG_SESSION_DESKTOP").orNull();
        String _XDG_CURRENT_DESKTOP = env.getEnv("XDG_CURRENT_DESKTOP").orNull();
        List<NId> a = new ArrayList<>();
        if (!NBlankable.isBlank(_XDG_SESSION_DESKTOP) && !NBlankable.isBlank(_XDG_SESSION_DESKTOP)) {
            String[] supportedSessions = new LinkedHashSet<>(
                    Arrays.stream(NStringUtils.trim(_XDG_CURRENT_DESKTOP).split(":"))
                            .map(x -> x.trim().toLowerCase()).filter(x -> x.length() > 0)
                            .collect(Collectors.toList())
            ).toArray(new String[0]);
            String sd = _XDG_SESSION_DESKTOP.toLowerCase();
            for (int i = 0; i < supportedSessions.length; i++) {
                NIdBuilder nb = NIdBuilder.of().setArtifactId(supportedSessions[i]);
                if ("kde".equals(sd)) {
                    String _KDE_FULL_SESSION = env.getEnv("KDE_FULL_SESSION").orNull();
                    String _KDE_SESSION_VERSION = env.getEnv("KDE_SESSION_VERSION").orNull();
                    if (_KDE_FULL_SESSION != null && "true".equals(_KDE_FULL_SESSION.trim())) {
                        nb.setProperty("full", "true");
                    }
                    if (_KDE_SESSION_VERSION != null) {
                        nb.setProperty("version", _KDE_SESSION_VERSION.trim());
                    }
                }
                String _XDG_SESSION_TYPE = env.getEnv("XDG_SESSION_TYPE").orNull();
                String _XSESSION_IS_UP = env.getEnv("XSESSION_IS_UP").orNull();
                String _XDG_SESSION_CLASS = env.getEnv("XDG_SESSION_CLASS").orNull();
                if (_XDG_SESSION_TYPE != null) {
                    nb.setProperty("type", _XDG_SESSION_TYPE.trim().toLowerCase());
                }
                if (_XDG_SESSION_CLASS != null) {
                    nb.setProperty("class", _XDG_SESSION_CLASS.trim().toLowerCase());
                }
                a.add(nb.build());
            }
        }
        return a.toArray(new NId[0]);
    }

    public static Set<NId> getDesktopEnvironments0(NEnv env) {
        if (!env.isGraphicalDesktopEnvironment()) {
            return Collections.singleton(
                    NIdBuilder.of().setArtifactId(NDesktopEnvironmentFamily.HEADLESS.id()).build());
        }
        switch (NEnv.of().getOsFamily()) {
            case WINDOWS: {
                return Collections.singleton(NIdBuilder.of().setArtifactId(NDesktopEnvironmentFamily.WINDOWS_SHELL.id()).build());
            }
            case MACOS: {
                return Collections.singleton(NIdBuilder.of().setArtifactId(NDesktopEnvironmentFamily.MACOS_AQUA.id()).build());
            }
            case UNIX:
            case LINUX: {
                NId[] all = getDesktopEnvironmentsXDGOrEmpty(env);
                if (all.length == 0) {
                    return Collections.singleton(NIdBuilder.of().setArtifactId(NDesktopEnvironmentFamily.UNKNOWN.id()).build());
                }
                return Collections.unmodifiableSet(new LinkedHashSet<>(Arrays.asList(all)));
            }
            default: {
                return Collections.singleton(NIdBuilder.of().setArtifactId(NDesktopEnvironmentFamily.UNKNOWN.id()).build());
            }
        }
    }


    public static Set<NShellFamily> getShellFamilies(
            NEnv env,
            boolean allEvenNonInstalled) {
        ArrayList<NShellFamily> shellFamilies = new ArrayList<>();
        switch (env.getOsFamily()) {
            case UNIX:
            case LINUX:
            case MACOS: {
                LinkedHashSet<NShellFamily> families = new LinkedHashSet<>();
                families.add(env.getShellFamily());
                //add bash with existing rc
                NShellFamily[] all = {
                        NShellFamily.SH,
                        NShellFamily.BASH,
                        NShellFamily.ZSH,
                        NShellFamily.CSH,
                        NShellFamily.KSH,
                        NShellFamily.FISH
                };
                for (NShellFamily f : all) {
                    if (f != null) {
                        Path path = Paths.get("/bin").resolve(f.id());
                        if (Files.exists(path)) {
                            families.add(f);
                        }
                    }
                }
                if (allEvenNonInstalled) {
                    families.addAll(Arrays.asList(all));
                }
                shellFamilies.addAll(families);
                break;
            }
            case WINDOWS: {
                LinkedHashSet<NShellFamily> families = new LinkedHashSet<>();
                families.add(env.getShellFamily());
                //add bash with existing rc
                families.add(NShellFamily.WIN_CMD);
                if (env.getOs().getVersion().compareTo("7") >= 0) {
                    families.add(NShellFamily.WIN_POWER_SHELL);
                }
                shellFamilies.addAll(families);

                break;
            }
            default: {
                shellFamilies.add(NShellFamily.UNKNOWN);
            }
        }
        return new LinkedHashSet<>(shellFamilies);
    }

    public static NSupportMode getDesktopIntegrationSupport(NEnv env, NDesktopIntegrationItem item) {
        NAssert.requireNonBlank(item, "item");

        switch (item) {
            case DESKTOP: {
                NSupportMode a = NWorkspace.of().getBootOptions().getDesktopLauncher().orNull();
                if (a != null) {
                    return a;
                }
                break;
            }
            case MENU: {
                NSupportMode a = NWorkspace.of().getBootOptions().getMenuLauncher().orNull();
                if (a != null) {
                    return a;
                }
                break;
            }
            case USER: {
                NSupportMode a = NWorkspace.of().getBootOptions().getUserLauncher().orNull();
                if (a != null) {
                    return a;
                }
                break;
            }
        }
        switch (env.getOsFamily()) {
            case LINUX: {
                switch (item) {
                    case DESKTOP: {
                        return NSupportMode.SUPPORTED;
                    }
                    case MENU: {
                        return NSupportMode.PREFERRED;
                    }
                    case USER: {
                        return NSupportMode.PREFERRED;
                    }
                }
                break;
            }
            case UNIX: {
                return NSupportMode.NEVER;
            }
            case WINDOWS: {
                switch (item) {
                    case DESKTOP: {
                        if (Files.isDirectory(getDesktopPath(env))) {
                            return NSupportMode.PREFERRED;
                        }
                        return NSupportMode.SUPPORTED;
                    }
                    case MENU: {
                        return NSupportMode.PREFERRED;
                    }
                    case USER: {
                        return NSupportMode.PREFERRED;
                    }
                }
                break;
            }
            case MACOS: {
                return NSupportMode.NEVER;
            }
            case UNKNOWN: {
                return NSupportMode.NEVER;
            }
        }
        return NSupportMode.NEVER;
    }

    public static Path getDesktopPath(NEnv env) {
        switch (env.getOsFamily()) {
            case LINUX:
            case UNIX:
            case MACOS: {
                File f = new File(System.getProperty("user.home"), ".config/user-dirs.dirs");
                if (f.exists()) {
                    try (BufferedReader r = new BufferedReader(new FileReader(f))) {
                        String line;
                        while ((line = r.readLine()) != null) {
                            line = line.trim();
                            if (line.startsWith("#")) {
                                //ignore
                            } else {
                                Matcher m = UNIX_USER_DIRS_PATTERN.matcher(line);
                                if (m.find()) {
                                    String k = m.group("k");
                                    if (k.equals("XDG_DESKTOP_DIR")) {
                                        String v = m.group("v");
                                        v = v.trim();
                                        if (v.startsWith("\"")) {
                                            int last = v.indexOf('\"', 1);
                                            String s = v.substring(1, last);
                                            s = s.replace("$HOME", System.getProperty("user.home"));
                                            return Paths.get(s);
                                        } else {
                                            return Paths.get(v);
                                        }
                                    }
                                } else {
                                    //this is unexpected format!
                                    break;
                                }
                            }
                        }
                    } catch (IOException ex) {
                        //ignore
                    }
                }
                return new File(System.getProperty("user.home"), "Desktop").toPath();
            }
            case WINDOWS: {
                return new File(System.getProperty("user.home"), "Desktop").toPath();
            }
            default: {
                return new File(System.getProperty("user.home"), "Desktop").toPath();
            }
        }
    }

    public static String getHostName(NEnv env) {
        String hostName=null;
        switch (env.getOsFamily()) {
            case WINDOWS: {
                String computername = NEnv.of().getEnv("COMPUTERNAME").orNull();
                if (computername != null) {
                    hostName = computername;
                } else {
                    try {
                        String hostname = NExec.of(
                                ).addCommand("hostname")
                                .failFast()
                                .getGrabbedOutOnlyString();
                        hostName = NStringUtils.trim(hostname);
                    } catch (Exception any) {
                        //
                    }
                }
                if (hostName == null) {
                    hostName = "";
                }
                break;
            }
            case UNIX:
            case LINUX:
            case MACOS:
            default: {
                String h = null;
                try {
                    h = NStringUtils.trim(NPath.of("/etc/hostname")
                            .readString());
                } catch (Exception e) {
                    //ignore
                }
                if (NBlankable.isBlank(h)) {
                    h = NExec.of()
                            .system()
                            .addCommand("/bin/hostname")
                            .getGrabbedOutOnlyString();
                }
                hostName = NStringUtils.trim(h);
                break;
            }
        }
        return hostName;
    }

}
