package net.thevpc.nuts.toolbox.nwork.filescanner;

import java.nio.file.Path;
import java.util.Set;

public interface RichPath {
    Path getPath();

    Set<TagInfo> getTags();
    Set<TagInfo> getTags(String type);
}
