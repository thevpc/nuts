/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 *
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * Copyright (C) 2016-2017 Taha BEN SALAH
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts;

import java.util.Locale;
import java.util.Map;

/**
 *
 * @author vpc
 * @since 0.5.4
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
        switch (location) {
            case APPS: {
                switch (getPlatformOsFamily()) {
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
                switch (getPlatformOsFamily()) {
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
                switch (getPlatformOsFamily()) {
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
                switch (getPlatformOsFamily()) {
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
                switch (getPlatformOsFamily()) {
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
                switch (getPlatformOsFamily()) {
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
                switch (getPlatformOsFamily()) {
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
                switch (getPlatformOsFamily()) {
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
     * @param folderType folder type to resolve home for
     * @param storeLocationLayout location layout to resolve home for
     * @param homeLocations workspace home locations
     * @param global global workspace
     * @param workspaceName workspace name or id (discriminator)
     * @return home folder path
     */
    public static String getPlatformHomeFolder(
            NutsOsFamily storeLocationLayout,
            NutsStoreLocation folderType,
            Map<String, String> homeLocations,
            boolean global,
            String workspaceName) {
        NutsStoreLocation folderType0 = folderType;
        if (folderType == null) {
            folderType = NutsStoreLocation.CONFIG;
        }
        if (workspaceName == null || workspaceName.isEmpty()) {
            workspaceName = NutsConstants.Names.DEFAULT_WORKSPACE_NAME;
        }
        boolean wasSystem = false;
        if (storeLocationLayout == null) {
            storeLocationLayout = PrivateNutsPlatformUtils.getPlatformOsFamily();
        }
        String s = null;
        String folderTypeName = folderType.id();
        s = PrivateNutsUtils.trim(System.getProperty("nuts.home." + folderTypeName + "." + storeLocationLayout.id()));
        if (!s.isEmpty()) {
            return s + "/" + workspaceName;
        }
        s = PrivateNutsUtils.trim(System.getProperty("nuts.export.home." + folderTypeName + "." + storeLocationLayout.id()));
        if (!s.isEmpty()) {
            return s.trim() + "/" + workspaceName;
        }
        String key = NutsDefaultWorkspaceOptions.createHomeLocationKey(storeLocationLayout, folderType);
        s = homeLocations == null ? "" : PrivateNutsUtils.trim(homeLocations.get(key));
        if (!s.isEmpty()) {
            return s.trim() + "/" + workspaceName;
        }
        key = NutsDefaultWorkspaceOptions.createHomeLocationKey(null, folderType);
        s = homeLocations == null ? "" : PrivateNutsUtils.trim(homeLocations.get(key));
        if (!s.isEmpty()) {
            return s.trim() + "/" + workspaceName;
        } else if (global) {
            return getPlatformGlobalHomeFolder(folderType, workspaceName);
        } else {
            switch (folderType) {
                case VAR:
                case APPS:
                case LIB: {
                    switch (storeLocationLayout) {
                        case WINDOWS: {
                            return System.getProperty("user.home") + PrivateNutsUtils.syspath("/AppData/Roaming/nuts/" + folderTypeName + "/" + workspaceName);
                        }
                        case MACOS:
                        case LINUX: {
                            String val = PrivateNutsUtils.trim(System.getenv("XDG_DATA_HOME"));
                            if (!val.isEmpty()) {
                                return val + "/nuts/" + folderTypeName + "/" + workspaceName;
                            }
                            return System.getProperty("user.home") + "/.local/share/nuts/" + folderTypeName + "/" + workspaceName;
                        }
                    }
                    break;
                }
                case LOG: {
                    switch (storeLocationLayout) {
                        case WINDOWS: {
                            return System.getProperty("user.home") + PrivateNutsUtils.syspath("/AppData/LocalLow/nuts/" + folderTypeName + "/" + workspaceName);
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
                    switch (storeLocationLayout) {
                        case WINDOWS: {
                            return System.getProperty("user.home") + PrivateNutsUtils.syspath("/AppData/Local/nuts/" + folderTypeName + "/" + workspaceName);
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
                    switch (storeLocationLayout) {
                        case WINDOWS: {
                            return System.getProperty("user.home") + PrivateNutsUtils.syspath("/AppData/Roaming/nuts/" + folderTypeName + "/" + workspaceName
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
                    switch (storeLocationLayout) {
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
                    switch (storeLocationLayout) {
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
        throw new NutsIllegalArgumentException(null, "Unsupported " + storeLocationLayout + "/" + folderType + " for " + workspaceName);
    }

}
