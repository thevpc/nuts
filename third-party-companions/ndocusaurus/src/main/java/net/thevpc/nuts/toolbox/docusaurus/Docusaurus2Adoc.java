/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.docusaurus;

import net.thevpc.nuts.elem.NArrayElement;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NObjectElement;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.lib.md.MdBody;
import net.thevpc.nuts.lib.md.MdElement;
import net.thevpc.nuts.lib.md.MdText;
import net.thevpc.nuts.lib.md.MdTitle;
import net.thevpc.nuts.lib.md.asciidoctor.AsciiDoctorWriter;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * @author thevpc
 */
public class Docusaurus2Adoc {

    //    protected DocusaurusProject project;
    protected String[] headers;
    protected String projectName;
    protected String projectTitle;
    protected DocusaurusFolder rootFolder;

//    public Docusaurus2Adoc(String projectName, String projectTitle, String[] headers, DocusaurusFolder docs,NSession session) {
//        this.projectName = projectName;
//        this.projectTitle = projectTitle;
//        this.headers = headers;
//        this.rootFolder = docs;
//        this.session = session;
//    }

    public Docusaurus2Adoc(DocusaurusProject project) {
        NObjectElement asciidoctorConfig = project.getConfigAsciiDoctor();
        if (asciidoctorConfig == null) {
            throw new IllegalArgumentException("missing asciidoctor condig");
        }
        NArrayElement headersJson = asciidoctorConfig.getObject("pdf")
                .orElse(NObjectElement.ofEmpty())
                .getArray("headers")
                .orElse(NArrayElement.ofEmpty());
        List<String> headersList = new ArrayList<>();
        for (NElement jsonItem : headersJson) {
            headersList.add(jsonItem.asString().get());
        }
        this.projectName = project.getProjectName();
        this.projectTitle = project.getTitle();
        this.headers = headersList.toArray(new String[0]);
        this.rootFolder = project.getSidebarsDocsFolder();
    }

    public Docusaurus2Adoc(File project, NSession session) {
        this(new DocusaurusProject(project.getPath(), null));
    }

    public String runToString() {
        StringWriter w = new StringWriter();
        run(w);
        return w.toString();
    }

    public void run(Path file) {
        try (PrintStream out = new PrintStream(Files.newOutputStream(file))) {
            run(new LenientWriter(new PrintWriter(out, true), file.toString()));
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public void run(PrintStream out) {
        PrintWriter w = null;
        try {
            w = new PrintWriter(out, true);
            run(w);
        } finally {
            if (w != null) {
                w.flush();
            }
        }
    }

    public void run(Writer out) {
        run(new LenientWriter(out, null));
    }

    protected String resolvePathParent(String s) {
        if (s == null) {
            return null;
        }
        if (s.trim().isEmpty()) {
            return null;
        }
        return Paths.get(s).getParent().toString();
    }

    protected void run(DocusaurusFileOrFolder part, LenientWriter out, AsciiDoctorWriter asciiDoctorWriter, int minDepth) {
        String toPath = resolvePathParent(out.path);
        if (part instanceof DocusaurusFile) {
            DocusaurusFile item = (DocusaurusFile) part;
            String fromPath = null;
            if (item instanceof DocusaurusPathFile) {
                fromPath = resolvePathParent(((DocusaurusPathFile) item).getPath().toString());
            }
            MdElement tree = item.getContent();
            if (tree != null) {
                MdElement tree2 = new DocusaurusTreeTransform(minDepth + 1, fromPath, toPath).transformDocument(tree);
                if (tree2 != null) {
                    out.println();
                    asciiDoctorWriter.write(new MdBody(new MdElement[]{
                            new MdTitle("#", MdText.phrase(item.getTitle()), minDepth, new MdElement[0]),
                            tree2
                    }));
                }
            }
        } else if (part instanceof DocusaurusFolder) {
            DocusaurusFolder folder = (DocusaurusFolder) part;
            out.println();
//                out.println("# " + entry.getKey());
            out.println("");
            MdElement tree = folder.getContent();
            MdElement tree2 = tree;
            if (tree != null) {
                String p = folder.getPath();
                tree2 = new DocusaurusTreeTransform(minDepth, p, toPath).transformDocument(tree);
            }
            List<MdElement> b = new ArrayList<>();
//            if (tree2 == null || !startsWithTitle(tree2, minDepth)) {
//                b.add(new MdTitle("#", MdText.phrase(part.getTitle()), minDepth, new MdElement[0]));
//            }
            if (tree2 != null) {
                if (tree2 instanceof MdBody
                        && ((MdBody) tree2).getChildren().length > 0
                        && ((MdBody) tree2).getChildren()[0] instanceof MdTitle
                        && ((MdTitle) (((MdBody) tree2).getChildren()[0])).type().depth() == minDepth
                ) {
                    //discard the first title and replace with chapter title
                    List<MdElement> cc = new ArrayList<>();
                    MdTitle t0 = ((MdTitle) (((MdBody) tree2).getChildren()[0]));
                    MdTitle t2 = new MdTitle(t0.getCode(), MdText.phrase(part.getTitle()), t0.type().depth(), t0.getChildren());
                    cc.add(t2);
                    MdBody b2 = new MdBody(cc.toArray(new MdElement[0]));
                    b.add(b2);
                } else if (tree2 instanceof MdTitle && ((MdTitle) tree2).type().depth() == minDepth) {
                    //discard the first title and replace with chapter title
                    MdTitle t0 = ((MdTitle) tree2);
                    MdTitle t2 = new MdTitle(t0.getCode(), MdText.phrase(part.getTitle()), t0.type().depth(), t0.getChildren());
                    b.add(t2);
                } else {
                    //add chapter title
                    b.add(new MdTitle("#", MdText.phrase(part.getTitle()), minDepth, new MdElement[0]));
                    b.add(tree2);
                }
            } else {
                b.add(new MdTitle("#", MdText.phrase(part.getTitle()), minDepth, new MdElement[0]));
            }
            if (!b.isEmpty()) {
                asciiDoctorWriter.write(new MdBody(b.toArray(new MdElement[0])));
            }


//                out.println("\n"
//                        + "[partintro]\n"
//                        + "--\n"
//                        + "This is the introduction to the first part of our mud-encrusted journey.\n"
//                        + "-- \n"
//                );
            out.println("");
            for (DocusaurusFileOrFolder child : folder.getChildren()) {
                run(child, out, asciiDoctorWriter, minDepth + 1);
            }
        }
    }

    private boolean startsWithTitle(MdElement tree, int minDepth) {
        if (tree.isTitle()) {
            if (tree.asTitle().type().depth() <= minDepth) {
                return true;
            }
            return false;
        }
        if (tree.isBody()) {
            for (MdElement ee : tree.asBody().getChildren()) {
                if (startsWithTitle(ee, minDepth)) {
                    return true;
                } else if (ee.isText()) {
                    String text = ee.asText().getText();
                    if (text.trim().isEmpty()) {
                        //
                    } else {
                        return false;
                    }
                } else {
                    return false;
                }
            }
            return false;
        }
        return false;
    }

    protected void run(LenientWriter out) {
        try {
            try {
                AsciiDoctorWriter asciiDoctorWriter = new AsciiDoctorWriter(out.writer);
                writeHeader(out);
                for (DocusaurusFileOrFolder docusaurusFileOrFolder : rootFolder.getChildren()) {
                    //in asciidoctor the min depth = 2
                    run(docusaurusFileOrFolder, out, asciiDoctorWriter, 2);
                }
            } finally {
                out.writer.flush();
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private void writeHeader(LenientWriter out) throws IOException {
        out.println("= " + projectTitle);
        for (String jsonItem : headers) {
            out.println(jsonItem);
        }
        out.println();
    }


}
