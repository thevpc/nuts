package net.thevpc.nuts.lib.md.docusaurus;

import net.thevpc.nuts.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class DocusaurusProject {
    public static final String DOCUSAURUS_FOLDER_CONFIG = ".docusaurus-folder-config.json";
    public static final String DOCUSAURUS_FOLDER_CONFIG_MIMETYPE = "text/x-json-docusaurus-folder-config";
    NameResolver nameResolver = new NameResolver() {
        @Override
        public boolean accept(DocusaurusFileOrFolder item, String name) {
            if (item.isFolder()) {
                if (item.getTitle().equals(name)) {
                    return true;
                }
            }
            return item.getShortId().equals(name);
        }
    };


    private String docusaurusConfigBaseFolder;
    private String docusaurusBaseFolder;

    private NutsObjectElement config;

    private NutsObjectElement sidebars;
    private NutsSession session;

    public DocusaurusProject(String docusaurusBaseFolder, String docusaurusConfigBaseFolder,NutsSession session) {
        this.docusaurusBaseFolder = Paths.get(docusaurusBaseFolder).toAbsolutePath().toString();
        if(docusaurusConfigBaseFolder==null){
            this.docusaurusConfigBaseFolder=docusaurusBaseFolder;
        }else{
            this.docusaurusConfigBaseFolder=docusaurusConfigBaseFolder;
        }
        this.session = session;
        if (!Files.exists(Paths.get(resolvePath("docusaurus.config.js")))) {
            throw new IllegalArgumentException("Invalid docusaurus v2 folder : " + toCanonicalPath(this.docusaurusBaseFolder));
        }
        this.sidebars = loadModuleExportsFile("sidebars.js").asSafeObject();
        this.config = loadModuleExportsFile("docusaurus.config.js").asSafeObject();
    }

    public NutsSession getSession() {
        return session;
    }

    private static String extractPartialPathParentString(Path p, Path rootPath) {
        Path pp = extractPartialPath(p, rootPath).getParent();
        return pp == null ? null : pp.toString();
    }

    private static Path extractPartialPath(Path p, Path rootPath) {
        if (p.startsWith(rootPath)) {
            return p.subpath(rootPath.getNameCount(), p.getNameCount());
        } else {
            throw new IllegalArgumentException("Invalid partial path");
        }
    }

    static DocusaurusFile extractItem(Path p, String partialPath) {
        try {
            BufferedReader br = Files.newBufferedReader(p);
            String line1 = br.readLine();
            if ("---".equals(line1)) {
                Map<String, String> props = new HashMap<>();
                while (true) {
                    line1 = br.readLine();
                    if (line1 == null || line1.equals("---")) {
                        break;
                    }
                    if (line1.matches("[a-z_]+:.*")) {
                        int colon = line1.indexOf(':');
                        props.put(line1.substring(0, colon).trim(), line1.substring(colon + 1).trim());
                    }
                }
                String id = props.get("id");
                Integer menu_order = DocusaurusUtils.parseInt(props.get("menu_order"));
                if (menu_order != null) {
                    if (menu_order.intValue() <= 0) {
                        throw new IllegalArgumentException("invalid menu_order in " + p);
                    }
                } else {
                    menu_order = 0;
                }
                if (id != null) {
                    return DocusaurusFile.ofPath(id, (partialPath == null || partialPath.isEmpty()) ? id : (partialPath + "/" + id), props.get("title"), p, menu_order);
                }
            }
        } catch (IOException iOException) {
        }
        return null;
    }


    public String getDocusaurusBaseFolder() {
        return this.docusaurusBaseFolder;
    }

    public NutsElement getSidebars() {
        return this.sidebars;
    }

    public String getTitle() {
        return this.config.get("title").asString();
    }

    public String getProjectName() {
        return this.config.get("projectName").asString();
    }

    public NutsObjectElement getConfig() {
        return this.config;
    }

    private String resolvePath(String path) {
        String p = this.docusaurusBaseFolder;
        if (!p.endsWith("/") && !path.startsWith("/")) {
            p = p + "/";
        }
        return p + path;
    }

    public NutsElement loadModuleExportsFile(String path) {
        String a = null;
        try {
            a = new String(Files.readAllBytes(Paths.get(resolvePath(path))));
        } catch (IOException ex) {
            return session.getWorkspace().elem().forNull();
        }
        String json = a.replace("module.exports =", "").trim();
        if (json.endsWith(";")) {
            json = json.substring(0, json.length() - 1);
        }
        return session.getWorkspace().elem().setContentType(NutsContentType.JSON)
                .parse(json,NutsElement.class);
    }

    private String toCanonicalPath(String path) {
        try {
            return new File(path).getCanonicalPath();
        } catch (IOException ex) {
            return new File(path).getAbsolutePath();
        }
    }

    public DocusaurusFileOrFolder extractFileOrFolder(Path path, Path root,Path configRoot) {
        if (Files.isDirectory(path) && path.equals(root)) {
            try {
                return DocusaurusFolder.ofRoot(
                        session, Files.list(path).map(p -> DocusaurusFolder.ofFileOrFolder(session, p, root,configRoot))
                                .filter(Objects::nonNull)
                                .toArray(DocusaurusFileOrFolder[]::new)
                );
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        } else if (Files.isDirectory(path)) {
            String longId = path.subpath(root.getNameCount(), path.getNameCount()).toString();
            Path dfi = path.resolve(DOCUSAURUS_FOLDER_CONFIG);
            NutsObjectElement config = session.getWorkspace().elem().forObject().build();
            if (Files.isRegularFile(dfi)) {
                try {
                    config = session.getWorkspace().elem().parse(new String(Files.readAllBytes(dfi))).asSafeObject();
                } catch (IOException e) {
                    //ignore...
                }
            }
            try {
                int order = config.getSafe("order").asSafeInt(0);
                if (order <= 0) {
                    throw new IllegalArgumentException("");
                }
                String title = config.getSafeString("title",path.getFileName().toString());
                return new DocusaurusFolder(
                        longId,
                        title,
                        order,
                        config,
                        Files.list(path).map(p -> DocusaurusFolder.ofFileOrFolder(session, p, root,configRoot))
                                .filter(Objects::nonNull)
                                .toArray(DocusaurusFileOrFolder[]::new)
                );
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        } else {
            int from = root.getNameCount();
            int to = path.getNameCount() - 1;
            String partial = from == to ? "" : path.subpath(from, to).toString();
            return extractItem(path, partial);
        }
    }

    public DocusaurusFileOrFolder[] LJSON_to_DocusaurusFileOrFolder_list(NutsElement a, String path, DocusaurusFolder root) {
        if (a.isString()) {
            return new DocusaurusFileOrFolder[]{
                    //DocusaurusUtils.concatPath(path, member.getValue().asString())
                    root.getPage(a.asString(), true, null)
            };
        } else if (a.isArray()) {
            List<DocusaurusFileOrFolder> aa = new ArrayList<>();
            for (NutsElement ljson : a.asArray()) {
                aa.addAll(Arrays.asList(LJSON_to_DocusaurusFileOrFolder_list(ljson, path, root)));
            }
            return aa.toArray(new DocusaurusFileOrFolder[0]);
        } else if (a.isObject()) {
            List<DocusaurusFileOrFolder> aa = new ArrayList<>();
            int order = 0;
            for (NutsElementEntry member : a.asObject()) {
                DocusaurusFileOrFolder[] cc = LJSON_to_DocusaurusFileOrFolder_list(member.getValue(), path, root);
                aa.add(new DocusaurusFolder(
                        path,
                        member.getKey().asString(),
                        ++order,
                        session.getWorkspace().elem().forObject().build(),
                        cc
                ));
            }
            return aa.toArray(new DocusaurusFileOrFolder[0]);
        } else {
            throw new IllegalArgumentException("invalid");
        }
    }

    public DocusaurusFolder getSidebarsDocsFolder() {
        DocusaurusFileOrFolder[] someSidebars = LJSON_to_DocusaurusFileOrFolder_list(this.sidebars.getSafe("someSidebar"), "/", getPhysicalDocsFolder());
        return DocusaurusFolder.ofRoot(session, someSidebars);
    }

    public DocusaurusFolder getPhysicalDocsFolder() {
        Path docs = Paths.get(this.docusaurusBaseFolder).resolve("docs").toAbsolutePath();
        DocusaurusFolder root = (DocusaurusFolder) DocusaurusFolder.ofFileOrFolder(session, docs, docs,
                Paths.get(docusaurusConfigBaseFolder).resolve("docs").toAbsolutePath());
        return root;
    }

//    public DocusaurusPart[] loadDocusaurusProject() {
//        if (!Files.exists(Paths.get(resolvePath("docusaurus.config.js")))) {
//            throw new IllegalArgumentException("Invalid docusaurus v2 folder : " + toCanonicalPath(docusaurusBaseFolder));
//        }
//        try {
//            this.sidebars = loadModuleExportsFile("sidebars.js");
//            this.config = loadModuleExportsFile("docusaurus.config.js");
//            LinkedHashMap<String, List<DocusaurusFile>> project = new LinkedHashMap<>();
//            String docs = this.docusaurusBaseFolder + "/docs";
//            Map<String, List<DocusaurusFile>> ir = allDocs(docs, docs, true);
//            for (LJSON.Member member : this.sidebars.get("someSidebar").objectMembers()) {
//                ArrayList<DocusaurusFile> items = new ArrayList<>();
//                project.put(member.getName(), items);
//                for (LJSON jsonValue : member.getValue().arrayMembers()) {
//                    String s = jsonValue.asString();
//                    int ls = s.lastIndexOf('/');
////                    Map<String, List<DocusaurusFile>> ir = null;
////                    if (ls > 0) {
////                        String z=s.substring(0,ls);
////                        ir = allDocs(docs + "/" + z, docs,false);
////                    } else {
////                        ir = allDocs(docs, docs,false);
////                    }
//                    List<DocusaurusFile> u = ir.get(s);
//                    if (u != null && u.size() > 0) {
//                        if (u.size() == 1) {
//                            items.add(u.get(0));
//                        } else {
//                            System.err.println("ambiguous: " + s + " :: " + u.stream().map(x -> x.getPath()).collect(Collectors.toList()));
//                        }
//                    } else {
//                        System.err.println("document id not found: " + s);
//                    }
//                }
//            }
//            List<DocusaurusPart> parts = new ArrayList<>();
//            for (Map.Entry<String, List<DocusaurusFile>> entry : project.entrySet()) {
//                DocusaurusPart p = new DocusaurusPart(entry.getKey(), entry.getValue().toArray(new DocusaurusFile[0]));
//                parts.add(p);
//            }
//            return parts.toArray(new DocusaurusPart[0]);
//        } catch (IOException ex) {
//            throw new UncheckedIOException(ex);
//        }
//    }
}
