package net.thevpc.nuts.runtime.standalone.io.path.spi;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NutsCommandLine;
import net.thevpc.nuts.format.NutsTreeVisitor;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.runtime.standalone.session.NutsSessionUtils;
import net.thevpc.nuts.spi.NutsFormatSPI;
import net.thevpc.nuts.spi.NutsPathFactory;
import net.thevpc.nuts.spi.NutsPathSPI;
import net.thevpc.nuts.text.NutsTexts;
import net.thevpc.nuts.util.NutsStream;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.*;

public class GenericFilePath implements NutsPathSPI {

    private final String value;
    private final NutsPathPartList parts;
    private final NutsSession session;


    public GenericFilePath(String value, NutsSession session) {
        this.value = value == null ? "" : value;
        this.parts = NutsPathPartParser.parseParts(this.value, session);
        this.session = session;
    }

    @Override
    public NutsStream<NutsPath> list(NutsPath basePath) {
        return NutsStream.ofEmpty(getSession());
    }

    @Override
    public NutsFormatSPI formatter(NutsPath basePath) {
        return new MyPathFormat(this);
    }

    @Override
    public String getName(NutsPath basePath) {
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
    public String getProtocol(NutsPath basePath) {
        return "";
    }


    @Override
    public NutsPath resolve(NutsPath basePath, String path) {
        NutsPathPartList newParts = NutsPathPartParser.parseParts(path, session);
        if (newParts.isEmpty()) {
            return basePath;
        }
        if (parts.isEmpty() || newParts.get(0).getSeparator().length() > 0) {
            return NutsPath.of(path, session);
        }
        return partsToPath(parts.concat(newParts));
    }

    @Override
    public NutsPath resolve(NutsPath basePath, NutsPath path) {
        return resolve(basePath, path == null ? null : path.toString());
    }

    @Override
    public NutsPath resolveSibling(NutsPath basePath, String path) {
        if (path == null || path.isEmpty()) {
            return getParent(basePath);
        }
        if (isRoot(basePath)) {
            List<NutsPathPart> a = new ArrayList<>();
            if (parts.size() > 0) {
                a.add(new NutsPathPart(parts.get(0).getSeparator(), ""));
            } else {
                a.add(new NutsPathPart(File.separator, ""));
            }
            return partsToPath(new NutsPathPartList(a, session).concat(NutsPathPartParser.parseParts(path, session)));
        } else {
            return getParent(basePath).resolve(path);
        }
    }

    @Override
    public NutsPath resolveSibling(NutsPath basePath, NutsPath path) {
        return resolveSibling(basePath, path == null ? null : path.toString());
    }

    @Override
    public NutsPath toCompressedForm(NutsPath basePath) {
        return null;
    }

    @Override
    public URL toURL(NutsPath basePath) {
        try {
            if (URLPath.MOSTLY_URL_PATTERN.matcher(value).matches()) {
                return new URL(value);
            }
            return new URL("file:" + value);
        } catch (MalformedURLException e) {
            throw new NutsIOException(session, e);
        }
    }

    @Override
    public Path toFile(NutsPath basePath) {
        try {
            return Paths.get(value);
        } catch (Exception ex) {
            throw new NutsIOException(session, ex);
        }
    }

    @Override
    public boolean isSymbolicLink(NutsPath basePath) {
        return false;
    }

    @Override
    public boolean isOther(NutsPath basePath) {
        return false;
    }

    @Override
    public boolean isDirectory(NutsPath basePath) {
        return false;
    }

    @Override
    public boolean isRegularFile(NutsPath basePath) {
        return false;
    }

    public boolean exists(NutsPath basePath) {
        return false;
    }

    public long getContentLength(NutsPath basePath) {
        return -1;
    }

    @Override
    public String getContentEncoding(NutsPath basePath) {
        return null;
    }

    @Override
    public String getContentType(NutsPath basePath) {
        return null;
    }

    @Override
    public String getLocation(NutsPath basePath) {
        return value;
    }

    public InputStream getInputStream(NutsPath basePath) {
        throw new NutsIOException(session, NutsMessage.ofCstyle("path not found %s", this));
    }

    public OutputStream getOutputStream(NutsPath basePath) {
        throw new NutsIOException(session, NutsMessage.ofCstyle("path not found %s", this));
    }

    @Override
    public NutsSession getSession() {
        return session;
    }

    @Override
    public void delete(NutsPath basePath, boolean recurse) {
        throw new NutsIOException(getSession(), NutsMessage.ofCstyle("unable to delete path %s", this));
    }

    @Override
    public void mkdir(boolean parents, NutsPath basePath) {
        throw new NutsIOException(getSession(), NutsMessage.ofCstyle("unable to create folder out of regular file %s", this));
    }

    @Override
    public Instant getLastModifiedInstant(NutsPath basePath) {
        return null;
    }

    @Override
    public Instant getLastAccessInstant(NutsPath basePath) {
        return null;
    }

    @Override
    public Instant getCreationInstant(NutsPath basePath) {
        return null;
    }

    @Override
    public NutsPath getParent(NutsPath basePath) {
        if (isRoot(basePath)) {
            return null;
        }
        if (parts.isEmpty()) {
            return null;
        }
        if (parts.size() == 1) {
            NutsPathPart p = parts.get(0);
            if (p.isTrailingSeparator()) {
                return null;
            }
            if (p.isName()) {
                return null;
            }
            return partsToPath(new NutsPathPartList(Arrays.asList(new NutsPathPart(p.getSeparator(), "")), session));
        }
        if (parts.get(parts.size() - 1).isTrailingSeparator()) {
            return partsToPath(parts.subList(0, parts.size() - 2));
        }
        return partsToPath(parts.subList(0, parts.size() - 1));
    }

    @Override
    public NutsPath toAbsolute(NutsPath basePath, NutsPath rootPath) {
        if (isAbsolute(basePath)) {
            return basePath;
        }
        if (rootPath == null) {
            return partsToPath(NutsPathPartParser.parseParts(System.getProperty("user.dir"), session).concat(parts));
        }
        return rootPath.toAbsolute().resolve(toString());
    }

    @Override
    public NutsPath normalize(NutsPath basePath) {
        List<NutsPathPart> p2 = new ArrayList<>();
        for (NutsPathPart part : parts) {
            NutsPathPart p = part;
            if (p.isSeparated() && !p.getSeparator().equals(File.separator)) {
                p = new NutsPathPart(File.separator, p.getName());
            }
            if (p.isTrailingSeparator() && p2.size() > 0) {
                //ignore
            } else if (p.getName().equals(".")) {
                if (p2.size() > 0) {
                    //ignore
                } else {
                    if (!p.getSeparator().isEmpty()) {
                        p = new NutsPathPart(p.getSeparator(), "");
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
                        p = new NutsPathPart(p.getSeparator(), "");
                        p2.add(p);
                    }
                }
                if (p2.size() == 1) {
                    NutsPathPart r = p2.get(0);
                    if (r.isTrailingSeparator()) {
                        //
                    } else {
                        p = new NutsPathPart(p.getSeparator(), "");
                        p2.add(p);
                    }
                } else {
                    p2.remove(p2.size() - 1);
                }
            } else {
                p2.add(p);
            }
        }
        return partsToPath(new NutsPathPartList(p2, session));
    }

    private NutsPath partsToPath(NutsPathPartList p2) {
        return NutsPath.of(
                p2.toString(),
                session
        );
    }

    @Override
    public boolean isAbsolute(NutsPath basePath) {
        if(parts.size() != 0) {
            NutsPathPart f = parts.first();
            if(f.getSeparator().length() > 0){
                return true;
            }
            if(session.env().getOsFamily()==NutsOsFamily.WINDOWS) {
                String n = f.getName();
                //test if the name is a drive name
                if (n.length()==2 && n.charAt(1)==':') {
                    char c=n.charAt(0);
                    if((c>='A' && c<='Z') || (c>='a' && c<='z')){
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public String owner(NutsPath basePath) {
        return null;
    }

    @Override
    public String group(NutsPath basePath) {
        return null;
    }

    @Override
    public Set<NutsPathPermission> getPermissions(NutsPath basePath) {
        Set<NutsPathPermission> p = new LinkedHashSet<>();
        return Collections.unmodifiableSet(p);
    }

    @Override
    public void setPermissions(NutsPath basePath, NutsPathPermission... permissions) {
    }

    @Override
    public void addPermissions(NutsPath basePath, NutsPathPermission... permissions) {
    }

    @Override
    public void removePermissions(NutsPath basePath, NutsPathPermission... permissions) {
    }

    @Override
    public boolean isName(NutsPath basePath) {
        if (parts.size() == 0) {
            return true;
        }
        if (parts.size() > 1) {
            return false;
        }
        NutsPathPart v = parts.get(0);
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
    public int getPathCount(NutsPath basePath) {
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
    public boolean isRoot(NutsPath basePath) {
        if (parts.isEmpty()) {
            return true;
        }
        if (parts.size() == 1 && parts.get(0).isTrailingSeparator()) {
            return true;
        }
        return false;
    }

    @Override
    public NutsStream<NutsPath> walk(NutsPath basePath, int maxDepth, NutsPathOption[] options) {
        return NutsStream.ofEmpty(getSession());
    }

    @Override
    public NutsPath subpath(NutsPath basePath, int beginIndex, int endIndex) {
        NutsPathPartList parts = this.parts.subList(beginIndex, endIndex);
        return partsToPath(parts);
    }

    @Override
    public List<String> getItems(NutsPath basePath) {
        NutsPathPartList parts = this.parts;
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

    private static class MyPathFormat implements NutsFormatSPI {

        private final GenericFilePath p;

        public MyPathFormat(GenericFilePath p) {
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

    @Override
    public void moveTo(NutsPath basePath, NutsPath other, NutsPathOption... options) {
        throw new NutsIOException(session, NutsMessage.ofCstyle("unable to move %s", this));
    }

    @Override
    public void copyTo(NutsPath basePath, NutsPath other, NutsPathOption... options) {
        throw new NutsIOException(session, NutsMessage.ofCstyle("unable to copy %s", this));
    }

    @Override
    public NutsPath getRoot(NutsPath basePath) {
        if (isRoot(basePath)) {
            return basePath;
        }
        NutsPath r = basePath.getParent();
        if (r != null) {
            return r.getRoot();
        }
        return null;
    }

    @Override
    public void walkDfs(NutsPath basePath, NutsTreeVisitor<NutsPath> visitor, int maxDepth, NutsPathOption... options) {

    }

    @Override
    public boolean isLocal(NutsPath basePath) {
        return true;
    }

    @Override
    public NutsPath toRelativePath(NutsPath basePath, NutsPath parentPath) {
        String child = basePath.getLocation();
        String parent = parentPath.getLocation();
        if (child.startsWith(parent)) {
            child = child.substring(parent.length());
            if (child.startsWith("/") || child.startsWith("\\")) {
                child = child.substring(1);
            }
            return NutsPath.of(child, session);
        }
        return null;
    }

    public static class GenericPathFactory implements NutsPathFactory {
        NutsWorkspace ws;

        public GenericPathFactory(NutsWorkspace ws) {
            this.ws = ws;
        }

        @Override
        public NutsSupported<NutsPathSPI> createPath(String path, NutsSession session, ClassLoader classLoader) {
            NutsSessionUtils.checkSession(ws, session);
            if (path != null) {
                if (path.trim().length() > 0) {
                    for (char c : path.toCharArray()) {
                        if (c < 32) {
                            return null;
                        }
                    }
                    return NutsSupported.of(1, () -> new GenericFilePath(path, session));
                }
            }
            return null;
        }
    }
}
