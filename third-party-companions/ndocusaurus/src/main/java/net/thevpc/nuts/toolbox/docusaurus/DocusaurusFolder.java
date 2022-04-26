package net.thevpc.nuts.toolbox.docusaurus;

import net.thevpc.nuts.*;
import net.thevpc.nuts.lib.md.MdElement;
import net.thevpc.nuts.lib.md.docusaurus.DocusaurusUtils;
import net.thevpc.nuts.lib.md.util.MdUtils;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DocusaurusFolder implements DocusaurusFileOrFolder {
    public static final String FOLDER_INFO_NAME = "folder-info.md";

    public static final NameResolver DEFAULT_NAME_RESOLVER = new NameResolver() {
        @Override
        public boolean accept(DocusaurusFileOrFolder item, String name) {
            return item.getShortId().equals(name);
        }
    };
    public static final Comparator<DocusaurusFileOrFolder> DFOF_COMPARATOR = new Comparator<DocusaurusFileOrFolder>() {
        @Override
        public int compare(DocusaurusFileOrFolder o1, DocusaurusFileOrFolder o2) {
            if (o1.isFolder() && o2.isFile()) {
                return -1;
            }
            if (o1.isFile() && o2.isFolder()) {
                return 1;
            }
            int x = Integer.compare(o1.getOrder(), o2.getOrder());
            if (x != 0) {
                return x;
            }
            String title1 = NutsUtilStrings.trim(o1.getTitle());
            String title2 = NutsUtilStrings.trim(o2.getTitle());
            x = title1.toLowerCase().compareTo(title2.toLowerCase());
            if (x != 0) {
                return x;
            }
            return title1.compareTo(title2);
        }
    };
    private final MdElement tree;
    private String longId;
    private String shortId;
    private String title;
    private int order;
    private NutsObjectElement config;
    private DocusaurusFileOrFolder[] children;
    private String path;

    public DocusaurusFolder(String longId, String title, int order, NutsObjectElement config, DocusaurusFileOrFolder[] children, MdElement tree, String path) {
        this.longId = longId;
        String[] r = getPathArray(longId);
        this.shortId = r.length == 0 ? "/" : r[r.length - 1];
        this.title = title;
        this.order = order;
        this.config = config;
        this.children = children;
        this.tree = tree;
        this.path = path;
        Arrays.sort(children, DFOF_COMPARATOR);
    }

    public static DocusaurusFileOrFolder ofFileOrFolder(NutsSession session, Path path, Path root, Path configRoot) {
        return ofFileOrFolder(session, path, root, configRoot, -1);
    }

    public static DocusaurusFileOrFolder ofFileOrFolder(NutsSession session, Path path, Path root, Path configRoot, int maxDepth) {
        return Files.isDirectory(path) ? DocusaurusFolder.ofFolder(session, path, root, configRoot, maxDepth) : DocusaurusPathFile.ofFile(path, root, session);
    }

    public static DocusaurusFolder ofFolder(NutsSession session, Path path, Path root, Path configRoot, int maxDepth) {
        if (Files.isDirectory(path) && path.equals(root)) {
            try {
                DocusaurusFile baseContent = null;
                List<DocusaurusFileOrFolder> children = new ArrayList<>();
                int nexDepth = maxDepth < 0 ? -1 : maxDepth - 1;
                for (Path path1 : Files.list(path).collect(Collectors.toList())) {
                    if (Files.isRegularFile(path1) && path1.getFileName().toString().equals(FOLDER_INFO_NAME)) {
                        baseContent = (DocusaurusFile) DocusaurusFolder.ofFileOrFolder(session, path1, root, configRoot, nexDepth);
                    } else if (maxDepth != 0 && (Files.isDirectory(path1) || path1.getFileName().toString().endsWith(".md"))) {
                        DocusaurusFileOrFolder cc = DocusaurusFolder.ofFileOrFolder(session, path1, root, configRoot, nexDepth);
                        if (cc != null) {
                            children.add(cc);
                        }
                    }
                }

                return DocusaurusFolder.ofRoot(session, children.toArray(new DocusaurusFileOrFolder[0]),
                        baseContent == null ? null : baseContent.getContent(session), path.toString()
                );
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        } else if (Files.isDirectory(path)) {
            String longId = path.subpath(root.getNameCount(), path.getNameCount()).toString();
            Path dfi = path.resolve(FOLDER_INFO_NAME);
//            Path cfi = configRoot.resolve(longId).resolve(".docusaurus-folder-config.json");
            int order = 1;
            NutsElement config = null;
            String title = null;
            if (Files.isRegularFile(dfi)) {
                DocusaurusFile ff = DocusaurusPathFile.ofFile(dfi, root, session);
                if (ff != null) {
                    order = ff.getOrder();
                    title = ff.getTitle();
                    config = ff.getConfig();
                }
            }
            if (config == null) {
                config = NutsElements.of(session).ofObject().build();
            }
            if (title == null || title.trim().isEmpty()) {
                title = path.getFileName().toString();
            }
            try {
                if (order <= 0) {
                    throw new IllegalArgumentException("invalid order " + order + " in " + dfi);
                }
                DocusaurusFile baseContent = null;
                List<DocusaurusFileOrFolder> children = new ArrayList<>();
                int nexDepth = maxDepth < 0 ? -1 : maxDepth - 1;
                for (Path path1 : Files.list(path).collect(Collectors.toList())) {
                    if (Files.isRegularFile(path1) && path1.getFileName().toString().equals(FOLDER_INFO_NAME)) {
                        baseContent = (DocusaurusFile) DocusaurusFolder.ofFileOrFolder(session, path1, root, configRoot, nexDepth);
                    } else if (maxDepth != 0 && (Files.isDirectory(path1) || path1.getFileName().toString().endsWith(".md"))) {
                        DocusaurusFileOrFolder cc = DocusaurusFolder.ofFileOrFolder(session, path1, root, configRoot, nexDepth);
                        if (cc != null) {
                            children.add(cc);
                        }
                    }
                }
                return new DocusaurusFolder(
                        longId,
                        title,
                        order,
                        config.asObject().get(session),
                        children.toArray(new DocusaurusFileOrFolder[0]),
                        baseContent == null ? null : baseContent.getContent(session),
                        path.toString()
                );
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        } else {
            throw new IllegalArgumentException("Unexpected");
        }
    }

    public static DocusaurusFolder ofRoot(NutsSession session, DocusaurusFileOrFolder[] children, MdElement tree, String path) {
        return new DocusaurusFolder("/", "/", 0, NutsElements.of(session).ofObject().build(), children, tree, path);
    }

    public static DocusaurusFolder of(String longId, String title, int order, NutsObjectElement config, DocusaurusFileOrFolder[] children, String path) {
        return new DocusaurusFolder(longId, title, order, config, children, null, path);
    }

    private static String[] getPathArray(String path) {
        return Arrays.stream(path.split("/")).filter(x -> x.length() > 0).toArray(String[]::new);
    }

    public String getPath() {
        return path;
    }

    @Override
    public String getShortId() {
        return shortId;
    }

    @Override
    public String getLongId() {
        return longId;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public boolean isFile() {
        return false;
    }

    public int getOrder() {
        return order;
    }

    @Override
    public boolean isFolder() {
        return true;
    }

    public MdElement getContent(NutsSession session) {
        return tree;
    }

    public String toJSON(int indent) {
        StringBuilder sb = new StringBuilder();
        char[] indentChars = DocusaurusUtils.indentChars(indent);
        sb.append(indentChars);
        int filesCount = 0;
        int foldersCount = 0;
        for (DocusaurusFileOrFolder child : children) {
            if (child.isFolder()) {
                foldersCount++;
            } else if (child.isFile()) {
                filesCount++;
            }
        }
        if (foldersCount == 0 && filesCount == 0) {

        } else if (foldersCount > 0 && filesCount > 0) {
            sb.append("'").append(MdUtils.escapeString(getTitle() + "': {"));
            if (children.length == 0) {
                sb.append("}");
            } else {
                for (DocusaurusFileOrFolder child : children) {
                    if (child instanceof DocusaurusFile) {
                        sb.append("\n").append("'").append(MdUtils.escapeString(child.getTitle() + "': ["));
                        sb.append(child.toJSON(indent + 1));
                        sb.append("],");
                    } else {
                        sb.append("\n").append(child.toJSON(indent + 1)).append(",");
                    }
                }
                sb.append("\n").append(indentChars).append("}");
            }
            return sb.toString();
        } else if (foldersCount > 0) {
            sb.append("'").append(MdUtils.escapeString(getTitle() + "': {"));
            if (children.length == 0) {
                sb.append("}");
            } else {
                for (DocusaurusFileOrFolder child : children) {
                    sb.append("\n").append(child.toJSON(indent + 1)).append(",");
                }
                sb.append("\n").append(indentChars).append("}");
            }
            return sb.toString();
        } else if (filesCount > 0) {
            sb.append("'").append(MdUtils.escapeString(getTitle() + "': ["));
            if (children.length == 0) {
                sb.append("]");
            } else {
                for (DocusaurusFileOrFolder child : children) {
                    sb.append("\n").append(child.toJSON(indent + 1)).append(",");
                }
                sb.append("\n").append(indentChars).append("]");
            }
            return sb.toString();
        } else {

        }

        sb.append("'").append(MdUtils.escapeString(getTitle() + "': ["));
        if (children.length == 0) {
            sb.append("]");
        } else {
            for (DocusaurusFileOrFolder child : children) {
                sb.append("\n").append(child.toJSON(indent + 1)).append(",");
            }
            sb.append("\n").append(indentChars).append("]");
        }
        return sb.toString();
    }

    public DocusaurusFileOrFolder getImmediate(String name, boolean required, NameResolver nameResolver) {
        DocusaurusFileOrFolder[] a = getAllImmediate(name, nameResolver);
        if (a.length == 0) {
            if (required) {
                throw new NoSuchElementException("path not found: " + DocusaurusUtils.concatPath(longId, name));
            }
            return null;
        }
        if (a.length > 1) {
            throw new IllegalArgumentException("Ambiguous " + DocusaurusUtils.concatPath(longId, name) + ":"
                    + (Arrays.stream(a).map(x -> {
                        if (x instanceof DocusaurusPathFile) {
                            if (((DocusaurusPathFile) x).getPath() != null) {
                                return x + ":" + ((DocusaurusPathFile) x).getPath();
                            }
                        }
                        return x.toString();
                    })
                    .collect(Collectors.joining(" ; "))

            ));
        }
        return a[0];
    }

    public DocusaurusFileOrFolder[] getAllImmediate(String name, NameResolver nameResolver) {
        Predicate<DocusaurusFileOrFolder> filter2 = nameResolver != null ? x -> nameResolver.accept(x, name) : (x -> DEFAULT_NAME_RESOLVER.accept(x, name));
        return children().filter(filter2).toArray(DocusaurusFileOrFolder[]::new);
    }

    public DocusaurusFile getPage(String path, boolean required, NameResolver nameResolver) {
        DocusaurusFileOrFolder t = get(path, required, nameResolver);
        if (t instanceof DocusaurusFile) {
            return (DocusaurusFile) t;
        }
        if (required) {
            throw new NoSuchElementException("Invalid page path at path: " + path);
        }
        return null;
    }

    public DocusaurusFileOrFolder get(String path, boolean required, NameResolver nameResolver) {
        path = path.trim();
        if (path.isEmpty()/* || path.startsWith("/")*/) {
            throw new IllegalArgumentException("Invalid path: " + path);
        }
        String[] pathArray = getPathArray(path);
        return get(pathArray, required, nameResolver);
    }

    public DocusaurusFileOrFolder get(String[] pathArray, boolean required, NameResolver nameResolver) {
        if (pathArray.length == 0) {
            throw new IllegalArgumentException("Invalid path: <empty>");
        }
        DocusaurusFileOrFolder immediate = getImmediate(pathArray[0], required, nameResolver);
        if (immediate == null) {
            return null;
        }
        if (pathArray.length > 1) {
            if (immediate instanceof DocusaurusFolder) {
                String[] nextArray = new String[pathArray.length - 1];
                System.arraycopy(pathArray, 1, nextArray, 0, nextArray.length);
                return ((DocusaurusFolder) immediate).get(nextArray, required, nameResolver);
            } else {
                if (required) {
                    throw new NoSuchElementException("path not found: " + DocusaurusUtils.concatPath(longId, String.join("/", pathArray)));
                }
                return null;
            }
        }
        return immediate;
    }

    public Stream<DocusaurusFileOrFolder> children() {
        return Arrays.stream(children);
    }

    public DocusaurusFileOrFolder[] getChildren() {
        return children;
    }

    public NutsObjectElement getConfig() {
        return config;
    }

    public String toString() {
        String s = longId;
        if (!s.startsWith("/")) {
            s = "/" + s;
        }
        if (!s.endsWith("/")) {
            s = s + "/";
        }
        return s;
    }
}
