/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.ncode;

/**
 * @author thevpc
 */
public class FileLookup implements SourceProcessor {

    public FileLookup() {
    }

    @Override
    public void process(Source source) {
        System.out.println(source.getExternalPath());
    }

}
