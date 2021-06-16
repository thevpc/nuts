package net.thevpc.nuts.runtime.standalone.io;

import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.runtime.core.util.CoreIOUtils;

import java.net.URL;
import java.util.Objects;

public class URLPath extends NutsPathBase {
    private URL url;

    public URLPath(URL url, NutsSession session) {
        super(session);
        if (url == null) {
            throw new IllegalArgumentException("invalid url");
        }
        this.url = url;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        URLPath urlPath = (URLPath) o;
        return Objects.equals(url, urlPath.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url);
    }

    public String getName() {
        return CoreIOUtils.getURLName(url);
    }

    @Override
    public String toString() {
        return url.toString();
    }
}
