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
package net.thevpc.nuts.boot;

import net.thevpc.nuts.*;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * Created by vpc on 1/15/17.
 *
 * @app.category Internal
 * @since 0.5.4
 */
public final class PrivateNutsUtils {


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
        return NutsOptional.ofEmpty(x -> NutsMessage.cstyle("error with type %s not found", type.getSimpleName()));
    }

    public static boolean isValidWorkspaceName(String workspace) {
        if (NutsBlankable.isBlank(workspace)) {
            return true;
        }
        String workspaceName = workspace.trim();
        return workspaceName.matches("[^/\\\\]+")
                && !workspaceName.equals(".")
                && !workspaceName.equals("..");
    }

    public static String resolveValidWorkspaceName(String workspace) {
        if (NutsBlankable.isBlank(workspace)) {
            return NutsConstants.Names.DEFAULT_WORKSPACE_NAME;
        }
        String workspaceName = workspace.trim();
        if (workspaceName.matches("[^/\\\\]+")
                && !workspaceName.equals(".")
                && !workspaceName.equals("..")) {
            return workspaceName;
        } else {
            String p = null;
            try {
                p = new File(workspaceName).getCanonicalFile().getName();
            } catch (IOException ex) {
                p = new File(workspaceName).getAbsoluteFile().getName();
            }
            if (p.isEmpty() || p.equals(".") || p.equals("..")) {
                return "unknown";
            }
            return p;
        }
    }

    public static String idToPath(NutsId id) {
        return id.getGroupId().replace('.', '/') + "/"
                + id.getArtifactId() + "/" + id.getVersion();
    }

    public static boolean isAbsolutePath(String location) {
        return new File(location).isAbsolute();
    }

    public static String resolveJavaCommand(String javaHome) {
        String exe = NutsOsFamily.getCurrent().equals(NutsOsFamily.WINDOWS) ? "java.exe" : "java";
        if (javaHome == null || javaHome.isEmpty()) {
            javaHome = System.getProperty("java.home");
            if (NutsBlankable.isBlank(javaHome) || "null".equals(javaHome)) {
                //this may happen is using a precompiled image (such as with graalvm)
                return exe;
            }
        }
        return javaHome + File.separator + "bin" + File.separator + exe;
    }

    public static boolean isActualJavaOptions(String options) {
        //FIX ME
        return true;
    }

    public static boolean isActualJavaCommand(String cmd) {
        if (cmd == null || cmd.trim().isEmpty()) {
            return true;
        }
        String javaHome = System.getProperty("java.home");
        if (NutsBlankable.isBlank(javaHome) || "null".equals(javaHome)) {
            return cmd.equals("java") || cmd.equals("java.exe") || cmd.equals("javaw.exe") || cmd.equals("javaw");
        }
        String jh = javaHome.replace("\\", "/");
        cmd = cmd.replace("\\", "/");
        if (cmd.equals(jh + "/bin/java")) {
            return true;
        }
        if (cmd.equals(jh + "/bin/java.exe")) {
            return true;
        }
        if (cmd.equals(jh + "/bin/javaw")) {
            return true;
        }
        if (cmd.equals(jh + "/bin/javaw.exe")) {
            return true;
        }
        if (cmd.equals(jh + "/jre/bin/java")) {
            return true;
        }
        return cmd.equals(jh + "/jre/bin/java.exe");
    }

    public static String desc(Object s) {
        if (s == null) {
            return "<EMPTY>";
        }
        String ss
                = (s instanceof Enum) ? ((Enum<?>) s).name().toLowerCase().replace('_', '-')
                : s.toString().trim();
        return ss.isEmpty() ? "<EMPTY>" : ss;
    }

    public static String coalesce(Object... all) {
        for (Object object : all) {
            if (object != null) {
                return desc(object);
            }
        }
        return desc(null);
    }

    public static String formatLogValue(Object unresolved, Object resolved) {
        String a = PrivateNutsUtils.desc(unresolved);
        String b = PrivateNutsUtils.desc(resolved);
        if (a.equals(b)) {
            return a;
        } else {
            return a + " => " + b;
        }
    }

    public static String formatURL(URL url) {
        if (url == null) {
            return "<EMPTY>";
        }
        File f = PrivateNutsUtilIO.toFile(url);
        if (f != null) {
            return f.getPath();
        }
        return url.toString();
    }

    public static boolean getSysBoolNutsProperty(String property, boolean defaultValue) {
        return
                NutsUtilStrings.parseBoolean(System.getProperty("nuts." + property)).ifEmpty(defaultValue).orElse(false)
                        || NutsUtilStrings.parseBoolean(System.getProperty("nuts.export." + property)).ifEmpty(defaultValue).orElse(false)
                ;
    }


    public static Set<NutsId> parseDependencies(String s) {
        return s == null ? Collections.emptySet() :
                Arrays.stream(s.split(";"))
                        .map(String::trim)
                        .filter(x -> x.length() > 0)
                        .map(x -> NutsId.of(x).get())
                        .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public static int firstIndexOf(String string, char[] chars) {
        char[] value = string.toCharArray();
        for (int i = 0; i < value.length; i++) {
            for (char aChar : chars) {
                if (value[i] == aChar) {
                    return i;
                }
            }
        }
        return -1;
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

    public static boolean isInfiniteLoopThread(String className, String methodName) {
        Thread thread = Thread.currentThread();
        StackTraceElement[] elements = thread.getStackTrace();

        if (elements == null || elements.length == 0) {
            return false;
        }

        for (int i = 0; i < elements.length; i++) {
            StackTraceElement element = elements[elements.length - (i + 1)];
            if (className.equals(element.getClassName())) {
                if (methodName.equals(element.getMethodName())) {
                    return true;
                }
            }
        }
        return false;
    }

    public static String getHome(NutsStoreLocation storeFolder, NutsWorkspaceBootOptions bOptions) {
        return NutsUtilPlatforms.getPlatformHomeFolder(
                bOptions.getStoreLocationLayout(),
                storeFolder,
                bOptions.getHomeLocations(),
                bOptions.isGlobal(),
                bOptions.getName()
        );
    }

    public static <K, V> LinkedHashMap<K, V> copy(Map<K, V> o) {
        if (o == null) {
            return new LinkedHashMap<>();
        }
        return new LinkedHashMap<>(o);
    }

    public static <K> LinkedHashSet<K> copy(Set<K> o) {
        if (o != null) {
            return new LinkedHashSet<>(o);
        }
        return new LinkedHashSet<>();
    }

    public static Integer parseInt(String value, Integer emptyValue, Integer errorValue) {
        if (NutsBlankable.isBlank(value)) {
            return emptyValue;
        }
        value = value.trim();
        try {
            return Integer.parseInt(value);
        } catch (Exception ex) {
            return errorValue;
        }
    }

    public static Integer parseInt16(String value, Integer emptyValue, Integer errorValue) {
        if (NutsBlankable.isBlank(value)) {
            return emptyValue;
        }
        value = value.trim();
        try {
            return Integer.parseInt(value, 16);
        } catch (Exception ex) {
            return errorValue;
        }
    }

    public static NutsOptional<Integer> parseFileSizeInBytes(String value, Integer defaultMultiplier) {
        if (NutsBlankable.isBlank(value)) {
            return NutsOptional.ofEmpty(session -> NutsMessage.cstyle("empty size"));
        }
        value = value.trim();
        Integer i = parseInt(value, null, null);
        if (i != null) {
            if (defaultMultiplier != null) {
                return NutsOptional.of(i * defaultMultiplier);
            } else {
                return NutsOptional.of(i);
            }
        }
        for (String s : new String[]{"kb", "mb", "gb", "k", "m", "g"}) {
            if (value.toLowerCase().endsWith(s)) {
                String v = value.substring(0, value.length() - s.length()).trim();
                i = parseInt(v, null, null);
                if (i != null) {
                    switch (s) {
                        case "k":
                        case "kb":
                            return NutsOptional.of(i * 1024);
                        case "m":
                        case "mb":
                            return NutsOptional.of(i * 1024 * 1024);
                        case "g":
                        case "gb":
                            return NutsOptional.of(i * 1024 * 1024 * 1024);
                    }
                }
            }
        }
        String finalValue = value;
        return NutsOptional.ofError(session->NutsMessage.cstyle("invalid size :%s", finalValue));
    }



}
