package net.thevpc.nuts.runtime.standalone.io.path;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NEDesc;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.runtime.standalone.xtra.glob.GlobUtils;
import net.thevpc.nuts.util.NFunction;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NStream;

import java.util.*;
import java.util.regex.Pattern;

public class DirectoryScanner {
    private NPath initialPattern;
//    private String root;
//    private String patternString;
//    private Pattern pattern;
    private PathPart[] parts;
//    private DirectoryScannerFS fs;

    public DirectoryScanner(NPath pattern) {
        this.initialPattern = pattern.toAbsolute().normalize();
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

    private static boolean containsWildcard(String name){
        char[] patternChars = name.toCharArray();
        for (char c : patternChars) {
            if (c == '*' || c == '?') {
                return true;
            }
        }
        return false;
    }

    private static PathPart[] buildParts(NPath initialPattern) {
        List<PathPart> parts = new ArrayList<>();
        NPath h=initialPattern;
        while(h!=null){
            String name = h.getName();
            if (containsWildcard(name)) {
                if (name.contains("**")) {
                    parts.add(0,new SubPathWildCardPathPart(name));
                } else {
                    parts.add(0,new NameWildCardPathPart(name));
                }
            } else {
                parts.add(0,new PlainPathPart(name));
            }
            NPath p = h.getParent();
            if(p==h){
                h=null;
            }else{
                h=p;
            }
        }
        return parts.toArray(new PathPart[0]);
    }

    @Override
    public String toString() {
        return initialPattern.toString();
    }

    public NPath[] toArray() {
        return stream().toArray(NPath[]::new);
    }

    public NStream<NPath> stream() {
        return stream(null, parts, 0);
    }

    private NStream<NPath> stream(NPath r, PathPart[] parts, int from) {
        for (int i = from; i < parts.length; i++) {
            if (parts[i] instanceof PlainPathPart) {
                if (r == null) {
                    r = initialPattern.getRoot();
                }
                if (r == null) {
                    r= NPath.of(((PlainPathPart) parts[i]).value);
                }else {
                    r = r.resolve(((PlainPathPart) parts[i]).value);
                }
                if(!r.exists()){
                    return NStream.ofEmpty();
                }
            } else if (parts[i] instanceof NameWildCardPathPart) {
                NameWildCardPathPart w = (NameWildCardPathPart) parts[i];
                if (r == null) {
                    r = initialPattern.getRoot();
                }
                if (r == null) {
                    return NStream.ofEmpty();
                }
                NStream<NPath> t = r.stream().filter(x -> w.matchesName(x.getName())).withDesc(NEDesc.of("getName"));
                if (parts.length - i - 1 == 0) {
                    return t;
                } else {
                    int i0 = i;
                    NFunction<NPath, NStream<NPath>> f = NFunction.of((NPath x) -> stream(x, parts, i0 + 1)).withDesc(NEDesc.of("subStream"));
                    return t.flatMapStream((NFunction) f);
                }
            } else if (parts[i] instanceof SubPathWildCardPathPart) {
                SubPathWildCardPathPart w = (SubPathWildCardPathPart) parts[i];
                if (r == null) {
                    r = initialPattern.getRoot();
                }

                NStream<NPath> t = new SubPathWildCardPathPartIterator(w, r).stream();
                if (parts.length - i - 1 == 0) {
                    return t;
                } else {
                    int i0 = i;

                    NFunction<NPath, NStream<NPath>> f = NFunction.of((NPath x) -> stream(x, parts, i0 + 1)).withDesc(NEDesc.of("subStream"));
                    return t.flatMapStream((NFunction) f).distinct();
                }
            } else {
                throw new NIllegalArgumentException(NMsg.ofC("unsupported %s",parts[i]));
            }
        }
        if (r == null) {
            return NStream.ofSingleton(initialPattern.getRoot());
        }
        return NStream.ofSingleton(r);
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

        public boolean matchesSubPath(NPath subPath) {
            return pattern.matcher((subPath==null?"":subPath.toString())).matches();
        }

        @Override
        public String toString() {
            return "Path{" + value + '}';
        }
    }

    private class SubPathWildCardPathPartIterator implements Iterator<NPath> {
        private final Stack<NPath> stack=new Stack<>();
        private final SubPathWildCardPathPart w;
        NPath last;
        NPath root;

        public SubPathWildCardPathPartIterator(SubPathWildCardPathPart w, NPath root) {
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
        public NPath next() {
            return last;
        }

        public NPath next0() {
            while (!stack.isEmpty()) {
                NPath pop = stack.pop();
                NPath[] t = pop.stream().toArray(NPath[]::new);
                for (int i = t.length - 1; i >= 0; i--) {
                    stack.push(t[i]);
                }
                if (w.matchesSubPath(pop.toRelative(root).orNull())) {
                    return pop;
                }
            }
            return null;
        }

        public NStream<NPath> stream() {
            return NStream.of(this);
        }
    }
}
