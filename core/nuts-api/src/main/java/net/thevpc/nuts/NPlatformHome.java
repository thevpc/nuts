package net.thevpc.nuts;

import net.thevpc.nuts.reserved.NApiUtilsRPI;
import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NStringUtils;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

public class NPlatformHome {
    public static final NPlatformHome USER = of(null);
    public static final NPlatformHome SYSTEM = ofSystem(null);
    private final NOsFamily platformOsFamily;
    private final boolean system;
    private final Function<String, String> env;
    private final Function<String, String> props;
    private final String wsPrefix = "ws";
    private final String sysPrefix = "system";


    public static NPlatformHome ofSystem(NOsFamily platformOsFamily, Function<String, String> env, Function<String, String> props) {
        return new NPlatformHome(platformOsFamily, true, env, props);
    }

    public static NPlatformHome of(NOsFamily platformOsFamily, Function<String, String> env, Function<String, String> props) {
        return new NPlatformHome(platformOsFamily, false, env, props);
    }

    public static NPlatformHome ofSystem(NOsFamily platformOsFamily) {
        return new NPlatformHome(platformOsFamily, true, null, null);
    }

    public static NPlatformHome of(NOsFamily platformOsFamily) {
        return new NPlatformHome(platformOsFamily, false, null, null);
    }

    public static NPlatformHome of(NOsFamily platformOsFamily, boolean system) {
        return new NPlatformHome(platformOsFamily, system, null, null);
    }

    public static NPlatformHome ofPortable(NOsFamily platformOsFamily, String userName) {
        return ofPortable(platformOsFamily, false, userName);
    }

    public static NPlatformHome ofPortableSystem(NOsFamily platformOsFamily, String userName) {
        return ofPortable(platformOsFamily, true, userName);
    }

    public static NPlatformHome ofPortable(NOsFamily platformOsFamily, boolean system, String userName) {
        NAssert.requireNonBlank(userName, "userName");
        return new NPlatformHome(platformOsFamily, system, p -> null, p -> portableProp(p, platformOsFamily, null, x -> {
            switch (x) {
                case "user.name":
                    return userName;
            }
            return null;
        }));
    }

    public static NPlatformHome ofPortable(NOsFamily platformOsFamily, boolean system, Function<String, String> env, Function<String, String> props) {
        return new NPlatformHome(platformOsFamily, system, p -> null, p -> portableProp(p, platformOsFamily, env, props));
    }


    private static String portableProp(String p, NOsFamily platformOsFamily, Function<String, String> env, Function<String, String> props) {
        NOsFamily osFamily = platformOsFamily == null ? NOsFamily.UNIX : platformOsFamily;
        switch (p) {
            case "user.name": {
                String userName = NAssert.requireNonBlank(props == null ? null : props.apply("user.name"), "user.name");
                return userName;
            }
            case "user.home": {
                String home=props == null ? null : props.apply("user.home");
                if(!NBlankable.isBlank(home)){
                    return home;
                }
                switch (osFamily) {
                    case WINDOWS: {
                        String userName = NAssert.requireNonBlank(props == null ? null : props.apply("user.name"), "user.name");
                        return "C:\\Users\\" + userName;
                    }
                    default: {
                        String userName = NAssert.requireNonBlank(props == null ? null : props.apply("user.name"), "user.name");
                        switch (userName) {
                            case "root":
                                return "/root";
                            default:
                                return "/home/" + userName;
                        }
                    }
                }
                //break;
            }
            case "java.io.tmpdir": {
                String temp=props == null ? null : props.apply("java.io.tmpdir");
                if(!NBlankable.isBlank(temp)){
                    return temp;
                }
                switch (osFamily) {
                    case WINDOWS: {
                        String userName = NAssert.requireNonBlank(props == null ? null : props.apply("user.name"), "user.name");
                        return "C:\\Users\\" + userName + "\\AppData\\Local\\Temp";
                    }
                    default: {
                        return "/tmp";
                    }
                }
                //break;
            }
        }
        return null;
    }


    public NPlatformHome(NOsFamily platformOsFamily, boolean system, Function<String, String> env, Function<String, String> props) {
        this.platformOsFamily = platformOsFamily != null ? platformOsFamily : NOsFamily.getCurrent();
        this.system = system;
        this.env = env != null ? env : System::getenv;
        this.props = props != null ? props : System::getProperty;
    }

    /**
     * resolves custom nuts home folder from {@code homeLocations}.
     * Home folder is the root for nuts folders.
     * It depends on folder type and store layout.
     *
     * @param location      folder type to resolve home for
     * @param homeLocations workspace home locations
     * @return home folder path or null
     */
    public String getCustomPlatformHomeFolder(NStoreType location, Map<NHomeLocation, String> homeLocations) {
        if (location == null) {
            return null;
        }
        String s;
        String locationName = location.id();
        s = NStringUtils.trim(props.apply("nuts.home." + locationName + "." + platformOsFamily.id()));
        if (!s.isEmpty()) {
            return s/* + "/" + workspaceName*/;
        }
        s = NStringUtils.trim(props.apply("nuts.export.home." + locationName + "." + platformOsFamily.id()));
        if (!s.isEmpty()) {
            return s/* + "/" + workspaceName*/;
        }
        if (homeLocations != null && homeLocations.size() > 0) {
            NHomeLocation key = NHomeLocation.of(platformOsFamily, location);
            s = NStringUtils.trim(homeLocations.get(key));
            if (!s.isEmpty()) {
                return s/* + "/" + workspaceName*/;
            }
            key = NHomeLocation.of(null, location);
            s = NStringUtils.trim(homeLocations.get(key));
            if (!s.isEmpty()) {
                return s /* + "/" + workspaceName*/;
            }
        }
        return null;
    }

    /**
     * resolves nuts home folder.Home folder is the root for nuts folders.It
     * depends on folder type and store layout. For instance log folder depends
     * on on the underlying operating system (linux,windows,...).
     * Specifications: XDG Base Directory Specification
     * (https://specifications.freedesktop.org/basedir-spec/basedir-spec-latest.html)
     *
     * @param location      folder type to resolve home for
     * @param homeLocations workspace home locations
     * @param workspaceName workspace name or id (discriminator)
     * @return home folder path
     * TODO : rename me
     */
    public String getWorkspaceLocation(NStoreType location, Map<NHomeLocation, String> homeLocations, String workspaceName) {
        if (location == null) {
            return getWorkspaceLocation(workspaceName);
        }
        String s = getCustomPlatformHomeFolder(location, homeLocations);
        if (s != null) {
            return s;
        }
        return getWorkspaceStore(location, workspaceName);
    }

    public String getWorkspaceLocation(String workspaceName) {
        if (NBlankable.isBlank(workspaceName)) {
            workspaceName = NConstants.Names.DEFAULT_WORKSPACE_NAME;
        } else if (workspaceName.equals(".") || workspaceName.equals("..") || workspaceName.indexOf('/') >= 0 || workspaceName.indexOf('\\') >= 0) {
            //this is a path!
            //return it as is and make it absolute
            return Paths.get(workspaceName).normalize().toAbsolutePath().toString();
        }
        return getHome() + getNativePath("/ws/" + workspaceName);
    }

    public String getHome() {
        if (system) {
            switch (platformOsFamily) {
                case WINDOWS: {
                    return getWindowsProgramFiles() + "\\nuts";
                }
                default: {
                    return "/etc/opt/nuts";
                }
            }
        } else {
            String userHome = props.apply("user.home");
            return userHome + getNativePath("/.nuts");
        }
    }

    /**
     * resolves nuts home folder.Home folder is the root for nuts folders.It
     * depends on folder type and store layout. For instance log folder depends
     * on on the underlying operating system (linux,windows,...).
     * Specifications: XDG Base Directory Specification
     * (https://specifications.freedesktop.org/basedir-spec/basedir-spec-latest.html)
     *
     * @param location      folder type to resolve home for
     * @param workspaceName workspace name or id (discriminator)
     * @return home folder path
     */
    public String getWorkspaceStore(NStoreType location, String workspaceName) {
        if (location == null) {
            return getWorkspaceLocation(workspaceName);
        }
        if (NBlankable.isBlank(workspaceName)) {
            workspaceName = NConstants.Names.DEFAULT_WORKSPACE_NAME;
        } else {
            Path fileName = Paths.get(workspaceName).normalize().toAbsolutePath().getFileName();
            if (fileName == null) {
                //this happens when workspaceName='/' in that case use NutsConstants.Names.DEFAULT_WORKSPACE_NAME
                workspaceName = NConstants.Names.DEFAULT_WORKSPACE_NAME;
            } else {
                workspaceName = fileName.toString();
            }
        }
        return getStore(location) + getNativePath("/ws/" + getNativePath(workspaceName));
    }

    public String getStore(NStoreType location) {
        if (location == null) {
            return getHome();
        }
        NOsFamily platformOsFamily = this.platformOsFamily;
        if (platformOsFamily == null) {
            platformOsFamily = NOsFamily.getCurrent();
        }
        String locationName = location.id();
        if (system) {
            String s = null;
            s = NStringUtils.trim(props.apply("nuts.store.system." + locationName + "." + platformOsFamily.id()));
            if (!s.isEmpty()) {
                return s;
            }
            s = NStringUtils.trim(props.apply("nuts.export.store.system." + locationName + "." + platformOsFamily.id()));
            if (!s.isEmpty()) {
                return s.trim();
            }
            switch (location) {
                case BIN: {
                    switch (platformOsFamily) {
                        case WINDOWS: {
                            return getWindowsProgramFiles() + "\\nuts\\" + locationName;
                        }
                        default: {
                            return "/opt/nuts/" + locationName;
                        }
                    }
                }
                case LIB: {
                    switch (platformOsFamily) {
                        case WINDOWS: {
                            return getWindowsProgramFiles() + "\\nuts\\" + locationName;
                        }
                        default: {
                            return "/opt/nuts/" + locationName;
                        }
                    }
                }
                case CONF: {
                    switch (platformOsFamily) {
                        case WINDOWS: {
                            return getWindowsProgramFiles() + "\\nuts\\" + locationName;
                        }
                        default: {
                            return "/etc/opt/nuts/" + locationName;
                        }
                    }
                }
                case LOG: {
                    switch (platformOsFamily) {
                        case WINDOWS: {
                            return getWindowsProgramFiles() + "\\nuts\\" + locationName;
                        }
                        default: {
                            return "/var/log/nuts";
                        }
                    }
                }
                case CACHE: {
                    switch (platformOsFamily) {
                        case WINDOWS: {
                            return getWindowsProgramFiles() + "\\nuts\\" + locationName;
                        }
                        default: {
                            return "/var/cache/nuts";
                        }
                    }
                }
                case VAR: {
                    switch (platformOsFamily) {
                        case WINDOWS: {
                            return getWindowsProgramFiles() + "\\nuts\\" + locationName;
                        }
                        default: {
                            return "/var/opt/nuts";
                        }
                    }
                }
                case TEMP: {
                    switch (platformOsFamily) {
                        case WINDOWS: {
                            String pf = env.apply("TMP");
                            if (NBlankable.isBlank(pf)) {
                                pf = getWindowsSystemRoot() + "\\Temp";
                            }
                            return pf + "\\nuts";
                        }
                        default: {
                            return "/tmp/nuts/" + sysPrefix;
                        }
                    }
                }
                case RUN: {
                    switch (platformOsFamily) {
                        case WINDOWS: {
                            String pf = env.apply("TMP");
                            if (NBlankable.isBlank(pf)) {
                                pf = getWindowsSystemRoot() + "\\Temp";
                            }
                            return pf + "\\nuts\\run";
                        }
                        default: {
                            return "/tmp/run/nuts/" + sysPrefix;
                        }
                    }
                }
            }
        } else {
            String userHome = props.apply("user.home");
            String userName = props.apply("user.name");
            switch (location) {
                case VAR:
                case BIN:
                case LIB: {
                    switch (platformOsFamily) {
                        case WINDOWS: {
                            return userHome + getNativePath("/AppData/Roaming/nuts/" + locationName);
                        }
                        default: {
                            String val = NStringUtils.trim(env.apply("XDG_DATA_HOME"));
                            if (!val.isEmpty()) {
                                return val + "/nuts/" + locationName;
                            }
                            return userHome + "/.local/share/nuts/" + locationName;
                        }
                    }
                }
                case LOG: {
                    switch (platformOsFamily) {
                        case WINDOWS: {
                            return userHome + getNativePath("/AppData/LocalLow/nuts/" + locationName);
                        }
                        default: {
                            String val = NStringUtils.trim(env.apply("XDG_LOG_HOME"));
                            if (!val.isEmpty()) {
                                return val + "/nuts";
                            }
                            return userHome + "/.local/log/nuts";
                        }
                    }
                }
                case RUN: {
                    switch (platformOsFamily) {
                        case WINDOWS: {
                            return userHome + getNativePath("/AppData/Local/nuts/" + locationName);
                        }
                        default: {
                            String val = NStringUtils.trim(env.apply("XDG_RUNTIME_DIR"));
                            if (!val.isEmpty()) {
                                return val + "/nuts";
                            }
                            return userHome + "/.local/run/nuts";
                        }
                    }
                }
                case CONF: {
                    switch (platformOsFamily) {
                        case WINDOWS: {
                            return userHome + getNativePath("/AppData/Roaming/nuts/" + locationName);
                        }
                        default: {
                            String val = NStringUtils.trim(env.apply("XDG_CONFIG_HOME"));
                            if (!val.isEmpty()) {
                                return val + "/nuts";
                            }
                            return userHome + "/.config/nuts";
                        }
                    }
                }
                case CACHE: {
                    switch (platformOsFamily) {
                        case WINDOWS: {
                            return userHome + getNativePath("/AppData/Local/nuts/cache");
                        }
                        default: {
                            String val = NStringUtils.trim(env.apply("XDG_CACHE_HOME"));
                            if (!val.isEmpty()) {
                                return val + "/nuts";
                            }
                            return userHome + "/.cache/nuts";
                        }
                    }
                }
                case TEMP: {
                    switch (platformOsFamily) {
                        case WINDOWS:
                            return userHome + getNativePath("/AppData/Local/nuts/" + locationName);
                        default:
                            //on macos/unix/linux temp folder is shared. will add user folder as discriminator
                            return props.apply("java.io.tmpdir") + getNativePath("/" + userName + "/nuts");
                    }
                }
            }
        }
        throw new NIllegalArgumentException(NMsg.ofC("unsupported getDefaultPlatformHomeFolderBase %s/%s", platformOsFamily, location));
    }

    public String getWindowsProgramFiles() {
        String s = env.apply("ProgramFiles");
        if (!NBlankable.isBlank(s)) {
            return s;
        }
        String c = getWindowsSystemDrive();
        if (!NBlankable.isBlank(c)) {
            return c + "\\Program Files";
        }
        return "C:\\Program Files";
    }

    public String getWindowsProgramFilesX86() {
        String s = env.apply("ProgramFiles(x86)");
        if (!NBlankable.isBlank(s)) {
            return s;
        }
        String c = getWindowsSystemDrive();
        if (!NBlankable.isBlank(c)) {
            return c + "\\Program Files (x86)";
        }
        return "C:\\Program Files (x86)";
    }


    public String getWindowsSystemRoot() {
        String e;
        e = env.apply("SystemRoot");
        if (!NBlankable.isBlank(e)) {
            return e;
        }
        e = env.apply("windir");
        if (!NBlankable.isBlank(e)) {
            return e;
        }
        e = env.apply("SystemDrive");
        if (!NBlankable.isBlank(e)) {
            return e + "\\Windows";
        }
        return "C:\\Windows";
    }

    public String getWindowsSystemDrive() {
        String e = env.apply("SystemDrive");
        if (!NBlankable.isBlank(e)) {
            return e;
        }
        e = env.apply("SystemRoot");
        if (!NBlankable.isBlank(e)) {
            return e.substring(0, 2);
        }
        e = env.apply("windir");
        if (!NBlankable.isBlank(e)) {
            return e.substring(0, 2);
        }
        return null;
    }

    private String getNativePath(String s) {
        switch (platformOsFamily) {
            case WINDOWS:
                return s.replace('/', '\\');
        }
        return s.replace('\\', '/');
    }

    /**
     * @param storeStrategy     storeStrategy or null
     * @param baseLocations     baseLocations or null
     * @param homeLocations     homeLocations or null
     * @param workspaceLocation workspaceName or null
     * @return locations map
     */
    public Map<NStoreType, String> buildLocations(
            NStoreStrategy storeStrategy,
            Map<NStoreType, String> baseLocations,
            Map<NHomeLocation, String> homeLocations,
            String workspaceLocation) {
        workspaceLocation = getWorkspaceLocation(workspaceLocation);
        String[] homes = new String[NStoreType.values().length];
        for (NStoreType location : NStoreType.values()) {
            String platformHomeFolder = getWorkspaceLocation(location, homeLocations, workspaceLocation);
            if (NBlankable.isBlank(platformHomeFolder)) {
                throw new NIllegalArgumentException(NMsg.ofC("missing Home for %s", location.id()));
            }
            homes[location.ordinal()] = platformHomeFolder;
        }
        if (storeStrategy == null) {
            storeStrategy = NStoreStrategy.EXPLODED;
        }
        Map<NStoreType, String> storeLocations = new LinkedHashMap<>();
        if (baseLocations != null) {
            for (Map.Entry<NStoreType, String> e : baseLocations.entrySet()) {
                NStoreType loc = e.getKey();
                if (loc == null) {
                    throw new NIllegalArgumentException(NMsg.ofPlain("null location"));
                }
                storeLocations.put(loc, e.getValue());
            }
        }
        for (NStoreType location : NStoreType.values()) {
            String _storeLocation = storeLocations.get(location);
            if (NBlankable.isBlank(_storeLocation)) {
                switch (storeStrategy) {
                    case STANDALONE: {
                        String c = getCustomPlatformHomeFolder(location, homeLocations);
                        storeLocations.put(location, c == null ? (workspaceLocation + File.separator + location.id()) : c);
                        break;
                    }
                    case EXPLODED: {
                        storeLocations.put(location, homes[location.ordinal()]);
                        break;
                    }
                }
            } else if (!Paths.get(_storeLocation).isAbsolute()) {
                switch (storeStrategy) {
                    case STANDALONE: {
                        String c = getCustomPlatformHomeFolder(location, homeLocations);
                        storeLocations.put(location, c == null ?
                                (workspaceLocation + File.separator + location.id() + NApiUtilsRPI.getNativePath("/" + _storeLocation))
                                :
                                (c + NApiUtilsRPI.getNativePath("/" + _storeLocation)));
                        break;
                    }
                    case EXPLODED: {
                        storeLocations.put(location, homes[location.ordinal()] + NApiUtilsRPI.getNativePath("/" + _storeLocation));
                        break;
                    }
                }
            } else {
                storeLocations.put(location, _storeLocation);
            }
        }
        return storeLocations;
    }
}
