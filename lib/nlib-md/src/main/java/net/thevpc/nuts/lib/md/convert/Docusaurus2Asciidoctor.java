/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.lib.md.convert;


import net.thevpc.nuts.NutsElement;
import net.thevpc.nuts.NutsObjectElement;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.lib.md.docusaurus.DocusaurusProject;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 *
 * @author thevpc
 */
public class Docusaurus2Asciidoctor {

    protected Path adocFile;
    protected Path pdfFile;
    protected boolean generatePdf;
    protected DocusaurusProject project;

    public Docusaurus2Asciidoctor(DocusaurusProject project) {
        this.project = project;
    }

    public Docusaurus2Asciidoctor(File project, NutsSession session) {
        this.project = new DocusaurusProject(project.getPath(),session);
    }

    public void run() {
        Docusaurus2Adoc d2a = new Docusaurus2Adoc(project);
        String asciiDoctorBaseFolder = getAsciiDoctorBaseFolder();
        String pn = project.getProjectName();
        if (pn == null || pn.isEmpty()) {
            pn = "docusaurus";
        }
        adocFile = toCanonicalFile(Paths.get(asciiDoctorBaseFolder).resolve(pn + ".adoc"));
        d2a.run(adocFile);
        pdfFile = new Adoc2Pdf().generatePdf(getAdoc2PdfConfig());
    }

    private Path toCanonicalFile(Path path) {
        return path.toAbsolutePath().normalize();
    }

    public Adoc2PdfConfig getAdoc2PdfConfig() {
        Adoc2PdfConfig config = new Adoc2PdfConfig();
        NutsObjectElement asciiDoctorConfig = getAsciiDoctorConfig();
        config.setBin(asciiDoctorConfig.getSafeObject("pdf").getSafeObject("command").getSafeString("bin"));
        config.setArgs(asciiDoctorConfig.getSafeObject("pdf").getSafeObject("command").getSafeArray("args")
                .stream().map(x->x.asString()).toArray(String[]::new));
        config.setWorkDir(toCanonicalFile(Paths.get(project.getDocusaurusBaseFolder())).toString());
        config.setBaseDir(toCanonicalFile(Paths.get(getAsciiDoctorBaseFolder())).toString());
        config.setInputAdoc(adocFile.toString());
        NutsElement output = asciiDoctorConfig.getSafeObject("pdf").getSafe("output");
        String pdfFile=project.getProjectName();
        if(output.isString()){
            String s=output.asString().trim();
            if(!s.isEmpty()){
                if(s.endsWith("/") ||s.endsWith("\\")){
                    s+=project.getProjectName()+".pdf";
                }
                pdfFile=s;
            }
        }else{
            pdfFile=project.getProjectName()+".pdf";
        }
        pdfFile=toCanonicalFile(Paths.get(pdfFile)).toString();
        config.setOutputPdf(pdfFile);
        config.setPlaceHolderReplacer((String varName) -> {
            if (varName.equals("asciidoctor.baseDir")) {
                String r = getAsciiDoctorBaseFolder();
                if(r!=null){
                    r=Paths.get(r).normalize().toAbsolutePath().toString();
                }
                return r;
            }
            if (varName.equals("docusaurus.baseDir")) {
                String r = project.getDocusaurusBaseFolder();
                if(r!=null){
                    r=Paths.get(r).normalize().toAbsolutePath().toString();
                }
                return r;
            }
            if (varName.startsWith("asciidoctor.")) {
                return asciiDoctorConfig.get(varName.substring("asciidoctor.".length())).asString();
            }
            if (varName.startsWith("docusaurus.")) {
                return project.getConfig().get(varName.substring("docusaurus.".length())).asString();
            }
            //if (varName.startsWith("docusaurus.")) {
                return project.getConfig().get(varName).asString();
            //}
            //return null;
        });
        return config;
    }

    private String getAsciiDoctorBaseFolder() {
        String s = getAsciiDoctorConfig().get("path").asString();
        if (!new File(s).isAbsolute()) {
            s = project.getDocusaurusBaseFolder() + "/" + s;
        }
        return s;
    }

    private NutsObjectElement getAsciiDoctorConfig() {
        return project.getConfig().getSafeObject("customFields").getSafeObject("asciidoctor");
    }

}
