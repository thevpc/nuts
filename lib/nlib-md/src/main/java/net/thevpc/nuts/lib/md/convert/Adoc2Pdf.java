/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.lib.md.convert;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author thevpc
 */
public class Adoc2Pdf {

    public Path getPdfFile(Adoc2PdfConfig config) {
        File baseDir = toCanonicalFile(new File(config.getBaseDir()));
        String pdfFileName = config.getOutputPdf();
        if (!pdfFileName.endsWith(".pdf")) {
            pdfFileName += ".pdf";
        }
        if (!isAbsolutePath(pdfFileName)) {
            pdfFileName = new File(baseDir, pdfFileName).getPath();
        }
        return Paths.get(pdfFileName);
    }

    public Path generatePdf(Adoc2PdfConfig config) {
        String bin = config.getBin();
        String[] args = config.getArgs();
        File workDir = toCanonicalFile(new File(config.getWorkDir()));
        File baseDir = toCanonicalFile(new File(config.getBaseDir()));

        if (args == null) {
            args = new String[0];
        }
        if (bin == null) {
            bin = "asciidoctor-pdf";
        }
        List<String> cmdList = new ArrayList<>();
        cmdList.add(StringUtils.replace(bin, config.getPlaceHolderReplacer()));
        cmdList.addAll(Arrays.asList(args).stream().map(x -> StringUtils.replace(x, config.getPlaceHolderReplacer())).collect(Collectors.toList()));
        String pdfFileName = config.getOutputPdf();
        if (!pdfFileName.endsWith(".pdf")) {
            pdfFileName += ".pdf";
        }
        if (!isAbsolutePath(pdfFileName)) {
            pdfFileName = new File(baseDir, pdfFileName).getPath();
        }
        String adocFilePath = config.getInputAdoc();
        if (!isAbsolutePath(adocFilePath)) {
            adocFilePath = new File(baseDir, adocFilePath).getPath();
        }
        File adocFile = new File(adocFilePath);

        cmdList.add("--out-file");
        cmdList.add(pdfFileName);
        cmdList.add(adocFile.toString());
        ProcessBuilder pb = new ProcessBuilder(cmdList)
                .inheritIO()
                .redirectErrorStream(true)
                .directory(workDir);
        int result = 0;
        System.out.println(cmdList.stream().map(x->{
            if(x.indexOf(' ')>=0){
                return "\""+x+"\"";
            }
            return x;
        }).collect(Collectors.joining(" ")));
        try {
            result = pb.start().waitFor();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        if (result != 0) {
            throw new RuntimeException("Pdf generation failed with error code:" + result);
        }
        return Paths.get(pdfFileName);
    }

    private boolean isAbsolutePath(String path) {
        return new File(path).isAbsolute();
    }
    
    private File toCanonicalFile(File path) {
        try {
            return path.getCanonicalFile();
        } catch (IOException ex) {
            return path.getAbsoluteFile();
        }
    }
}
