package net.thevpc.nuts;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;

public interface NutsExecutionEntryManager {

    /**
     * parse Execution Entries
     *
     * @param file jar file
     * @return execution entries (class names with main method)
     */
    NutsExecutionEntry[] parse(File file);

    /**
     * parse Execution Entries
     *
     * @param file jar file
     * @return execution entries (class names with main method)
     */
    NutsExecutionEntry[] parse(Path file);

    /**
     * parse Execution Entries
     *
     * @param inputStream stream
     * @param type        stream type
     * @param sourceName  stream source name (optional)
     * @return execution entries (class names with main method)
     */
    NutsExecutionEntry[] parse(InputStream inputStream, String type, String sourceName);
}
