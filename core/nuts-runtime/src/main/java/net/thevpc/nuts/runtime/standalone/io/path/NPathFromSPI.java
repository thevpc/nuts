package net.thevpc.nuts.runtime.standalone.io.path;


import net.thevpc.nuts.NStoreType;
import net.thevpc.nuts.NWorkspace;
import net.thevpc.nuts.format.NTreeVisitor;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.runtime.standalone.io.path.spi.NPathSPIHelper;
import net.thevpc.nuts.runtime.standalone.io.path.spi.URLPath;
import net.thevpc.nuts.runtime.standalone.util.reflect.NUseDefaultUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceVarExpansionFunction;
import net.thevpc.nuts.runtime.standalone.xtra.expr.StringPlaceHolderParser;
import net.thevpc.nuts.spi.NPathSPI;
import net.thevpc.nuts.util.*;

import java.io.*;
import java.net.URL;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class NPathFromSPI extends NPathBase {
    private final NPathSPI base;
    private List<String> items;

    public NPathFromSPI(NPathSPI base) {
        super();
        this.base = base;
    }

    public NPathSPI getBase() {
        return base;
    }

    @Override
    public NPath copy() {
        return new NPathFromSPI(base).copyExtraFrom(this);
    }

    @Override
    public String contentEncoding() {
        return base.getContentEncoding(this);
    }

    @Override
    public String getContentType() {
        return base.getContentType(this);
    }

    @Override
    public String getCharset() {
        return base.getCharset(this);
    }

    @Override
    public String getName() {
        String n = base.getName(this);
        if (n == null) {
            String loc = getLocation();
            return loc == null ? "" : URLPath.getURLName(loc);
        }
        return n;
    }

    @Override
    public String getLocation() {
        String p = base.getLocation(this);
        if (p != null) {
            return p;
        }
        String str = toString();
        int u = str.indexOf(':');
        if (u > 0) {
            String ss = str.substring(0, u);
            if (ss.matches("[a-zA-Z][a-zA-Z-_0-9]*")) {
                String a = str.substring(u + 1);
                if (a.startsWith("//")) {
                    return a.substring(1);
                }
                return a;
            }
        }
        return str;
    }

    @Override
    public NPath resolve(String other) {
        if (NBlankable.isBlank(other)) {
            return this;
        }
        NPath p = base.resolve(this, other);
        if (p != null) {
            return p;
        }
        String old = toString();
        return NPath.of(NStringUtils.pjoin("/", old, other));
    }

    @Override
    public NPath resolve(NPath other) {
        NPath p = base.resolve(this, other.getLocation());
        if (p != null) {
            return p;
        }
        String old = toString();
        return NPath.of(NStringUtils.pjoin("/", old, other.getLocation()));
    }

    @Override
    public NPath resolveSibling(String other) {
        if (NBlankable.isBlank(other)) {
            return getParent();
        }
        NPath p = base.resolveSibling(this, other);
        if (p != null) {
            return p;
        }
        NPath parent = getParent();
        return parent.resolve(other);
    }

    @Override
    public NPath resolveSibling(NPath other) {
        if (NBlankable.isBlank(other)) {
            return getParent();
        }
        NPath p = base.resolveSibling(this, other.getLocation());
        if (p != null) {
            return p;
        }
        NPath parent = getParent();
        return parent.resolve(other);
    }

    @Override
    public byte[] readBytes(NPathOption... options) {
        long len = this.contentLength();
        int readSize = 1024;
        if (len < 0) {
            //unknown size!
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buffer = new byte[readSize];
            try (InputStream is = getInputStream(options)) {
                int count = 0;
                int offset = 0;
                while ((count = is.read(buffer, 0, readSize)) > 0) {
                    bos.write(buffer, 0, count);
                }
                return bos.toByteArray();
            } catch (IOException e) {
                throw new NIOException(NMsg.ofC("unable to read file %s", this), e);
            }
        }
        int ilen = (int) len;
        if (len > Integer.MAX_VALUE - 8) {
            throw new NIOException(NMsg.ofC("file is too large %s", this));
        }
        byte[] buffer = new byte[ilen];
        try (InputStream is = getInputStream(options)) {
            int count = 0;
            int offset = 0;
            while ((count = is.read(buffer, offset, ilen - offset)) > 0) {
                offset += count;
            }
            if (offset < ilen) {
                throw new NIOException(NMsg.ofC("premature read stop %s", this));
            }
            if (is.read() >= 0) {
                throw new NIOException(NMsg.ofC("invalid %s", this));
            }
            return buffer;
        } catch (IOException e) {
            throw new NIOException(NMsg.ofC("unable to read file %s", this), e);
        }
    }

    @Override
    public NPath writeBytes(byte[] bytes, NPathOption... options) {
        try (OutputStream os = getOutputStream()) {
            os.write(bytes);
        } catch (IOException ex) {
            throw new NIOException(NMsg.ofC("unable to write to %s", this));
        }
        return this;
    }

    @Override
    public String getProtocol() {
        String n = base.getProtocol(this);
        if (n == null) {
            String ts = base.toString();
            int i = ts.indexOf(':');
            if (i >= 0) {
                return ts.substring(0, i);
            }
            return null;
        }
        return n;
    }

    @Override
    public NPath toCompressedForm() {
        NPath n = base.toCompressedForm(this);
        if (n == null) {
            return new NCompressedPath(this, new DefaultNCompressedPathHelper());
        }
        return n;
    }

    @Override
    public NOptional<URL> toURL() {
        return base.toURL(this);
    }

    @Override
    public NOptional<Path> toPath() {
        return base.toPath(this);
    }

    @Override
    public NOptional<File> toFile() {
        return base.toPath(this).map(Path::toFile);
    }

    @Override
    public NStream<NPath> stream() {
        NStream<NPath> p = base.list(this);
        if (p != null) {
            return p;
        }
        return NStream.ofEmpty();
    }

    @Override
    public InputStream getInputStream(NPathOption... options) {
        return NInputSourceBuilder.of(base.getInputStream(this, options))
                .setMetadata(getMetaData())
                .createInputStream();
    }

    @Override
    public OutputStream getOutputStream(NPathOption... options) {
        return NOutputStreamBuilder.of(base.getOutputStream(this, options))
                .setMetadata(this.getMetaData())
                .createOutputStream()
                ;
    }

    @Override
    public NPath deleteTree() {
        base.delete(this, true);
        return this;
    }

    @Override
    public NPath ensureEmptyDirectory() {
        if (exists()) {
            if (isDirectory()) {
                try (NStream<NPath> stream = stream()) {
                    stream.forEach(x -> {
                        x.deleteTree();
                    });
                }
            } else {
                deleteTree();
                mkdirs();
            }
        } else {
            mkdir(true);
        }
        return this;
    }

    @Override
    public NPath ensureEmptyFile() {
        mkParentDirs();
        if (exists()) {
            if (!isRegularFile()) {
                deleteTree();
            }
        }
        writeBytes(new byte[0]);
        return this;
    }

    @Override
    public NPath delete(boolean recurse) {
        base.delete(this, recurse);
        return this;
    }

    @Override
    public NPath mkdir(boolean parents) {
        base.mkdir(parents, this);
        return this;
    }

    @Override
    public NPath mkdirs() {
        base.mkdir(true, this);
        return this;
    }

    @Override
    public NPath mkdir() {
        base.mkdir(false, this);
        return this;
    }

    public NPath expandPath(Function<String, String> resolver) {
        resolver = new EffectiveResolver(resolver);
        String s = StringPlaceHolderParser.replaceDollarPlaceHolders(toString(), resolver);
        if (s.length() > 0) {
            if (s.startsWith("~")) {
                if (s.equals("~~")) {
                    NWorkspace workspace = NWorkspace.of();
                    NPath nutsHome = workspace.getHomeLocation(NStoreType.CONF);
                    return nutsHome.normalize();
                } else if (s.startsWith("~~") && s.length() > 2 && (s.charAt(2) == '/' || s.charAt(2) == '\\')) {
                    NWorkspace workspace = NWorkspace.of();
                    NPath nutsHome = workspace.getHomeLocation(NStoreType.CONF);
                    return nutsHome.resolve(s.substring(3)).normalize();
                } else if (s.equals("~")) {
                    return NPath.ofUserHome();
                } else if (s.startsWith("~") && s.length() > 1 && (s.charAt(1) == '/' || s.charAt(1) == '\\')) {
                    return NPath.ofUserHome().resolve(s.substring(2));
                } else {
                    return NPath.of(s);
                }
            }
        }
        return NPath.of(s);
    }

    @Override
    public NPath mkParentDirs() {
        NPath p = getParent();
        if (p != null) {
            p.mkdir(true);
        }
        return this;
    }

    @Override
    public boolean isOther() {
        return base.type(this) == NPathType.OTHER;
    }

    @Override
    public boolean isSymbolicLink() {
        return base.type(this) == NPathType.SYMBOLIC_LINK;
    }

    @Override
    public boolean isDirectory() {
        return base.type(this) == NPathType.DIRECTORY;
    }

    @Override
    public boolean isRegularFile() {
        return base.type(this) == NPathType.FILE;
    }

    @Override
    public boolean isRemote() {
        return !base.isLocal(this);
    }

    @Override
    public boolean isLocal() {
        return base.isLocal(this);
    }

    @Override
    public boolean exists() {
        return base.exists(this);
    }

    @Override
    public long contentLength() {
        return base.contentLength(this);
    }

    @Override
    public Instant lastModifiedInstant() {
        return base.getLastModifiedInstant(this);
    }

    @Override
    public Instant lastAccessInstant() {
        return base.getLastAccessInstant(this);
    }

    @Override
    public Instant getCreationInstant() {
        return base.getCreationInstant(this);
    }

    @Override
    public NPath getParent() {
        NPath p = base.getParent(this);
        if (p != null) {
            return p;
        }
        if (isRoot()) {
            return this;
        }
        List<String> names = getNames();
        List<String> items = names.subList(0, names.size() - 1);
        NPath root = getRoot();
        for (String item : items) {
            root = root.resolve(item);
        }
        return root;
    }

    @Override
    public boolean isAbsolute() {
        return base.isAbsolute(this);
    }

    @Override
    public NPath normalize() {
        NPath p = base.normalize(this);
        if (p != null) {
            return p;
        }
        if (isRoot()) {
            return this;
        }
        List<String> names = getNames();
        NPath root = getRoot();
        List<String> newNames = NIOUtils.normalizePathNames(names);
        if (newNames.size() != names.size()) {
            for (String item : newNames) {
                root = root.resolve(item);
            }
            return root;
        }
        return this;
    }

    @Override
    public NPath toAbsolute() {
        return toAbsolute((NPath) null);
    }

    @Override
    public NPath toAbsolute(String rootPath) {
        return toAbsolute(rootPath == null ? null : NPath.of(rootPath));
    }

    @Override
    public NPath toAbsolute(NPath rootPath) {
        if (base.isAbsolute(this)) {
            return this;
        }
        NPath p = base.toAbsolute(this, rootPath);
        if (p != null) {
            return p;
        }
        if (rootPath == null) {
            return normalize();
        }
        return rootPath.toAbsolute().resolve(this);
    }

    @Override
    public String owner() {
        return base.owner(this);
    }

    @Override
    public String group() {
        return base.group(this);
    }

    @Override
    public Set<NPathPermission> getPermissions() {
        return base.getPermissions(this);
    }

    @Override
    public NPath setPermissions(NPathPermission... permissions) {
        base.setPermissions(this, permissions);
        return this;
    }

    @Override
    public NPath addPermissions(NPathPermission... permissions) {
        base.addPermissions(this, permissions);
        return this;
    }

    @Override
    public NPath removePermissions(NPathPermission... permissions) {
        base.removePermissions(this, permissions);
        return this;
    }

    @Override
    public boolean isName() {
        Boolean b = base.isName(this);
        if (b == null) {
            if (getNameCount() > 1) {
                return false;
            }
            String v = toString();
            switch (v) {
                case "/":
                case "\\":
                case ".":
                case "..": {
                    return false;
                }
            }
            for (char c : v.toCharArray()) {
                switch (c) {
                    case '/':
                    case '\\': {
                        return false;
                    }
                }
            }
            return true;
        }
        return b;
    }

    @Override
    public int getNameCount() {
        Integer r = base.getNameCount(this);
        if (r != 0) {
            return r;
        }
        return getNames().size();
    }

    @Override
    public List<NPathChildDigestInfo> listDigestInfo() {
        return listDigestInfo(null);
    }

    @Override
    public List<NPathChildDigestInfo> listDigestInfo(String algo) {
        List<NPathChildDigestInfo> infos = base.listDigestInfo(this,algo);
        if (infos != null) {
            return infos;
        }
        return list().stream().map(x -> new NPathChildDigestInfo().setName(x.getName()).setDigest(x.getDigest(algo))).collect(Collectors.toList());
    }

    @Override
    public List<NPathChildStringDigestInfo> listStringDigestInfo() {
        return listStringDigestInfo(null);
    }

    @Override
    public List<NPathChildStringDigestInfo> listStringDigestInfo(String algo) {
        List<NPathChildDigestInfo> infos = base.listDigestInfo(this,algo);
        if (infos != null) {
            return infos.stream().map(x->
                    new NPathChildStringDigestInfo()
                            .setName(x.getName())
                            .setDigest(NHex.fromBytes(x.getDigest()))
            ).collect(Collectors.toList());
        }
        return list().stream().map(x -> new NPathChildStringDigestInfo().setName(x.getName()).setDigest(
                NHex.fromBytes(x.getDigest())
        )).collect(Collectors.toList());
    }

    @Override
    public boolean isRoot() {
        Boolean b = base.isRoot(this);
        if (b != null) {
            return b;
        }
        return getNameCount() == 0;
    }

    @Override
    public NStream<NPath> walk(int maxDepth, NPathOption[] options) {
        NPathOption[] options1 = options == null ? new NPathOption[0]
                : Arrays.stream(options).filter(Objects::isNull)
                .distinct()
                .toArray(NPathOption[]::new);
        if (maxDepth <= 0) {
            maxDepth = Integer.MAX_VALUE;
        }
        if (NUseDefaultUtils.isUseDefault(base.getClass(), "walk",
                NPath.class, int.class, NPathOption[].class)) {
            return NPathSPIHelper.walk(this, maxDepth, options1);
        } else {
            NStream<NPath> walked = base.walk(this, maxDepth, options);
            if (walked != null) {
                return walked;
            }
            return NPathSPIHelper.walk(this, maxDepth, options1);
        }
    }

    @Override
    public NPath subpath(int beginIndex, int endIndex) {
        NPath subpath = base.subpath(this, beginIndex, endIndex);
        if (subpath != null) {
            return subpath;
        }
        List<String> items = getNames().subList(beginIndex, endIndex);
        NPath root = getRoot();
        for (String item : items) {
            root = root.resolve(item);
        }
        return root;
    }

    @Override
    public String getLocationItem(int index) {
        return getNames().get(index);
    }

    @Override
    public List<String> getNames() {
        if (items == null) {
            items = base.getNames(this);
            if (items == null) {
                String location = getLocation();
                items = NStringUtils.split(location, "/", true, true);
            }
        }
        return items;
    }

    @Override
    public void moveTo(NPath other, NPathOption... options) {
        if (!base.moveTo(this, other)) {
            copyTo(other, options);
            delete(true);
        }
    }

    @Override
    public void copyTo(NPath other, NPathOption... options) {
        if (!base.copyTo(this, other, options)) {
            try (InputStream in = this.getInputStream(options)) {
                NCp.of().from(in).to(other).addOptions(options).run();
            } catch (Exception e) {
                throw new NIOException(e);
            }
        }
    }

    @Override
    public NPath getRoot() {
        return base.getRoot(this);
    }

    @Override
    public NPath walkDfs(NTreeVisitor<NPath> visitor, NPathOption... options) {
        return walkDfs(visitor, Integer.MAX_VALUE, options);
    }

    @Override
    public NPath walkDfs(NTreeVisitor<NPath> visitor, int maxDepth, NPathOption... options) {
        if (maxDepth <= 0) {
            maxDepth = Integer.MAX_VALUE;
        }
        if (NUseDefaultUtils.isUseDefault(base.getClass(), "walkDfs",
                NPath.class, NTreeVisitor.class, int.class, NPathOption[].class)) {
            NPathSPIHelper.walkDfs(this, visitor, maxDepth, options);
        } else {
            boolean r = base.walkDfs(this, visitor, maxDepth, options);
            if (!r) {
                Stack<NPath> stack = new Stack<>();
                Stack<Boolean> visitedStack = new Stack<>();

                stack.push(this);
                visitedStack.push(false);

                while (!stack.isEmpty()) {
                    NPath currentPath = stack.pop();
                    boolean visited = visitedStack.pop();

                    if (visited) {
                        visitor.postVisitDirectory(currentPath, null);
                        continue;
                    }

                    if (currentPath.isDirectory()) {
                        visitor.preVisitDirectory(currentPath);
                        stack.push(currentPath);
                        visitedStack.push(true);
                        try {
                            if (maxDepth > 0) {
                                for (NPath nPath : currentPath.list()) {
                                    stack.push(nPath);
                                    visitedStack.push(false); // Mark children for pre-visit first
                                }
                            }
                        } catch (RuntimeException e) {
                            visitor.postVisitDirectory(currentPath, e);
                        }
                    } else {
                        visitor.visitFile(currentPath);
                    }
                }
            }
        }
        return this;
    }

    @Override
    public NStream<NPath> walkGlob(NPathOption... options) {
        return new DirectoryScanner(this).stream();
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), base);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        NPathFromSPI that = (NPathFromSPI) o;
        return Objects.equals(base, that.base);
    }

    @Override
    public String toString() {
        return base.toString();
    }

    private static class EffectiveResolver implements Function<String, String> {
        NWorkspaceVarExpansionFunction fallback;
        Function<String, String> resolver;

        public EffectiveResolver(Function<String, String> resolver) {
            this.resolver = resolver;
            fallback = NWorkspaceVarExpansionFunction.of();
        }

        @Override
        public String apply(String s) {
            if (resolver != null) {
                String v = resolver.apply(s);
                if (v != null) {
                    return v;
                }
            }
            return fallback.apply(s);
        }
    }

    @Override
    public byte[] getDigest(String algo) {
        if (NBlankable.isBlank(algo)) {
            algo = "SHA-1";
        }
        byte[] digest = base.getDigest(this, algo);
        if (digest == null) {
            return super.getDigest(algo);
        }
        return digest;
    }

    @Override
    public boolean isEqOrDeepChildOf(NPath other) {
        if (other == null) {
            return false;
        }
        return !toRelative(other).isPresent();
    }

    @Override
    public NPathType type() {
        return base.type(this);
    }

    @Override
    public NOptional<String> toRelative(NPath parentPath) {
        NOptional<String> r = base.toRelative(this, unwrapPath(parentPath));
        if (r != null) {
            return r;
        }
        //default impl
        String child = getLocation();
        String parent = parentPath.getLocation();
        return NOptional.ofNamed(NIOUtils.toRelativePath(child, parent), "relative path");
    }


    @Override
    public boolean startsWith(NPath other) {
        return toRelative(unwrapPath(other)).orNull() != null;
    }

    @Override
    public int compareTo(NPath other) {
        if (other == null) {
            return 1;
        }
        Integer r = base.compareTo(this, unwrapPath(other));
        if (r != null) {
            return r;
        }
        return toString().compareTo(other.toString());
    }


    @Override
    public boolean startsWith(String other) {
        return toRelative(NPath.of(other)).orNull() != null;
    }

}
