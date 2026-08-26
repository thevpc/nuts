/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . It's based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 *
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
package net.thevpc.nuts.internal;

import net.thevpc.nuts.boot.NBootLogConfig;
import net.thevpc.nuts.boot.NBootOptionsInfo;
import net.thevpc.nuts.core.NSession;
import net.thevpc.nuts.core.NWorkspace;
import net.thevpc.nuts.core.NWorkspaceOptions;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.log.NLogConfig;
import net.thevpc.nuts.util.*;

import java.io.File;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * this class implements several utility methods to be used by Nuts API
 * interfaces
 *
 * @author thevpc
 */
public class NApiUtilsRPI {

//    private static Logger LOG = Logger.getLogger(NApiUtilsRPI.class.getName());

    /**
     * N api utils rpi.
     *
     * @return n api utils rpi result
     */
    private NApiUtilsRPI() {
    }

    /**
     * Checks if is blank.
     *
     * @param any any
     * @return is blank result
     */
    public static boolean isBlank(Object any) {
        if (any == null) {
            return true;
        }
        if (any instanceof NBlankable) {
          /**
           * Return.
           *
           * @param any).isBlank( any).is blank(
           */
            return ((NBlankable) any).isBlank();
        }
        if (any instanceof String) {
            return NStringUtils.isBlank((String) any);
        }
        if (any instanceof CharSequence) {
            return NStringUtils.isBlank((CharSequence) any);
        }
        if (any instanceof char[]) {
            return NStringUtils.isBlank((char[]) any);
        }
        if (any.getClass().isArray()) {
            return Array.getLength(any) == 0;
        }
        if (any instanceof Collection) {
          /**
           * Return.
           *
           * @param any).isEmpty( any).is empty(
           */
            return ((Collection) any).isEmpty();
        }
        if (any instanceof Map) {
          /**
           * Return.
           *
           * @param any).isEmpty( any).is empty(
           */
            return ((Map) any).isEmpty();
        }
        return false;
    }

    /**
     * Resolve show stack trace.
     *
     * @param bo bo
     * @return resolve show stack trace result
     */
    public static boolean resolveShowStackTrace(NWorkspaceOptions bo) {
        if (bo.showStacktrace().isPresent()) {
            return bo.showStacktrace().get();
        } else if (bo.bot().orElse(false)) {
            return false;
        } else {
            if (NApiUtilsRPI.getSysBoolNutsProperty("stacktrace", false)) {
                return true;
            }
            if (bo.debug().isPresent() && !NBlankable.isBlank(bo.debug().get())) {
                return true;
            }
            NLogConfig nLogConfig = bo.logConfig().orElseGet(NLogConfig::new);
            if ((nLogConfig.logTermLevel() != null
                    && nLogConfig.logTermLevel().intValue() < Level.INFO.intValue())) {
                return true;
            }
            return false;
        }
    }

    /**
     * Resolve show stack trace.
     *
     * @param bo bo
     * @return resolve show stack trace result
     */
    public static boolean resolveShowStackTrace(NBootOptionsInfo bo) {
        if (bo.getShowStacktrace()!=null) {
            return bo.getShowStacktrace();
        } else if (bo.getBot()!=null && bo.getBot()) {
            return false;
        } else {
            if (NApiUtilsRPI.getSysBoolNutsProperty("stacktrace", false)) {
                return true;
            }
            if (bo.getDebug()!=null && !NBlankable.isBlank(bo.getDebug())) {
                return true;
            }
            NBootLogConfig nLogConfig = bo.getLogConfig();
            if (nLogConfig!=null && nLogConfig.getLogTermLevel() != null
                    && nLogConfig.getLogTermLevel().intValue() < Level.INFO.intValue()) {
                return true;
            }
            return false;
        }
    }


    /**
     * Resolve gui.
     *
     * @param bo bo
     * @return resolve gui result
     */
    public static boolean resolveGui(NWorkspaceOptions bo) {
        if (bo.bot().orElse(false)) {
            return false;
        }
        if (bo.gui().orElse(false)) {
            if (!NApiUtilsRPI.isGraphicalDesktopEnvironment()) {
                return false;
            }
            return true;
        } else {
            return false;
        }
    }
    /**
     * Resolve gui.
     *
     * @param bo bo
     * @return resolve gui result
     */
    public static boolean resolveGui(NBootOptionsInfo bo) {
        if (bo.getBot()!=null && bo.getBot()) {
            return false;
        }
        if (bo.getGui()!=null && bo.getGui()) {
            if (!NApiUtilsRPI.isGraphicalDesktopEnvironment()) {
                return false;
            }
            return true;
        } else {
            return false;
        }
    }


    /**
     * Checks if is graphical desktop environment.
     *
     * @return is graphical desktop environment result
     */
    public static boolean isGraphicalDesktopEnvironment() {
        return NReservedLangUtils.isGraphicalDesktopEnvironment();
    }

    /**
     * Returns the sys bool nuts property.
     *
     * @param property property
     * @param defaultValue default value
     * @return get sys bool nuts property result
     */
    public static boolean getSysBoolNutsProperty(String property, boolean defaultValue) {
        return NReservedUtils.getSysBoolNutsProperty(property, defaultValue);
    }

    /**
     * Parse file size in bytes.
     *
     * @param value value
     * @param defaultMultiplier default multiplier
     * @return parse file size in bytes result
     */
    public static NOptional<Integer> parseFileSizeInBytes(String value, Integer defaultMultiplier) {
        return NReservedLangUtils.parseFileSizeInBytes(value, defaultMultiplier);
    }

    @SuppressWarnings("unchecked")

    /**
     * Returns the or create ref property.
     *
     * @param name name
     * @param type type
     * @param sup sup
     * @return get or create ref property result
     */
    public static <T> T getOrCreateRefProperty(String name, Class<T> type, Supplier<T> sup) {
        name = NStringUtils.strip(name);
        if (NBlankable.isBlank(name)) {
            name = "default";
        }
        String key = type.getName() + "(" + name + ")";
        return NSession.of().getOrComputeProperty(key, () -> sup.get());
    }

    /**
     * Returns the or create ref property.
     *
     * @param type type
     * @param sup sup
     * @return get or create ref property result
     */
    public static <T> T getOrCreateRefProperty(Class<T> type, Supplier<T> sup) {
        /**
         * Returns the or create ref property.
         *
         * @param "default" "default"
         * @param type type
         * @param sup sup
         * @return get or create ref property result
         */
        return getOrCreateRefProperty("default", type, sup);
    }

    /**
     * Resolve valid error message.
     *
     * @param supplier supplier
     * @return resolve valid error message result
     */
    public static NMsg resolveValidErrorMessage(Supplier<NMsg> supplier) {
        if (supplier == null) {
            NMsg m = NMsg.ofC("unexpected error : %s", "empty message supplier").asError();
          /**
           * Safe log.
           *
           * @param m m
           */
            safeLog(m);
            return m;
        }
        NMsg t;
        try {
            t = supplier.get();
        } catch (Exception ex) {
            NMsg m = NMsg.ofC("unexpected error : %s", "message builder failed with : " + ex).asError();
          /**
           * Safe log.
           *
           * @param m m
           */
            safeLog(m);
            return m;
        }

        if (t == null) {
            NMsg m = NMsg.ofC("unexpected error : %s", "empty error message").asError();
          /**
           * Safe log.
           *
           * @param m m
           */
            safeLog(m);
            return m;
        }
        return t;
    }

    /**
     * Safe log.
     *
     * @param m m
     * @return safe log result
     */
    private static void safeLog(NMsg m){
        if(NWorkspace.get().isPresent()) {
            NLog.of(NApiUtilsRPI.class.getName()).log(m);
        }else {
            Logger.getLogger(NApiUtilsRPI.class.getName()).log(m.level(), new Throwable(m.toString()), m::toString);
        }
    }

    /**
     * Returns the native path.
     *
     * @param s s
     * @return get native path result
     */
    public static String getNativePath(String s) {
        return s.replace('/', File.separatorChar);
    }
}
