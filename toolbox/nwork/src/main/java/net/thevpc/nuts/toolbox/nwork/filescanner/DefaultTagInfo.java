package net.thevpc.nuts.toolbox.nwork.filescanner;

import java.util.Objects;

public class DefaultTagInfo implements TagInfo {
    private final String tag;

    public DefaultTagInfo(String tag) {
        this.tag = tag;
    }

    @Override
    public String getTag() {
        return tag;
    }

    @Override
    public int hashCode() {
        return Objects.hash(tag);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefaultTagInfo that = (DefaultTagInfo) o;
        return Objects.equals(tag, that.tag);
    }
}
