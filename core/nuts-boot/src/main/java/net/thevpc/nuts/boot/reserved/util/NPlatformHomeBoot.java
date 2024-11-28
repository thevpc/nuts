package net.thevpc.nuts.boot.reserved.util;

import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.boot.NBootException;
import net.thevpc.nuts.boot.NHomeLocationBoot;
import net.thevpc.nuts.boot.reserved.NAssertBoot;
import net.thevpc.nuts.boot.reserved.NMsgBoot;

import java.io.File;
import java.lang.String;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

public class NPlatformHomeBoot {
    public static final NPlatformHomeBoot USER = of(null);
    public static final NPlatformHomeBoot SYSTEM = ofSystem(null);
    private final String platformOsFamily;
    private final boolean system;
    private final Function<String, String> env;
    private final Function<String, String> props;
    private final String wsPrefix = "ws";
    private final String sysPrefix = "system";


    public static NPlatformHomeBoot ofSystem(String platformOsFamily, Function<String, String> env, Function<String, String> props) {
        return new NPlatformHomeBoot(platformOsFamily, true, env, props);
    }

    public static NPlatformHomeBoot of(String platformOsFamily, Function<String, String> env, Function<String, String> props) {
        return new NPlatformHomeBoot(platformOsFamily, false, env, props);
    }

    public static NPlatformHomeBoot ofSystem(String platformOsFamily) {
        return new NPlatformHomeBoot(platformOsFamily, true, null, null);
    }

    public static NPlatformHomeBoot of(String platformOsFamily) {
        return new NPlatformHomeBoot(platformOsFamily, false, null, null);
    }

    public static NPlatformHomeBoot of(String platformOsFamily, boolean system) {
        return new NPlatformHomeBoot(platformOsFamily, system, null, null);
    }

    public static NPlatformHomeBoot ofPortable(String platformOsFamily, String userName) {
        return ofPortable(platformOsFamily, false, userName);
    }

    public static NPlatformHomeBoot ofPortableSystem(String platformOsFamily, String userName) {
        return ofPortable(platformOsFamily, true, userName);
    }

    public static NPlatformHomeBoot ofPortable(String platformOsFamily, boolean system, String userName) {
        NAssertBoot.requireNonBlank(userName, "userName");
        return new NPlatformHomeBoot(platformOsFamily, system, p -> null, p -> portableProp(p, platformOsFamily, null, x -> {
            switch (x) {
                case "user.name":
                    return userName;
            }
            return null;
        }));
    }

    public static NPlatformHomeBoot ofPortable(String platformOsFamily, boolean system, Function<String, String> env, Function<String, String> props) {
        return new NPlatformHomeBoot(platformOsFamily, system, p -> null, p -> portableProp(p, platformOsFamily, env, props));
    }


    private static String portableProp(String p, String platformOsFamily, Function<String, String> env, Function<String, String> props) {
        String osFamily = NNameFormatBoot.CONST_NAME.format(NStringUtilsBoot.firstNonBlank(platformOsFamily,"UNIX"));
        switch (p) {
            case "user.name": {
                String userName = NAssertBoot.requireNonBlank(props == null ? null : props.apply("user.name"), "user.name");
                return userName;
            }
            case "user.home": {
                String home=props == null ? null : props.apply("user.home");
                if(!NStringUtilsBoot.isBlank(home)){
                    return home;
                }
                switch (osFamily) {
                    case "WINDOWS": {
                        String userName = NAssertBoot.requireNonBlank(props == null ? null : props.apply("user.name"), "user.name");
                        return "C:\\Users\\" + userName;
                    }
                    default: {
                        String userName = NAssertBoot.requireNonBlank(props == null ? null : props.apply("user.name"), "user.name");
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
                if(!NStringUtilsBoot.isBlank(temp)){
                    return temp;
                }
                switch (osFamily) {
                    case "WINDOWS": {
                        String userName = NAssertBoot.requireNonBlank(props == null ? null : props.apply("user.name"), "user.name");
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


    public NPlatformHomeBoot(String platformOsFamily, boolean system, Function<String, String> env, Function<String, String> props) {
        this.platformOsFamily = platformOsFamily != null ? platformOsFamily : currentOsFamily();
        this.system = system;
        this.env = env != null ? env : System::getenv;
        this.props = props != null ? props : System::getProperty;
    }

    /**
     * resolves custom nuts home folder from {@code homeLocations}.
     * Home folder is the root for nuts folders.
     * It depends on folder type and store layout.
     *
     * @param storeType      folder type to resolve home for
     * @param homeLocations workspace home locations
     * @return home folder path or null
     */
    public String getCustomPlatformHomeFolder(String storeType, Map<NHomeLocationBoot, String> homeLocations) {
        if (storeType == null) {
            return null;
        }
        String s;
        String locationName = NNameFormatBoot.LOWER_KEBAB_CASE.format(storeType);
        s = NStringUtilsBoot.trim(props.apply("nuts.home." + locationName + "." + NNameFormatBoot.LOWER_KEBAB_CASE.format(platformOsFamily)));
        if (!s.isEmpty()) {
            return s/* + "/" + workspaceName*/;
        }
        s = NStringUtilsBoot.trim(props.apply("nuts.export.home." + locationName + "." + NNameFormatBoot.LOWER_KEBAB_CASE.format(platformOsFamily)));
        if (!s.isEmpty()) {
            return s/* + "/" + workspaceName*/;
        }
        if (homeLocations != null && homeLocations.size() > 0) {
            NHomeLocationBoot key = NHomeLocationBoot.of(platformOsFamily, storeType);
            s = NStringUtilsBoot.trim(homeLocations.get(key));
            if (!s.isEmpty()) {
                return s/* + "/" + workspaceName*/;
            }
            key = NHomeLocationBoot.of(null, storeType);
            s = NStringUtilsBoot.trim(homeLocations.get(key));
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
    public String getWorkspaceLocation(String location, Map<NHomeLocationBoot, String> homeLocations, String workspaceName) {
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
        if (NStringUtilsBoot.isBlank(workspaceName)) {
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
            switch (NNameFormatBoot.CONST_NAME.format(platformOsFamily)) {
                case "WINDOWS": {
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
     * @param storeType      folder type to resolve home for
     * @param workspaceName workspace name or id (discriminator)
     * @return home folder path
     */
    public String getWorkspaceStore(String storeType, String workspaceName) {
        if (storeType == null) {
            return getWorkspaceLocation(workspaceName);
        }
        if (NStringUtilsBoot.isBlank(workspaceName)) {
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
        return getStore(storeType) + getNativePath("/ws/" + getNativePath(workspaceName));
    }

    public static String currentOsFamily() {
        return NStringUtilsBoot.firstNonBlank(parseOsFamily(System.getProperty("os.name")),"UNKNOWN");
    }

    public static String parseOsFamily(java.lang.String value) {
        java.lang.String e = NNameFormatBoot.CONST_NAME.format(value);
        switch (e) {
            case "W":
            case "WIN":
            case "WINDOWS":
                return "WINDOWS";
            case "L":
            case "LINUX":
                return "LINUX";
            case "M":
            case "MAC":
            case "MACOS":
                return "MACOS";
            case "U":
            case "UNIX":
                return "UNIX";
            case "unknown":
                return "UNKNOWN";
        }
        if (e.startsWith("LINUX")) {
            return "LINUX";
        }
        if (e.startsWith("WIN")) {
            return "WINDOWS";
        }
        if (e.startsWith("MAC")) {
            return "MACOS";
        }
        if (e.startsWith("SUNOS") || e.startsWith("SUN_OS")) {
            return "UNIX";
        }
        if (e.startsWith("FREEBSD") || e.startsWith("FREE_BSD")) {
            return "UNIX";
        }
        //process plexus os families
        switch (e) {
            case "DOS":
            case "MSDOS":
            case "MS_DOS":
                return "WINDOWS";
            case "NETWARE":
            case "NET_WARE":
                return "UNKNOWN";
            case "OS2":
            case "OS_2":
                return "UNKNOWN";
            case "TANDEM":
                return "UNKNOWN";
            case "Z_OS":
            case "ZOS":
                return "UNKNOWN";
            case "OS400":
            case "OS_400":
                return "UNIX";
            case "OPENVMS":
            case "OPEN_VMS":
                return "UNKNOWN";
        }
        return null;    }

    public String getStore(String location) {
        if (location == null) {
            return getHome();
        }
        String platformOsFamilyId = NUtilsBoot.enumId(this.platformOsFamily==null?currentOsFamily(): this.platformOsFamily);
        String locationId = NUtilsBoot.enumId(location);
        if (system) {
            String s = null;
            s = NStringUtilsBoot.trim(props.apply("nuts.store.system." + locationId + "." + platformOsFamilyId));
            if (!s.isEmpty()) {
                return s;
            }
            s = NStringUtilsBoot.trim(props.apply("nuts.export.store.system." + locationId + "." + platformOsFamilyId));
            if (!s.isEmpty()) {
                return s.trim();
            }
            switch (locationId) {
                case "bin": {
                    switch (platformOsFamilyId) {
                        case "windows": {
                            return getWindowsProgramFiles() + "\\nuts\\" + locationId;
                        }
                        default: {
                            return "/opt/nuts/" + locationId;
                        }
                    }
                }
                case "lib": {
                    switch (platformOsFamilyId) {
                        case "windows": {
                            return getWindowsProgramFiles() + "\\nuts\\" + locationId;
                        }
                        default: {
                            return "/opt/nuts/" + locationId;
                        }
                    }
                }
                case "conf": {
                    switch (platformOsFamilyId) {
                        case "windows": {
                            return getWindowsProgramFiles() + "\\nuts\\" + locationId;
                        }
                        default: {
                            return "/etc/opt/nuts/" + locationId;
                        }
                    }
                }
                case "log": {
                    switch (platformOsFamilyId) {
                        case "windows": {
                            return getWindowsProgramFiles() + "\\nuts\\" + locationId;
                        }
                        default: {
                            return "/var/log/nuts";
                        }
                    }
                }
                case "cache": {
                    switch (platformOsFamilyId) {
                        case "windows": {
                            return getWindowsProgramFiles() + "\\nuts\\" + locationId;
                        }
                        default: {
                            return "/var/cache/nuts";
                        }
                    }
                }
                case "var": {
                    switch (platformOsFamilyId) {
                        case "windows": {
                            return getWindowsProgramFiles() + "\\nuts\\" + locationId;
                        }
                        default: {
                            return "/var/opt/nuts";
                        }
                    }
                }
                case "temp": {
                    switch (platformOsFamilyId) {
                        case "windows": {
                            String pf = env.apply("TMP");
                            if (NStringUtilsBoot.isBlank(pf)) {
                                pf = getWindowsSystemRoot() + "\\Temp";
                            }
                            return pf + "\\nuts";
                        }
                        default: {
                            return "/tmp/nuts/" + sysPrefix;
                        }
                    }
                }
                case "run": {
                    switch (platformOsFamilyId) {
                        case "windows": {
                            String pf = env.apply("TMP");
                            if (NStringUtilsBoot.isBlank(pf)) {
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
            switch (locationId) {
                case "var":
                case "bin":
                case "lib": {
                    switch (platformOsFamilyId) {
                        case "windows": {
                            return userHome + getNativePath("/AppData/Roaming/nuts/" + locationId);
                        }
                        default: {
                            String val = NStringUtilsBoot.trim(env.apply("XDG_DATA_HOME"));
                            if (!val.isEmpty()) {
                                return val + "/nuts/" + locationId;
                            }
                            return userHome + "/.local/share/nuts/" + locationId;
                        }
                    }
                }
                case "log": {
                    switch (platformOsFamilyId) {
                        case "windows": {
                            return userHome + getNativePath("/AppData/LocalLow/nuts/" + locationId);
                        }
                        default: {
                            String val = NStringUtilsBoot.trim(env.apply("XDG_LOG_HOME"));
                            if (!val.isEmpty()) {
                                return val + "/nuts";
                            }
                            return userHome + "/.local/log/nuts";
                        }
                    }
                }
                case "run": {
                    switch (platformOsFamilyId) {
                        case "windows": {
                            return userHome + getNativePath("/AppData/Local/nuts/" + locationId);
                        }
                        default: {
                            String val = NStringUtilsBoot.trim(env.apply("XDG_RUNTIME_DIR"));
                            if (!val.isEmpty()) {
                                return val + "/nuts";
                            }
                            return userHome + "/.local/run/nuts";
                        }
                    }
                }
                case "conf": {
                    switch (platformOsFamilyId) {
                        case "windows": {
                            return userHome + getNativePath("/AppData/Roaming/nuts/" + locationId);
                        }
                        default: {
                            String val = NStringUtilsBoot.trim(env.apply("XDG_CONFIG_HOME"));
                            if (!val.isEmpty()) {
                                return val + "/nuts";
                            }
                            return userHome + "/.config/nuts";
                        }
                    }
                }
                case "cache": {
                    switch (platformOsFamilyId) {
                        case "windows": {
                            return userHome + getNativePath("/AppData/Local/nuts/cache");
                        }
                        default: {
                            String val = NStringUtilsBoot.trim(env.apply("XDG_CACHE_HOME"));
                            if (!val.isEmpty()) {
                                return val + "/nuts";
                            }
                            return userHome + "/.cache/nuts";
                        }
                    }
                }
                case "temp": {
                    switch (platformOsFamilyId) {
                        case "windows":
                            return userHome + getNativePath("/AppData/Local/nuts/" + locationId);
                        default:
                            //on macos/unix/linux temp folder is shared. will add user folder as discriminator
                            return props.apply("java.io.tmpdir") + getNativePath("/" + userName + "/nuts");
                    }
                }
            }
        }
        throw new NBootException(NMsgBoot.ofC("unsupported getDefaultPlatformHomeFolderBase %s/%s", platformOsFamilyId, location));
    }

    public String getWindowsProgramFiles() {
        String s = env.apply("ProgramFiles");
        if (!NStringUtilsBoot.isBlank(s)) {
            return s;
        }
        String c = getWindowsSystemDrive();
        if (!NStringUtilsBoot.isBlank(c)) {
            return c + "\\Program Files";
        }
        return "C:\\Program Files";
    }

    public String getWindowsProgramFilesX86() {
        String s = env.apply("ProgramFiles(x86)");
        if (!NStringUtilsBoot.isBlank(s)) {
            return s;
        }
        String c = getWindowsSystemDrive();
        if (!NStringUtilsBoot.isBlank(c)) {
            return c + "\\Program Files (x86)";
        }
        return "C:\\Program Files (x86)";
    }


    public String getWindowsSystemRoot() {
        String e;
        e = env.apply("SystemRoot");
        if (!NStringUtilsBoot.isBlank(e)) {
            return e;
        }
        e = env.apply("windir");
        if (!NStringUtilsBoot.isBlank(e)) {
            return e;
        }
        e = env.apply("SystemDrive");
        if (!NStringUtilsBoot.isBlank(e)) {
            return e + "\\Windows";
        }
        return "C:\\Windows";
    }

    public String getWindowsSystemDrive() {
        String e = env.apply("SystemDrive");
        if (!NStringUtilsBoot.isBlank(e)) {
            return e;
        }
        e = env.apply("SystemRoot");
        if (!NStringUtilsBoot.isBlank(e)) {
            return e.substring(0, 2);
        }
        e = env.apply("windir");
        if (!NStringUtilsBoot.isBlank(e)) {
            return e.substring(0, 2);
        }
        return null;
    }

    private String getNativePath(String s) {
        switch (NNameFormatBoot.CONST_NAME.format(platformOsFamily)) {
            case "WINDOWS":
                return s.replace('/', '\\');
        }
        return s.replace('\\', '/');
    }

    public static String[] osFamilies(){
       return new String[]{"WINDOWS",
               "LINUX",
               "MACOS",
               "UNIX",
               "UNKNOWN"
       };
    }

    public static String[] storeTypes(){
        return new String[]{
                "BIN",
                "CONF",
                "VAR",
                "LOG",
                "TEMP",
                "CACHE",
                "LIB",
                "RUN"
        };
    }
    /**
     * @param storeStrategy     storeStrategy or null
     * @param baseLocations     baseLocations or null
     * @param homeLocations     homeLocations or null
     * @param workspaceLocation workspaceName or null
     * @return locations map
     */
    public Map<String, String> buildLocations(
            String storeStrategy,
            Map<String, String> baseLocations,
            Map<NHomeLocationBoot, String> homeLocations,
            String workspaceLocation) {
        workspaceLocation = getWorkspaceLocation(workspaceLocation);
        String[] storeTypes = storeTypes();
        String[] homes = new String[storeTypes.length];
        for (int i = 0; i < storeTypes.length; i++) {
            String location = storeTypes[i];
            String platformHomeFolder = getWorkspaceLocation(location, homeLocations, workspaceLocation);
            if (NStringUtilsBoot.isBlank(platformHomeFolder)) {
                throw new NBootException(NMsgBoot.ofC("missing Home for %s", location));
            }
            homes[i] = platformHomeFolder;
        }
        if (storeStrategy == null) {
            storeStrategy = "EXPLODED";
        }
        Map<String, String> storeLocations = new LinkedHashMap<>();
        if (baseLocations != null) {
            for (Map.Entry<String, String> e : baseLocations.entrySet()) {
                String loc = e.getKey();
                if (loc == null) {
                    throw new NBootException(NMsgBoot.ofPlain("null location"));
                }
                storeLocations.put(loc, e.getValue());
            }
        }
        for (int i = 0; i < storeTypes.length; i++) {
            String location = storeTypes[i];
            String _storeLocation = storeLocations.get(location);
            if (NStringUtilsBoot.isBlank(_storeLocation)) {
                switch (NUtilsBoot.enumName(storeStrategy)) {
                    case "STANDALONE": {
                        String c = getCustomPlatformHomeFolder(location, homeLocations);
                        storeLocations.put(location, c == null ? (workspaceLocation + File.separator + NUtilsBoot.enumId(location)) : c);
                        break;
                    }
                    case "EXPLODED": {
                        storeLocations.put(location, homes[i]);
                        break;
                    }
                }
            } else if (!Paths.get(_storeLocation).isAbsolute()) {
                switch (NUtilsBoot.enumName(storeStrategy)) {
                    case "STANDALONE": {
                        String c = getCustomPlatformHomeFolder(location, homeLocations);
                        storeLocations.put(location, c == null ?
                                (workspaceLocation + File.separator + NUtilsBoot.enumId(location) + NReservedIOUtilsBoot.getNativePath("/" + _storeLocation))
                                :
                                (c + NReservedIOUtilsBoot.getNativePath("/" + _storeLocation)));
                        break;
                    }
                    case "EXPLODED": {
                        storeLocations.put(location, homes[i] + NReservedIOUtilsBoot.getNativePath("/" + _storeLocation));
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
