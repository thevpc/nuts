/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.xtra.expr.template;

import net.thevpc.nuts.expr.NExprContext;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

/**
 * @author thevpc
 */
public interface NTemplateProcessor {

    void processStream(InputStream source, OutputStream target, NExprContext context);

    void processStream(Reader source, Writer target, NExprContext context);
}
