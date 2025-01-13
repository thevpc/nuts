/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.lib.doc.processor;

import net.thevpc.nuts.lib.doc.context.NDocContext;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author thevpc
 */
public interface NDocStreamProcessor {

    public void processStream(InputStream source, OutputStream target, NDocContext context);
}
