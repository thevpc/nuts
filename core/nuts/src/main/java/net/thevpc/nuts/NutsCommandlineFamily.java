///**
// * ====================================================================
// * Nuts : Network Updatable Things Service
// * (universal package manager)
// * <br>
// * is a new Open Source Package Manager to help install packages and libraries
// * for runtime execution. Nuts is the ultimate companion for maven (and other
// * build managers) as it helps installing all package dependencies at runtime.
// * Nuts is not tied to java and is a good choice to share shell scripts and
// * other 'things' . Its based on an extensible architecture to help supporting a
// * large range of sub managers / repositories.
// * <br>
// * <p>
// * Copyright [2020] [thevpc] Licensed under the Apache License, Version 2.0 (the
// * "License"); you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
// * or agreed to in writing, software distributed under the License is
// * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// * KIND, either express or implied. See the License for the specific language
// * governing permissions and limitations under the License.
// * <br> ====================================================================
// */
//package net.thevpc.nuts;
//
//import net.thevpc.nuts.boot.NutsApiUtils;
//
///**
// * uniform platform architecture impl-note: list updated from
// * https://github.com/trustin/os-maven-plugin
// *
// * @author thevpc
// * @since 0.8.1
// * @app.category Base
// */
//public enum NutsCommandlineFamily implements NutsEnum {
//    DEFAULT,
//    SH,
//    WIN_CMD,
//    WIN_POWER_SHELL;
//
//    /**
//     * lower-cased identifier for the enum entry
//     */
//    private final String id;
//
//    NutsCommandlineFamily() {
//        this.id = name().toLowerCase();//.replace('_', '-');
//    }
//
//    public static NutsCommandlineFamily getCurrent() {
//        switch (NutsOsFamily.getCurrent()){
//            case WINDOWS:{
//                return WIN_CMD;
//            }
//            case MACOS:
//            case UNIX:
//            case LINUX:
//            {
//                return SH;
//            }
//            case UNKNOWN:{
//                return SH;
//            }
//        }
//        return SH;
//    }
//
//    public static NutsCommandlineFamily parseLenient(String arch) {
//        return parseLenient(arch, DEFAULT);
//    }
//
//    public static NutsCommandlineFamily parseLenient(String arch, NutsCommandlineFamily emptyOrErrorValue) {
//        return parseLenient(arch, emptyOrErrorValue, emptyOrErrorValue);
//    }
//
//    public static NutsCommandlineFamily parseLenient(String arch, NutsCommandlineFamily emptyValue, NutsCommandlineFamily errorValue) {
//        arch = arch == null ? "" : arch.toLowerCase().replace('-', '_').trim();
//        switch (arch) {
//            case "":
//                return emptyValue;
//            case "default":
//                return DEFAULT;
//            case "sh":
//            case "bash":
//            case "csh":
//            case "zsh":
//            case "ksh":
//            case "fish":
//                return SH;
//            case "windows_cmd":
//            case "win_cmd":
//            case "cmd":
//            case "win":
//                return WIN_CMD;
//            case "windows_power_shell":
//            case "windows_powershell":
//            case "win_power_shell":
//            case "win_powershell":
//            case "power_shell":
//            case "powershell":
//                return WIN_POWER_SHELL;
//        }
//        return errorValue;
//    }
//
//    public static NutsCommandlineFamily parse(String value, NutsSession session) {
//        return parse(value, null, session);
//    }
//
//    public static NutsCommandlineFamily parse(String value, NutsCommandlineFamily emptyValue, NutsSession session) {
//        NutsCommandlineFamily v = parseLenient(value, emptyValue, null);
//        NutsApiUtils.checkNonNullEnum(v,value,NutsCommandlineFamily.class,session);
//        return v;
//    }
//
//    /**
//     * lower cased identifier.
//     *
//     * @return lower cased identifier
//     */
//    public String id() {
//        return id;
//    }
//}
