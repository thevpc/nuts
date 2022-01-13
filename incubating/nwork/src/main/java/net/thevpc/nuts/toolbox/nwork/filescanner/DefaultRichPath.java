package net.thevpc.nuts.toolbox.nwork.filescanner;

import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class DefaultRichPath implements RichPath {
    private final Path path;
    private final Set<TagInfo> tags;
    private final Map<String,Object> shared;

    public DefaultRichPath(Path path, Set<TagInfo> tags,Map<String,Object> shared) {
        this.path = path;
        this.tags = tags;
        this.shared = shared;
    }

    public Map<String, Object> getShared() {
        return shared;
    }

    public Path getPath() {
        return path;
    }

    public Set<TagInfo> getTags() {
        return tags;
    }

    @Override
    public Set<TagInfo> getTags(String type) {
        return tags.stream().filter(x -> Objects.equals(x.getTag(), type)).collect(Collectors.toSet());
    }

    @Override
    public String toString() {
        return path.toString();
    }
}
