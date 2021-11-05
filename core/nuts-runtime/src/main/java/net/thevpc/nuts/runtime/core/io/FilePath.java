package net.thevpc.nuts.runtime.core.io;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.bundles.io.InputStreamMetadataAwareImpl;
import net.thevpc.nuts.runtime.core.util.CoreIOUtils;
import net.thevpc.nuts.spi.NutsFormatSPI;
import net.thevpc.nuts.spi.NutsPathSPI;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.*;
import java.time.Instant;
import java.util.*;

public class FilePath implements NutsPathSPI {
    private final Path value;
    private final NutsSession session;
    //    private PosixFileAttributes uattr = null;
    private final boolean uattrLoaded = false;


    public FilePath(Path value, NutsSession session) {
        if (value == null) {
            throw new IllegalArgumentException("invalid value");
        }
        this.value = value;
        this.session = session;
    }

    private NutsPath toNutsPathInstance() {
        return new NutsPathFromSPI(this);
    }

    @Override
    public NutsPath[] list() {
        if (Files.isDirectory(value)) {
            try {
                return Files.list(value).map(x -> NutsPath.of(x,getSession())).toArray(NutsPath[]::new);
            } catch (IOException e) {
                //
            }
        }
        return new NutsPath[0];
    }

    @Override
    public NutsFormatSPI getFormatterSPI() {
        return new MyPathFormat(this);
    }

    public String getName() {
        return CoreIOUtils.getURLName(value.toString());
    }

    @Override
    public String getProtocol() {
        return "";
    }

    @Override
    public NutsPath resolve(String other) {
        String[] others = Arrays.stream(NutsUtilStrings.trim(other).split("[/\\\\]"))
                .filter(x -> x.length() > 0).toArray(String[]::new);
        if (others.length > 0) {
            Path value2 = value;
            for (String s : others) {
                value2 = value2.resolve(s);
            }
            return NutsPath.of(value2,getSession());
        }
        return toNutsPathInstance();
    }

    public URL toURL() {
        try {
            return value.toUri().toURL();
        } catch (MalformedURLException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public Path toFile() {
        return value;
    }

    @Override
    public boolean isSymbolicLink() {
        PosixFileAttributes a = getUattr();
        return a != null && a.isSymbolicLink();
    }

    @Override
    public boolean isOther() {
        PosixFileAttributes a = getUattr();
        return a != null && a.isOther();
    }

    @Override
    public boolean isDirectory() {
        return Files.isDirectory(value);
    }

    @Override
    public boolean isRegularFile() {
        return Files.isRegularFile(value);
    }

    public boolean exists() {
        return Files.exists(value);
    }

    public long getContentLength() {
        try {
            return Files.size(value);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public String getContentEncoding() {
        return null;
    }

    @Override
    public String getContentType() {
        return NutsContentTypes.of(session).probeContentType(value);
    }

    @Override
    public String getLocation() {
        return value.toString();
    }

    public InputStream getInputStream() {
        try {
            return InputStreamMetadataAwareImpl.of(Files.newInputStream(value),
                    new NutsDefaultInputStreamMetadata(toNutsPathInstance())
            );
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public OutputStream getOutputStream() {
        try {
            return Files.newOutputStream(value);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public NutsSession getSession() {
        return session;
    }

    @Override
    public void delete(boolean recurse) {
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
    public void mkdir(boolean parents) {
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
    public Instant getLastModifiedInstant() {
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
    public Instant getLastAccessInstant() {
        BasicFileAttributes a = getBattr();
        if (a != null) {
            FileTime t = a.lastAccessTime();
            return t == null ? null : Instant.ofEpochMilli(t.toMillis());
        }
        return null;
    }

    @Override
    public Instant getCreationInstant() {
        BasicFileAttributes a = getBattr();
        if (a != null) {
            FileTime t = a.creationTime();
            return t == null ? null : Instant.ofEpochMilli(t.toMillis());
        }
        return null;
    }

    @Override
    public NutsPath getParent() {
        Path p = value.getParent();
        if (p == null) {
            return null;
        }
        return NutsPath.of(p,getSession());
    }

    @Override
    public NutsPath toAbsolute(NutsPath basePath) {
        if (isAbsolute()) {
            return new NutsPathFromSPI(this);
        }
        if (basePath == null) {
            return new NutsPathFromSPI(new FilePath(
                    value.normalize().toAbsolutePath(), session
            ));
        }
        return basePath.toAbsolute().resolve(toString());
    }

    @Override
    public NutsPath normalize() {
        return new NutsPathFromSPI(new FilePath(value.normalize(), session));
    }

    @Override
    public boolean isAbsolute() {
        return value.isAbsolute();
    }

    @Override
    public String owner() {
        PosixFileAttributes a = getUattr();
        if (a != null) {
            UserPrincipal o = a.owner();
            return o == null ? null : o.getName();
        }
        return null;
    }

    @Override
    public String group() {
        PosixFileAttributes a = getUattr();
        if (a != null) {
            GroupPrincipal o = a.group();
            return o == null ? null : o.getName();
        }
        return null;
    }

    @Override
    public Set<NutsPathPermission> permissions() {
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
    public void setPermissions(NutsPathPermission... permissions) {
        Set<NutsPathPermission> add = new LinkedHashSet<>(Arrays.asList(permissions));
        Set<NutsPathPermission> remove = new LinkedHashSet<>(EnumSet.allOf(NutsPathPermission.class));
        remove.addAll(add);
        setPermissions(add.toArray(new NutsPathPermission[0]), true);
//        setPermissions(remove.toArray(new NutsPathPermission[0]),false);
    }

    @Override
    public void addPermissions(NutsPathPermission... permissions) {
        setPermissions(permissions, true);
    }

    @Override
    public void removePermissions(NutsPathPermission... permissions) {
        //
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
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

    public void setPermissions(NutsPathPermission[] permissions, boolean f) {
        for (NutsPathPermission permission : permissions) {
            switch (permission) {
                case CAN_READ: {
                    value.toFile().setReadable(f);
                    break;
                }
                case CAN_WRITE: {
                    value.toFile().setWritable(f);
                    break;
                }
                case CAN_EXECUTE: {
                    value.toFile().setExecutable(f);
                    break;
                }
                case OWNER_READ: {
                    value.toFile().setReadable(f);
                    break;
                }
                case OWNER_WRITE: {
                    value.toFile().setWritable(f);
                    break;
                }
                case OWNER_EXECUTE: {
                    value.toFile().setExecutable(f);
                    break;
                }
                case GROUP_READ: {
                    value.toFile().setReadable(f);
                    break;
                }
                case GROUP_WRITE: {
                    value.toFile().setWritable(f);
                    break;
                }
                case GROUP_EXECUTE: {
                    value.toFile().setExecutable(f);
                    break;
                }
                case OTHERS_READ: {
                    value.toFile().setReadable(f);
                    break;
                }
                case OTHERS_WRITE: {
                    value.toFile().setWritable(f);
                    break;
                }
                case OTHERS_EXECUTE: {
                    value.toFile().setExecutable(f);
                    break;
                }
            }
        }
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
}
