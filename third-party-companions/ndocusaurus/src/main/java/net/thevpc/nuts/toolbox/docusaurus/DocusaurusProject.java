package net.thevpc.nuts.toolbox.docusaurus;

import net.thevpc.nuts.*;
import net.thevpc.nuts.lib.md.MdElement;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

//    static DocusaurusFile extractItem(Path p, String partialPath,NutsSession session) {
//        try {
//            BufferedReader br = Files.newBufferedReader(p);
//            String line1 = br.readLine();
//            if ("---".equals(line1)) {
//                Map<String, String> props = new HashMap<>();
//                while (true) {
//                    line1 = br.readLine();
//                    if (line1 == null || line1.equals("---")) {
//                        break;
//                    }
//                    if (line1.matches("[a-z_]+:.*")) {
//                        int colon = line1.indexOf(':');
//                        props.put(line1.substring(0, colon).trim(), line1.substring(colon + 1).trim());
//                    }
//                }
//                String id = props.get("id");
//                Integer menu_order = DocusaurusUtils.parseInt(props.get("menu_order"));
//                if (menu_order != null) {
//                    if (menu_order.intValue() <= 0) {
//                        throw new IllegalArgumentException("invalid menu_order in " + p);
//                    }
//                } else {
//                    menu_order = 0;
//                }
//                if (id != null) {
//                    return DocusaurusFile.ofPath(id, (partialPath == null || partialPath.isEmpty()) ? id : (partialPath + "/" + id), props.get("title"), p, menu_order,session);
//                }
//            }
//        } catch (IOException iOException) {
//        }
//        return null;
//    }


    public String getDocusaurusBaseFolder() {
        return this.docusaurusBaseFolder;
    }

    public NutsObjectElement getSidebars() {
        return loadModuleExportsFile("sidebars.js").asObject().orElse(NutsObjectElement.ofEmpty(session));
    }

    public String getTitle() {
        return getConfig().getString("title").get(session);
    }

    public String getProjectName() {
        return getConfig().getString("projectName").get(session);
    }

    public NutsObjectElement getConfig() {
        return loadModuleExportsFile("docusaurus.config.js")
                .asObject().orElse(NutsObjectElement.ofEmpty(session));
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
            return NutsElements.of(session).ofNull();
        }
        //(?s) stands for single line mode in which the dot includes line breaks
        Pattern p = Pattern.compile("(?s)module.exports[ ]*=[ ]*(?<json>.*[^;])[;]?");
        Matcher matcher = p.matcher(a.trim());
        String json=null;
        if(matcher.find()){
            json = matcher.group("json");
        }
        if (json==null) {
            return NutsElements.of(session).ofObject().build();
        }
        return NutsElements.of(session).json()
                .parse(json,NutsElement.class);
    }

    private String toCanonicalPath(String path) {
        try {
            return new File(path).getCanonicalPath();
        } catch (IOException ex) {
            return new File(path).getAbsolutePath();
        }
    }

//    public DocusaurusFileOrFolder extractFileOrFolder(Path path, Path root,Path configRoot) {
//        if (Files.isDirectory(path) && path.equals(root)) {
//            try {
//                return DocusaurusFolder.ofRoot(
//                        session, Files.list(path).map(p -> DocusaurusFolder.ofFileOrFolder(session, p, root,configRoot))
//                                .filter(Objects::nonNull)
//                                .toArray(DocusaurusFileOrFolder[]::new)
//                );
//            } catch (IOException ex) {
//                throw new UncheckedIOException(ex);
//            }
//        } else if (Files.isDirectory(path)) {
//            String longId = path.subpath(root.getNameCount(), path.getNameCount()).toString();
//            Path dfi = path.resolve(DOCUSAURUS_FOLDER_CONFIG);
//            NutsObjectElement config = NutsElements.of(session).forObject().build();
//            if (Files.isRegularFile(dfi)) {
//                try {
//                    config = NutsElements.of(session).parse(new String(Files.readAllBytes(dfi))).asSafeObject();
//                } catch (IOException e) {
//                    //ignore...
//                }
//            }
//            try {
//                int order = config.getSafe("order").asSafeInt(0);
//                if (order <= 0) {
//                    throw new IllegalArgumentException("");
//                }
//                String title = config.getSafeString("title",path.getFileName().toString());
//                DocusaurusFile baseContent=null;
//                List<DocusaurusFileOrFolder> children=new ArrayList<>();
//                for (Path path1 : Files.list(path).collect(Collectors.toList())) {
//                    if(Files.isRegularFile(path1) && path.getFileName().toString().equals(DocusaurusFolder.FOLDER_INFO_NAME)){
//                        baseContent=(DocusaurusFile) DocusaurusFolder.ofFileOrFolder(session, path1, root, configRoot,1);
//                    }else {
//                        children.add(DocusaurusFolder.ofFileOrFolder(session, path1, root, configRoot,- 1));
//                    }
//                }
//                return new DocusaurusFolder(
//                        longId,
//                        title,
//                        order,
//                        config,
//                        children.toArray(new DocusaurusFileOrFolder[0]),
//                        baseContent==null?null:baseContent.getContent(),
//                        baseContent==null?null:baseContent.getTree()
//                );
//            } catch (IOException e) {
//                throw new UncheckedIOException(e);
//            }
//        } else {
//            int from = root.getNameCount();
//            int to = path.getNameCount() - 1;
//            String partial = from == to ? "" : path.subpath(from, to).toString();
//            return extractItem(path, partial);
//        }
//    }

    public DocusaurusFileOrFolder[] LJSON_to_DocusaurusFileOrFolder_list(NutsElement a, DocusaurusFolder root) {
        if (a.isString()) {
            return new DocusaurusFileOrFolder[]{
                    //DocusaurusUtils.concatPath(path, member.getValue().asString())
                    root.getPage(a.asString().get(session), true, null)
            };
        } else if (a.isArray()) {
            List<DocusaurusFileOrFolder> aa = new ArrayList<>();
            for (NutsElement ljson : a.asArray().get(session)) {
                aa.addAll(Arrays.asList(LJSON_to_DocusaurusFileOrFolder_list(ljson, root)));
            }
            return aa.toArray(new DocusaurusFileOrFolder[0]);
        } else if (a.isObject()) {
            List<DocusaurusFileOrFolder> aa = new ArrayList<>();
            int order = 0;
            //detect effective folder from children
            for (NutsElementEntry member : a.asObject().get(session)) {
                DocusaurusFileOrFolder[] cc = LJSON_to_DocusaurusFileOrFolder_list(member.getValue(), root);
                String rootPath = root.getPath();
                Path parentPath = detectFileParent(cc);
                if(parentPath==null) {
                    if (rootPath != null) {
                        parentPath = Paths.get(rootPath);
                    } else {
                        parentPath = detectFileParent(cc);
                    }
                }
                aa.add(new DocusaurusFolder(
                        member.getKey().asString().get(session),//no id  here!
                        member.getKey().asString().get(session),
                        ++order,
                        NutsElements.of(session).ofObject().build(),
                        cc,
                        resolveFolderContent(parentPath),parentPath==null?null:parentPath.toString()
                ));
            }
            return aa.toArray(new DocusaurusFileOrFolder[0]);
        } else {
            throw new IllegalArgumentException("invalid");
        }
    }

    public Path detectFileParent(DocusaurusFileOrFolder[] f) {
        LinkedHashSet<Path> valid=new LinkedHashSet<>();
        for (DocusaurusFileOrFolder child :f) {
            Path path = detectFile(child);
            if(path!=null){
                valid.add(path.getParent());
            }
        }
        if(valid.size()>0){
            return valid.stream().findFirst().orElse(null);
        }
        return null;
    }
    public Path detectFile(DocusaurusFileOrFolder f) {
        if(f instanceof DocusaurusFolder){
            return detectFileParent(((DocusaurusFolder) f).getChildren());
        }
        if(f instanceof DocusaurusPathFile){
            return ((DocusaurusPathFile) f).getPath();
        }
        return null;
    }
    public DocusaurusFolder getSidebarsDocsFolder() {
        DocusaurusFileOrFolder[] someSidebars = LJSON_to_DocusaurusFileOrFolder_list(getSidebars()
                .get("someSidebar").orNull(),  getPhysicalDocsFolder());
        return new DocusaurusFolder("/", "/", 0,
                NutsElements.of(session).ofObject().build(), someSidebars,resolveFolderContent(getPhysicalDocsFolderBasePath()),
                getPhysicalDocsFolder().getPath()
                );
    }

    public MdElement resolveFolderContent(Path path) {
        if(path==null){
            return null;
        }
        Path in = path.resolve(DocusaurusFolder.FOLDER_INFO_NAME);
        if(Files.isRegularFile(in)) {
            DocusaurusFile baseContent = (DocusaurusFile) DocusaurusFolder.ofFileOrFolder(session, in, getPhysicalDocsFolderBasePath(), getPhysicalDocsFolderConfigPath(), -1);
            return baseContent == null ? null : baseContent.getContent(session);
        }
        return null;
    }


    public Path getPhysicalDocsFolderBasePath() {
        return Paths.get(this.docusaurusBaseFolder).resolve("docs").toAbsolutePath();
    }
    public Path getPhysicalDocsFolderConfigPath() {
        return Paths.get(this.docusaurusBaseFolder).resolve("docs").toAbsolutePath();
    }

    public DocusaurusFolder getPhysicalDocsFolder() {
        Path docs = getPhysicalDocsFolderBasePath();
        DocusaurusFolder root = (DocusaurusFolder) DocusaurusFolder.ofFileOrFolder(session, docs, docs,getPhysicalDocsFolderConfigPath());
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
