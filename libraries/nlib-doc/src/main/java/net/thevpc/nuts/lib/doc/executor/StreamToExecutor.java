/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.lib.doc.executor;

import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.lib.doc.context.NDocContext;
import net.thevpc.nuts.lib.doc.processor.NDocStreamExecutor;
import net.thevpc.nuts.lib.doc.util.FileProcessorUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;

/**
 *
 * @author thevpc
 */
public class StreamToExecutor implements NDocExecutor {

    private NDocStreamExecutor streamExecutor;

    public StreamToExecutor(NDocStreamExecutor streamExecutor) {
        this.streamExecutor = streamExecutor;
    }

    @Override
    public Object eval(InputStream source, NDocContext context) {
        return streamExecutor.eval(source, context);
    }

    @Override
    public void processPath(NPath source, String mimeType, NDocContext context) {
        String p = context.getPathTranslator().translatePath(source.toString());
        if (p != null) {
            NPath targetPath = NPath.of(p);
            FileProcessorUtils.mkdirs(targetPath.getParent());
            try (InputStream in = source.getInputStream();
                    OutputStream out = targetPath.getOutputStream();) {
//                context.getLog().debug(context.getContextName(), "update "+p+" (from "+source+") using "+streamProcessor);
                context.setVar("source",p);
                context.setVar("target",targetPath);
                streamExecutor.processStream(in, out, context);
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        }
    }

    @Override
    public void processStream(InputStream source, OutputStream target, NDocContext context) {
        streamExecutor.processStream(source, target, context);
    }

    @Override
    public String toString() {
        return String.valueOf(streamExecutor);
    }

}
