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
import net.thevpc.nuts.boot.DefaultNBootOptionsBuilder;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.log.NLogConfig;
import net.thevpc.nuts.reserved.io.NReservedIOUtils;
import net.thevpc.nuts.spi.NScopeType;
import net.thevpc.nuts.util.*;

import java.lang.reflect.Array;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.thevpc.nuts.boot.NBootOptionsBuilder;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.elem.NArrayElementBuilder;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.format.NContentType;
import net.thevpc.nuts.io.NIO;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.log.NLogOp;
import net.thevpc.nuts.log.NLogVerb;
import net.thevpc.nuts.reserved.boot.NReservedBootLog;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTexts;

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

    public static <T> T firstNonBlank(List<T> any) {
        for (T t : any) {
            if (!isBlank(t)) {
                return t;
            }
        }
        return null;
    }

    public static <T> T firstNonBlank(T... any) {
        for (T t : any) {
            if (!isBlank(t)) {
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

    public static int processThrowable(Throwable ex, String[] args) {
        DefaultNBootOptionsBuilder bo = new DefaultNBootOptionsBuilder();
        bo.setCmdLine(args);
        return processThrowable(ex, null, true, resolveShowStackTrace(bo), resolveGui(bo));
    }

    /**
     * process Throwable and return exit code
     *
     * @param ex exception
     * @param out out stream
     * @return exit code
     */
    public static int processThrowable(Throwable ex, NLog out) {
        if (ex == null) {
            return 0;
        }

        NSession session = NSessionAwareExceptionBase.resolveSession(ex).orNull();
        NBootOptionsBuilder bo = null;
        if (session != null) {
            bo = NBootManager.of().getBootOptions().builder();
        } else {
            NBootOptionsBuilder options = new DefaultNBootOptionsBuilder();
            //load inherited
            String nutsArgs = NStringUtils.trim(
                    NStringUtils.trim(System.getProperty("nuts.boot.args"))
                    + " " + NStringUtils.trim(System.getProperty("nuts.args"))
            );
            try {
                options.setCmdLine(NCmdLine.parseDefault(nutsArgs).get().toStringArray());
            } catch (Exception e) {
                //any, ignore...
            }
            bo = options;
        }
        return processThrowable(ex, out, true, resolveShowStackTrace(bo), resolveGui(bo));
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

    public static int processThrowable(Throwable ex, NLog out, boolean showMessage, boolean showStackTrace, boolean showGui) {
        if (ex == null) {
            return 0;
        }
        int errorCode = NExceptionWithExitCodeBase.resolveExitCode(ex).orElse(204);
        if (errorCode == 0) {
            return 0;
        }
        NSession session = NSessionAwareExceptionBase.resolveSession(ex).orNull();
        NMsg fm = NSessionAwareExceptionBase.resolveSessionAwareExceptionBase(ex).map(NSessionAwareExceptionBase::getFormattedMessage)
                .orNull();
        String m = NReservedLangUtils.getErrorMessage(ex);
        NPrintStream sout = null;
        if (out == null) {
            if (session != null) {
                try {
                    sout = NIO.of().getSystemTerminal().getErr();
                    if (fm != null) {
                        fm = NMsg.ofNtf(NTexts.of().ofBuilder().append(fm, NTextStyle.error()).build());
                    } else {
                        fm = NMsg.ofStyled(m, NTextStyle.error());
                    }
                } catch (Exception ex2) {
                    NLogOp.of(NApplications.class).level(Level.FINE).error(ex2).log(
                            NMsg.ofPlain("unable to get system terminal")
                    );
                    //
                }
            } else {
                if (fm != null) {
                    // session is null but the exception is of NutsException type
                    // This is kind of odd, so will ignore message fm
                    fm = null;
                } else {
                    out = new NReservedBootLog();
                }
            }
        } else {
            if (session != null) {
//                fout = NutsPrintStream.of(out, NutsTerminalMode.FORMATTED, null, session);
                sout = session.err();
            } else {
                sout = null;
            }
        }
        if (showMessage) {

            if (sout != null) {
                if (session.getOutputFormat().orDefault() == NContentType.PLAIN) {
                    if (fm != null) {
                        sout.println(fm);
                    } else {
                        sout.println(m);
                    }
                    if (showStackTrace) {
                        ex.printStackTrace(sout.asPrintStream());
                    }
                    sout.flush();
                } else {
                    if (fm != null) {
                        session.eout().add(NElements.of().ofObject()
                                .set("app-id", NStringUtils.toStringOrEmpty(NApp.of().getId().get()))
                                .set("error", NTexts.of().ofText(fm).filteredText())
                                .build()
                        );
                        if (showStackTrace) {
                            session.eout().add(NElements.of().ofObject().set("errorTrace",
                                    NElements.of().ofArray().addAll(NReservedLangUtils.stacktraceToArray(ex)).build()
                            ).build());
                        }
                        NArrayElementBuilder e = session.eout();
                        if (e.size() > 0) {
                            sout.println(e.build());
                            e.clear();
                        }
                        sout.flush();
                    } else {
                        session.eout().add(NElements.of().ofObject()
                                .set("app-id", NStringUtils.toStringOrEmpty(NApp.of().getId().get()))
                                .set("error", m)
                                .build());
                        if (showStackTrace) {
                            session.eout().add(NElements.of().ofObject().set("errorTrace",
                                    NElements.of().ofArray().addAll(NReservedLangUtils.stacktraceToArray(ex)).build()
                            ).build());
                        }
                        NArrayElementBuilder e = session.eout();
                        if (e.size() > 0) {
                            sout.println(e.build());
                            e.clear();
                        }
                        sout.flush();
                    }
                    sout.flush();
                }
            } else {
                if (out == null) {
                    out = new NReservedBootLog();
                }
                if (fm != null) {
                    out.with().level(Level.OFF).verb(NLogVerb.FAIL).log(fm);
                } else {
                    out.with().level(Level.OFF).verb(NLogVerb.FAIL).log(NMsg.ofPlain(m));
                }
                if (showStackTrace) {
                    out.with().level(Level.OFF).verb(NLogVerb.FAIL).log(NMsg.ofPlain("---------------"));
                    out.with().level(Level.OFF).verb(NLogVerb.FAIL).log(NMsg.ofPlain(">  STACKTRACE :"));
                    out.with().level(Level.OFF).verb(NLogVerb.FAIL).log(NMsg.ofPlain("---------------"));
                    out.with().level(Level.OFF).verb(NLogVerb.FAIL).log(NMsg.ofPlain(
                            NReservedLangUtils.stacktrace(ex)
                    ));
                }
            }
        }
        if (showGui) {
            StringBuilder sb = new StringBuilder();
            if (fm != null) {
                if (session != null) {
                    sb.append(NTexts.of().ofText(fm).filteredText());
                } else {
                    sb.append(fm);
                }
            } else {
                sb.append(m);
            }
            if (showStackTrace) {
                if (sb.length() > 0) {
                    sb.append("\n");
                    sb.append(NReservedLangUtils.stacktrace(ex));
                }
            }
            if (session != null) {
                //TODO should we delegate to the workspace implementation?
                NReservedLangUtils.showMessage(NMsg.ofPlain(sb.toString()).toString(), "Nuts Package Manager - Error", out);
            } else {
                NReservedLangUtils.showMessage(NMsg.ofPlain(sb.toString()).toString(), "Nuts Package Manager - Error", out);
            }
        }
        return (errorCode);
    }

    public static boolean isGraphicalDesktopEnvironment() {
        return NReservedLangUtils.isGraphicalDesktopEnvironment();
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
            URL[] urls = NReservedLangUtils.resolveClasspathURLs(cl, true);
            throw new NBootException(NMsg.ofPlain("unable to detect nuts digest. Most likely you are missing valid compilation of nuts." + "\n\t 'pom.properties' could not be resolved and hence, we are unable to resolve nuts version." + "\n\t java=" + System.getProperty("java.home") + " as " + System.getProperty("java.version") + "\n\t class-path=" + System.getProperty("java.class.path") + "\n\t urls=" + Arrays.toString(urls) + "\n\t class-loader=" + cl.getClass().getName() + " as " + cl));
        }
        return d;

    }

    public static String resolveNutsIdDigest() {
        //TODO COMMIT TO 0.8.4
        return resolveNutsIdDigest(NId.ofApi(Nuts.getVersion()).get(), NReservedLangUtils.resolveClasspathURLs(Nuts.class.getClassLoader(), true));
    }

    public static String resolveNutsIdDigest(NId id, URL[] urls) {
        return NReservedIOUtils.getURLDigest(NReservedLangUtils.findClassLoaderJar(id, urls), null);
    }

    public static URL findClassLoaderJar(NId id, URL[] urls) {
        return NReservedLangUtils.findClassLoaderJar(id, urls);
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
        return NSession.of().get().getOrComputeProperty(key, NScopeType.SESSION, () -> sup.get());
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
}
