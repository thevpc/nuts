/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.docusaurus;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;

/**
 *
 * @author thevpc
 */
class LenientWriter {
    
    Writer writer;
    String path;

    public LenientWriter(Writer w,String path) {
        this.writer = w;
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public Writer getWriter() {
        return writer;
    }

    public void println() {
        println("");
    }

    public void println(String s) {
        try {
            writer.append(s);
            writer.append("\n");
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
    
}
