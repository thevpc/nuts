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
import net.thevpc.nuts.boot.DefaultNBootOptionsBuilder;
import net.thevpc.nuts.reserved.*;
import net.thevpc.nuts.spi.NScopeType;

import java.lang.reflect.Array;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.logging.Level;

/**
 * this class implements several utility methods to be used by Nuts API interfaces
 *
 * @author thevpc
 */
public class NApiUtils {

    private NApiUtils() {
    }

    public static boolean isBlank(CharSequence s) {
        return s == null || isBlank(s.toString().toCharArray());
    }

    public static <T> T firstNonBlank(List<T> any) {
        for (T t : any) {
            if(!isBlank(t)){
                return t;
            }
        }
        return null;
    }

    public static <T> T firstNonBlank(T... any) {
        for (T t : any) {
            if(!isBlank(t)){
                return t;
            }
        }
        return null;
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

    public static int processThrowable(Throwable ex, NLog out) {
        return NReservedUtils.processThrowable(ex, out);
    }

    public static int processThrowable(Throwable ex, String[] args) {
        DefaultNBootOptionsBuilder bo = new DefaultNBootOptionsBuilder();
        bo.setCommandLine(args, null);
        try {
            if (NApiUtils.isGraphicalDesktopEnvironment()) {
                bo.setGui(false);
            }
        } catch (Exception e) {
            //exception may occur if the sdk is built without awt package for instance!
            bo.setGui(false);
        }
        boolean bot = bo.getBot().orElse(false);
        boolean gui = !bot && bo.getGui().orElse(false);
        boolean showStackTrace = bo.getDebug().isPresent();
        NLogConfig nLogConfig = bo.getLogConfig().orElseGet(NLogConfig::new);
        showStackTrace |= (nLogConfig.getLogTermLevel() != null
                && nLogConfig.getLogTermLevel().intValue() < Level.INFO.intValue());
        if (!showStackTrace) {
            showStackTrace = NApiUtils.getSysBoolNutsProperty("debug", false);
        }
        if (bot) {
            showStackTrace = false;
        }
        return processThrowable(ex, null, true, showStackTrace, gui);
    }

    public static int processThrowable(Throwable ex, NLog out, boolean showMessage, boolean showStackTrace, boolean showGui) {
        return NReservedUtils.processThrowable(ex, out, showMessage, showStackTrace, showGui);
    }

    public static boolean isGraphicalDesktopEnvironment() {
        return NReservedGuiUtils.isGraphicalDesktopEnvironment();
    }

    public static boolean getSysBoolNutsProperty(String property, boolean defaultValue) {
        return NReservedUtils.getSysBoolNutsProperty(property, defaultValue);
    }

    public static String resolveNutsVersionFromClassPath(NLog bLog) {
        return NReservedMavenUtils.resolveNutsApiVersionFromClassPath(bLog);
    }

    public static String resolveNutsIdDigestOrError() {
        String d = resolveNutsIdDigest();
        if (d == null) {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            URL[] urls = NReservedClassLoaderUtils.resolveClasspathURLs(cl, true);
            throw new NBootException(NMsg.ofPlain("unable to detect nuts digest. Most likely you are missing valid compilation of nuts." + "\n\t 'pom.properties' could not be resolved and hence, we are unable to resolve nuts version." + "\n\t java=" + System.getProperty("java.home") + " as " + System.getProperty("java.version") + "\n\t class-path=" + System.getProperty("java.class.path") + "\n\t urls=" + Arrays.toString(urls) + "\n\t class-loader=" + cl.getClass().getName() + " as " + cl));
        }
        return d;

    }

    public static String resolveNutsIdDigest() {
        //TODO COMMIT TO 0.8.4
        return resolveNutsIdDigest(NId.ofApi(Nuts.getVersion()).get(), NReservedClassLoaderUtils.resolveClasspathURLs(Nuts.class.getClassLoader(), true));
    }

    public static String resolveNutsIdDigest(NId id, URL[] urls) {
        return NReservedIOUtils.getURLDigest(NReservedClassLoaderUtils.findClassLoaderJar(id, urls), null);
    }

    public static URL findClassLoaderJar(NId id, URL[] urls) {
        return NReservedClassLoaderUtils.findClassLoaderJar(id, urls);
    }

    public static NOptional<Integer> parseFileSizeInBytes(String value, Integer defaultMultiplier) {
        return NReservedStringUtils.parseFileSizeInBytes(value, defaultMultiplier);
    }

    @SuppressWarnings("unchecked")


    public static <T> T getOrCreateRefProperty(String name, Class<T> type, NSession session, Supplier<T> sup) {
        NAssert.requireSession(session);
        name = NStringUtils.trim(name);
        if (NBlankable.isBlank(name)) {
            name = "default";
        }
        String key = type.getName() + "(" + name + ")";
        return session.getOrComputeProperty(key, NScopeType.SESSION, s->sup.get());
    }

    public static <T> T getOrCreateRefProperty(Class<T> type, NSession session, Supplier<T> sup) {
        return getOrCreateRefProperty("default", type, session, sup);
    }

}
