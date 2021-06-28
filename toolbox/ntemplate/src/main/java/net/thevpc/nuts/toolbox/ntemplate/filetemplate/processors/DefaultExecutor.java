/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.ntemplate.filetemplate.processors;

import net.thevpc.nuts.toolbox.ntemplate.filetemplate.ExprEvaluator;
import net.thevpc.nuts.toolbox.ntemplate.filetemplate.FileTemplater;
import net.thevpc.nuts.toolbox.ntemplate.filetemplate.StreamExecutor;
import net.thevpc.nuts.toolbox.ntemplate.filetemplate.StreamProcessor;
import net.thevpc.nuts.toolbox.ntemplate.filetemplate.util.FileProcessorUtils;

import java.io.*;

/**
 *
 * @author thevpc
 */
public class DefaultExecutor implements StreamExecutor, StreamProcessor {

    private ExprEvaluator evaluator;

    public DefaultExecutor(ExprEvaluator evaluator) {
        this.evaluator = evaluator;
    }

    @Override
    public void processStream(InputStream source, OutputStream target, FileTemplater context) {
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
    public Object execute(InputStream source, OutputStream target, FileTemplater context) {
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
