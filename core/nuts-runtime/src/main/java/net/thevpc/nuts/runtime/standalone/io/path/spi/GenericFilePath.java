package net.thevpc.nuts.runtime.standalone.io.path.spi;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NCommandLine;
import net.thevpc.nuts.format.NTreeVisitor;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.runtime.standalone.session.NSessionUtils;
import net.thevpc.nuts.spi.NFormatSPI;
import net.thevpc.nuts.spi.NPathFactory;
import net.thevpc.nuts.spi.NPathSPI;
import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.util.NStream;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.*;

public class GenericFilePath implements NPathSPI {

    private final String value;
    private final NPathPartList parts;
    private final NSession session;


    public GenericFilePath(String value, NSession session) {
        this.value = value == null ? "" : value;
        this.parts = NPathPartParser.parseParts(this.value, session);
        this.session = session;
    }

    @Override
    public NStream<NPath> list(NPath basePath) {
        return NStream.ofEmpty(getSession());
    }

    @Override
    public NFormatSPI formatter(NPath basePath) {
        return new MyPathFormat(this);
    }

    @Override
    public String getName(NPath basePath) {
        if (parts.isEmpty()) {
            return "";
        }
        if (parts.size() == 1) {
            return parts.first().getName();
        }
        if (parts.last().isTrailingSeparator()) {
            return parts.get(-2).getName();
        }
        return parts.last().getName();
    }

    @Override
    public String getProtocol(NPath basePath) {
        return "";
    }


    @Override
    public NPath resolve(NPath basePath, String path) {
        NPathPartList newParts = NPathPartParser.parseParts(path, session);
        if (newParts.isEmpty()) {
            return basePath;
        }
        if (parts.isEmpty() || newParts.get(0).getSeparator().length() > 0) {
            return NPath.of(path, session);
        }
        return partsToPath(parts.concat(newParts));
    }

    @Override
    public NPath resolve(NPath basePath, NPath path) {
        return resolve(basePath, path == null ? null : path.toString());
    }

    @Override
    public NPath resolveSibling(NPath basePath, String path) {
        if (path == null || path.isEmpty()) {
            return getParent(basePath);
        }
        if (isRoot(basePath)) {
            List<NPathPart> a = new ArrayList<>();
            if (parts.size() > 0) {
                a.add(new NPathPart(parts.get(0).getSeparator(), ""));
            } else {
                a.add(new NPathPart(File.separator, ""));
            }
            return partsToPath(new NPathPartList(a, session).concat(NPathPartParser.parseParts(path, session)));
        } else {
            return getParent(basePath).resolve(path);
        }
    }

    @Override
    public NPath resolveSibling(NPath basePath, NPath path) {
        return resolveSibling(basePath, path == null ? null : path.toString());
    }

    @Override
    public NPath toCompressedForm(NPath basePath) {
        return null;
    }

    @Override
    public URL toURL(NPath basePath) {
        try {
            if (URLPath.MOSTLY_URL_PATTERN.matcher(value).matches()) {
                return new URL(value);
            }
            return new URL("file:" + value);
        } catch (MalformedURLException e) {
            throw new NIOException(session, e);
        }
    }

    @Override
    public Path toFile(NPath basePath) {
        try {
            return Paths.get(value);
        } catch (Exception ex) {
            throw new NIOException(session, ex);
        }
    }

    @Override
    public boolean isSymbolicLink(NPath basePath) {
        return false;
    }

    @Override
    public boolean isOther(NPath basePath) {
        return false;
    }

    @Override
    public boolean isDirectory(NPath basePath) {
        return false;
    }

    @Override
    public boolean isRegularFile(NPath basePath) {
        return false;
    }

    public boolean exists(NPath basePath) {
        return false;
    }

    public long getContentLength(NPath basePath) {
        return -1;
    }

    @Override
    public String getContentEncoding(NPath basePath) {
        return null;
    }

    @Override
    public String getContentType(NPath basePath) {
        return null;
    }

    @Override
    public String getLocation(NPath basePath) {
        return value;
    }

    public InputStream getInputStream(NPath basePath, NPathOption... options) {
        throw new NIOException(session, NMsg.ofCstyle("path not found %s", this));
    }

    public OutputStream getOutputStream(NPath basePath, NPathOption... options) {
        throw new NIOException(session, NMsg.ofCstyle("path not found %s", this));
    }

    @Override
    public NSession getSession() {
        return session;
    }

    @Override
    public void delete(NPath basePath, boolean recurse) {
        throw new NIOException(getSession(), NMsg.ofCstyle("unable to delete path %s", this));
    }

    @Override
    public void mkdir(boolean parents, NPath basePath) {
        throw new NIOException(getSession(), NMsg.ofCstyle("unable to create folder out of regular file %s", this));
    }

    @Override
    public Instant getLastModifiedInstant(NPath basePath) {
        return null;
    }

    @Override
    public Instant getLastAccessInstant(NPath basePath) {
        return null;
    }

    @Override
    public Instant getCreationInstant(NPath basePath) {
        return null;
    }

    @Override
    public NPath getParent(NPath basePath) {
        if (isRoot(basePath)) {
            return null;
        }
        if (parts.isEmpty()) {
            return null;
        }
        if (parts.size() == 1) {
            NPathPart p = parts.get(0);
            if (p.isTrailingSeparator()) {
                return null;
            }
            if (p.isName()) {
                return null;
            }
            return partsToPath(new NPathPartList(Arrays.asList(new NPathPart(p.getSeparator(), "")), session));
        }
        if (parts.get(parts.size() - 1).isTrailingSeparator()) {
            return partsToPath(parts.subList(0, parts.size() - 2));
        }
        return partsToPath(parts.subList(0, parts.size() - 1));
    }

    @Override
    public NPath toAbsolute(NPath basePath, NPath rootPath) {
        if (isAbsolute(basePath)) {
            return basePath;
        }
        if (rootPath == null) {
            return partsToPath(NPathPartParser.parseParts(System.getProperty("user.dir"), session).concat(parts));
        }
        return rootPath.toAbsolute().resolve(toString());
    }

    @Override
    public NPath normalize(NPath basePath) {
        List<NPathPart> p2 = new ArrayList<>();
        for (NPathPart part : parts) {
            NPathPart p = part;
            if (p.isSeparated() && !p.getSeparator().equals(File.separator)) {
                p = new NPathPart(File.separator, p.getName());
            }
            if (p.isTrailingSeparator() && p2.size() > 0) {
                //ignore
            } else if (p.getName().equals(".")) {
                if (p2.size() > 0) {
                    //ignore
                } else {
                    if (!p.getSeparator().isEmpty()) {
                        p = new NPathPart(p.getSeparator(), "");
                        p2.add(p);
                    } else {
                        //ignore
                    }
                }
            } else if (p.getName().equals("..")) {
                if (p2.size() == 0) {
                    if (p.getSeparator().isEmpty()) {
                        p2.add(p);
                    } else {
                        p = new NPathPart(p.getSeparator(), "");
                        p2.add(p);
                    }
                }
                if (p2.size() == 1) {
                    NPathPart r = p2.get(0);
                    if (r.isTrailingSeparator()) {
                        //
                    } else {
                        p = new NPathPart(p.getSeparator(), "");
                        p2.add(p);
                    }
                } else {
                    p2.remove(p2.size() - 1);
                }
            } else {
                p2.add(p);
            }
        }
        return partsToPath(new NPathPartList(p2, session));
    }

    private NPath partsToPath(NPathPartList p2) {
        return NPath.of(
                p2.toString(),
                session
        );
    }

    @Override
    public boolean isAbsolute(NPath basePath) {
        if (parts.size() != 0) {
            NPathPart f = parts.first();
            if (f.getSeparator().length() > 0) {
                return true;
            }
            if (NEnvs.of(session).getOsFamily() == NOsFamily.WINDOWS) {
                String n = f.getName();
                //test if the name is a drive name
                if (n.length() == 2 && n.charAt(1) == ':') {
                    char c = n.charAt(0);
                    if ((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z')) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public String owner(NPath basePath) {
        return null;
    }

    @Override
    public String group(NPath basePath) {
        return null;
    }

    @Override
    public Set<NPathPermission> getPermissions(NPath basePath) {
        Set<NPathPermission> p = new LinkedHashSet<>();
        return Collections.unmodifiableSet(p);
    }

    @Override
    public void setPermissions(NPath basePath, NPathPermission... permissions) {
    }

    @Override
    public void addPermissions(NPath basePath, NPathPermission... permissions) {
    }

    @Override
    public void removePermissions(NPath basePath, NPathPermission... permissions) {
    }

    @Override
    public boolean isName(NPath basePath) {
        if (parts.size() == 0) {
            return true;
        }
        if (parts.size() > 1) {
            return false;
        }
        NPathPart v = parts.get(0);
        if (!v.getSeparator().isEmpty()) {
            return false;
        }
        switch (v.getName()) {
            case "/":
            case "\\":
            case ".":
            case "..": {
                return false;
            }
        }
        return true;
    }

    @Override
    public int getPathCount(NPath basePath) {
        if (parts.isEmpty()) {
            return 1;
        }
        if (parts.size() == 1 && parts.get(0).isTrailingSeparator()) {
            return 1;
        }
        if (parts.get(parts.size() - 1).isTrailingSeparator()) {
            return parts.size() - 1;
        }
        return parts.size();
    }

    @Override
    public boolean isRoot(NPath basePath) {
        if (parts.isEmpty()) {
            return true;
        }
        if (parts.size() == 1 && parts.get(0).isTrailingSeparator()) {
            return true;
        }
        return false;
    }

    @Override
    public NStream<NPath> walk(NPath basePath, int maxDepth, NPathOption[] options) {
        return NStream.ofEmpty(getSession());
    }

    @Override
    public NPath subpath(NPath basePath, int beginIndex, int endIndex) {
        NPathPartList parts = this.parts.subList(beginIndex, endIndex);
        return partsToPath(parts);
    }

    @Override
    public List<String> getItems(NPath basePath) {
        NPathPartList parts = this.parts;
        if (parts.isEmpty()) {
            return Collections.emptyList();
        }
        if (parts.size() == 1 && parts.get(0).isTrailingSeparator()) {
            return Arrays.asList("");
        }
        if (parts.get(parts.size() - 1).isTrailingSeparator()) {
            return parts.subList(0, parts.size() - 1).toStringList();
        }
        return parts.subList(0, parts.size()).toStringList();
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
        GenericFilePath urlPath = (GenericFilePath) o;
        return Objects.equals(value, urlPath.value);
    }

    @Override
    public String toString() {
        return value;
    }

    private static class MyPathFormat implements NFormatSPI {

        private final GenericFilePath p;

        @Override
        public String getName() {
            return "path";
        }

        public MyPathFormat(GenericFilePath p) {
            this.p = p;
        }

        public NString asFormattedString() {
            return NTexts.of(p.getSession()).ofText(p.value);
        }

        @Override
        public void print(NOutStream out) {
            out.print(asFormattedString());
        }

        @Override
        public boolean configureFirst(NCommandLine commandLine) {
            return false;
        }
    }

    @Override
    public void moveTo(NPath basePath, NPath other, NPathOption... options) {
        throw new NIOException(session, NMsg.ofCstyle("unable to move %s", this));
    }

    @Override
    public void copyTo(NPath basePath, NPath other, NPathOption... options) {
        throw new NIOException(session, NMsg.ofCstyle("unable to copy %s", this));
    }

    @Override
    public NPath getRoot(NPath basePath) {
        if (isRoot(basePath)) {
            return basePath;
        }
        NPath r = basePath.getParent();
        if (r != null) {
            return r.getRoot();
        }
        return null;
    }

    @Override
    public void walkDfs(NPath basePath, NTreeVisitor<NPath> visitor, int maxDepth, NPathOption... options) {

    }

    @Override
    public boolean isLocal(NPath basePath) {
        return true;
    }

    @Override
    public NPath toRelativePath(NPath basePath, NPath parentPath) {
        String child = basePath.getLocation();
        String parent = parentPath.getLocation();
        if (child.startsWith(parent)) {
            child = child.substring(parent.length());
            if (child.startsWith("/") || child.startsWith("\\")) {
                child = child.substring(1);
            }
            return NPath.of(child, session);
        }
        return null;
    }

    public static class GenericPathFactory implements NPathFactory {
        NWorkspace ws;

        public GenericPathFactory(NWorkspace ws) {
            this.ws = ws;
        }

        @Override
        public NSupported<NPathSPI> createPath(String path, NSession session, ClassLoader classLoader) {
            NSessionUtils.checkSession(ws, session);
            if (path != null) {
                if (path.trim().length() > 0) {
                    for (char c : path.toCharArray()) {
                        if (c < 32) {
                            return null;
                        }
                    }
                    return NSupported.of(1, () -> new GenericFilePath(path, session));
                }
            }
            return null;
        }
    }
}
