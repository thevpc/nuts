package net.thevpc.nuts.reserved;

import net.thevpc.nuts.Nuts;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.log.NLogVerb;
import net.thevpc.nuts.util.*;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.stream.Collectors;

public final class NReservedLangUtils {

    public static String getErrorMessage(Throwable ex) {
        String m = ex.getMessage();
        if (m == null || m.length() < 5) {
            m = ex.toString();
        }
        return m;
    }

    public static <T> NOptional<T> findThrowable(Throwable th, Class<T> type, Predicate<Throwable> filter) {
        Set<Throwable> visited = new HashSet<>();
        Stack<Throwable> stack = new Stack<>();
        if (th != null) {
            stack.push(th);
        }
        while (!stack.isEmpty()) {
            Throwable a = stack.pop();
            if (visited.add(a)) {
                if (type.isAssignableFrom(th.getClass())) {
                    if (filter == null || filter.test(th)) {
                        return NOptional.of((T) th);
                    }
                }
                Throwable c = th.getCause();
                if (c != null) {
                    stack.add(c);
                }
                if (th instanceof InvocationTargetException) {
                    c = ((InvocationTargetException) th).getTargetException();
                    if (c != null) {
                        stack.add(c);
                    }
                }
            }
        }
        return NOptional.ofEmpty(() -> NMsg.ofC("error with type %s not found", type.getSimpleName()));
    }

    public static List<String> splitDefault(String str) {
        return NStringUtils.split(str, " ;,\n\r\t|", true, true);
    }

    public static List<String> parseAndTrimToDistinctList(String s) {
        if (s == null) {
            return new ArrayList<>();
        }
        return splitDefault(s).stream().map(String::trim)
                .filter(x -> x.length() > 0)
                .distinct().collect(Collectors.toList());
    }

    public static String joinAndTrimToNull(List<String> args) {
        return NStringUtils.trimToNull(
                String.join(",", args)
        );
    }

    public static NOptional<Integer> parseFileSizeInBytes(String value, Integer defaultMultiplier) {
        if (NBlankable.isBlank(value)) {
            return NOptional.ofEmpty(() -> NMsg.ofPlain("empty size"));
        }
        value = value.trim();
        Integer i = NLiteral.of(value).asIntValue().orNull();
        if (i != null) {
            if (defaultMultiplier != null) {
                return NOptional.of(i * defaultMultiplier);
            } else {
                return NOptional.of(i);
            }
        }
        for (String s : new String[]{"kb", "mb", "gb", "k", "m", "g"}) {
            if (value.toLowerCase().endsWith(s)) {
                String v = value.substring(0, value.length() - s.length()).trim();
                i = NLiteral.of(v).asIntValue().orNull();
                if (i != null) {
                    switch (s) {
                        case "k":
                        case "kb":
                            return NOptional.of(i * 1024);
                        case "m":
                        case "mb":
                            return NOptional.of(i * 1024 * 1024);
                        case "g":
                        case "gb":
                            return NOptional.of(i * 1024 * 1024 * 1024);
                    }
                }
            }
        }
        String finalValue = value;
        return NOptional.ofError(() -> NMsg.ofC("invalid size :%s", finalValue));
    }

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

    public static boolean isGraphicalDesktopEnvironment() {
        try {
            if (!java.awt.GraphicsEnvironment.isHeadless()) {
                return false;
            }
            try {
                java.awt.GraphicsDevice[] screenDevices = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
                if (screenDevices == null || screenDevices.length == 0) {
                    return false;
                }
            } catch (java.awt.HeadlessException e) {
                return false;
            }
            return true;
        } catch (UnsatisfiedLinkError e) {
            //exception may occur if the sdk is built in headless mode
            return false;
        } catch (Throwable e) {
            //exception may occur if the sdk is built without awt package for instance!
            return false;
        }
    }

    public static String inputString(String message, String title, Supplier<String> in, NLog bLog) {
        try {
            if (title == null) {
                title = "Nuts Package Manager - " + Nuts.getVersion();
            }
            String line = javax.swing.JOptionPane.showInputDialog(
                    null,
                    message, title, javax.swing.JOptionPane.QUESTION_MESSAGE
            );
            if (line == null) {
                line = "";
            }
            return line;
        } catch (UnsatisfiedLinkError e) {
            //exception may occur if the sdk is built in headless mode
            bLog.with().level(Level.OFF).verb(NLogVerb.WARNING).log( NMsg.ofC("[Graphical Environment Unsupported] %s", title));
            if (in == null) {
                return new Scanner(System.in).nextLine();
            }
            return in.get();
        }
    }

    public static void showMessage(String message, String title, NLog bLog) {
        if (title == null) {
            title = "Nuts Package Manager";
        }
        try {
            javax.swing.JOptionPane.showMessageDialog(null, message);
        } catch (UnsatisfiedLinkError e) {
            //exception may occur if the sdk is built in headless mode
            bLog.with().level(Level.OFF).verb(NLogVerb.WARNING).log( NMsg.ofC("[Graphical Environment Unsupported] %s", title));
        }
    }

}
