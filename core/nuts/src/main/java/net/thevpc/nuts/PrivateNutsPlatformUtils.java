/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * <br>
 *
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
package net.thevpc.nuts;

import java.util.Locale;
import java.util.Map;

/**
 *
 * @author vpc
 * @since 0.5.4
 * @category Internal
 */
final class PrivateNutsPlatformUtils {

    public static NutsOsFamily getPlatformOsFamily() {
        String property = System.getProperty("os.name").toLowerCase(Locale.ENGLISH);
        if (property.startsWith("linux")) {
            return NutsOsFamily.LINUX;
        }
        if (property.startsWith("win")) {
            return NutsOsFamily.WINDOWS;
        }
        if (property.startsWith("mac")) {
            return NutsOsFamily.MACOS;
        }
        if (property.startsWith("sunos")) {
            return NutsOsFamily.UNIX;
        }
        if (property.startsWith("freebsd")) {
            return NutsOsFamily.UNIX;
        }
        return NutsOsFamily.UNKNOWN;
    }

    public static String getPlatformGlobalHomeFolder(NutsStoreLocation location, String workspaceName) {
        String s = null;
        String locationName = location.id();
        NutsOsFamily platformOsFamily = getPlatformOsFamily();
        s = PrivateNutsUtils.trim(System.getProperty("nuts.home.global." + locationName + "." + platformOsFamily.id()));
        if (!s.isEmpty()) {
            return s + "/" + workspaceName;
        }
        s = PrivateNutsUtils.trim(System.getProperty("nuts.export.home.global." + locationName + "." + platformOsFamily.id()));
        if (!s.isEmpty()) {
            return s.trim() + "/" + workspaceName;
        }
        switch (location) {
            case APPS: {
                switch (platformOsFamily) {
                    case LINUX:
                    case MACOS:
                    case UNIX:
                    case UNKNOWN: {
                        return "/opt/nuts/apps/" + workspaceName;
                    }
                    case WINDOWS: {
                        String pf = System.getenv("ProgramFiles");
                        if (PrivateNutsUtils.isBlank(pf)) {
                            pf = "C:\\Program Files";
                        }
                        return pf + "\\nuts\\" + PrivateNutsUtils.syspath(workspaceName);
                    }
                }
                break;
            }
            case LIB: {
                switch (platformOsFamily) {
                    case LINUX:
                    case MACOS:
                    case UNIX:
                    case UNKNOWN: {
                        return "/opt/nuts/lib/" + workspaceName;
                    }
                    case WINDOWS: {
                        String pf = System.getenv("ProgramFiles");
                        if (PrivateNutsUtils.isBlank(pf)) {
                            pf = "C:\\Program Files";
                        }
                        return pf + "\\nuts\\" + PrivateNutsUtils.syspath(workspaceName);
                    }
                }
                break;
            }
            case CONFIG: {
                switch (platformOsFamily) {
                    case LINUX:
                    case MACOS:
                    case UNIX:
                    case UNKNOWN: {
                        return "/etc/opt/nuts/" + workspaceName;
                    }
                    case WINDOWS: {
                        String pf = System.getenv("ProgramFiles");
                        if (PrivateNutsUtils.isBlank(pf)) {
                            pf = "C:\\Program Files";
                        }
                        return pf + "\\nuts" + PrivateNutsUtils.syspath(workspaceName);
                    }
                }
                break;
            }
            case LOG: {
                switch (platformOsFamily) {
                    case LINUX:
                    case MACOS:
                    case UNIX:
                    case UNKNOWN: {
                        return "/var/log/nuts/" + workspaceName;
                    }
                    case WINDOWS: {
                        String pf = System.getenv("ProgramFiles");
                        if (PrivateNutsUtils.isBlank(pf)) {
                            pf = "C:\\Program Files";
                        }
                        return pf + "\\nuts" + PrivateNutsUtils.syspath(workspaceName);
                    }
                }
                break;
            }
            case CACHE: {
                switch (platformOsFamily) {
                    case LINUX:
                    case MACOS:
                    case UNIX:
                    case UNKNOWN: {
                        return "/var/cache/nuts/" + workspaceName;
                    }
                    case WINDOWS: {
                        String pf = System.getenv("ProgramFiles");
                        if (PrivateNutsUtils.isBlank(pf)) {
                            pf = "C:\\Program Files";
                        }
                        return pf + "\\nuts" + PrivateNutsUtils.syspath(workspaceName);
                    }
                }
                break;
            }
            case VAR: {
                switch (platformOsFamily) {
                    case LINUX:
                    case MACOS:
                    case UNIX:
                    case UNKNOWN: {
                        return "/var/opt/nuts/" + workspaceName;
                    }
                    case WINDOWS: {
                        String pf = System.getenv("ProgramFiles");
                        if (PrivateNutsUtils.isBlank(pf)) {
                            pf = "C:\\Program Files";
                        }
                        return pf + "\\nuts" + PrivateNutsUtils.syspath(workspaceName);
                    }
                }
                break;
            }
            case TEMP: {
                switch (platformOsFamily) {
                    case LINUX:
                    case MACOS:
                    case UNIX:
                    case UNKNOWN: {
                        return "/tmp/nuts/global/" + workspaceName;
                    }
                    case WINDOWS: {
                        String pf = System.getenv("TMP");
                        if (PrivateNutsUtils.isBlank(pf)) {
                            pf = "C:\\windows\\TEMP";
                        }
                        return pf + "\\nuts" + PrivateNutsUtils.syspath(workspaceName);
                    }
                }
                break;
            }
            case RUN: {
                switch (platformOsFamily) {
                    case LINUX:
                    case MACOS:
                    case UNIX:
                    case UNKNOWN: {
                        return "/tmp/run/nuts/global/" + workspaceName;
                    }
                    case WINDOWS: {
                        String pf = System.getenv("TMP");
                        if (PrivateNutsUtils.isBlank(pf)) {
                            pf = "C:\\windows\\TEMP";
                        }
                        return pf + "\\nuts\\run" + PrivateNutsUtils.syspath(workspaceName);
                    }
                }
                break;
            }
        }
        throw new UnsupportedOperationException();
    }

    /**
     * resolves nuts home folder.Home folder is the root for nuts folders.It
     * depends on folder type and store layout. For instance log folder depends
     * on on the underlying operating system (linux,windows,...).
     * Specifications: XDG Base Directory Specification
     * (https://specifications.freedesktop.org/basedir-spec/basedir-spec-latest.html)
     *
     * @param location folder type to resolve home for
     * @param platformOsFamily location layout to resolve home for
     * @param homeLocations workspace home locations
     * @param global global workspace
     * @param workspaceName workspace name or id (discriminator)
     * @return home folder path
     */
    public static String getPlatformHomeFolder(
            NutsOsFamily platformOsFamily,
            NutsStoreLocation location,
            Map<String, String> homeLocations,
            boolean global,
            String workspaceName) {
        NutsStoreLocation folderType0 = location;
        if (location == null) {
            location = NutsStoreLocation.CONFIG;
        }
        if (workspaceName == null || workspaceName.isEmpty()) {
            workspaceName = NutsConstants.Names.DEFAULT_WORKSPACE_NAME;
        }
        boolean wasSystem = false;
        if (platformOsFamily == null) {
            platformOsFamily = PrivateNutsPlatformUtils.getPlatformOsFamily();
        }
        String s = null;
        String locationName = location.id();
        s = PrivateNutsUtils.trim(System.getProperty("nuts.home." + locationName + "." + platformOsFamily.id()));
        if (!s.isEmpty()) {
            return s + "/" + workspaceName;
        }
        s = PrivateNutsUtils.trim(System.getProperty("nuts.export.home." + locationName + "." + platformOsFamily.id()));
        if (!s.isEmpty()) {
            return s.trim() + "/" + workspaceName;
        }
        String key = PrivateBootWorkspaceOptions.createHomeLocationKey(platformOsFamily, location);
        s = homeLocations == null ? "" : PrivateNutsUtils.trim(homeLocations.get(key));
        if (!s.isEmpty()) {
            return s.trim() + "/" + workspaceName;
        }
        key = PrivateBootWorkspaceOptions.createHomeLocationKey(null, location);
        s = homeLocations == null ? "" : PrivateNutsUtils.trim(homeLocations.get(key));
        if (!s.isEmpty()) {
            return s.trim() + "/" + workspaceName;
        } else if (global) {
            return getPlatformGlobalHomeFolder(location, workspaceName);
        } else {
            switch (location) {
                case VAR:
                case APPS:
                case LIB: {
                    switch (platformOsFamily) {
                        case WINDOWS: {
                            return System.getProperty("user.home") + PrivateNutsUtils.syspath("/AppData/Roaming/nuts/" + locationName + "/" + workspaceName);
                        }
                        case MACOS:
                        case LINUX: {
                            String val = PrivateNutsUtils.trim(System.getenv("XDG_DATA_HOME"));
                            if (!val.isEmpty()) {
                                return val + "/nuts/" + locationName + "/" + workspaceName;
                            }
                            return System.getProperty("user.home") + "/.local/share/nuts/" + locationName + "/" + workspaceName;
                        }
                    }
                    break;
                }
                case LOG: {
                    switch (platformOsFamily) {
                        case WINDOWS: {
                            return System.getProperty("user.home") + PrivateNutsUtils.syspath("/AppData/LocalLow/nuts/" + locationName + "/" + workspaceName);
                        }
                        case MACOS:
                        case LINUX: {
                            String val = PrivateNutsUtils.trim(System.getenv("XDG_LOG_HOME"));
                            if (!val.isEmpty()) {
                                return val + "/nuts/" + workspaceName;
                            }
                            return System.getProperty("user.home") + "/.local/log/nuts" + "/" + workspaceName;
                        }
                    }
                    break;
                }
                case RUN: {
                    switch (platformOsFamily) {
                        case WINDOWS: {
                            return System.getProperty("user.home") + PrivateNutsUtils.syspath("/AppData/Local/nuts/" + locationName + "/" + workspaceName);
                        }
                        case MACOS:
                        case LINUX: {
                            String val = PrivateNutsUtils.trim(System.getenv("XDG_RUNTIME_DIR"));
                            if (!val.isEmpty()) {
                                return val + "/nuts" + "/" + workspaceName;
                            }
                            return System.getProperty("user.home") + "/.local/run/nuts" + "/" + workspaceName;
                        }
                    }
                    break;
                }
                case CONFIG: {
                    switch (platformOsFamily) {
                        case WINDOWS: {
                            return System.getProperty("user.home") + PrivateNutsUtils.syspath("/AppData/Roaming/nuts/" + locationName + "/" + workspaceName
                                    + (folderType0 == null ? "" : "/config")
                            );
                        }
                        case MACOS:
                        case LINUX: {
                            String val = PrivateNutsUtils.trim(System.getenv("XDG_CONFIG_HOME"));
                            if (!val.isEmpty()) {
                                return val + "/nuts" + "/" + workspaceName + (folderType0 == null ? "" : "/config");
                            }
                            return System.getProperty("user.home") + "/.config/nuts" + "/" + workspaceName + (folderType0 == null ? "" : "/config");
                        }
                    }
                    break;
                }
                case CACHE: {
                    switch (platformOsFamily) {
                        case WINDOWS: {
                            return System.getProperty("user.home") + PrivateNutsUtils.syspath("/AppData/Local/nuts/cache/" + workspaceName);
                        }
                        case MACOS:
                        case LINUX: {
                            String val = PrivateNutsUtils.trim(System.getenv("XDG_CACHE_HOME"));
                            if (!val.isEmpty()) {
                                return val + "/nuts" + "/" + workspaceName;
                            }
                            return System.getProperty("user.home") + "/.cache/nuts" + "/" + workspaceName;
                        }
                    }
                    break;
                }
                case TEMP: {
                    switch (platformOsFamily) {
                        case WINDOWS:
                            return System.getProperty("user.home") + PrivateNutsUtils.syspath("/AppData/Local/nuts/log/" + workspaceName);
                        case MACOS:
                        case LINUX:
                            //on macos/unix/linux temp folder is shared. will add user folder as discriminator
                            return System.getProperty("java.io.tmpdir") + PrivateNutsUtils.syspath("/" + System.getProperty("user.name") + "/nuts" + "/" + workspaceName);
                    }
                }
            }
        }
        throw new NutsIllegalArgumentException(null, "Unsupported " + platformOsFamily + "/" + location + " for " + workspaceName);
    }

}
