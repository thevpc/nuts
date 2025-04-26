package net.thevpc.nuts.runtime.standalone.io.path.spi;

import net.thevpc.nuts.*;
import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.cmdline.NCmdLine;

import net.thevpc.nuts.io.*;
import net.thevpc.nuts.runtime.standalone.io.path.NPathFromSPI;
import net.thevpc.nuts.runtime.standalone.io.util.CoreIOUtils;
import net.thevpc.nuts.spi.NFormatSPI;
import net.thevpc.nuts.spi.NPathFactorySPI;
import net.thevpc.nuts.spi.NPathSPI;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.NOsFamily;
import net.thevpc.nuts.util.NStream;

import java.io.*;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.*;

public class GenericFilePath implements NPathSPI {

    private final String value;
    private final NPathPartList parts;


    public GenericFilePath(String value) {
        this.value = value == null ? "" : value;
        this.parts = NPathPartParser.parseParts(this.value);
    }

    @Override
    public NStream<NPath> list(NPath basePath) {
        return NStream.ofEmpty();
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


    public NPath resolve(NPath basePath, String path) {
        NPathPartList newParts = NPathPartParser.parseParts(path);
        if (newParts.isEmpty()) {
            return basePath;
        }
        if (parts.isEmpty() || newParts.get(0).getSeparator().length() > 0) {
            return NPath.of(path);
        }
        return partsToPath(parts.concat(newParts));
    }


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
            return partsToPath(new NPathPartList(a).concat(NPathPartParser.parseParts(path)));
        } else {
            return getParent(basePath).resolve(path);
        }
    }


    @Override
    public NPath toCompressedForm(NPath basePath) {
        return null;
    }

    @Override
    public NOptional<URL> toURL(NPath basePath) {
        try {
            if (URLPath.MOSTLY_URL_PATTERN.matcher(value).matches()) {
                return NOptional.of(CoreIOUtils.urlOf(value));
            }
            return NOptional.of(CoreIOUtils.urlOf("file:" + value));
        } catch (Exception e) {
            return NOptional.ofNamedError(NMsg.ofC("not an url %s", value));
        }
    }

    @Override
    public NOptional<Path> toPath(NPath basePath) {
        try {
            return NOptional.of(Paths.get(value));
        } catch (Exception ex) {
            return NOptional.ofNamedError(NMsg.ofC("not a path %s", value));
        }
    }

    @Override
    public NPathType type(NPath basePath) {
        return NPathType.NOT_FOUND;
    }

    public boolean exists(NPath basePath) {
        return false;
    }

    public long contentLength(NPath basePath) {
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
    public String getCharset(NPath basePath) {
        return null;
    }

    @Override
    public String getLocation(NPath basePath) {
        return value;
    }

    public InputStream getInputStream(NPath basePath, NPathOption... options) {
        throw new NIOException(NMsg.ofC("path not found %s", this));
    }

    public OutputStream getOutputStream(NPath basePath, NPathOption... options) {
        throw new NIOException(NMsg.ofC("path not found %s", this));
    }

    @Override
    public void delete(NPath basePath, boolean recurse) {
        throw new NIOException(NMsg.ofC("unable to delete path %s", this));
    }

    @Override
    public void mkdir(boolean parents, NPath basePath) {
        throw new NIOException(NMsg.ofC("unable to create folder out of regular file %s", this));
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
            return partsToPath(new NPathPartList(Arrays.asList(new NPathPart(p.getSeparator(), ""))));
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
            return partsToPath(NPathPartParser.parseParts(System.getProperty("user.dir")).concat(parts));
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
        return partsToPath(new NPathPartList(p2));
    }

    private NPath partsToPath(NPathPartList p2) {
        return NPath.of(
                p2.toString()
        );
    }

    @Override
    public boolean isAbsolute(NPath basePath) {
        if (parts.size() != 0) {
            NPathPart f = parts.first();
            if (f.getSeparator().length() > 0) {
                return true;
            }
            if (NWorkspace.of().getOsFamily() == NOsFamily.WINDOWS) {
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
    public Boolean isName(NPath basePath) {
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
    public Integer getNameCount(NPath basePath) {
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
    public Boolean isRoot(NPath basePath) {
        if (parts.isEmpty()) {
            return true;
        }
        if (parts.size() == 1 && parts.get(0).isTrailingSeparator()) {
            return true;
        }
        return false;
    }

    @Override
    public NPath subpath(NPath basePath, int beginIndex, int endIndex) {
        NPathPartList parts = this.parts.subList(beginIndex, endIndex);
        return partsToPath(parts);
    }

    @Override
    public List<String> getNames(NPath basePath) {
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

        public NText asFormattedString() {
            return NText.of(p.value);
        }

        @Override
        public void print(NPrintStream out) {
            out.print(asFormattedString());
        }

        @Override
        public boolean configureFirst(NCmdLine cmdLine) {
            return false;
        }
    }

    @Override
    public boolean moveTo(NPath basePath, NPath other, NPathOption... options) {
        throw new NIOException(NMsg.ofC("unable to move %s", this));
    }

    @Override
    public NPath getRoot(NPath basePath) {
        if (isRoot(basePath)) {
            return basePath;
        }
        return new NPathFromSPI(new GenericFilePath(""));
    }

    @Override
    public boolean isLocal(NPath basePath) {
        return true;
    }

    public static class GenericPathFactory implements NPathFactorySPI {

        public GenericPathFactory() {
        }

        @Override
        public NCallableSupport<NPathSPI> createPath(String path, String protocol, ClassLoader classLoader) {
            if (path != null) {
                if (path.trim().length() > 0) {
                    for (char c : path.toCharArray()) {
                        if (c < 32) {
                            return null;
                        }
                    }
                    return NCallableSupport.of(1, () -> new GenericFilePath(path));
                }
            }
            return null;
        }

        @Override
        public int getSupportLevel(NSupportLevelContext context) {
            String path = context.getConstraints();
            try {
                if (path != null) {
                    if (path.trim().length() > 0) {
                        for (char c : path.toCharArray()) {
                            if (c < 32) {
                                return NConstants.Support.NO_SUPPORT;
                            }
                        }
                        return 1;
                    }
                }
            } catch (Exception ex) {
                //ignore
            }
            return NConstants.Support.NO_SUPPORT;
        }

    }

}
