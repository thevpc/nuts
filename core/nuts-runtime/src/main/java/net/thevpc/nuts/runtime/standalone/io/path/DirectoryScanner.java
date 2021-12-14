package net.thevpc.nuts.runtime.standalone.io.path;

import net.thevpc.nuts.NutsFunction;
import net.thevpc.nuts.NutsPath;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.NutsStream;
import net.thevpc.nuts.runtime.standalone.xtra.glob.GlobUtils;

import java.util.*;
import java.util.regex.Pattern;

public class DirectoryScanner {
    private NutsPath initialPattern;
    private String root;
    private String patternString;
    private Pattern pattern;
    private PathPart[] parts;
//    private DirectoryScannerFS fs;
    private NutsSession session;

    public DirectoryScanner(NutsPath pattern,NutsSession session) {
        this.initialPattern = pattern;
        this.session = session;
        parts = buildParts(initialPattern);
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

    private static PathPart[] buildParts(NutsPath initialPattern) {
        List<PathPart> parts = new ArrayList<>();
        char[] patternChars = initialPattern.getLocation().toCharArray();
        int pos = 0;
        while (pos < patternChars.length) {
            int x = pos;
            boolean someWildCards = false;
            while (x < patternChars.length && patternChars[x] != '/' && patternChars[x] != '\\') {
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

    public NutsPath[] toArray() {
        return stream().toArray(NutsPath[]::new);
    }

    public NutsStream<NutsPath> stream() {
        return stream(null, parts, 0);
    }


    private NutsStream<NutsPath> stream(NutsPath r, PathPart[] parts, int from) {
        for (int i = from; i < parts.length; i++) {
            if (parts[i] instanceof PlainPathPart) {
                if (r == null) {
                    r = initialPattern.getRoot();
                }
                r = r.resolve(((PlainPathPart) parts[i]).value);
                if(!r.exists()){
                    return NutsStream.ofEmpty(session);
                }
            } else if (parts[i] instanceof NameWildCardPathPart) {
                NameWildCardPathPart w = (NameWildCardPathPart) parts[i];
                if (r == null) {
                    r = initialPattern.getRoot();
                }
                NutsStream<NutsPath> t = r.list().filter(x -> w.matchesName(x.getName()),"getName");
                if (parts.length - i - 1 == 0) {
                    return t;
                } else {
                    int i0 = i;
                    NutsFunction<NutsPath, NutsStream<NutsPath>> f = NutsFunction.of(x -> stream(x, parts, i0 + 1),"subStream");
                    return t.flatMap((NutsFunction) f);
                }
            } else if (parts[i] instanceof SubPathWildCardPathPart) {
                SubPathWildCardPathPart w = (SubPathWildCardPathPart) parts[i];
                if (r == null) {
                    r = initialPattern.getRoot();
                }

                NutsStream<NutsPath> t = new SubPathWildCardPathPartIterator(w, r).stream();
                if (parts.length - i - 1 == 0) {
                    return t;
                } else {
                    int i0 = i;

                    NutsFunction<NutsPath, NutsStream<NutsPath>> f = NutsFunction.of(x -> stream(x, parts, i0 + 1),"subStream");
                    return t.flatMap((NutsFunction) f).distinct();
                }
            } else {
                throw new IllegalArgumentException("Unsupported " + parts[i]);
            }
        }
        if (r == null) {
            return NutsStream.ofSingleton(initialPattern.getRoot(),session);
        }
        return NutsStream.ofSingleton(r,session);
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
            this.pattern = GlobUtils.glob(value,"/\\");
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
            this.pattern = GlobUtils.glob(value,"/\\");
        }

        public boolean matchesSubPath(NutsPath subPath) {
            return pattern.matcher(subPath.toString()).matches();
        }

        @Override
        public String toString() {
            return "Path{" + value + '}';
        }
    }

    private class SubPathWildCardPathPartIterator implements Iterator<NutsPath> {
        private final Stack<NutsPath> stack=new Stack<>();
        private final SubPathWildCardPathPart w;
        NutsPath last;
        NutsPath root;

        public SubPathWildCardPathPartIterator(SubPathWildCardPathPart w, NutsPath root) {
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
        public NutsPath next() {
            return last;
        }

        public NutsPath next0() {
            while (!stack.isEmpty()) {
                NutsPath pop = stack.pop();
                NutsPath[] t = pop.list().toArray(NutsPath[]::new);
                for (int i = t.length - 1; i >= 0; i--) {
                    stack.push(t[i]);
                }
                if (w.matchesSubPath(pop.toRelativePath(root))) {
                    return pop;
                }
            }
            return null;
        }

        public NutsStream<NutsPath> stream() {
            return NutsStream.of(this,session);
        }
    }
}
