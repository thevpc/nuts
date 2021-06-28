package net.thevpc.nuts.toolbox.ntemplate.filetemplate;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;

public interface TemplateProcessor {

    void processStream(InputStream source, OutputStream target, FileTemplater context);
    
    void processPath(Path source, String mimeType, FileTemplater context);
}
