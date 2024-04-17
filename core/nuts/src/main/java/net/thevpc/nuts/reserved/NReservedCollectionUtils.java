package net.thevpc.nuts.reserved;

import net.thevpc.nuts.*;
import net.thevpc.nuts.boot.DefaultNBootOptionsBuilder;
import net.thevpc.nuts.boot.NBootOptionsBuilder;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.elem.NArrayElementBuilder;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.format.NContentType;
import net.thevpc.nuts.io.NIO;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.log.NLogConfig;
import net.thevpc.nuts.log.NLogOp;
import net.thevpc.nuts.log.NLogVerb;
import net.thevpc.nuts.reserved.boot.NReservedBootLog;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NStringUtils;

import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class NReservedCollectionUtils {
    public static <T, V> Map<T, V> nonNullMap(Map<T, V> other) {
        if (other == null) {
            return new LinkedHashMap<>();
        }
        return new LinkedHashMap<>(other);
    }

    public static <T> List<T> nonNullListFromArray(T[] other) {
        return nonNullList(Arrays.asList(other));
    }

    public static <T> List<T> unmodifiableOrNullList(Collection<T> other) {
        if (other == null) {
            return null;
        }
        return Collections.unmodifiableList(new ArrayList<>(other));
    }

    public static <T> Set<T> unmodifiableOrNullSet(Collection<T> other) {
        if (other == null) {
            return null;
        }
        return Collections.unmodifiableSet(new LinkedHashSet<>(other));
    }

    public static <T, V> Map<T, V> unmodifiableOrNullMap(Map<T, V> other) {
        if (other == null) {
            return null;
        }
        return Collections.unmodifiableMap(new LinkedHashMap<>(other));
    }

    public static <T> List<T> copyOrNullList(Collection<T> other) {
        if (other == null) {
            return null;
        }
        return new ArrayList<>(other);
    }

    public static <T> List<T> nonNullList(Collection<T> other) {
        if (other == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(other);
    }

    public static <T> Set<T> nonNullSet(Collection<T> other) {
        if (other == null) {
            return new LinkedHashSet<>();
        }
        return new LinkedHashSet<>(other);
    }

    public static List<String> addUniqueNonBlankList(List<String> list, String... values) {
        LinkedHashSet<String> newList = new LinkedHashSet<>();
        if (list != null) {
            newList.addAll(list);
        }
        boolean someUpdates = false;
        if (values != null) {
            for (String value : values) {
                if (!NBlankable.isBlank(value)) {
                    if (newList.add(NStringUtils.trim(value))) {
                        someUpdates = true;
                    }
                }
            }
        }
        if (someUpdates) {
            list = new ArrayList<>(newList);
        }
        return list;
    }


    public static <T> List<T> uniqueNonBlankList(Collection<T> other) {
        return uniqueList(other).stream().filter(x -> !NBlankable.isBlank(x)).collect(Collectors.toList());
    }

    public static <T> List<T> addUniqueNonBlankList(List<T> list, Collection<T> other) {
        if (other != null) {
            for (T t : other) {
                if (!NBlankable.isBlank(t)) {
                    if (!list.contains(t)) {
                        list.add(t);
                    }
                }
            }
        }
        return list;
    }

    public static <T> Set<T> nonBlankSet(Collection<T> other) {
        return set(other).stream().filter(x -> !NBlankable.isBlank(x)).collect(Collectors.toSet());
    }

    public static <T> List<T> uniqueList(Collection<T> other) {
        if (other == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(new LinkedHashSet<>(other));
    }

    public static <T> Set<T> set(Collection<T> other) {
        if (other == null) {
            return new LinkedHashSet<>();
        }
        return new LinkedHashSet<>(other);
    }

    public static <T> List<T> unmodifiableList(Collection<T> other) {
        return other == null ? Collections.emptyList() : Collections.unmodifiableList(new ArrayList<>(other));
    }

    public static <T, V> Map<T, V> unmodifiableMap(Map<T, V> other) {
        return other == null ? Collections.emptyMap() : Collections.unmodifiableMap(new LinkedHashMap<>(other));
    }

    public static <T> List<T> unmodifiableUniqueList(Collection<T> other) {
        return other == null ? Collections.emptyList() : Collections.unmodifiableList(uniqueList(other));
    }

    /**
     * process throwable and return exit code
     *
     * @param ex  exception
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
            bo = NBootManager.of(session).getBootOptions().builder();
            if (bo.getGui().orElse(false)) {
                if (!NEnvs.of(session).isGraphicalDesktopEnvironment()) {
                    bo.setGui(false);
                }
            }
        } else {
            NBootOptionsBuilder options = new DefaultNBootOptionsBuilder();
            //load inherited
            String nutsArgs = NStringUtils.trim(
                    NStringUtils.trim(System.getProperty("nuts.boot.args"))
                            + " " + NStringUtils.trim(System.getProperty("nuts.args"))
            );
            try {
                options.setCmdLine(NCmdLine.parseDefault(nutsArgs).get().toStringArray(), null);
            } catch (Exception e) {
                //any, ignore...
            }
            bo = options;
            if (bo.getGui().orElse(false)) {
                if (!NApiUtilsRPI.isGraphicalDesktopEnvironment()) {
                    bo.setGui(false);
                }
            }
        }

        boolean bot = bo.getBot().orElse(false);
        boolean gui = bo.getGui().orElse(false);
        boolean showStackTrace = bo.getDebug() != null;
        NLogConfig logConfig = bo.getLogConfig().orElseGet(NLogConfig::new);
        showStackTrace |= (logConfig != null
                && logConfig.getLogTermLevel() != null
                && logConfig.getLogTermLevel().intValue() < Level.INFO.intValue());
        if (!showStackTrace) {
            showStackTrace = NReservedUtils.getSysBoolNutsProperty("debug", false);
        }
        if (bot) {
            showStackTrace = false;
            gui = false;
        }
        return processThrowable(ex, out, true, showStackTrace, gui);
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
        NPrintStream fout = null;
        if (out == null) {
            if (session != null) {
                try {
                    fout = NIO.of(session).getSystemTerminal().getErr();
                    if (fm != null) {
                        fm = NMsg.ofNtf(NTexts.of(session).ofBuilder().append(fm, NTextStyle.error()).build());
                    } else {
                        fm = NMsg.ofStyled(m, NTextStyle.error());
                    }
                } catch (Exception ex2) {
                    NLogOp.of(NApplications.class, session).level(Level.FINE).error(ex2).log(
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
                fout = session.err();
            } else {
                fout = null;
            }
        }
        if (showMessage) {

            if (fout != null) {
                if (session.getOutputFormat() == NContentType.PLAIN) {
                    if (fm != null) {
                        fout.println(fm);
                    } else {
                        fout.println(m);
                    }
                    if (showStackTrace) {
                        ex.printStackTrace(fout.asPrintStream());
                    }
                    fout.flush();
                } else {
                    if (fm != null) {
                        session.eout().add(NElements.of(session).ofObject()
                                .set("app-id", session.getAppId() == null ? "" : session.getAppId().toString())
                                .set("error", NTexts.of(session).ofText(fm).filteredText())
                                .build()
                        );
                        if (showStackTrace) {
                            session.eout().add(NElements.of(session).ofObject().set("errorTrace",
                                    NElements.of(session).ofArray().addAll(NReservedLangUtils.stacktraceToArray(ex)).build()
                            ).build());
                        }
                        NArrayElementBuilder e = session.eout();
                        if (e.size() > 0) {
                            fout.println(e.build());
                            e.clear();
                        }
                        fout.flush();
                    } else {
                        session.eout().add(NElements.of(session).ofObject()
                                .set("app-id", session.getAppId() == null ? "" : session.getAppId().toString())
                                .set("error", m)
                                .build());
                        if (showStackTrace) {
                            session.eout().add(NElements.of(session).ofObject().set("errorTrace",
                                    NElements.of(session).ofArray().addAll(NReservedLangUtils.stacktraceToArray(ex)).build()
                            ).build());
                        }
                        NArrayElementBuilder e = session.eout();
                        if (e.size() > 0) {
                            fout.println(e.build());
                            e.clear();
                        }
                        fout.flush();
                    }
                    fout.flush();
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
                    sb.append(NTexts.of(session).ofText(fm).filteredText());
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
                //TODO show we delegate to the workspace implementation?
                NReservedGuiUtils.showMessage(NMsg.ofPlain(sb.toString()).toString(), "Nuts Package Manager - Error", out);
            } else {
                NReservedGuiUtils.showMessage(NMsg.ofPlain(sb.toString()).toString(), "Nuts Package Manager - Error", out);
            }
        }
        return (errorCode);
    }
}
