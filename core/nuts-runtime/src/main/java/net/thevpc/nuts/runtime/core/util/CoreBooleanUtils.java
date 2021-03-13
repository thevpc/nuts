/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 *
 * Copyright [2020] [thevpc] Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.runtime.core.util;

import net.thevpc.nuts.runtime.core.app.DefaultNutsArgument;

/**
 *
 * @author vpc
 */
public final class CoreBooleanUtils {

    private CoreBooleanUtils() {
    }
    

    //    public static boolean isYes(String s) {
    //        switch (s == null ? "" : s.trim().toLowerCase()) {
    //            case "ok":
    //            case "true":
    //            case "yes":
    //            case "always":
    //            case "y":
    //                return true;
    //        }
    //        return false;
    //    }
    //
    //    public static boolean isNo(String s) {
    //        switch (s == null ? "" : s.trim().toLowerCase()) {
    //            case "false":
    //            case "no":
    //            case "none":
    //            case "never":
    //                return true;
    //        }
    //        return false;
    //    }
    public static Boolean parseBoolean(String value, Boolean defaultValue) {
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        value = value.trim().toLowerCase();
        if (value.matches("true|enable|enabled|yes|always|y|on|ok|t|o")) {
            return true;
        }
        if (value.matches("false|disable|disabled|no|none|never|n|off|ko|f")) {
            return false;
        }
        return defaultValue;
    }

    public static Boolean parseBoolean(String value, Boolean emptyValue, Boolean incorrectValue) {
        if (value == null || value.trim().isEmpty()) {
            return emptyValue;
        }
        value = value.trim().toLowerCase();
        if (value.matches("true|enable|enabled|yes|always|y|on|ok|t|o")) {
            return true;
        }
        if (value.matches("false|disable|disabled|no|none|never|n|off|ko|f")) {
            return false;
        }
        return incorrectValue;
    }

    public static boolean getSysBoolNutsProperty(String property, boolean defaultValue) {
        return getSystemBoolean("nuts." + property, defaultValue) || getSystemBoolean("nuts.export." + property, defaultValue);
    }

    public static boolean getSystemBoolean(String property, boolean defaultValue) {
        String o = System.getProperty(property);
        if (o == null) {
            return defaultValue;
        }
        DefaultNutsArgument u = new DefaultNutsArgument(o);
        return u.getBoolean(defaultValue);
    }
    
}
