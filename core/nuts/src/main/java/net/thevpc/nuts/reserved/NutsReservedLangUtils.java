package net.thevpc.nuts.reserved;

import net.thevpc.nuts.NutsMessage;
import net.thevpc.nuts.NutsOptional;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

public final class NutsReservedLangUtils {
    public static <T extends Enum> NutsOptional<T> parseEnum(String value, Class<T> type) {
        if (value == null) {
            value = "";
        } else {
            value = value.toUpperCase().trim().replace('-', '_');
        }
        if (value.isEmpty()) {
            return NutsOptional.ofEmpty(s -> NutsMessage.ofCstyle("%s is empty",type.getSimpleName()));
        }
        try {
            return NutsOptional.of((T) Enum.valueOf(type, value.toUpperCase()));
        } catch (Exception notFound) {
            String finalValue = value;
            return NutsOptional.ofError(s -> NutsMessage.ofCstyle(type.getSimpleName() + " invalid value : %s", finalValue));
        }
    }

    public static <T extends Enum> NutsOptional<T> parseEnum(String value, Class<T> type, Function<String, NutsOptional<T>> mapper) {
        if (value == null) {
            value = "";
        } else {
            value = value.toUpperCase().trim().replace('-', '_');
        }
        if (value.isEmpty()) {
            return NutsOptional.ofEmpty(s -> NutsMessage.ofCstyle("%s is empty",type.getSimpleName()));
        }
        try {
            NutsOptional<T> o = mapper.apply(value);
            if (o != null) {
                return o;
            }
        } catch (Exception notFound) {
            //ignore
        }
        try {
            return NutsOptional.of((T) Enum.valueOf(type, value.toUpperCase()));
        } catch (Exception notFound) {
            String finalValue = value;
            return NutsOptional.ofError(s -> NutsMessage.ofCstyle(type.getSimpleName() + " invalid value : %s", finalValue),notFound);
        }
    }

    public static String getErrorMessage(Throwable ex){
        String m = ex.getMessage();
        if (m == null || m.length() < 5) {
            m = ex.toString();
        }
        return m;
    }

    public static <T> NutsOptional<T> findThrowable(Throwable th, Class<T> type, Predicate<Throwable> filter) {
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
                        return NutsOptional.of((T) th);
                    }
                }
                Throwable c = th.getCause();
                if (c != null) {
                    stack.add(c);
                }
            }
        }
        return NutsOptional.ofEmpty(x -> NutsMessage.ofCstyle("error with type %s not found", type.getSimpleName()));
    }

    public static String[] stacktraceToArray(Throwable th) {
        try {
            StringWriter sw = new StringWriter();
            try (PrintWriter pw = new PrintWriter(sw)) {
                th.printStackTrace(pw);
            }
            BufferedReader br = new BufferedReader(new StringReader(sw.toString()));
            List<String> s = new ArrayList<>();
            String line;
            while ((line = br.readLine()) != null) {
                s.add(line);
            }
            return s.toArray(new String[0]);
        } catch (Exception ex) {
            // ignore
        }
        return new String[0];
    }

    public static String stacktrace(Throwable th) {
        try {
            StringWriter sw = new StringWriter();
            try (PrintWriter pw = new PrintWriter(sw)) {
                th.printStackTrace(pw);
            }
            return sw.toString();
        } catch (Exception ex) {
            // ignore
        }
        return "";
    }
}
