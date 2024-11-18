package net.thevpc.nuts.runtime.standalone.io.urlpart;

import net.thevpc.nuts.io.NIOException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.function.Predicate;

class URLPartWeb extends URLPart {
    public URLPartWeb(URLPart parent, String path, Object obj) {
        super(parent, Type.WEB, path, obj);
    }

    public InputStream getInputStream() {
        try {
            if (obj instanceof URL) {
                return ((URL) obj).openStream();
            } else {
                return new URL(path).openStream();
            }
        } catch (IOException e) {
            throw new NIOException(e);
        }
    }

    public URLPart[] getChildren(boolean includeFolders, boolean deep, final Predicate<URLPart> filter) {
        throw new UnsupportedOperationException("unsupported");
    }

    @Override
    public URLPart rootSibling(String path) {
        return of(path);
    }
}
