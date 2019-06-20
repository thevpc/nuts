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

/**
 *
 * @author vpc
 * @since 0.5.4
 */
public class NutsPlatformUtils {

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

    public static String getPlatformOsGlobalHome(NutsStoreLocation location, String id) {
        switch (location) {
            case PROGRAMS: {
                switch (getPlatformOsFamily()) {
                    case LINUX:
                    case MACOS:
                    case UNIX:
                    case UNKNOWN: {
                        return "/opt/nuts/programs/" + id;
                    }
                    case WINDOWS: {
                        String pf = System.getenv("ProgramFiles");
                        if (NutsUtilsLimited.isBlank(pf)) {
                            pf = "C:\\Program Files";
                        }
                        return pf + "\\nuts\\" + NutsUtilsLimited.syspath(id);
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
                        return "/opt/nuts/lib/" + id;
                    }
                    case WINDOWS: {
                        String pf = System.getenv("ProgramFiles");
                        if (NutsUtilsLimited.isBlank(pf)) {
                            pf = "C:\\Program Files";
                        }
                        return pf + "\\nuts\\" + NutsUtilsLimited.syspath(id);
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
                        return "/etc/opt/nuts/" + id;
                    }
                    case WINDOWS: {
                        String pf = System.getenv("ProgramFiles");
                        if (NutsUtilsLimited.isBlank(pf)) {
                            pf = "C:\\Program Files";
                        }
                        return pf + "\\nuts" + NutsUtilsLimited.syspath(id);
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
                        return "/var/log/nuts/" + id;
                    }
                    case WINDOWS: {
                        String pf = System.getenv("ProgramFiles");
                        if (NutsUtilsLimited.isBlank(pf)) {
                            pf = "C:\\Program Files";
                        }
                        return pf + "\\nuts" + NutsUtilsLimited.syspath(id);
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
                        return "/var/cache/nuts/" + id;
                    }
                    case WINDOWS: {
                        String pf = System.getenv("ProgramFiles");
                        if (NutsUtilsLimited.isBlank(pf)) {
                            pf = "C:\\Program Files";
                        }
                        return pf + "\\nuts" + NutsUtilsLimited.syspath(id);
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
                        return "/var/opt/nuts/" + id;
                    }
                    case WINDOWS: {
                        String pf = System.getenv("ProgramFiles");
                        if (NutsUtilsLimited.isBlank(pf)) {
                            pf = "C:\\Program Files";
                        }
                        return pf + "\\nuts" + NutsUtilsLimited.syspath(id);
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
                        return "/tmp/nuts/global/" + id;
                    }
                    case WINDOWS: {
                        String pf = System.getenv("TMP");
                        if (NutsUtilsLimited.isBlank(pf)) {
                            pf = "C:\\windows\\TEMP";
                        }
                        return pf + "\\nuts" + NutsUtilsLimited.syspath(id);
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
                        return "/tmp/run/nuts/global/" + id;
                    }
                    case WINDOWS: {
                        String pf = System.getenv("TMP");
                        if (NutsUtilsLimited.isBlank(pf)) {
                            pf = "C:\\windows\\TEMP";
                        }
                        return pf + "\\nuts\\run" + NutsUtilsLimited.syspath(id);
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
     * @param homeLocations
     * @param defaultHomeLocations
     * @param global
     * @param workspaceName workspace name or id (discriminator)
     * @return home folder path
     */
    public static String resolveHomeFolder(
            NutsOsFamily storeLocationLayout,
            NutsStoreLocation folderType,
            String[] homeLocations,
            String[] defaultHomeLocations,
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
            storeLocationLayout = NutsPlatformUtils.getPlatformOsFamily();
        }
        String s = null;
        String folderTypeName = folderType.name().toLowerCase();
        s = NutsUtilsLimited.trim(System.getProperty("nuts.home." + folderTypeName + "." + storeLocationLayout.name().toLowerCase()));
        if (!s.isEmpty()) {
            return s + "/" + workspaceName;
        }
        s = NutsUtilsLimited.trim(System.getProperty("nuts.export.home." + folderTypeName + "." + storeLocationLayout.name().toLowerCase()));
        if (!s.isEmpty()) {
            return s.trim() + "/" + workspaceName;
        }
        s = homeLocations == null ? "" : NutsUtilsLimited.trim(homeLocations[storeLocationLayout.ordinal() * NutsStoreLocation.values().length + folderType.ordinal()]);
        if (!s.isEmpty()) {
            return s.trim() + "/" + workspaceName;
        }
        s = defaultHomeLocations == null ? "" : NutsUtilsLimited.trim(defaultHomeLocations[folderType.ordinal()]);
        if (!s.isEmpty()) {
            return s.trim() + "/" + workspaceName;
        }
        if (global) {
            return getPlatformOsGlobalHome(folderType, workspaceName);
        } else {
            switch (folderType) {
                case VAR:
                case PROGRAMS:
                case LIB: {
                    switch (storeLocationLayout) {
                        case WINDOWS: {
                            return System.getProperty("user.home") + NutsUtilsLimited.syspath("/AppData/Roaming/nuts/" + folderTypeName + "/" + workspaceName);
                        }
                        case MACOS:
                        case LINUX: {
                            String val = NutsUtilsLimited.trim(System.getenv("XDG_DATA_HOME"));
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
                            return System.getProperty("user.home") + NutsUtilsLimited.syspath("/AppData/Roaming/nuts/" + folderTypeName + "/" + workspaceName);
                        }
                        case MACOS:
                        case LINUX: {
                            String val = NutsUtilsLimited.trim(System.getenv("XDG_LOG_HOME"));
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
                            return System.getProperty("user.home") + NutsUtilsLimited.syspath("/AppData/Local/nuts/" + folderTypeName + "/" + workspaceName);
                        }
                        case MACOS:
                        case LINUX: {
                            String val = NutsUtilsLimited.trim(System.getenv("XDG_RUNTIME_DIR"));
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
                            return System.getProperty("user.home") + NutsUtilsLimited.syspath("/AppData/Roaming/nuts/" + folderTypeName + "/" + workspaceName
                                    + (folderType0 == null ? "" : "/config")
                            );
                        }
                        case MACOS:
                        case LINUX: {
                            String val = NutsUtilsLimited.trim(System.getenv("XDG_CONFIG_HOME"));
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
                            return System.getProperty("user.home") + NutsUtilsLimited.syspath("/AppData/Local/nuts/cache/" + workspaceName);
                        }
                        case MACOS:
                        case LINUX: {
                            String val = NutsUtilsLimited.trim(System.getenv("XDG_CACHE_HOME"));
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
                            return System.getProperty("user.home") + NutsUtilsLimited.syspath("/AppData/Local/nuts/log/" + workspaceName);
                        case MACOS:
                        case LINUX:
                            //on macos/unix/linux temp folder is shared. will add user folder as discriminator
                            return System.getProperty("java.io.tmpdir") + NutsUtilsLimited.syspath("/" + System.getProperty("user.name") + "/nuts" + "/" + workspaceName);
                    }
                }
            }
        }
        throw new NutsIllegalArgumentException(null, "Unsupported " + storeLocationLayout + "/" + folderType + " for " + workspaceName);
    }

}
