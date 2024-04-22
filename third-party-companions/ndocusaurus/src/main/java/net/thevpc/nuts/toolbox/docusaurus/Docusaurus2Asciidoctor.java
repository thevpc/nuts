/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.docusaurus;


import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NObjectElement;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.lib.md.convert.Adoc2Pdf;
import net.thevpc.nuts.lib.md.convert.Adoc2PdfConfig;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 *
 * @author thevpc
 */
public class Docusaurus2Asciidoctor {

//    protected Path adocFile;
    protected Path pdfFile;
    protected boolean generatePdf;
    protected DocusaurusProject project;

    public Docusaurus2Asciidoctor(DocusaurusProject project) {
        this.project = project;
    }

    public Docusaurus2Asciidoctor(File project, NSession session) {
        this.project = new DocusaurusProject(project.getPath(),null,session);
    }

    public Path getPdfFile() {
        return new Adoc2Pdf().getPdfFile(getAdoc2PdfConfig());
    }

    public Path getAdocFile() {
        NSession session = project.getSession();
        String asciiDoctorBaseFolder = getAsciiDoctorBaseFolder();
        String pdfOutput = project.getConfigAsciiDoctor()
                .getObject("pdf")
                .orElse(NObjectElement.ofEmpty(session))
                .getString("output").orNull();
        String pn=null;
        if(pdfOutput!=null && pdfOutput.endsWith(".pdf")){
            String pn0 = Paths.get(pdfOutput).getFileName().toString();
            pn0=pn0.substring(0,pn0.length()-4);
            if(pn0.length()>0){
                pn=pn0;
            }
        }
        if(pn==null){
            String pn0 = project.getProjectName();
            if(pn0!=null && pn0.length()>0){
                pn=pn0;
            }
        }
        if(pn==null){
            pn = "docusaurus";
        }
        return toCanonicalFile(Paths.get(asciiDoctorBaseFolder).resolve(pn + ".adoc"));
    }

    public void createAdocFile() {
        Docusaurus2Adoc d2a = new Docusaurus2Adoc(project);
        d2a.run(getAdocFile());
    }

    public void createPdfFile() {
        pdfFile = new Adoc2Pdf().generatePdf(getAdoc2PdfConfig());
    }

    public void run() {
        createAdocFile();
        createPdfFile();
    }

    private Path toCanonicalFile(Path path,Path basePath) {
        if(path.isAbsolute()){
            return toCanonicalFile(path);
        }
        if(basePath!=null){
            if(basePath.isAbsolute()){
                return toCanonicalFile(basePath.resolve(path));
            }else{
                throw new IllegalArgumentException("base path must be absolute");
            }
        }
        return toCanonicalFile(path);
    }
    private Path toCanonicalFile(Path path) {
        return path.toAbsolutePath().normalize();
    }

    public Adoc2PdfConfig getAdoc2PdfConfig() {
        NSession session = project.getSession();
        Adoc2PdfConfig config = new Adoc2PdfConfig();
        config.setSession(session);
        NObjectElement asciiDoctorConfig = project.getConfigAsciiDoctor();
        config.setBin(asciiDoctorConfig.getStringByPath("pdf","command","bin").get(session));
        config.setArgs(asciiDoctorConfig.getArrayByPath("pdf","command","args").get(session)
                .stream().map(x->x.asString().get(session)).toArray(String[]::new));
        config.setWorkDir(toCanonicalFile(Paths.get(project.getDocusaurusBaseFolder())).toString());
        config.setBaseDir(toCanonicalFile(Paths.get(getAsciiDoctorBaseFolder())).toString());
        config.setInputAdoc(getAdocFile().toString());
        NElement output = asciiDoctorConfig.getByPath("pdf","output"). get(session);
        String pdfFile=project.getProjectName();
        if(output.isString()){
            String s=output.asString().get(session).trim();
            if(!s.isEmpty()){
                if(s.endsWith("/") ||s.endsWith("\\")){
                    s+=project.getProjectName()+".pdf";
                }
                pdfFile=s;
            }
        }else{
            pdfFile=project.getProjectName()+".pdf";
        }
        pdfFile=toCanonicalFile(Paths.get(pdfFile),toCanonicalFile(Paths.get(project.getDocusaurusBaseFolder()))).toString();
        config.setOutputPdf(pdfFile);
        config.setPlaceHolderReplacer((String varName) -> {
            if (varName.equals("asciidoctor.baseDir")) {
                String r = getAsciiDoctorBaseFolder();
                if(r!=null){
                    r=Paths.get(r).toAbsolutePath().normalize().toString();
                }
                return r;
            }
            if (varName.equals("docusaurus.baseDir")) {
                String r = project.getDocusaurusBaseFolder();
                if(r!=null){
                    r=Paths.get(r).toAbsolutePath().normalize().toString();
                }
                return r;
            }
            if (varName.startsWith("asciidoctor.")) {
                return asciiDoctorConfig.getString(varName.substring("asciidoctor.".length())).get(session);
            }
            if (varName.startsWith("docusaurus.")) {
                return project.getConfig().getString(varName.substring("docusaurus.".length())).get(session);
            }
            //if (varName.startsWith("docusaurus.")) {
                return project.getConfig().getString(varName).get(session);
            //}
            //return null;
        });
        return config;
    }

    private String getAsciiDoctorBaseFolder() {
        NSession session = project.getSession();
        String s = project.getConfigAsciiDoctor().getString("path").get(session);
        if (!new File(s).isAbsolute()) {
            s = project.getDocusaurusBaseFolder() + "/" + s;
        }
        return s;
    }
}
