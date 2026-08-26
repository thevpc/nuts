package net.thevpc.nuts.platform;

import net.thevpc.nuts.core.NConstants;
import net.thevpc.nuts.text.NI18n;
import net.thevpc.nuts.core.NStoreStrategy;
import net.thevpc.nuts.internal.NApiUtilsRPI;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.*;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * NPlatformHome class.
 *
 * @author thevpc
 * @since 0.8.0
 */
public class NPlatformHome {
    public static final NPlatformHome USER = of(null);
    public static final NPlatformHome SYSTEM = ofSystem(null);
    private final NOsFamily platformOsFamily;
    private final boolean system;
    private final Function<String, String> env;
    private final Function<String, String> props;
    private final String sysPrefix = "system";


    /**
     * Creates a new instance of of system.
     *
     * @param platformOsFamily platform os family
     * @param env env
     * @param props props
     * @return of system result
     */
    public static NPlatformHome ofSystem(NOsFamily platformOsFamily, Function<String, String> env, Function<String, String> props) {
        return new NPlatformHome(platformOsFamily, true, env, props);
    }

    /**
     * Creates a new instance of of.
     *
     * @param platformOsFamily platform os family
     * @param env env
     * @param props props
     * @return of result
     */
    public static NPlatformHome of(NOsFamily platformOsFamily, Function<String, String> env, Function<String, String> props) {
        return new NPlatformHome(platformOsFamily, false, env, props);
    }

    /**
     * Creates a new instance of of system.
     *
     * @param platformOsFamily platform os family
     * @return of system result
     */
    public static NPlatformHome ofSystem(NOsFamily platformOsFamily) {
        return new NPlatformHome(platformOsFamily, true, null, null);
    }

    /**
     * Creates a new instance of of.
     *
     * @param platformOsFamily platform os family
     * @return of result
     */
    public static NPlatformHome of(NOsFamily platformOsFamily) {
        return new NPlatformHome(platformOsFamily, false, null, null);
    }

    /**
     * Creates a new instance of of.
     *
     * @param platformOsFamily platform os family
     * @param system system
     * @return of result
     */
    public static NPlatformHome of(NOsFamily platformOsFamily, boolean system) {
        return new NPlatformHome(platformOsFamily, system, null, null);
    }

    /**
     * Creates a new instance of of.
     *
     * @param system system
     * @return of result
     */
    public static NPlatformHome of(boolean system) {
        return new NPlatformHome(null, system, null, null);
    }

    /**
     * Creates a new instance of of portable.
     *
     * @param platformOsFamily platform os family
     * @param userName user name
     * @return of portable result
     */
    public static NPlatformHome ofPortable(NOsFamily platformOsFamily, String userName) {
        /**
         * Creates a new instance of of portable.
         *
         * @param platformOsFamily platform os family
         * @param false false
         * @param userName user name
         * @return of portable result
         */
        return ofPortable(platformOsFamily, false, userName);
    }

    /**
     * Creates a new instance of of portable system.
     *
     * @param platformOsFamily platform os family
     * @param userName user name
     * @return of portable system result
     */
    public static NPlatformHome ofPortableSystem(NOsFamily platformOsFamily, String userName) {
        /**
         * Creates a new instance of of portable.
         *
         * @param platformOsFamily platform os family
         * @param true true
         * @param userName user name
         * @return of portable result
         */
        return ofPortable(platformOsFamily, true, userName);
    }

    /**
     * Creates a new instance of of portable.
     *
     * @param platformOsFamily platform os family
     * @param system system
     * @param userName user name
     * @return of portable result
     */
    public static NPlatformHome ofPortable(NOsFamily platformOsFamily, boolean system, String userName) {
        NAssert.requireNamedNonBlank(userName, "userName");
        return new NPlatformHome(platformOsFamily, system, p -> null, p -> portableProp(p, platformOsFamily, null, x -> {
            switch (x) {
                case "user.name":
                    return userName;
            }
            return null;
        }));
    }

    /**
     * Creates a new instance of of portable.
     *
     * @param platformOsFamily platform os family
     * @param system system
     * @param env env
     * @param props props
     * @return of portable result
     */
    public static NPlatformHome ofPortable(NOsFamily platformOsFamily, boolean system, Function<String, String> env, Function<String, String> props) {
        return new NPlatformHome(platformOsFamily, system, p -> null, p -> portableProp(p, platformOsFamily, env, props));
    }


    /**
     * Portable prop.
     *
     * @param p p
     * @param platformOsFamily platform os family
     * @param env env
     * @param props props
     * @return portable prop result
     */
    private static String portableProp(String p, NOsFamily platformOsFamily, Function<String, String> env, Function<String, String> props) {
        NOsFamily osFamily = platformOsFamily == null ? NOsFamily.UNIX : platformOsFamily;
        switch (p) {
            case "user.name": {
                String userName = NAssert.requireNamedNonBlank(props == null ? null : props.apply("user.name"), "user.name");
                return userName;
            }
            case "user.home": {
                String home=props == null ? null : props.apply("user.home");
                if(!NBlankable.isBlank(home)){
                    return home;
                }
                switch (osFamily) {
                    case WINDOWS: {
                        String userName = NAssert.requireNamedNonBlank(props == null ? null : props.apply("user.name"), "user.name");
                        return "C:\\Users\\" + userName;
                    }
                    default: {
                        String userName = NAssert.requireNamedNonBlank(props == null ? null : props.apply("user.name"), "user.name");
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
                        String userName = NAssert.requireNamedNonBlank(props == null ? null : props.apply("user.name"), "user.name");
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


    /**
     * N platform home.
     *
     * @param platformOsFamily platform os family
     * @param system system
     * @param env env
     * @param props props
     * @return n platform home result
     */
    public NPlatformHome(NOsFamily platformOsFamily, boolean system, Function<String, String> env, Function<String, String> props) {
        this.platformOsFamily = platformOsFamily != null ? platformOsFamily : NOsFamily.current();
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
        s = NStringUtils.strip(props.apply("nuts.home." + locationName + "." + platformOsFamily.id()));
        if (!s.isEmpty()) {
            return s/* + "/" + workspaceName*/;
        }
        s = NStringUtils.strip(props.apply("nuts.export.home." + locationName + "." + platformOsFamily.id()));
        if (!s.isEmpty()) {
            return s/* + "/" + workspaceName*/;
        }
        if (homeLocations != null && homeLocations.size() > 0) {
            NHomeLocation key = NHomeLocation.of(platformOsFamily, location);
            s = NStringUtils.strip(homeLocations.get(key));
            if (!s.isEmpty()) {
                return s/* + "/" + workspaceName*/;
            }
            key = NHomeLocation.of(null, location);
            s = NStringUtils.strip(homeLocations.get(key));
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
            /**
             * Returns the workspace location.
             *
             * @param workspaceName workspace name
             * @return get workspace location result
             */
            return getWorkspaceLocation(workspaceName);
        }
        String s = getCustomPlatformHomeFolder(location, homeLocations);
        if (s != null) {
            return s;
        }
        /**
         * Returns the workspace store.
         *
         * @param location location
         * @param workspaceName workspace name
         * @return get workspace store result
         */
        return getWorkspaceStore(location, workspaceName);
    }

    /**
     * Returns the base location.
     *
     * @param location location
     * @param homeLocations home locations
     * @return get base location result
     */
    public String getBaseLocation(NStoreType location, Map<NHomeLocation, String> homeLocations) {
        if (location == null) {
            /**
             * Home.
             *
             * @param nativePath("/ws" native path("/ws"
             * @return home result
             */
            return home() + nativePath("/ws");
        }
        String s = getCustomPlatformHomeFolder(location, homeLocations);
        if (!NBlankable.isBlank(s)) {
            return s;
        }
        /**
         * Returns the store.
         *
         * @param nativePath("/ws/" native path("/ws/"
         * @return get store result
         */
        return getStore(location) + nativePath("/ws/");
    }

    /**
     * Returns the global location.
     *
     * @param location location
     * @param homeLocations home locations
     * @return get global location result
     */
    public String getGlobalLocation(NStoreType location, Map<NHomeLocation, String> homeLocations) {
        if (location == null) {
            /**
             * Global location.
             *
             * @return global location result
             */
            return globalLocation();
        }
        String s = getCustomPlatformHomeFolder(location, homeLocations);
        if (s != null) {
            return s;
        }
        /**
         * Returns the global store.
         *
         * @param location location
         * @return get global store result
         */
        return getGlobalStore(location);
    }

    /**
     * Returns the workspace location.
     *
     * @param workspaceName workspace name
     * @return get workspace location result
     */
    public String getWorkspaceLocation(String workspaceName) {
        if (NBlankable.isBlank(workspaceName)) {
            workspaceName = NConstants.Names.DEFAULT_WORKSPACE_NAME;
        } else if (workspaceName.equals(".") || workspaceName.equals("..") || workspaceName.indexOf('/') >= 0 || workspaceName.indexOf('\\') >= 0) {
            //this is a path!
            //return it as is and make it absolute
            return Paths.get(workspaceName).normalize().toAbsolutePath().toString();
        }
        /**
         * Home.
         *
         * @param workspaceName workspace name
         * @return home result
         */
        return home() + nativePath("/ws/" + workspaceName);
    }

    /**
     * Global location.
     *
     * @return global location result
     */
    public String globalLocation() {
        /**
         * Home.
         *
         * @param nativePath("/global" native path("/global"
         * @return home result
         */
        return home() + nativePath("/global");
    }

    /**
     * Home.
     *
     * @return home result
     */
    public String home() {
        if (system) {
            switch (platformOsFamily) {
                case WINDOWS: {
                    return windowsProgramFiles() + "\\nuts";
                }
                default: {
                    return "/etc/opt/nuts";
                }
            }
        } else {
            String userHome = props.apply("user.home");
            return userHome + nativePath("/.nuts");
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
            /**
             * Returns the workspace location.
             *
             * @param workspaceName workspace name
             * @return get workspace location result
             */
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
        /**
         * Returns the store.
         *
         * @param nativePath(workspaceName) native path(workspace name)
         * @return get store result
         */
        return getStore(location) + nativePath("/ws/" + nativePath(workspaceName));
    }

    /**
     * Returns the global store.
     *
     * @param storeType store type
     * @return get global store result
     */
    public String getGlobalStore(NStoreType storeType) {
        /**
         * Returns the store.
         *
         * @param nativePath("/global" native path("/global"
         * @return get store result
         */
        return getStore(storeType) + nativePath("/global");
    }

    /**
     * Returns the store.
     *
     * @param location location
     * @return get store result
     */
    public String getStore(NStoreType location) {
        if (location == null) {
            /**
             * Home.
             *
             * @return home result
             */
            return home();
        }
        NOsFamily platformOsFamily = this.platformOsFamily;
        if (platformOsFamily == null) {
            platformOsFamily = NOsFamily.current();
        }
        String locationName = location.id();
        if (system) {
            String s = null;
            s = NStringUtils.strip(props.apply("nuts.store.system." + locationName + "." + platformOsFamily.id()));
            if (!s.isEmpty()) {
                return s;
            }
            s = NStringUtils.strip(props.apply("nuts.export.store.system." + locationName + "." + platformOsFamily.id()));
            if (!s.isEmpty()) {
                return NStringUtils.strip(s);
            }
            switch (location) {
                case BIN: {
                    switch (platformOsFamily) {
                        case WINDOWS: {
                            return windowsProgramFiles() + "\\nuts\\" + locationName;
                        }
                        default: {
                            return "/opt/nuts/" + locationName;
                        }
                    }
                }
                case LIB: {
                    switch (platformOsFamily) {
                        case WINDOWS: {
                            return windowsProgramFiles() + "\\nuts\\" + locationName;
                        }
                        default: {
                            return "/opt/nuts/" + locationName;
                        }
                    }
                }
                case CONF: {
                    switch (platformOsFamily) {
                        case WINDOWS: {
                            return windowsProgramFiles() + "\\nuts\\" + locationName;
                        }
                        default: {
                            return "/etc/opt/nuts/" + locationName;
                        }
                    }
                }
                case LOG: {
                    switch (platformOsFamily) {
                        case WINDOWS: {
                            return windowsProgramFiles() + "\\nuts\\" + locationName;
                        }
                        default: {
                            return "/var/log/nuts";
                        }
                    }
                }
                case CACHE: {
                    switch (platformOsFamily) {
                        case WINDOWS: {
                            return windowsProgramFiles() + "\\nuts\\" + locationName;
                        }
                        default: {
                            return "/var/cache/nuts";
                        }
                    }
                }
                case VAR: {
                    switch (platformOsFamily) {
                        case WINDOWS: {
                            return windowsProgramFiles() + "\\nuts\\" + locationName;
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
                                pf = windowsSystemRoot() + "\\Temp";
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
                                pf = windowsSystemRoot() + "\\Temp";
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
                            return userHome + nativePath("/AppData/Roaming/nuts/" + locationName);
                        }
                        default: {
                            String val = NStringUtils.strip(env.apply("XDG_DATA_HOME"));
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
                            return userHome + nativePath("/AppData/LocalLow/nuts/" + locationName);
                        }
                        default: {
                            String val = NStringUtils.strip(env.apply("XDG_LOG_HOME"));
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
                            return userHome + nativePath("/AppData/Local/nuts/" + locationName);
                        }
                        default: {
                            String val = NStringUtils.strip(env.apply("XDG_RUNTIME_DIR"));
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
                            return userHome + nativePath("/AppData/Roaming/nuts/" + locationName);
                        }
                        default: {
                            String val = NStringUtils.strip(env.apply("XDG_CONFIG_HOME"));
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
                            return userHome + nativePath("/AppData/Local/nuts/cache");
                        }
                        default: {
                            String val = NStringUtils.strip(env.apply("XDG_CACHE_HOME"));
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
                            return userHome + nativePath("/AppData/Local/nuts/" + locationName);
                        default:
                            //on macos/unix/linux temp folder is shared. will add user folder as discriminator
                            return props.apply("java.io.tmpdir") + nativePath("/" + userName + "/nuts");
                    }
                }
            }
        }
        throw NException.ofSafeIllegalArgumentException(NMsg.ofC(NI18n.of("unsupported getDefaultPlatformHomeFolderBase %s/%s"), platformOsFamily, location));
    }

    /**
     * Windows program files.
     *
     * @return windows program files result
     */
    public String windowsProgramFiles() {
        String s = env.apply("ProgramFiles");
        if (!NBlankable.isBlank(s)) {
            return s;
        }
        String c = windowsSystemDrive();
        if (!NBlankable.isBlank(c)) {
            return c + "\\Program Files";
        }
        return "C:\\Program Files";
    }

    /**
     * Windows program files x86.
     *
     * @return windows program files x86 result
     */
    public String windowsProgramFilesX86() {
        String s = env.apply("ProgramFiles(x86)");
        if (!NBlankable.isBlank(s)) {
            return s;
        }
        String c = windowsSystemDrive();
        if (!NBlankable.isBlank(c)) {
            return c + "\\Program Files (x86)";
        }
        return "C:\\Program Files (x86)";
    }


    /**
     * Windows system root.
     *
     * @return windows system root result
     */
    public String windowsSystemRoot() {
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

    /**
     * Windows system drive.
     *
     * @return windows system drive result
     */
    public String windowsSystemDrive() {
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

    /**
     * Native path.
     *
     * @param s s
     * @return native path result
     */
    private String nativePath(String s) {
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
                throw NException.ofSafeIllegalArgumentException(NMsg.ofC(NI18n.of("missing Home for %s"), location.id()));
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
                    throw NException.ofSafeIllegalArgumentException(NMsg.ofP(NI18n.of("null location")));
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
