package net.thevpc.nuts.runtime.standalone.io.path.spi;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.io.util.InputStreamMetadataAwareImpl;
import net.thevpc.nuts.runtime.standalone.io.util.CoreIOUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NutsWorkspaceUtils;
import net.thevpc.nuts.spi.NutsFormatSPI;
import net.thevpc.nuts.spi.NutsPathFactory;
import net.thevpc.nuts.spi.NutsPathSPI;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.*;
import java.nio.file.attribute.*;
import java.time.Instant;
import java.util.*;

public class FilePath implements NutsPathSPI {

    private final Path value;
    private final NutsSession session;

    public FilePath(Path value, NutsSession session) {
        if (value == null) {
            throw new IllegalArgumentException("invalid value");
        }
        this.value = value;
        this.session = session;
    }

    private NutsPath fastPath(Path p, NutsSession s) {
        return NutsPath.of(new FilePath(p, s), s);
    }

    @Override
    public NutsStream<NutsPath> list(NutsPath basePath) {
        if (Files.isDirectory(value)) {
            try {
                return NutsStream.of(Files.list(value).map(x -> fastPath(x, getSession())),
                        getSession());
            } catch (IOException e) {
                //
            }
        }
        return NutsStream.ofEmpty(getSession());
    }

    @Override
    public NutsFormatSPI formatter(NutsPath basePath) {
        return new MyPathFormat(this);
    }

    @Override
    public String getName(NutsPath basePath) {
        return value.getFileName().toString();
    }

    @Override
    public String getProtocol(NutsPath basePath) {
        return "";
    }

    @Override
    public NutsPath resolve(NutsPath basePath, String path) {
        if (NutsBlankable.isBlank(path)) {
            return fastPath(value, getSession());
        }
        try {
            return fastPath(value.resolve(path), getSession());
        } catch (Exception ex) {
            //always return an instance if is invalid
            return NutsPath.of(value + getSep() + path, getSession());
        }
    }

    @Override
    public NutsPath resolve(NutsPath basePath, NutsPath path) {
        if (path == null) {
            return fastPath(value, getSession());
        }
        if (path.toString().isEmpty()) {
            return fastPath(value, getSession());
        }
        Path f = path.asFile();
        if (f != null) {
            return fastPath(value.resolve(f), getSession());
        }
        return resolve(basePath, path.toString());
    }

    @Override
    public NutsPath resolveSibling(NutsPath basePath, String path) {
        if (path == null) {
            return getParent(basePath);
        }
        if (path.isEmpty()) {
            return getParent(basePath);
        }
        try {
            return fastPath(value.resolveSibling(path), getSession());
        } catch (Exception e) {
            Path p = value.getParent();
            if (p == null) {
                return NutsPath.of(path, session);
            }
            return fastPath(p, session).resolve(path);
        }
    }

    @Override
    public NutsPath resolveSibling(NutsPath basePath, NutsPath path) {
        if (path == null) {
            return getParent(basePath);
        }
        return resolveSibling(basePath, path.toString());
    }

    @Override
    public NutsPath toCompressedForm(NutsPath basePath) {
        return null;
    }

    @Override
    public URL toURL(NutsPath basePath) {
        try {
            return value.toUri().toURL();
        } catch (MalformedURLException e) {
            throw new NutsIOException(session, e);
        }
    }

    @Override
    public Path toFile(NutsPath basePath) {
        return value;
    }

    @Override
    public boolean isSymbolicLink(NutsPath basePath) {
        PosixFileAttributes a = getUattr();
        return a != null && a.isSymbolicLink();
    }

    @Override
    public boolean isOther(NutsPath basePath) {
        PosixFileAttributes a = getUattr();
        return a != null && a.isOther();
    }

    @Override
    public boolean isDirectory(NutsPath basePath) {
        return Files.isDirectory(value);
    }

    @Override
    public boolean isLocal(NutsPath basePath) {
        //how about NFS?
        return true;
    }

    @Override
    public boolean isRegularFile(NutsPath basePath) {
        return Files.isRegularFile(value);
    }

    public boolean exists(NutsPath basePath) {
        return Files.exists(value);
    }

    public long getContentLength(NutsPath basePath) {
        try {
            return Files.size(value);
        } catch (IOException e) {
            return -1;
        }
    }

    @Override
    public String getContentEncoding(NutsPath basePath) {
        return null;
    }

    @Override
    public String getContentType(NutsPath basePath) {
        return NutsContentTypes.of(session).probeContentType(value);
    }

    @Override
    public String getLocation(NutsPath basePath) {
        return value.toString();
    }

    public InputStream getInputStream(NutsPath basePath) {
        try {
            return InputStreamMetadataAwareImpl.of(Files.newInputStream(value),
                    basePath.getStreamMetadata()
            );
        } catch (IOException e) {
            throw new NutsIOException(session, e);
        }
    }

    public OutputStream getOutputStream(NutsPath basePath) {
        try {
            return Files.newOutputStream(value);
        } catch (IOException e) {
            throw new NutsIOException(session, e);
        }
    }

    @Override
    public NutsSession getSession() {
        return session;
    }

    @Override
    public void delete(NutsPath basePath, boolean recurse) {
        if (Files.isRegularFile(value)) {
            try {
                Files.delete(value);
            } catch (IOException e) {
                throw new NutsIOException(getSession(), e);
            }
        } else if (Files.isDirectory(value)) {
            if (recurse) {
                CoreIOUtils.delete(getSession(), value);
            } else {
                try {
                    Files.delete(value);
                } catch (IOException e) {
                    throw new NutsIOException(getSession(), e);
                }
            }
        } else {
            throw new NutsIOException(getSession(), NutsMessage.cstyle("unable to delete path %s", value));
        }
    }

    @Override
    public void mkdir(boolean parents, NutsPath basePath) {
        if (Files.isRegularFile(value)) {
            throw new NutsIOException(getSession(), NutsMessage.cstyle("unable to create folder out of regular file %s", value));
        } else if (Files.isDirectory(value)) {
            return;
        } else {
            try {
                Files.createDirectories(value);
            } catch (IOException e) {
                throw new NutsIOException(getSession(), NutsMessage.cstyle("unable to create folders %s", value));
            }
        }
    }

    @Override
    public Instant getLastModifiedInstant(NutsPath basePath) {
        FileTime r = null;
        try {
            r = Files.getLastModifiedTime(value);
            if (r != null) {
                return r.toInstant();
            }
        } catch (IOException e) {
            //
        }
        return null;
    }

    @Override
    public Instant getLastAccessInstant(NutsPath basePath) {
        BasicFileAttributes a = getBattr();
        if (a != null) {
            FileTime t = a.lastAccessTime();
            return t == null ? null : Instant.ofEpochMilli(t.toMillis());
        }
        return null;
    }

    @Override
    public Instant getCreationInstant(NutsPath basePath) {
        BasicFileAttributes a = getBattr();
        if (a != null) {
            FileTime t = a.creationTime();
            return t == null ? null : Instant.ofEpochMilli(t.toMillis());
        }
        return null;
    }

    @Override
    public NutsPath getParent(NutsPath basePath) {
        Path p = value.getParent();
        if (p == null) {
            return null;
        }
        return fastPath(p, getSession());
    }

    @Override
    public NutsPath toAbsolute(NutsPath basePath, NutsPath rootPath) {
        if (isAbsolute(basePath)) {
            return basePath;
        }
        if (rootPath == null) {
            return fastPath(value.normalize().toAbsolutePath(), session);
        }
        return rootPath.toAbsolute().resolve(toString());
    }

    @Override
    public NutsPath normalize(NutsPath basePath) {
        return fastPath(value.normalize(), session);
    }

    @Override
    public boolean isAbsolute(NutsPath basePath) {
        return value.isAbsolute();
    }

    @Override
    public String owner(NutsPath basePath) {
        PosixFileAttributes a = getUattr();
        if (a != null) {
            UserPrincipal o = a.owner();
            return o == null ? null : o.getName();
        }
        return null;
    }

    @Override
    public String group(NutsPath basePath) {
        PosixFileAttributes a = getUattr();
        if (a != null) {
            GroupPrincipal o = a.group();
            return o == null ? null : o.getName();
        }
        return null;
    }

    @Override
    public Set<NutsPathPermission> getPermissions(NutsPath basePath) {
        Set<NutsPathPermission> p = new LinkedHashSet<>();
        PosixFileAttributes a = getUattr();
        File file = value.toFile();
        if (file.canRead()) {
            p.add(NutsPathPermission.CAN_READ);
        }
        if (file.canWrite()) {
            p.add(NutsPathPermission.CAN_WRITE);
        }
        if (file.canExecute()) {
            p.add(NutsPathPermission.CAN_EXECUTE);
        }
        if (a != null) {
            for (PosixFilePermission permission : a.permissions()) {
                switch (permission) {
                    case OWNER_READ: {
                        p.add(NutsPathPermission.OWNER_READ);
                    }
                    case OWNER_WRITE: {
                        p.add(NutsPathPermission.OWNER_WRITE);
                    }
                    case OWNER_EXECUTE: {
                        p.add(NutsPathPermission.OWNER_EXECUTE);
                    }
                    case GROUP_READ: {
                        p.add(NutsPathPermission.GROUP_READ);
                    }
                    case GROUP_WRITE: {
                        p.add(NutsPathPermission.GROUP_WRITE);
                    }
                    case GROUP_EXECUTE: {
                        p.add(NutsPathPermission.GROUP_EXECUTE);
                    }
                    case OTHERS_READ: {
                        p.add(NutsPathPermission.OTHERS_READ);
                    }
                    case OTHERS_WRITE: {
                        p.add(NutsPathPermission.OTHERS_WRITE);
                    }
                    case OTHERS_EXECUTE: {
                        p.add(NutsPathPermission.OTHERS_EXECUTE);
                    }
                }
            }
        }
        return Collections.unmodifiableSet(p);
    }

    @Override
    public void setPermissions(NutsPath basePath, NutsPathPermission... permissions) {
        Set<NutsPathPermission> add = new LinkedHashSet<>(Arrays.asList(permissions));
        Set<NutsPathPermission> remove = new LinkedHashSet<>(EnumSet.allOf(NutsPathPermission.class));
        remove.addAll(add);
        setPermissions(add.toArray(new NutsPathPermission[0]), true);
//        setPermissions(remove.toArray(new NutsPathPermission[0]),false);
    }

    @Override
    public void addPermissions(NutsPath basePath, NutsPathPermission... permissions) {
        setPermissions(permissions, true);
    }

    @Override
    public void removePermissions(NutsPath basePath, NutsPathPermission... permissions) {
        //
    }

    @Override
    public boolean isName(NutsPath basePath) {
        if (value.getNameCount() > 1) {
            return false;
        }
        String v = value.toString();
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

    @Override
    public int getPathCount(NutsPath basePath) {
        return value.getNameCount();
    }

    @Override
    public boolean isRoot(NutsPath basePath) {
        return value.getNameCount() == 0;
    }

    @Override
    public NutsPath getRoot(NutsPath basePath) {
        if (isRoot(basePath)) {
            return basePath;
        }
        return basePath.getParent().getRoot();
    }

    @Override
    public NutsStream<NutsPath> walk(NutsPath basePath, int maxDepth, NutsPathOption[] options) {
        FileVisitOption[] fileOptions = Arrays.stream(options)
                .map(x -> {
                    if (x == null) {
                        return null;
                    }
                    switch (x) {
                        case FOLLOW_LINKS:
                            return FileVisitOption.FOLLOW_LINKS;
                    }
                    return null;
                }).filter(Objects::nonNull).toArray(FileVisitOption[]::new);
        if (Files.isDirectory(value)) {
            try {
                return NutsStream.of(Files.walk(value, maxDepth, fileOptions).map(x -> fastPath(x, getSession())),
                        getSession());
            } catch (IOException e) {
                //
            }
        }
        return NutsStream.ofEmpty(getSession());
    }

    @Override
    public NutsPath subpath(NutsPath basePath, int beginIndex, int endIndex) {
        return fastPath(value.subpath(beginIndex, endIndex), getSession());
    }

    @Override
    public String[] getItems(NutsPath basePath) {
        int nameCount = value.getNameCount();
        String[] names = new String[nameCount];
        for (int i = 0; i < nameCount; i++) {
            names[i] = value.getName(i).toString();
        }
        return names;
    }

    @Override
    public void moveTo(NutsPath basePath, NutsPath other, NutsPathOption... options) {
        Path f = other.asFile();
        if (f != null) {
            try {
                Files.move(value, f, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw new NutsIOException(session, e);
            }
        } else {
            copyTo(basePath, other, options);
            delete(basePath, true);
        }
    }

    @Override
    public void copyTo(NutsPath basePath, NutsPath other, NutsPathOption... options) {
        NutsCp.of(session).from(fastPath(value, session)).to(other).run();
    }

    @Override
    public void walkDfs(NutsPath basePath, NutsTreeVisitor<NutsPath> visitor, int maxDepth, NutsPathOption... options) {
        Set<FileVisitOption> foptions = new HashSet<>();
        for (NutsPathOption option : options) {
            switch (option) {
                case FOLLOW_LINKS: {
                    foptions.add(FileVisitOption.FOLLOW_LINKS);
                    break;
                }
            }
        }
        try {
            Files.walkFileTree(value, foptions, maxDepth, new FileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    return fileVisitResult(visitor.preVisitDirectory(fastPath(dir, session), session));
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    return fileVisitResult(visitor.visitFile(fastPath(file, session), session));
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                    return fileVisitResult(visitor.visitFileFailed(fastPath(file, session), exc, session));
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    return fileVisitResult(visitor.postVisitDirectory(fastPath(dir, session), exc, session));
                }

                private FileVisitResult fileVisitResult(NutsTreeVisitResult z) {
                    if (z != null) {
                        switch (z) {
                            case CONTINUE:
                                return FileVisitResult.CONTINUE;
                            case TERMINATE:
                                return FileVisitResult.TERMINATE;
                            case SKIP_SUBTREE:
                                return FileVisitResult.SKIP_SUBTREE;
                            case SKIP_SIBLINGS:
                                return FileVisitResult.SKIP_SIBLINGS;
                        }
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new NutsIOException(getSession(), e);
        }
    }

    private String getSep() {
        for (char c : value.toString().toCharArray()) {
            switch (c) {
                case '/':
                case '\\': {
                    return String.valueOf(c);
                }
            }
        }
        return "/";
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FilePath urlPath = (FilePath) o;
        return Objects.equals(value, urlPath.value);
    }

    @Override
    public String toString() {
        return value.toString();
    }

    private BasicFileAttributes getBattr() {
        try {
            return Files.readAttributes(value, BasicFileAttributes.class);
        } catch (Exception ex) {
            //
        }
        return null;
    }

    private PosixFileAttributes getUattr() {
        try {
            return Files.readAttributes(value, PosixFileAttributes.class);
        } catch (Exception ex) {
            //
        }
        return null;
    }

    public boolean setPermissions(NutsPathPermission[] permissions, boolean f) {
        int count = 0;
        permissions = permissions == null ? new NutsPathPermission[0]
                : Arrays.stream(permissions).filter(Objects::nonNull).distinct().toArray(NutsPathPermission[]::new);
        for (NutsPathPermission permission : permissions) {
            switch (permission) {
                case CAN_READ: {
                    boolean b = value.toFile().setReadable(f);
                    if (b) {
                        count++;
                    }
                    break;
                }
                case CAN_WRITE: {
                    boolean b = value.toFile().setWritable(f);
                    if (b) {
                        count++;
                    }
                    break;
                }
                case CAN_EXECUTE: {
                    boolean b = value.toFile().setExecutable(f);
                    if (b) {
                        count++;
                    }
                    break;
                }
                case OWNER_READ: {
                    boolean b = value.toFile().setReadable(f);
                    if (b) {
                        count++;
                    }
                    break;
                }
                case OWNER_WRITE: {
                    boolean b = value.toFile().setWritable(f);
                    if (b) {
                        count++;
                    }
                    break;
                }
                case OWNER_EXECUTE: {
                    boolean b = value.toFile().setExecutable(f);
                    if (b) {
                        count++;
                    }
                    break;
                }
                case GROUP_READ: {
                    boolean b = value.toFile().setReadable(f);
                    if (b) {
                        count++;
                    }
                    break;
                }
                case GROUP_WRITE: {
                    boolean b = value.toFile().setWritable(f);
                    if (b) {
                        count++;
                    }
                    break;
                }
                case GROUP_EXECUTE: {
                    boolean b = value.toFile().setExecutable(f);
                    if (b) {
                        count++;
                    }
                    break;
                }
                case OTHERS_READ: {
                    boolean b = value.toFile().setReadable(f);
                    if (b) {
                        count++;
                    }
                    break;
                }
                case OTHERS_WRITE: {
                    boolean b = value.toFile().setWritable(f);
                    if (b) {
                        count++;
                    }
                    break;
                }
                case OTHERS_EXECUTE: {
                    boolean b = value.toFile().setExecutable(f);
                    if (b) {
                        count++;
                    }
                    break;
                }
            }
        }
        return count == permissions.length;
    }

    private static class MyPathFormat implements NutsFormatSPI {

        private final FilePath p;

        public MyPathFormat(FilePath p) {
            this.p = p;
        }

        public NutsString asFormattedString() {
            return NutsTexts.of(p.getSession()).toText(p.value);
        }

        @Override
        public void print(NutsPrintStream out) {
            out.print(asFormattedString());
        }

        @Override
        public boolean configureFirst(NutsCommandLine commandLine) {
            return false;
        }
    }

    public static class FilePathFactory implements NutsPathFactory {
        NutsWorkspace ws;

        public FilePathFactory(NutsWorkspace ws) {
            this.ws = ws;
        }

        @Override
        public NutsSupported<NutsPathSPI> createPath(String path, NutsSession session, ClassLoader classLoader) {
            NutsWorkspaceUtils.checkSession(ws, session);
            try {
                if (URLPath.MOSTLY_URL_PATTERN.matcher(path).matches()) {
                    return null;
                }
                Path value = Paths.get(path);
                return NutsSupported.of(10, () -> new FilePath(value, session));
            } catch (Exception ex) {
                //ignore
            }
            return null;
        }
    }

    @Override
    public NutsPath toRelativePath(NutsPath basePath, NutsPath parentPath) {
        String child=basePath.getLocation();
        String parent=parentPath.getLocation();
        if (child.startsWith(parent)) {
            child = child.substring(parent.length());
            if (child.startsWith("/") || child.startsWith("\\")) {
                child = child.substring(1);
            }
            return NutsPath.of(child,session);
        }
        return null;
    }

}
