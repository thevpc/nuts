/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.lib.md.convert;

import net.thevpc.nuts.NutsArrayElement;
import net.thevpc.nuts.NutsElement;
import net.thevpc.nuts.NutsObjectElement;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.lib.md.MdElement;
import net.thevpc.nuts.lib.md.asciidoctor.AsciiDoctorWriter;
import net.thevpc.nuts.lib.md.docusaurus.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author thevpc
 */
public class Docusaurus2Adoc {

//    protected DocusaurusProject project;
    protected String[] headers;
    protected String projectName;
    protected String projectTitle;
    protected DocusaurusFolder rootFolder;
    protected NutsSession session;

    public Docusaurus2Adoc(String projectName, String projectTitle, String[] headers, DocusaurusFolder docs,NutsSession session) {
        this.projectName = projectName;
        this.projectTitle = projectTitle;
        this.headers = headers;
        this.rootFolder = docs;
        this.session = session;
    }

    public Docusaurus2Adoc(DocusaurusProject project) {
        NutsObjectElement asciidoctorConfig = project.getConfig().getSafeObject("customFields").getSafeObject("asciidoctor");
        if (asciidoctorConfig == null) {
            throw new IllegalArgumentException("missing customFields.asciidoctor in docusaurus.config.js file");
        }
        NutsArrayElement headersJson = asciidoctorConfig.getSafeObject("pdf").getSafeArray("headers");
        List<String> headersList = new ArrayList<>();
        for (NutsElement jsonItem : headersJson) {
            headersList.add(jsonItem.asString());
        }
        this.projectName = project.getProjectName();
        this.projectTitle = project.getTitle();
        this.headers = headersList.toArray(new String[0]);
        this.rootFolder = project.getSidebarsDocsFolder();
    }

    public Docusaurus2Adoc(File project, NutsSession session) {
        this(new DocusaurusProject(project.getPath(),session));
    }

    public String runToString() {
        StringWriter w = new StringWriter();
        run(w);
        return w.toString();
    }

    public void run(Path file) {
        try (PrintStream out = new PrintStream(Files.newOutputStream(file))) {
            run(out);
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
        try {
            run(new LenientWriter(out));
        } finally {
            try {
                out.flush();
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        }
    }

    protected void run(DocusaurusFileOrFolder part, LenientWriter out, AsciiDoctorWriter asciiDoctorWriter) {
        if(part instanceof DocusaurusFile){
            DocusaurusFile item=(DocusaurusFile) part;
            MdElement tree = null;
            switch (item.getType()) {
                case PATH: {
                    DocusaurusMdParser p = null;
                    try {
                        p = new DocusaurusMdParser(Files.newBufferedReader(item.getPath()));
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                    tree = p.parse();
                    break;
                }
                case CONTENT: {
                    DocusaurusMdParser p = new DocusaurusMdParser(new StringReader(item.getContent()));
                    tree = p.parse();
                    break;
                }
                case TREE: {
                    tree = item.getTree();
                    break;
                }
                default: {
                    throw new IllegalArgumentException("Unsupported type");
                }
            }
            if (tree != null) {
                MdElement tree2 = new DocusaurusTreeTransform(session).transformDocument(tree);
                if (tree2 != null) {
                    out.println();
                    out.println("== " + item.getTitle());
                    asciiDoctorWriter.write(tree2);
                }
            }
        }else if(part instanceof DocusaurusFolder){
            DocusaurusFolder folder=(DocusaurusFolder)part;
            out.println();
//                out.println("# " + entry.getKey());
            out.println("");
            out.println("= " + part.getTitle());

//                out.println("\n"
//                        + "[partintro]\n"
//                        + "--\n"
//                        + "This is the introduction to the first part of our mud-encrusted journey.\n"
//                        + "-- \n"
//                );
            out.println("");
            for (DocusaurusFileOrFolder child : folder.getChildren()) {
                run(child,out,asciiDoctorWriter);
            }
        }
    }
    protected void run(LenientWriter out) {
        try {
            AsciiDoctorWriter asciiDoctorWriter = new AsciiDoctorWriter(out.writer);
            writeHeader(out);
            for (DocusaurusFileOrFolder docusaurusFileOrFolder : rootFolder.getChildren()) {
                run(docusaurusFileOrFolder,out,asciiDoctorWriter);
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
