/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.lib.doc.processor.impl;

import net.thevpc.nuts.lib.doc.executor.NDocExprEvaluator;
import net.thevpc.nuts.lib.doc.context.NDocContext;
import net.thevpc.nuts.lib.doc.processor.NDocStreamExecutor;
import net.thevpc.nuts.lib.doc.processor.NDocStreamProcessor;
import net.thevpc.nuts.lib.doc.util.FileProcessorUtils;

import java.io.*;

/**
 *
 * @author thevpc
 */
public class DefaultNDocStreamExecutor implements NDocStreamExecutor {

    private NDocExprEvaluator evaluator;

    public DefaultNDocStreamExecutor(NDocExprEvaluator evaluator) {
        this.evaluator = evaluator;
    }

    @Override
    public Object eval(InputStream source, NDocContext context) {
        String text = FileProcessorUtils.loadString(source, null);
        if (text != null) {
            return evaluator.eval(text, context);
        }
        return null;
    }

    @Override
    public void processStream(InputStream source, OutputStream target, NDocContext context) {
        String text = FileProcessorUtils.loadString(source, null);
        if (text != null) {
            Object z = evaluator.eval(text, context);
            if (z != null) {
                try {
                    Writer w = new OutputStreamWriter(target);
                    w.write(String.valueOf(z));
                    w.flush();
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
        }
    }

    @Override
    public Object execute(InputStream source, OutputStream target, NDocContext context) {
        String text = FileProcessorUtils.loadString(source, null);
        if (text != null) {
            Object z = evaluator.eval(text, context);
            return z == null ? "" : z;
        }
        return "";
    }

    @Override
    public String toString() {
        return "Executor(" + evaluator + ")";
    }

}
