package net.thevpc.nuts.toolbox.nwork.filescanner;

import net.thevpc.nuts.toolbox.nwork.filescanner.eval.*;
import net.thevpc.nuts.toolbox.nwork.filescanner.tags.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.UserPrincipal;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class FileScanner {
    public static final String NODEJS_PROJECT = "nodejs-project";
    public static final String GRADLE_PROJECT = "gradle-project";
    public static final String MAVEN_PROJECT = "maven-project";
    public static final String PROJECT_ROOT = "project-root";
    private final List<TagScanner> tagScanners = new ArrayList<>();
    private final List<Path> source = new ArrayList<>();
    private Predicate<RichPath> pathFilter;
    private Predicate<String> supportedTags;

    public FileScanner() {
        getTagScanners().add(new MavenProjectTagScanner());
        getTagScanners().add(new GradleProjectTagScanner());
        getTagScanners().add(new NodeJsProjectTagScanner());
        getTagScanners().add(new GitIgnoreTagScanner());
        getTagScanners().add(new BackupIgnoreTagScanner());
    }


    public static Predicate<RichPath> parseExpr(String anyStr) {
        Evaluator.Node n = new SyntaxParser(anyStr, new ScannerEvaluator()).parse();
        return richPath -> EvalUtils.castToBoolean(n.eval(new RichPathContext(richPath)));
    }

    public List<TagScanner> getTagScanners() {
        return tagScanners;
    }

    public List<Path> getSource() {
        return source;
    }

    public Predicate<RichPath> getPathFilter() {
        return pathFilter;
    }

    public FileScanner setPathFilter(Predicate<RichPath> pathFilter) {
        this.pathFilter = pathFilter;
        return this;
    }

    public Predicate<String> getSupportedTags() {
        return supportedTags;
    }

    public FileScanner setSupportedTags(Predicate<String> supportedTags) {
        this.supportedTags = supportedTags;
        return this;
    }

    public Stream<RichPath> scan() {
        Map<String, Object> shared = new LinkedHashMap<>();
        PathRichPathFunction m = new PathRichPathFunction(this, shared);
        Stream<RichPath> a = null;
        for (Path s : source) {
            try {
                if(Files.isRegularFile(s)){
                    if (a == null) {
                        a = Stream.of(m.apply(s));
                    } else {
                        a = Stream.concat(a, Stream.of(m.apply(s)));
                    }
                }else if(Files.isDirectory(s)){
                    Stream<RichPath> z = Files.walk(s).map(m);
                    if (a == null) {
                        a = z;
                    } else {
                        a = Stream.concat(a, z);
                    }
                }
            } catch (IOException e) {
                throw new IllegalArgumentException(e);
            }
        }
        if (a == null) {
            return Stream.empty();
        }
        Predicate<RichPath> p = getPathFilter();
        if (p != null) {
            a = a.filter(p);
        }
        return a;
    }

    private static class RichPathContext implements Evaluator.Context {
        private final RichPath richPath;

        public RichPathContext(RichPath richPath) {
            this.richPath = richPath;
        }

        @Override
        public Object getVar(String name) {
            switch (name) {
                case "path":
                    return richPath.getPath().toString();
                case "this":
                    return richPath;
                case "name":
                    return richPath.getPath().getFileName().toString();
                case "length":
                case "size": {
                    long size = 0;
                    try {
                        size = Files.size(richPath.getPath());
                    } catch (Exception ex) {
                        //
                    }
                    return size;
                }
                case "dir": {
                    return Files.isDirectory(richPath.getPath());
                }
                case "file": {
                    return Files.isRegularFile(richPath.getPath());
                }
                case "readable": {
                    return Files.isReadable(richPath.getPath());
                }
                case "executable": {
                    return Files.isExecutable(richPath.getPath());
                }
                case "exists": {
                    return Files.exists(richPath.getPath());
                }
                case "hidden": {
                    try {
                        return Files.isHidden(richPath.getPath());
                    } catch (Exception ex) {
                        //
                    }
                    return false;
                }
                case "symbolic": {
                    try {
                        return Files.isSymbolicLink(richPath.getPath());
                    } catch (Exception ex) {
                        //
                    }
                    return false;
                }
                case "writable": {
                    try {
                        return Files.isWritable(richPath.getPath());
                    } catch (Exception ex) {
                        //
                    }
                    return false;
                }
                case "owner": {
                    try {
                        UserPrincipal o = Files.getOwner(richPath.getPath());
                        if (o != null) {
                            return o.getName();
                        }
                    } catch (Exception ex) {
                        //
                    }
                    return null;
                }
                case "lastModified": {
                    try {
                        return Files.getLastModifiedTime(richPath.getPath());
                    } catch (Exception ex) {
                        //
                    }
                    return null;
                }
            }
            return null;
        }
    }

    private static class ScannerEvaluator extends SimpleEvaluator {
        @Override
        public Object evalFunction(FctNode functionNode, Context context) {
            switch (functionNode.getName()) {
                case "tags":
                case "tag": {
                    RichPath rc = (RichPath) context.getVar("this");
                    for (Node arg : functionNode.getArgs()) {
                        Object v = arg.eval(context);
                        if (v != null) {
                            if (rc.getTags(EvalUtils.castToString(v)).size() == 0) {
                                return false;
                            }
                        }
                    }
                    if(rc.getPath().toString().endsWith(".java")){
                        for (Node arg : functionNode.getArgs()) {
                            Object v = arg.eval(context);
                            if (v != null) {
                                if (rc.getTags(EvalUtils.castToString(v)).size() == 0) {
                                    return false;
                                }
                            }
                        }
                    }
                    return true;
                }
            }
            return super.evalFunction(functionNode, context);
        }
    }
}
