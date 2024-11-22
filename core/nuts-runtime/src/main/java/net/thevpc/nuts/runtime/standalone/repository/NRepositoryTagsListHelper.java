package net.thevpc.nuts.runtime.standalone.repository;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

public class NRepositoryTagsListHelper {
    LinkedHashSet<String> tags = new LinkedHashSet<>();

    public NRepositoryTagsListHelper() {
    }

    public NRepositoryTagsListHelper add(NRepositoryTagsListHelper all) {
        if (all != null) {
            for (String s : all.tags) {
                add(s);
            }
        }
        return this;
    }

    public NRepositoryTagsListHelper add(Collection<String> all) {
        if (all != null) {
            for (String s : all) {
                add(s);
            }
        }
        return this;
    }

    public NRepositoryTagsListHelper add(String[] all) {
        if (all != null) {
            for (String s : all) {
                add(s);
            }
        }
        return this;
    }

    public NRepositoryTagsListHelper add(String tag) {
        if (tag != null) {
            String s = tag.trim();
            if (!s.isEmpty()) {
                tags.add(s);
            }
        }
        return this;
    }

    public String[] toArray() {
        return tags.toArray(new String[0]);
    }

    @Override
    public String toString() {
        return String.valueOf(tags);
    }

    public Set<String> toSet() {
        return new HashSet<>(tags);
    }
}
