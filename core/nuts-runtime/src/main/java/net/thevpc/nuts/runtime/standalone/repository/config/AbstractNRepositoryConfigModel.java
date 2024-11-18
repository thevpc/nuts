package net.thevpc.nuts.runtime.standalone.repository.config;

import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NStringUtils;

import java.util.LinkedHashSet;
import java.util.Set;

public abstract class AbstractNRepositoryConfigModel implements NRepositoryConfigModel{
    protected Set<String> tags = new LinkedHashSet<>();

    public boolean isPreview() {
        return tags.contains(NConstants.RepoTags.PREVIEW);
    }

    @Override
    public boolean containsTag(String tag) {
        return tags.contains(NStringUtils.trim(tag));
    }

    @Override
    public Set<String> getTags() {
        return tags;
    }

    @Override
    public void addTag(String tag) {
        if (!NBlankable.isBlank(tag)) {
            if (tags.add(NStringUtils.trim(tag))) {
                fireConfigurationChanged("tags");
            }
        }
    }

    @Override
    public void removeTag(String tag) {
        this.tags.remove(tag);
    }
}
