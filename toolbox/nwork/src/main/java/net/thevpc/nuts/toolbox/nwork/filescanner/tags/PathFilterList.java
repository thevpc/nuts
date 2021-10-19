package net.thevpc.nuts.toolbox.nwork.filescanner.tags;

import net.thevpc.nuts.toolbox.nwork.filescanner.eval.EvalUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;

class PathFilterList {
    PatternGroupConf includes = new PatternGroupConf();
    PatternGroupConf excludes = new PatternGroupConf();
    List<Path> alreadyIgnored = new ArrayList<>();

    public void loadFrom(Path file) {
        if (Files.isRegularFile(file)) {
            try {
                Files.lines(file).forEach(x -> {
                    add(file.getParent(), x);
                });
            } catch (IOException ex) {
                throw new IllegalArgumentException(ex);
            }
        }
    }

    public boolean markIgnored(Path root) {
        if (!isIgnoredOrParent(root)) {
            if (Files.isDirectory(root)) {
                alreadyIgnored.add(root);
            }
            return true;
        } else {
            return false;
        }
    }

    public boolean isIgnoredOrParent(Path root) {
        for (Path path : alreadyIgnored) {
            if (path.equals(root) || root.startsWith(path)) {
                return true;
            }
        }
        return false;
    }

    public void add(Path root, String line) {
        line = line.trim();
        if (line.isEmpty() || line.startsWith("#")) {
            return;
        }
        if (line.startsWith("!")) {
            line = line.substring(1).trim();
            excludes.add(root, line);
        } else {
            includes.add(root, line);
        }
    }

    public boolean accept(Path p) {
        boolean i = includes.size() > 0;
        boolean e = excludes.size() > 0;
        if (i && e) {
            return includes.accept(p) && !excludes.accept(p);
        } else if (i) {
            return includes.accept(p);
        } else if (e) {
            return !excludes.accept(p);
        } else {
            return true;
        }
    }

    private static class PatternGroupConf {
        HashSet<String> prefixes = new HashSet<>();
        HashSet<String> exact = new HashSet<>();
        HashSet<Pattern> patterns = new HashSet<>();

        public int size() {
            return prefixes.size() + exact.size() + patterns.size();
        }

        public void add(Path root, String line) {
            if (line.isEmpty()) {
                return;
            }
            //if(line.startsWith("!")){
            while (line.startsWith("/")) {
                line = line.substring(1);
            }
            String a = (line.isEmpty() ? root : root.resolve(line)).toString();
            if (a.contains("*")) {
                if (a.endsWith("/*") && !a.substring(0, a.length() - 2).contains("*")) {
                    prefixes.add(a.substring(0, a.length() - 2));
                } else {
                    patterns.add(EvalUtils.glob(a));
                }
            } else {
                exact.add(a);
            }
        }

        public boolean accept(Path path) {
            String s = path.toString();
            if (exact.contains(s)) {
                return true;
            }
            for (String prefix : exact) {
                if (s.startsWith(prefix)) {
                    if (s.length() == prefix.length()) {
                        return true;
                    }
                    return s.charAt(prefix.length()) == '/';
                }
            }
            for (String prefix : prefixes) {
                if (s.startsWith(prefix)) {
                    if (s.length() == prefix.length()) {
                        return true;
                    }
                    return s.charAt(prefix.length()) == '/';
                }
            }
            for (Pattern pattern : patterns) {
                if (pattern.matcher(s).matches()) {
                    return true;
                }
            }
            return false;
        }
    }
}
