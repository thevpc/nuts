/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 * <p>
 * Copyright [2020] [thevpc]  
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License"); 
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an 
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language 
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.runtime.standalone.util;

import net.thevpc.nuts.util.NEnum;

import java.util.NoSuchElementException;

/**
 * @author thevpc
 */
public class CoreEnumUtils {

    public static String getEnumString(Enum e) {
        return e.toString().toLowerCase().replace("_", "-");
    }

    public static <T extends Enum> T parseEnumString(String val, Class<T> e, boolean lenient) {
        if (NEnum.class.isAssignableFrom(e)) {
            T r = (T) NEnum.parse((Class) e, val).orNull();
            if (r != null) {
                return r;
            }
            if (lenient) {
                return null;
            }
            throw new NoSuchElementException(val + " of type " + e.getSimpleName());
        }
        if (val == null) {
            val = "";
        } else {
            val = val.trim();
        }
        T r = null;
        if (!val.isEmpty()) {
            r = (T) Enum.valueOf((Class) e, val);
        }

//        String v2 = val.toUpperCase().replace("-", "_");
//
//        T r = null;
//        try {
//            r = (T) m.invoke(null, val);
//        } catch (Exception ex) {
//            throw new IllegalArgumentException("unable to run  valueOf(String)");
//        }

        if (r != null) {
            return r;
        }
        if (lenient) {
            return null;
        }
        throw new NoSuchElementException(val + " of type " + e.getSimpleName());
    }

}
