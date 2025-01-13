/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.lib.doc.processor;

import net.thevpc.nuts.lib.doc.context.NDocContext;
import net.thevpc.nuts.lib.doc.util.FileProcessorUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 *
 * @author thevpc
 */
public class StreamToProcessor implements NDocProcessor {

    private NDocStreamProcessor streamProcessor;

    public StreamToProcessor(NDocStreamProcessor streamProcessor) {
        this.streamProcessor = streamProcessor;
    }


    @Override
    public void processPath(Path source, String mimeType, NDocContext context) {
        String p = context.getPathTranslator().translatePath(source.toString());
        if (p != null) {
            Path targetPath = Paths.get(p);
            FileProcessorUtils.mkdirs(targetPath.getParent());
            try (InputStream in = Files.newInputStream(source);
                    OutputStream out = Files.newOutputStream(targetPath);) {
//                context.getLog().debug(context.getContextName(), "update "+p+" (from "+source+") using "+streamProcessor);
                context.setVar("source",p);
                context.setVar("target",targetPath);
                streamProcessor.processStream(in, out, context);
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        }
    }

    @Override
    public void processStream(InputStream source, OutputStream target, NDocContext context) {
        streamProcessor.processStream(source, target, context);
    }

    @Override
    public String toString() {
        return String.valueOf(streamProcessor);
    }

}
