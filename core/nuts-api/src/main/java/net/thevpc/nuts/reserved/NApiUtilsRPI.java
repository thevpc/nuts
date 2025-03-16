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
package net.thevpc.nuts.reserved;

import net.thevpc.nuts.*;

import net.thevpc.nuts.boot.NBootLogConfig;
import net.thevpc.nuts.boot.NBootOptionsInfo;
import net.thevpc.nuts.boot.reserved.cmdline.NBootWorkspaceCmdLineParser;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTextBuilder;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.log.NLogConfig;
import net.thevpc.nuts.spi.NScopeType;
import net.thevpc.nuts.util.*;

import java.io.File;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.elem.NArrayElementBuilder;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.format.NContentType;
import net.thevpc.nuts.io.NIO;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.log.NLogOp;
import net.thevpc.nuts.log.NLogVerb;
import net.thevpc.nuts.text.NTextStyle;

/**
 * this class implements several utility methods to be used by Nuts API
 * interfaces
 *
 * @author thevpc
 */
public class NApiUtilsRPI {

    private static Logger LOG = Logger.getLogger(NApiUtilsRPI.class.getName());

    private NApiUtilsRPI() {
    }

    public static boolean isBlank(CharSequence s) {
        return s == null || isBlank(s.toString().toCharArray());
    }

    public static boolean isBlank(Object any) {
        if (any == null) {
            return true;
        }
        if (any instanceof NBlankable) {
            return ((NBlankable) any).isBlank();
        }
        if (any instanceof CharSequence) {
            return isBlank((CharSequence) any);
        }
        if (any instanceof char[]) {
            return isBlank((char[]) any);
        }
        if (any.getClass().isArray()) {
            return Array.getLength(any) == 0;
        }
        if (any instanceof Collection) {
            return ((Collection) any).isEmpty();
        }
        if (any instanceof Map) {
            return ((Map) any).isEmpty();
        }
        return false;
    }

    public static boolean isBlank(char[] string) {
        if (string == null || string.length == 0) {
            return true;
        }
        for (char c : string) {
            if (c > ' ') {
                return false;
            }
        }
        return true;
    }

    public static boolean resolveShowStackTrace(NWorkspaceOptions bo) {
        if (bo.getShowStacktrace().isPresent()) {
            return bo.getShowStacktrace().get();
        } else if (bo.getBot().orElse(false)) {
            return false;
        } else {
            if (NApiUtilsRPI.getSysBoolNutsProperty("stacktrace", false)) {
                return true;
            }
            if (bo.getDebug().isPresent() && !NBlankable.isBlank(bo.getDebug().get())) {
                return true;
            }
            NLogConfig nLogConfig = bo.getLogConfig().orElseGet(NLogConfig::new);
            if ((nLogConfig.getLogTermLevel() != null
                    && nLogConfig.getLogTermLevel().intValue() < Level.INFO.intValue())) {
                return true;
            }
            return false;
        }
    }

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


    public static boolean resolveGui(NWorkspaceOptions bo) {
        if (bo.getBot().orElse(false)) {
            return false;
        }
        if (bo.getGui().orElse(false)) {
            if (!NApiUtilsRPI.isGraphicalDesktopEnvironment()) {
                return false;
            }
            return true;
        } else {
            return false;
        }
    }
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


    public static boolean isGraphicalDesktopEnvironment() {
        return NReservedLangUtils.isGraphicalDesktopEnvironment();
    }

    public static boolean getSysBoolNutsProperty(String property, boolean defaultValue) {
        return NReservedUtils.getSysBoolNutsProperty(property, defaultValue);
    }

    public static NOptional<Integer> parseFileSizeInBytes(String value, Integer defaultMultiplier) {
        return NReservedLangUtils.parseFileSizeInBytes(value, defaultMultiplier);
    }

    @SuppressWarnings("unchecked")

    public static <T> T getOrCreateRefProperty(String name, Class<T> type, Supplier<T> sup) {
        name = NStringUtils.trim(name);
        if (NBlankable.isBlank(name)) {
            name = "default";
        }
        String key = type.getName() + "(" + name + ")";
        return NApp.of().getOrComputeProperty(key, NScopeType.SESSION, () -> sup.get());
    }

    public static <T> T getOrCreateRefProperty(Class<T> type, Supplier<T> sup) {
        return getOrCreateRefProperty("default", type, sup);
    }

    public static NMsg resolveValidErrorMessage(Supplier<NMsg> supplier) {
        if (supplier == null) {
            NMsg m = NMsg.ofC("unexpected error : %s", "empty message supplier");
            LOG.log(Level.SEVERE, new Throwable(m.toString()), m::toString);
            return m;
        }
        NMsg t;
        try {
            t = supplier.get();
        } catch (Exception ex) {
            NMsg m = NMsg.ofC("unexpected error : %s", "message builder failed with : " + ex);
            LOG.log(Level.SEVERE, new Throwable(m.toString()), m::toString);
            return m;
        }

        if (t == null) {
            NMsg m = NMsg.ofC("unexpected error : %s", "empty error message");
            LOG.log(Level.SEVERE, new Throwable(m.toString()), m::toString);
            return m;
        }
        return t;
    }

    public static String getNativePath(String s) {
        return s.replace('/', File.separatorChar);
    }
}
