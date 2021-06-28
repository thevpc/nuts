/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.lib.md.convert;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;

/**
 *
 * @author thevpc
 */
class LenientWriter {
    
    Writer writer;

    public LenientWriter(Writer w) {
        this.writer = w;
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
