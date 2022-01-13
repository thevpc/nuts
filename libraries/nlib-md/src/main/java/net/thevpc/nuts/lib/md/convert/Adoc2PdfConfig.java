/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.lib.md.convert;

import java.util.function.Function;

/**
 *
 * @author thevpc
 */
public class Adoc2PdfConfig {

    private String bin;
    private String[] args;
    private String workDir;
    private String baseDir;
    private String outputPdf;
    private String inputAdoc;
    private Function<String, String> placeHolderReplacer;

    public String getOutputPdf() {
        return outputPdf;
    }

    public void setOutputPdf(String outputPdf) {
        this.outputPdf = outputPdf;
    }

    public String getInputAdoc() {
        return inputAdoc;
    }

    public void setInputAdoc(String inputAdoc) {
        this.inputAdoc = inputAdoc;
    }

    public String getBin() {
        return bin;
    }

    public void setBin(String bin) {
        this.bin = bin;
    }

    public String[] getArgs() {
        return args;
    }

    public void setArgs(String[] args) {
        this.args = args;
    }

    public String getWorkDir() {
        return workDir;
    }

    public void setWorkDir(String workDir) {
        this.workDir = workDir;
    }

    public String getBaseDir() {
        return baseDir;
    }

    public void setBaseDir(String baseDir) {
        this.baseDir = baseDir;
    }

    public Function<String, String> getPlaceHolderReplacer() {
        return placeHolderReplacer;
    }

    public void setPlaceHolderReplacer(Function<String, String> placeHolderReplacer) {
        this.placeHolderReplacer = placeHolderReplacer;
    }

}
