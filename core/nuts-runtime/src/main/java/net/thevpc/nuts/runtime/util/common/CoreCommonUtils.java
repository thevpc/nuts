/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * <br>
 *
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
package net.thevpc.nuts.runtime.util.common;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.util.io.InputStreamVisitor;
import net.thevpc.nuts.runtime.util.io.ZipUtils;
import net.thevpc.nuts.runtime.util.iter.PushBackIterator;
import net.thevpc.nuts.runtime.util.CoreNutsUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.lang.reflect.Array;
import java.net.URL;
import java.time.Instant;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.ServiceConfigurationError;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import net.thevpc.nuts.runtime.app.DefaultNutsArgument;

public class CoreCommonUtils {
    public static String indexToString(int x) {
        if(x<0){
            return "-"+indexToString(-x);
        }
        StringBuilder sb=new StringBuilder();
        while(x>0){
            int y=x%10;
            if(y==0) {
                sb.insert(0, '0');
            }else{
                sb.insert(0, ((char) ('A' + (y-1))));
            }
            x=x/10;
        }
        if(sb.length()==0){
            return "A";
        }
        return sb.toString();
    }
    public static String[] toArraySet(String[] values0, String[]... values) {
        Set<String> set = toSet(values0);
        if (values != null) {
            for (String[] value : values) {
                set.addAll(toSet(value));
            }
        }
        return set.toArray(new String[0]);
    }

    public static Set<String> toSet(String[] values0) {
        LinkedHashSet<String> set = new LinkedHashSet<>();
        if (values0 != null) {
            for (String a : values0) {
                a = CoreStringUtils.trim(a);
                if (!CoreStringUtils.isBlank(a) && !set.contains(a)) {
                    set.add(a);
                }
            }
        }
        return set;
    }

    public static NutsClassifierMapping[] toArraySet(NutsClassifierMapping[] classifierMappings) {
        Set<NutsClassifierMapping> set = toSet(classifierMappings);
        return set.toArray(new NutsClassifierMapping[0]);
    }

    public static Set<NutsClassifierMapping> toSet(NutsClassifierMapping[] classifierMappings) {
        LinkedHashSet<NutsClassifierMapping> set = new LinkedHashSet<>();
        if (classifierMappings != null) {
            for (NutsClassifierMapping a : classifierMappings) {
                if (a!=null) {
                    set.add(a);
                }
            }
        }
        return set;
    }

    public static NutsIdLocation[] toArraySet(NutsIdLocation[] classifierMappings) {
        Set<NutsIdLocation> set = toSet(classifierMappings);
        return set.toArray(new NutsIdLocation[0]);
    }

    public static Set<NutsIdLocation> toSet(NutsIdLocation[] classifierMappings) {
        LinkedHashSet<NutsIdLocation> set = new LinkedHashSet<>();
        if (classifierMappings != null) {
            for (NutsIdLocation a : classifierMappings) {
                if (a!=null) {
                    set.add(a);
                }
            }
        }
        return set;
    }

    public static Set<String> loadServiceClassNames(URL url,Class service) {
        LinkedHashSet<String> found=new LinkedHashSet<>();
        try(InputStream jarStream=url.openStream()) {
            if(jarStream!=null) {
                ZipUtils.visitZipStream(jarStream, s -> s.equals("META-INF/services/" + service.getName())
                        , new InputStreamVisitor() {
                            @Override
                            public boolean visit(String path, InputStream inputStream) throws IOException {
                                BufferedReader reader=new BufferedReader(new InputStreamReader(inputStream));
                                String line=null;
                                while((line=reader.readLine())!=null) {
                                    line=line.trim();
                                    if(line.length()>0 &&!line.startsWith("#")){
                                        found.add(line);
                                    }
                                }
                                return false;
                            }
                        });
            }
        }catch (IOException ex){
            throw new UncheckedIOException(ex);
        }
        return found;
    }

    public static List<Class> loadServiceClasses(Class service, ClassLoader classLoader) {
        String fullName = "META-INF/services/" + service.getName();
        Enumeration<URL> configs;
        LinkedHashSet<String> names = new LinkedHashSet<>();
        try {
            if (classLoader == null) {
                configs = ClassLoader.getSystemResources(fullName);
            } else {
                configs = classLoader.getResources(fullName);
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
        while (configs.hasMoreElements()) {
            names.addAll(loadServiceClasses(service, configs.nextElement()));
        }
        List<Class> classes = new ArrayList<>();
        for (String n : names) {
            Class<?> c = null;
            try {
                c = Class.forName(n, false, classLoader);
            } catch (ClassNotFoundException x) {
                throw new NutsException(null, x);
            }
            if (!service.isAssignableFrom(c)) {
                throw new NutsException(null, "Not a valid type " + c + " <> " + service);
            }
            classes.add(c);
        }
        return classes;
    }

    public static List<String> loadServiceClasses(Class<?> service, URL u) throws ServiceConfigurationError {
        InputStream in = null;
        BufferedReader r = null;
        List<String> names = new ArrayList<>();
        try {
            in = u.openStream();
            r = new BufferedReader(new InputStreamReader(in, "utf-8"));
            int lc = 1;
            String line;
            while ((line = r.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty() && line.charAt(0) != '#') {
                    names.add(line);
                }
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        } finally {
            try {
                if (r != null) {
                    r.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex2) {
                throw new UncheckedIOException(ex2);
            }
        }
        return names;
    }

    private static String suffix(String s) {
        char[] chars = s.toCharArray();
        chars[0] = Character.toUpperCase(chars[0]);
        return new String(chars);
    }

    public static boolean getSysBoolNutsProperty(String property, boolean defaultValue) {
        return (getSystemBoolean("nuts." + property, defaultValue)
                || getSystemBoolean("nuts.export." + property, defaultValue));
    }

    public static boolean getSystemBoolean(String property, boolean defaultValue) {
        String o = System.getProperty(property);
        if (o == null) {
            return defaultValue;
        }
        DefaultNutsArgument u = new DefaultNutsArgument(o, '=');
        return u.getBoolean(defaultValue);
    }

    public static String[] concatArrays(String[]... arrays) {
        return concatArrays(String.class, arrays);
    }

    public static <T> T[] concatArrays(Class<T> cls, T[]... arrays) {
        List<T> all = new ArrayList<>();
        if (arrays != null) {
            for (T[] v : arrays) {
                if (v != null) {
                    all.addAll(Arrays.asList(v));
                }
            }
        }
        return all.toArray((T[]) Array.newInstance(cls, all.size()));
    }

    public static Integer convertToInteger(String value, Integer defaultValue) {
        if (CoreStringUtils.isBlank(value)) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (Exception ex) {
            return defaultValue;
        }
    }

    public static String formatPeriodMilli(long period) {
        StringBuilder sb = new StringBuilder();
        boolean started = false;
        int h = (int) (period / (1000L * 60L * 60L));
        int mn = (int) ((period % (1000L * 60L * 60L)) / 60000L);
        int s = (int) ((period % 60000L) / 1000L);
        int ms = (int) (period % 1000L);
        if (h > 0) {
            sb.append(CoreStringUtils.alignRight(String.valueOf(h), 2)).append("h ");
            started = true;
        }
        if (mn > 0 || started) {
            sb.append(CoreStringUtils.alignRight(String.valueOf(mn), 2)).append("mn ");
            started = true;
        }
        if (s > 0 || started) {
            sb.append(CoreStringUtils.alignRight(String.valueOf(s), 2)).append("s ");
            //started=true;
        }
        sb.append(CoreStringUtils.alignRight(String.valueOf(ms), 3)).append("ms");
        return sb.toString();
    }

    public static String setterName(String name) {
        //Class<?> type = field.getDataType();
        return "set" + suffix(name);
    }

    public static String getterName(String name, Class type) {
        if (Boolean.TYPE.equals(type)) {
            return "is" + suffix(name);
        }
        return "get" + suffix(name);
    }

    public static <T> List<T> toList(Iterator<T> it) {
        List<T> all = new ArrayList<>();
        while (it.hasNext()) {
            all.add(it.next());
        }
        return all;
    }

    public static <T> Iterator<T> nullifyIfEmpty(Iterator<T> other) {
        if (other == null) {
            return null;
        }
        if (other instanceof PushBackIterator) {
            PushBackIterator<T> b = (PushBackIterator<T>) other;
            if (!b.isEmpty()) {
                return b;
            } else {
                return null;
            }
        }
        PushBackIterator<T> b = new PushBackIterator<>(other);
        if (!b.isEmpty()) {
            return b;
        } else {
            return null;
        }
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

    public static String getEnumString(Enum e) {
        return e.toString().toLowerCase().replace("_", "-");
    }

    public static <T extends Enum> T parseEnumString(String val, Class<T> e, boolean lenient) {
        String v2 = val.toUpperCase().replace("-", "_");
        for (T enumConstant : e.getEnumConstants()) {
            if (enumConstant.toString().equals(v2)) {
                return enumConstant;
            }
        }
        if (lenient) {
            return null;
        }
        throw new NoSuchElementException(val + " of type " + e.getSimpleName());
    }

    public static String stringValueFormatted(Object o, boolean escapeString, NutsSession session) {
        if (o == null) {
            return "";
        }
        if (o instanceof NutsPrimitiveElement) {
            o = ((NutsPrimitiveElement) o).getValue();
        } else if (o instanceof NutsArrayElement) {
            o = ((NutsArrayElement) o).children();
        } else if (o instanceof NutsObjectElement) {
            Collection<NutsNamedElement> c= ((NutsObjectElement) o).children();
            Object[] a = c.toArray();
            if (a.length == 0) {
                return "";
            }
            if (a.length == 1) {
                return stringValue(a[0]);
            }
            return "\\{" + String.join(", ", (List) c.stream().map(x -> stringValueFormatted(x, escapeString, session)).collect(Collectors.toList())) + "\\}";
            
        } else if (o instanceof NutsNamedElement) {
            NutsNamedElement ne = (NutsNamedElement) o;
            StringBuilder sb = new StringBuilder();
            sb.append(stringValueFormatted(ne.getName(), escapeString, session));
            sb.append("=");
            if (ne.getValue().type() == NutsElementType.STRING) {
                sb.append(CoreStringUtils.dblQuote(stringValueFormatted(ne.getValue(), escapeString, session)));
            } else {
                sb.append(stringValueFormatted(ne.getValue(), escapeString, session));
            }
            o = sb.toString();
        } else if (o instanceof Map.Entry) {
            Map.Entry ne = (Map.Entry) o;
            StringBuilder sb = new StringBuilder();
            sb.append(stringValueFormatted(ne.getKey(), escapeString, session));
            sb.append("=");
            if (ne.getValue() instanceof String || (ne.getValue() instanceof NutsPrimitiveElement && ((NutsPrimitiveElement) ne.getValue()).type() == NutsElementType.STRING)) {
                sb.append(CoreStringUtils.dblQuote(stringValueFormatted(ne.getValue(), escapeString, session)));
            } else {
                sb.append(stringValueFormatted(ne.getValue(), escapeString, session));
            }
            o = sb.toString();
        } else if (o instanceof Map) {
            o = ((Map) o).entrySet();
        }
        if (o == null) {
            return "";
        }
        NutsWorkspace ws = session.getWorkspace();
        if (o instanceof Boolean) {
            return ws.formats().text().escapeText(String.valueOf(o));
        }
        if (o.getClass().isEnum()) {
            return ws.formats().text().escapeText(getEnumString((Enum) o));
        }
        if (o instanceof Instant) {
            return ws.formats().text().escapeText(
                    CoreNutsUtils.DEFAULT_DATE_TIME_FORMATTER.format(((Instant) o))
            );
        }
        if (o instanceof Temporal) {
            return ws.formats().text().escapeText(
                    CoreNutsUtils.DEFAULT_DATE_TIME_FORMATTER.format(((Temporal) o))
            );
        }
        if (o instanceof Date) {
            return ws.formats().text().escapeText(
                    CoreNutsUtils.DEFAULT_DATE_TIME_FORMATTER.format(((Date) o).toInstant())
            );
        }
        if (o instanceof NutsFormattable) {
            return ws.formats().of((NutsFormattable) o).format();
        }
        if (o instanceof Collection) {
            Collection c = ((Collection) o);
            Object[] a = c.toArray();
            if (a.length == 0) {
                return "";
            }
            if (a.length == 1) {
                return stringValue(a[0]);
            }
            return "\\[" + String.join(", ", (List) c.stream().map(x -> stringValueFormatted(x, escapeString, session)).collect(Collectors.toList())) + "\\]";
        }
        if (o instanceof Map) {
            Map c = ((Map) o);
            Map.Entry[] a = (Map.Entry[]) c.entrySet().toArray(new Map.Entry[0]);
            if (a.length == 0) {
                return "";
            }
            if (a.length == 1) {
                return stringValue(a[0]);
            }
            return "\\{" + String.join(", ", Arrays.stream(a).map(x -> stringValueFormatted(x, escapeString, session)).collect(Collectors.toList())) + "\\}";
        }
        if (o.getClass().isArray()) {
            int len = Array.getLength(o);
            if (len == 0) {
                return "";
            }
            if (len == 1) {
                return stringValueFormatted(Array.get(o, 0), escapeString, session);
            }
            List<String> all = new ArrayList<>(len);
            for (int i = 0; i < len; i++) {
                all.add(stringValueFormatted(Array.get(o, i), escapeString, session));
            }
            return "\\[" + String.join(", ", all) + "\\]";
        }
//        if (o instanceof Iterable) {
//            Iterable x = (Iterable) o;
//            return stringValueFormatted(x.iterator(), escapeString, session);
//        }
        if (o instanceof Iterator) {
            Iterator x = (Iterator) o;
            List<String> all = new ArrayList<>();
            while (x.hasNext()) {
                all.add(stringValueFormatted(x.next(), escapeString, session));
            }
            return stringValueFormatted(all, escapeString, session);
        }
        String s = o.toString();
        if (escapeString) {
            s = session.getWorkspace().formats().text().escapeText(s);
        }
        return s;
    }

    public static String stringValue(Object o) {
        if (o == null) {
            return "";
        }
        if (o.getClass().isEnum()) {
            return getEnumString((Enum) o);
        }
        if (o instanceof Instant) {
            return CoreNutsUtils.DEFAULT_DATE_TIME_FORMATTER.format(((Instant) o));
        }
        if (o instanceof Date) {
            return CoreNutsUtils.DEFAULT_DATE_TIME_FORMATTER.format(((Date) o).toInstant());
        }
        if (o instanceof Collection) {
            Collection c = ((Collection) o);
            Object[] a = c.toArray();
            if (a.length == 0) {
                return "";
            }
            if (a.length == 1) {
                return stringValue(a[0]);
            }
            return "[" + String.join(", ", (List) c.stream().map(x -> stringValue(x)).collect(Collectors.toList())) + "]";
        }
        if (o.getClass().isArray()) {
            int len = Array.getLength(o);
            if (len == 0) {
                return "";
            }
            if (len == 1) {
                return stringValue(Array.get(o, 0));
            }
            List<String> all = new ArrayList<>(len);
            for (int i = 0; i < len; i++) {
                all.add(stringValue(Array.get(o, i)));
            }
            return "[" + String.join(", ", all) + "]";
        }
        return o.toString();
    }

}
