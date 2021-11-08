package net.thevpc.nuts.toolbox.nsh.jshell.util;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class DirectoryScanner {
    public static PathDirectoryScannerFS PATH_FILE_SYSTEM=new PathDirectoryScannerFS();
    private String initialPattern;
    private String root;
    private String patternString;
    private Pattern pattern;
    private PathPart[] parts;
    private DirectoryScannerFS fs;

    public DirectoryScanner(String pattern) {
        this(pattern, PATH_FILE_SYSTEM);
    }

    public DirectoryScanner(String pattern, DirectoryScannerFS fs) {
        this.initialPattern = pattern;
        this.fs = fs;
        parts = buildParts(initialPattern);
//        this.pattern = Pattern.compile(ShellUtils.simpexpToRegexp(pattern));
    }

    private DirectoryScanner(String root, PathPart[] subPaths, DirectoryScannerFS fs) {
        this.fs = fs;
        this.root = root;
        this.parts = subPaths;
    }
    public static String escape(String s){
        StringBuilder sb=new StringBuilder();
        for (char c : s.toCharArray()) {
            switch (c){
                case '\\':
                case '*':
                case '?':{
                    sb.append('\\').append(c);
                    break;
                }
                default:{
                    sb.append(c);
                    break;
                }
            }
        }
        return sb.toString();
    }

    private static PathPart[] buildParts(String initialPattern) {
        List<PathPart> parts = new ArrayList<>();
        char[] patternChars = initialPattern.toCharArray();
        int pos = 0;
        while (pos < patternChars.length) {
            int x = pos;
            boolean someWildCards = false;
            while (x < patternChars.length && patternChars[x] != '/') {
                if (patternChars[x] == '*' || patternChars[x] == '?') {
                    someWildCards = true;
                }
                x++;
            }
            if (x >= patternChars.length) {
                String s = new String(patternChars, pos, patternChars.length - pos);
                if (someWildCards) {
                    if (s.contains("**")) {
                        parts.add(new SubPathWildCardPathPart(s));
                    } else {
                        parts.add(new NameWildCardPathPart(s));
                    }
                } else {
                    parts.add(new PlainPathPart(s));
                }
                pos = x;
            } else if (x > pos) {
                String s = new String(patternChars, pos, x - pos);
                if (someWildCards) {
                    if (s.contains("**")) {
                        parts.add(new SubPathWildCardPathPart(s));
                    } else {
                        parts.add(new NameWildCardPathPart(s));
                    }
                } else {
                    parts.add(new PlainPathPart(s));
                }
                pos = x;
            } else {
                //ignore
                pos++;
            }
        }
        return parts.toArray(new PathPart[0]);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (PathPart part : parts) {
            sb.append("/");
            sb.append(part.toString());
        }
        return sb.toString();
    }

    public String[] toArray() {
        return stream().toArray(String[]::new);
    }

    public Stream<String> stream() {
        return stream(null, parts, 0);
    }


    private Stream<String> stream(String r, PathPart[] parts, int from) {
        System.out.println("stream " + r + " "+Arrays.asList(parts).subList(from,parts.length));
        for (int i = from; i < parts.length; i++) {
            if (parts[i] instanceof PlainPathPart) {
                if (r == null) {
                    r = fs.root();
                }
                r = fs.resolve(r, ((PlainPathPart) parts[i]).value);
                if(!fs.exists(r)){
                    return Stream.empty();
                }
            } else if (parts[i] instanceof NameWildCardPathPart) {
                NameWildCardPathPart w = (NameWildCardPathPart) parts[i];
                if (r == null) {
                    r = fs.root();
                }
                Stream<String> t = fs.dirImmediateStream(r).filter(x -> w.matchesName(fs.fileName(x)));
                if (parts.length - i - 1 == 0) {
                    return t;
                } else {
                    int i0 = i;

//                    String[] ee = t.toArray(String[]::new);
//                    t=Arrays.stream(ee);

                    return t.flatMap(x -> stream(x, parts, i0 + 1));


                }
            } else if (parts[i] instanceof SubPathWildCardPathPart) {
                SubPathWildCardPathPart w = (SubPathWildCardPathPart) parts[i];
                if (r == null) {
                    r = fs.root();
                }

                Stream<String> t = new SubPathWildCardPathPartIterator(w, r).stream();
                if (parts.length - i - 1 == 0) {
                    return t;
                } else {
                    int i0 = i;
                    return t.flatMap(x -> stream(x, parts, i0 + 1)).distinct();
                }
            } else {
                throw new IllegalArgumentException("Unsupported " + parts[i]);
            }
        }
        if (r == null) {
            return Stream.of(fs.root());
        }
        return Stream.of(r);
    }

    public interface DirectoryScannerFS {

        String relativePath(String parent, String child);

        String resolve(String parent, String child);

        String fileName(String path);

        String root();

        Stream<String> dirImmediateStream(String dir);

        boolean exists(String dir);

        boolean isAbsolute(String value);

    }

    private static class PathPart {

    }

    private static class PlainPathPart extends PathPart {
        String value;

        public PlainPathPart(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return "Plain{" + value + '}';
        }
    }

    private static class NameWildCardPathPart extends PathPart {
        String value;
        Pattern pattern;

        public NameWildCardPathPart(String value) {
            this.value = value;
            this.pattern = Pattern.compile(ShellUtils.simpexpToRegexp(value));
        }

        public boolean matchesName(String name) {
            return pattern.matcher(name).matches();
        }

        @Override
        public String toString() {
            return "Name{" + value + '}';
        }
    }

    private static class SubPathWildCardPathPart extends PathPart {
        String value;
        Pattern pattern;

        public SubPathWildCardPathPart(String value) {
            this.value = value;
            this.pattern = Pattern.compile(ShellUtils.simpexpToRegexp(value));
        }

        public boolean matchesSubPath(String subPath) {
            return pattern.matcher(subPath).matches();
        }

        @Override
        public String toString() {
            return "Path{" + value + '}';
        }
    }

    public static class PathDirectoryScannerFS implements DirectoryScannerFS {

        @Override
        public String relativePath(String parent, String child) {
            if (child.startsWith(parent)) {
                child = child.substring(parent.length());
                if (child.startsWith("/")) {
                    child = child.substring(1);
                }
                return child;
            }
            return "";
        }

        @Override
        public String resolve(String parent, String child) {
            return Paths.get(parent).resolve(child).toString();
        }

        @Override
        public String fileName(String path) {
            return Paths.get(path).getFileName().toString();
        }

        @Override
        public String root() {
            return "/";
        }

        @Override
        public boolean exists(String dir) {
            return Files.exists(Paths.get(dir));
        }

        public Stream<String> dirImmediateStream(String dir) {
            Path path = Paths.get(dir);
            if (Files.isDirectory(path)) {
                DirectoryStream<Path> paths = null;
                try {
                    paths = Files.newDirectoryStream(path);
                } catch (IOException ex) {
                    throw new UncheckedIOException(ex);
                }
                return StreamSupport.stream(paths.spliterator(), false).map(Path::toString);
            }
            return Stream.empty();
        }

        public boolean isAbsolute(String value) {
            return Paths.get(value).isAbsolute();
        }
    }

    public static abstract class SimpleDirectoryScannerFS implements DirectoryScannerFS {
        private String sep;

        public SimpleDirectoryScannerFS(String sep) {
            this.sep = sep;
        }

        @Override
        public boolean exists(String dir) {
            return dir.startsWith(sep);
        }

        @Override
        public boolean isAbsolute(String value) {
            return false;
        }

        @Override
        public String relativePath(String parent, String child) {
            if (child.startsWith(parent)) {
                child = child.substring(parent.length());
                if (child.startsWith(sep)) {
                    child = child.substring(1);
                }
                return child;
            }
            return "";
        }

        @Override
        public String resolve(String parent, String child) {
            if (parent.endsWith(sep)) {
                return parent + child;
            }
            return parent + sep + child;
        }

        @Override
        public String fileName(String path) {
            int i = path.lastIndexOf(sep);
            if (i >= 0) {
                return path.substring(i + 1);
            }
            return path;
        }

        @Override
        public String root() {
            return sep;
        }

        public Stream<String> dirImmediateStream(String dir) {
            return Stream.empty();
        }
    }

    private class SubPathWildCardPathPartIterator implements Iterator<String> {
        private final Stack<String> stack=new Stack<>();
        private final SubPathWildCardPathPart w;
        String last;
        String root;

        public SubPathWildCardPathPartIterator(SubPathWildCardPathPart w, String root) {
            stack.push(root);
            this.w = w;
            this.root = root;
        }

        @Override
        public boolean hasNext() {
            last = next0();
            return last != null;
        }

        @Override
        public String next() {
            return last;
        }

        public String next0() {
            while (!stack.isEmpty()) {
                String pop = stack.pop();
                String[] t = fs.dirImmediateStream(pop).toArray(String[]::new);
                for (int i = t.length - 1; i >= 0; i--) {
                    stack.push(t[i]);
                }
                if (w.matchesSubPath(fs.relativePath(root, pop))) {
                    return pop;
                }
            }
            return null;
        }

        public Stream<String> stream() {
            return StreamSupport.stream(
                    Spliterators.spliteratorUnknownSize(this, Spliterator.ORDERED),
                    false
            );
        }
    }
}
