/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * <br>
 * <p>
 * Copyright [2020] [thevpc]
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain a
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
 */
package net.thevpc.nuts.util;


import net.thevpc.nuts.*;
import net.thevpc.nuts.reserved.NReservedIOUtils;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author thevpc
 * @app.category Util
 * @since 0.8.1
 */
public final class NPlatformUtils {

    /**
     * resolves custom nuts home folder from {@code homeLocations}.
     * Home folder is the root for nuts folders.
     * It depends on folder type and store layout.
     *
     * @param location         folder type to resolve home for
     * @param platformOsFamily location layout to resolve home for
     * @param homeLocations    workspace home locations
     * @return home folder path or null
     */
    public static String getCustomPlatformHomeFolder(NOsFamily platformOsFamily, NStoreLocation location, Map<NHomeLocation, String> homeLocations) {
        if (location == null) {
            return null;
        }
        if (platformOsFamily == null) {
            platformOsFamily = NOsFamily.getCurrent();
        }
        String s;
        String locationName = location.id();
        s = NStringUtils.trim(System.getProperty("nuts.home." + locationName + "." + platformOsFamily.id()));
        if (!s.isEmpty()) {
            return s/* + "/" + workspaceName*/;
        }
        s = NStringUtils.trim(System.getProperty("nuts.export.home." + locationName + "." + platformOsFamily.id()));
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
     * @param location         folder type to resolve home for
     * @param platformOsFamily location layout to resolve home for
     * @param homeLocations    workspace home locations
     * @param global           global workspace
     * @param workspaceName    workspace name or id (discriminator)
     * @return home folder path
     */
    public static String getPlatformHomeFolder(NOsFamily platformOsFamily, NStoreLocation location, Map<NHomeLocation, String> homeLocations, boolean global, String workspaceName) {
        if (location == null) {
            return getWorkspaceLocation(platformOsFamily, global, workspaceName);
        }
        String s = getCustomPlatformHomeFolder(platformOsFamily, location, homeLocations);
        if (s != null) {
            return s;
        }
        return getDefaultPlatformHomeFolder(platformOsFamily, location, global, workspaceName);
    }

    public static String getWorkspaceLocation(NOsFamily platformOsFamily, boolean global, String workspaceName) {
        if (NBlankable.isBlank(workspaceName)) {
            workspaceName = NConstants.Names.DEFAULT_WORKSPACE_NAME;
        } else if (workspaceName.equals(".") || workspaceName.equals("..") || workspaceName.indexOf('/') >= 0 || workspaceName.indexOf('\\') >= 0) {
            //this is a path!
            //return it as is and make it absolute
            return Paths.get(workspaceName).normalize().toAbsolutePath().toString();
        }
        if (platformOsFamily == null) {
            platformOsFamily = NOsFamily.getCurrent();
        }
        if (global) {
            switch (platformOsFamily) {
                case WINDOWS: {
                    return getWindowsProgramFiles() + "\\nuts\\" + getNativePath(workspaceName);
                }
                default: {
                    return "/etc/opt/nuts/" + workspaceName;
                }
            }
        } else {
            switch (platformOsFamily) {
                case WINDOWS: {
                    return System.getProperty("user.home") + getNativePath("/AppData/Roaming/nuts/config/" + workspaceName);
                }
                default: {
                    String val = NStringUtils.trim(System.getenv("XDG_CONFIG_HOME"));
                    if (!val.isEmpty()) {
                        return val + "/nuts/" + workspaceName;
                    }
                    return System.getProperty("user.home") + "/.config/nuts" + "/" + workspaceName;
                }
            }
        }
    }

    /**
     * resolves nuts home folder.Home folder is the root for nuts folders.It
     * depends on folder type and store layout. For instance log folder depends
     * on on the underlying operating system (linux,windows,...).
     * Specifications: XDG Base Directory Specification
     * (https://specifications.freedesktop.org/basedir-spec/basedir-spec-latest.html)
     *
     * @param location         folder type to resolve home for
     * @param platformOsFamily location layout to resolve home for
     * @param global           global workspace
     * @param workspaceName    workspace name or id (discriminator)
     * @return home folder path
     */
    public static String getDefaultPlatformHomeFolder(NOsFamily platformOsFamily, NStoreLocation location, boolean global, String workspaceName) {
        if (location == null) {
            return getWorkspaceLocation(platformOsFamily, global, workspaceName);
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

        if (platformOsFamily == null) {
            platformOsFamily = NOsFamily.getCurrent();
        }
        String locationName = location.id();
        if (global) {
            String s = null;
            s = NStringUtils.trim(System.getProperty("nuts.home.global." + locationName + "." + platformOsFamily.id()));
            if (!s.isEmpty()) {
                return s + "/" + workspaceName;
            }
            s = NStringUtils.trim(System.getProperty("nuts.export.home.global." + locationName + "." + platformOsFamily.id()));
            if (!s.isEmpty()) {
                return s.trim() + "/" + workspaceName;
            }
            switch (location) {
                case APPS: {
                    switch (platformOsFamily) {
                        case WINDOWS: {
                            return getWindowsProgramFiles() + "\\nuts\\apps\\" + getNativePath(workspaceName);
                        }
                        default: {
                            return "/opt/nuts/apps/" + workspaceName;
                        }
                    }
                }
                case LIB: {
                    switch (platformOsFamily) {
                        case WINDOWS: {
                            return getWindowsProgramFiles() + "\\nuts\\lib\\" + getNativePath(workspaceName);
                        }
                        default: {
                            return "/opt/nuts/lib/" + workspaceName;
                        }
                    }
                }
                case CONFIG: {
                    switch (platformOsFamily) {
                        case WINDOWS: {
                            return getWindowsProgramFiles() + "\\nuts\\config\\" + getNativePath(workspaceName);
                        }
                        default: {
                            return "/etc/opt/nuts/" + workspaceName + "/config";
                        }
                    }
                }
                case LOG: {
                    switch (platformOsFamily) {
                        case WINDOWS: {
                            return getWindowsProgramFiles() + "\\nuts\\log\\" + getNativePath(workspaceName);
                        }
                        default: {
                            return "/var/log/nuts/" + workspaceName;
                        }
                    }
                }
                case CACHE: {
                    switch (platformOsFamily) {
                        case WINDOWS: {
                            return getWindowsProgramFiles() + "\\nuts\\cache\\" + getNativePath(workspaceName);
                        }
                        default: {
                            return "/var/cache/nuts/" + workspaceName;
                        }
                    }
                }
                case VAR: {
                    switch (platformOsFamily) {
                        case WINDOWS: {
                            return getWindowsProgramFiles() + "\\nuts\\var\\" + getNativePath(workspaceName);
                        }
                        default: {
                            return "/var/opt/nuts/" + workspaceName;
                        }
                    }
                }
                case TEMP: {
                    switch (platformOsFamily) {
                        case WINDOWS: {
                            String pf = System.getenv("TMP");
                            if (NBlankable.isBlank(pf)) {
                                pf = getWindowsSystemRoot() + "\\TEMP";
                            }
                            return pf + "\\nuts\\" + getNativePath(workspaceName);
                        }
                        default: {
                            return "/tmp/nuts/global/" + workspaceName;
                        }
                    }
                }
                case RUN: {
                    switch (platformOsFamily) {
                        case WINDOWS: {
                            String pf = System.getenv("TMP");
                            if (NBlankable.isBlank(pf)) {
                                pf = getWindowsSystemRoot() + "\\TEMP";
                            }
                            return pf + "\\nuts\\run\\" + getNativePath(workspaceName);
                        }
                        default: {
                            return "/tmp/run/nuts/global/" + workspaceName;
                        }
                    }
                }
            }
        } else {
            switch (location) {
                case VAR:
                case APPS:
                case LIB: {
                    switch (platformOsFamily) {
                        case WINDOWS: {
                            return System.getProperty("user.home") + getNativePath("/AppData/Roaming/nuts/" + locationName + "/" + workspaceName);
                        }
                        default: {
                            String val = NStringUtils.trim(System.getenv("XDG_DATA_HOME"));
                            if (!val.isEmpty()) {
                                return val + "/nuts/" + locationName + "/" + workspaceName;
                            }
                            return System.getProperty("user.home") + "/.local/share/nuts/" + locationName + "/" + workspaceName;
                        }
                    }
                }
                case LOG: {
                    switch (platformOsFamily) {
                        case WINDOWS: {
                            return System.getProperty("user.home") + getNativePath("/AppData/LocalLow/nuts/" + locationName + "/" + workspaceName);
                        }
                        default: {
                            String val = NStringUtils.trim(System.getenv("XDG_LOG_HOME"));
                            if (!val.isEmpty()) {
                                return val + "/nuts/" + workspaceName;
                            }
                            return System.getProperty("user.home") + "/.local/log/nuts" + "/" + workspaceName;
                        }
                    }
                }
                case RUN: {
                    switch (platformOsFamily) {
                        case WINDOWS: {
                            return System.getProperty("user.home") + getNativePath("/AppData/Local/nuts/" + locationName + "/" + workspaceName);
                        }
                        default: {
                            String val = NStringUtils.trim(System.getenv("XDG_RUNTIME_DIR"));
                            if (!val.isEmpty()) {
                                return val + "/nuts/" + workspaceName;
                            }
                            return System.getProperty("user.home") + "/.local/run/nuts" + "/" + workspaceName;
                        }
                    }
                }
                case CONFIG: {
                    return Paths.get(getWorkspaceLocation(platformOsFamily, global, workspaceName)).resolve("config").toString();
                }
                case CACHE: {
                    switch (platformOsFamily) {
                        case WINDOWS: {
                            return System.getProperty("user.home") + getNativePath("/AppData/Local/nuts/cache/" + workspaceName);
                        }
                        default: {
                            String val = NStringUtils.trim(System.getenv("XDG_CACHE_HOME"));
                            if (!val.isEmpty()) {
                                return val + "/nuts/" + workspaceName;
                            }
                            return System.getProperty("user.home") + "/.cache/nuts" + "/" + workspaceName;
                        }
                    }
                }
                case TEMP: {
                    switch (platformOsFamily) {
                        case WINDOWS:
                            return System.getProperty("user.home") + getNativePath("/AppData/Local/nuts/log/" + workspaceName);
                        default:
                            //on macos/unix/linux temp folder is shared. will add user folder as discriminator
                            return System.getProperty("java.io.tmpdir") + getNativePath("/" + System.getProperty("user.name") + "/nuts/" + workspaceName);
                    }
                }
            }
        }
        throw new NBootException(NMsg.ofC("unsupported %s/%s for %s", platformOsFamily, location, workspaceName));
    }

    public static String getWindowsProgramFiles() {
        String s = System.getenv("ProgramFiles");
        if (!NBlankable.isBlank(s)) {
            return s;
        }
        String c = getWindowsSystemDrive();
        if (!NBlankable.isBlank(c)) {
            return c + "\\Program Files";
        }
        return "C:\\Program Files";
    }

    public static String getWindowsProgramFilesX86() {
        String s = System.getenv("ProgramFiles(x86)");
        if (!NBlankable.isBlank(s)) {
            return s;
        }
        String c = getWindowsSystemDrive();
        if (!NBlankable.isBlank(c)) {
            return c + "\\Program Files (x86)";
        }
        return "C:\\Program Files (x86)";
    }


    public static String getWindowsSystemRoot() {
        String e;
        e = System.getenv("SystemRoot");
        if (!NBlankable.isBlank(e)) {
            return e;
        }
        e = System.getenv("windir");
        if (!NBlankable.isBlank(e)) {
            return e;
        }
        e = System.getenv("SystemDrive");
        if (!NBlankable.isBlank(e)) {
            return e + "\\Windows";
        }
        return "C:\\Windows";
    }

    public static String getWindowsSystemDrive() {
        String e = System.getenv("SystemDrive");
        if (!NBlankable.isBlank(e)) {
            return e;
        }
        e = System.getenv("SystemRoot");
        if (!NBlankable.isBlank(e)) {
            return e.substring(0, 2);
        }
        e = System.getenv("windir");
        if (!NBlankable.isBlank(e)) {
            return e.substring(0, 2);
        }
        return null;
    }

    private static String getNativePath(String s) {
        return s.replace('/', File.separatorChar);
    }

    /**
     * @param platformOsFamily      platformOsFamily or null
     * @param storeLocationStrategy storeLocationStrategy or null
     * @param baseLocations         baseLocations or null
     * @param homeLocations         homeLocations or null
     * @param global                global
     * @param workspaceLocation     workspaceName or null
     * @param session               session or null
     * @return locations map
     */
    public static Map<NStoreLocation, String> buildLocations(
            NOsFamily platformOsFamily,
            NStoreLocationStrategy storeLocationStrategy,
            Map<NStoreLocation, String> baseLocations,
            Map<NHomeLocation, String> homeLocations,
            boolean global, String workspaceLocation, NSession session) {
        workspaceLocation = getWorkspaceLocation(platformOsFamily, global, workspaceLocation);
        String[] homes = new String[NStoreLocation.values().length];
        for (NStoreLocation location : NStoreLocation.values()) {
            String platformHomeFolder = getPlatformHomeFolder(platformOsFamily, location, homeLocations,
                    global, workspaceLocation);
            if (NBlankable.isBlank(platformHomeFolder)) {
                if (session == null) {
                    throw new NBootException(NMsg.ofC("missing Home for %s", location.id()));
                } else {
                    throw new NIllegalArgumentException(session, NMsg.ofC("missing Home for %s", location.id()));
                }
            }
            homes[location.ordinal()] = platformHomeFolder;
        }
        if (storeLocationStrategy == null) {
            storeLocationStrategy = NStoreLocationStrategy.EXPLODED;
        }
        Map<NStoreLocation, String> storeLocations = new LinkedHashMap<>();
        if (baseLocations != null) {
            for (Map.Entry<NStoreLocation, String> e : baseLocations.entrySet()) {
                NStoreLocation loc = e.getKey();
                if (loc == null) {
                    if (session == null) {
                        throw new NBootException(NMsg.ofPlain("null location"));
                    } else {
                        throw new NIllegalArgumentException(session, NMsg.ofPlain("null location"));
                    }
                }
                storeLocations.put(loc, e.getValue());
            }
        }
        for (NStoreLocation location : NStoreLocation.values()) {
            String _storeLocation = storeLocations.get(location);
            if (NBlankable.isBlank(_storeLocation)) {
                switch (storeLocationStrategy) {
                    case STANDALONE: {
                        String c = NPlatformUtils.getCustomPlatformHomeFolder(platformOsFamily, location, homeLocations);
                        storeLocations.put(location, c == null ? (workspaceLocation + File.separator + location.id()) : c);
                        break;
                    }
                    case EXPLODED: {
                        storeLocations.put(location, homes[location.ordinal()]);
                        break;
                    }
                }
            } else if (!Paths.get(_storeLocation).isAbsolute()) {
                switch (storeLocationStrategy) {
                    case STANDALONE: {
                        String c = NPlatformUtils.getCustomPlatformHomeFolder(platformOsFamily, location, homeLocations);
                        storeLocations.put(location, c == null ?
                                (workspaceLocation + File.separator + location.id() + NReservedIOUtils.getNativePath("/" + _storeLocation))
                                :
                                (c + NReservedIOUtils.getNativePath("/" + _storeLocation)));
                        break;
                    }
                    case EXPLODED: {
                        storeLocations.put(location, homes[location.ordinal()] + NReservedIOUtils.getNativePath("/" + _storeLocation));
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
