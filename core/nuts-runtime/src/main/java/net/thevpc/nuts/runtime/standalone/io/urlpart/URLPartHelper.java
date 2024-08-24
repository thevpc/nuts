package net.thevpc.nuts.runtime.standalone.io.urlpart;

import net.thevpc.nuts.NSession;
import net.thevpc.nuts.format.NVisitResult;
import net.thevpc.nuts.io.NIOException;
import net.thevpc.nuts.runtime.standalone.io.util.InputStreamVisitor;
import net.thevpc.nuts.runtime.standalone.io.util.ZipUtils;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public class URLPartHelper {
    static URLPart[] searchStream(InputStream root, Function<String,URLPart> factory, boolean includeFolders, boolean deep, final Predicate<URLPart> filter, NSession session)  {
//        String pv = base.getPath();
//        if (pv.startsWith("/")) {
//            // zip entries does not start with '/'
//            pv = pv.substring(1);
//        }
//        if (!pv.endsWith("/")) {
//            pv += "/";
//        }
//        String finalPv = pv;
        List<URLPart> ff = new ArrayList<>();
        ZipUtils.visitZipStream(root, new InputStreamVisitor() {
            @Override
            public NVisitResult visit(String entry, InputStream inputStream) throws IOException {
                String y=entry;
//                String y = entry.substring(finalPv.length());
                if (y.endsWith("/")) {
                    y = y.substring(0, y.length() - 1);
                }
                if (deep || y.indexOf('/') < 0) {
                    if (y.length() > 0) {
                        if (includeFolders || !entry.endsWith("/")) {

//                            String s = "jar:" + root.toString() + "!";
//                            if (!entry.startsWith("/")) {
//                                s += "/";
//                            }
//                            s += entry;
                            URLPart uuu = factory.apply(entry);

                            if (uuu!=null && (filter == null || filter.test(uuu))) {
                                ff.add(uuu);
                            }
                        }
                    }
                }
                return NVisitResult.CONTINUE;
            }
        }, null);
        return ff.toArray(new URLPart[0]);
    }
//    static URLPart[] searchURL(URL root, URLPart base, boolean includeFolders, boolean deep, final Predicate<URLPart> filter) throws IOException {
//
//        URLConnection urlConnection = root.openConnection();
//        String pv = base.getPath();
//        if (pv.startsWith("/")) {
//            // zip entries does not start with '/'
//            pv = pv.substring(1);
//        }
//        if (!pv.endsWith("/")) {
//            pv += "/";
//        }
//        if (urlConnection instanceof JarURLConnection) {
//            List<URLPart> ff = new ArrayList<>();
//            JarURLConnection urlcon = (JarURLConnection) urlConnection;
//            try (final JarFile jar = urlcon.getJarFile()) {
//                Enumeration<JarEntry> entries = jar.entries();
//                while (entries.hasMoreElements()) {
//                    String entry = entries.nextElement().getName();
//                    if (entry.startsWith(pv)) {
//                        String y = entry.substring(pv.length());
//                        if (y.endsWith("/")) {
//                            y = y.substring(0, y.length() - 1);
//                        }
//                        if (deep || y.indexOf('/') < 0) {
//                            if (y.length() > 0) {
//                                if (includeFolders || !entry.endsWith("/")) {
//                                    StringBuilder s = new StringBuilder();
//                                    s.append(root.toString());
//                                    s.append(entry);
//                                    final URL uuu = new URL(s.toString());
//                                    URLPart p= URLPart.of(uuu);
//                                    if (filter == null || filter.test(p)) {
//                                        ff.add(p);
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//            return ff.toArray(new URLPart[0]);
//        } else {
//            try(InputStream is=root.openStream()) {
//                return searchStream(is,base, includeFolders, deep, filter);
//            }
//        }
//    }

    static URLPart[] searchFile(File root, boolean includeFolders, boolean deep, final Predicate<URLPart> filter,NSession session) {
        if (deep) {
            List<URLPart> found = new ArrayList<>();
            try {
                Files.walkFileTree(
                        root.toPath(),
                        new FileVisitor<Path>() {
                            @Override
                            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                                if (includeFolders) {
                                    URLPart url = URLPart.of(dir.toFile());
                                    if (filter == null) {
                                        found.add(url);
                                    } else {
                                        if (filter.test(url)) {
                                            found.add(url);
                                        }
                                    }
                                }
                                return FileVisitResult.CONTINUE;
                            }

                            @Override
                            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                                URLPart url = URLPart.of(file.toFile());
                                if (filter == null) {
                                    found.add(url);
                                } else {
                                    if (filter.test(url)) {
                                        found.add(url);
                                    }
                                }
                                return FileVisitResult.CONTINUE;
                            }

                            @Override
                            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                                return FileVisitResult.CONTINUE;
                            }

                            @Override
                            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                                return FileVisitResult.CONTINUE;
                            }
                        }
                );
            } catch (IOException e) {
                throw new NIOException(session, e);
            }
            return found.toArray(new URLPart[0]);
        } else {
            final File[] listFiles = root.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    if (pathname.isFile() || (includeFolders && pathname.isDirectory())) {
                        return filter == null || filter.test(URLPart.of(pathname));
                    }
                    return false;
                }
            });
            if (listFiles == null) {
                return new URLPart[0];
            }
            URLPart[] found = new URLPart[listFiles.length];
            for (int j = 0; j < found.length; j++) {
                found[j] = URLPart.of(listFiles[j]);
            }
            return found;
        }
    }
}
