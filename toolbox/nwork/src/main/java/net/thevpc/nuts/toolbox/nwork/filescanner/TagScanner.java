package net.thevpc.nuts.toolbox.nwork.filescanner;

import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

public interface TagScanner {
    String[] supportedTags();

    /**
     * scan path for te given tags
     *
     * @param path path to scan
     * @param tags tags to loog for
     * @param shared
     * @return tags detected
     */
    TagInfo[] scanTags(Path path, Set<String> tags, Map<String, Object> shared);
}
