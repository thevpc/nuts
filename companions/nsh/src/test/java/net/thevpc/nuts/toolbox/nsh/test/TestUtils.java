/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.nsh.test;

import net.thevpc.nuts.Nuts;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.NVersion;
import net.thevpc.nuts.NWorkspace;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author thevpc
 */
public class TestUtils {

    public static final String LINUX_LOG = new File(System.getProperty("user.home") + "/.local/log/nuts").getPath();
    public static final String LINUX_APPS = new File(System.getProperty("user.home") + "/.local/share/nuts/apps").getPath();
    public static final String LINUX_CONFIG = new File(System.getProperty("user.home") + "/.config/nuts").getPath();
    public static final String LINUX_CACHE = new File(System.getProperty("user.home") + "/.cache/nuts").getPath();
    public static final String LINUX_TEMP = new File(System.getProperty("java.io.tmpdir") + "/" + System.getProperty("user.name") + "/nuts").getPath();
    public static final String[] NUTS_STD_FOLDERS = {LINUX_CONFIG, LINUX_CACHE, LINUX_TEMP, LINUX_APPS};
    public static final NVersion NUTS_VERSION = Nuts.getVersion();

    public static Set<String> createNamesSet(String... names) {
        return new HashSet<String>(Arrays.asList(names));
    }

    public static int count(File d, FileFilter f) {
        return list(d, f).length;
    }

    public static String[] sarr(String[] s, String... a) {
        return concat(s, sarr(a));
    }

    public static String[] sarr(String[] s1, String[] s2, String... a) {
        return concat(s1, s2, sarr(a));
    }

    public static String[] sarr(String... a) {
        return a;
    }

    public static String[] concat(String[]... a) {
        return Stream.of(a).flatMap(Stream::of)
                .toArray(String[]::new);
    }

    public static String[] createSysDirs(File base) {
        return new String[]{
                "--system-bin-home", new File(base, "system.bin").getPath(),
                "--system-conf-home", new File(base, "system.conf").getPath(),
                "--system-var-home", new File(base, "system.var").getPath(),
                "--system-log-home", new File(base, "system.log").getPath(),
                "--system-temp-home", new File(base, "system.temp").getPath(),
                "--system-cache-home", new File(base, "system.cache").getPath(),
                "--system-lib-home", new File(base, "system.lib").getPath(),
                "--system-run-home", new File(base, "system.run").getPath()
        };
//        Stream.concat(Arrays.stream(a), Arrays.stream(b))
//                .toArray(String[]::new)
    }

    public static Set<String> listNamesSet(File d, FileFilter f) {
        return Arrays.stream(list(d, f)).map(x -> x.getName()).collect(Collectors.toSet());
    }

    public static File[] list(File d, FileFilter f) {
        if (!d.isDirectory()) {
            return new File[0];
        }
        return d.listFiles(f);
    }

    public static void setSystemProperties(Map<String, String> params) {
        if (params != null) {
            final Properties p = System.getProperties();
            for (Map.Entry<String, String> entry : params.entrySet()) {
                p.put(entry.getKey(), entry.getValue());
            }
        }
    }

    public static void unsetNutsSystemProperties() {
        final Properties props = System.getProperties();
        for (Object k : new HashSet(props.keySet())) {
            String ks = String.valueOf(k);
            if (ks.startsWith("nuts.")) {
                TestUtils.println("## removed " + ks + "=" + props.getProperty(ks));
                props.remove(ks);
            } else if (ks.startsWith("nuts_")) {
                TestUtils.println("## removed " + ks + "=" + props.getProperty(ks));
                props.remove(ks);
            }
        }
    }

    public static void unsetSystemProperties(Map<String, String> params) {
        if (params != null) {
            final Properties p = System.getProperties();
            for (Map.Entry<String, String> entry : params.entrySet()) {
                p.remove(entry.getKey());
            }
        }
    }

    public static String getCallerSimpleClassMethod(int index) {
        StackTraceElement i = getCallerStackTraceElement0(3 + index);
        String cn = i.getClassName();
        String m = i.getClassName();
        return cn + "." + m;
    }

    public static File getTestFolder() {
        String test_id = TestUtils.getCallerMethodId(2);
        try {
            return new File("./runtime/test/" + test_id).getCanonicalFile();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public static Path locateFolder(String a) {
        String test_id = TestUtils.getCallerMethodId(1);
        File path;
        try {
            path = new File("./runtime/test/" + test_id+"/"+a).getCanonicalFile();
            return path.toPath();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
    public static Path initFolder(String a) {
        String test_id = TestUtils.getCallerMethodId(1);
        File path;
        try {
            path = new File("./runtime/test/" + test_id+"/"+a).getCanonicalFile();
            delete(null, path);
            return path.toPath();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public static NWorkspace openNewTestWorkspace(String... args) {
        return openOrReOpenTestWorkspace(true,false,args);
    }
    public static NWorkspace openExistingTestWorkspace(String... args) {
        return openOrReOpenTestWorkspace(false,false,args);
    }
    public static NWorkspace runNewTestWorkspace(String... args) {
        return openOrReOpenTestWorkspace(true,true,args);
    }
    public static NWorkspace runExistingTestWorkspace(String... args) {
        return openOrReOpenTestWorkspace(false,true,args);
    }

    public static File getTestBaseFolder() {
        String test_id = TestUtils.getCallerMethodId(1);
        File path;
        try {
            return new File("./runtime/test/" + test_id).getCanonicalFile();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private static NWorkspace openOrReOpenTestWorkspace(boolean deleteFolder, boolean run, String... args) {
        String test_id = TestUtils.getCallerMethodId(2);
        File path;
        try {
            path = new File("./runtime/test/" + test_id).getCanonicalFile();
            if(deleteFolder) {
                delete(null, path);
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
        List<String> argsList=new ArrayList<>();
        //create a new workspace for each new test case
        argsList.add("--workspace="+path.getPath());
        //workspace will contain all the data (do not consider system XDG layout)
        //this is not mandatory though, as we are creating a path based workspace
        argsList.add("--standalone");
        //disable creation of desktop icons
        argsList.add("--desktop-launcher=unsupported");
        //disable creation of desktop menus
        argsList.add("--menu-launcher=unsupported");
        //disable creation of any icons
        argsList.add("--user-launcher=unsupported");
        //disable creating of bashrc, etc...
        argsList.add("--!switch");
        //disable auto-detection of java
        argsList.add("--!init-platforms");
        //disable auto-creation of nuts scripts
        argsList.add("--!init-scripts");
        //disable auto-creation of nuts icons and menus
        argsList.add("--!init-launchers");
        //disable progress indicator
        argsList.add("--!progress");
        //disable interactive mode and 'always confirm'
        argsList.add("--yes");
        //disable companions
        argsList.add("--install-companions=false");
        argsList.add("--shared-instance=true");
//        argsList.add("--embedded");
        argsList.addAll(Arrays.asList(args));
        if(run){
            return Nuts.runWorkspace(argsList.toArray(new String[0]));
        }else {
            return Nuts.openWorkspace(argsList.toArray(new String[0]));
        }
    }

    public static File getAndResetTestFolder() {
        String test_id = TestUtils.getCallerMethodId(2);
        try {
            File path = new File("./runtime/test/" + test_id).getCanonicalFile();
            delete(null, path);
            return path;
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public static String getCallerMethodId() {
        return getCallerMethodId(1);
    }

    public static String getCallerMethodId(int index) {
        StackTraceElement i = getCallerStackTraceElement0(3 + index);
        String cn = i.getClassName();
        if (cn.indexOf('.') > 0) {
            cn = cn.substring(cn.lastIndexOf('.') + 1);
        }
        String m = i.getMethodName();
        return cn + "_" + m;
    }

    public static String getCallerMethodName() {
        return getCallerMethodId(1);
    }

    public static String getCallerMethodName(int index) {
        return getCallerStackTraceElement0(3 + index).getMethodName();
    }

    public static String getCallerClassSimpleName() {
        return getCallerClassSimpleName(1);
    }

    public static String getCallerClassSimpleName(int index) {
        StackTraceElement i = getCallerStackTraceElement0(3 + index);
        String cn = i.getClassName();
        if (cn.indexOf('.') > 0) {
            cn = cn.substring(cn.lastIndexOf('.') + 1);
        }
        return cn;
    }

    public static StackTraceElement getCallerStackTraceElement(int index) {
        return getCallerStackTraceElement0(3 + index);
    }

    public static StackTraceElement getCallerStackTraceElement() {
        return getCallerStackTraceElement0(3);
    }

    public static StackTraceElement getCallerStackTraceElement0(int index) {
        StackTraceElement[] s = Thread.currentThread().getStackTrace();
        return s[index];
    }

    public static void println(Object any) {
        System.out.println("[TEST] "+any);
    }
    public static void print(Object any) {
        System.out.print("[TEST] "+any);
    }


    public static void delete(NWorkspace ws, File file) throws IOException {
        delete(ws, file.toPath());
    }

    public static void delete(Path file) throws IOException {
        delete(null, file);
    }

    public static void delete(NWorkspace ws, Path file) throws IOException {
        if (!Files.exists(file)) {
            return;
        }
        if (Files.isRegularFile(file)) {
            try {
                Files.delete(file);
            } catch (IOException e) {
                return;
            }
        }
        final int[] deleted = new int[]{0, 0, 0};
        Files.walkFileTree(file, new FileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                try {
                    Files.delete(file);
                    deleted[0]++;
                } catch (IOException e) {
                    deleted[2]++;
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                try {
                    Files.delete(dir);
                    deleted[1]++;
                } catch (IOException e) {
                    deleted[2]++;
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }

}
