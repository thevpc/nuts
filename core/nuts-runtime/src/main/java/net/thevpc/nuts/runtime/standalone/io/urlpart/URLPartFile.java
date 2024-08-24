package net.thevpc.nuts.runtime.standalone.io.urlpart;

import net.thevpc.nuts.NSession;
import net.thevpc.nuts.io.NIOException;

import java.io.*;
import java.net.URL;
import java.util.function.Predicate;

class URLPartFile extends URLPart {
    public URLPartFile(URLPart parent, String path, File obj) {
        super(parent, Type.FS_FILE, path, obj);
    }

    public URLPart rootSibling(String path) {
        return of(path);
    }

    public InputStream getInputStream(NSession session) {
        try {
            return new FileInputStream(((File) obj));
        } catch (IOException e) {
            throw new NIOException(session, e);
        }
    }

    public URLPart[] getChildren(boolean includeFolders, boolean deep, final Predicate<URLPart> filter, NSession session) {
        return URLPartHelper.searchFile((File) obj, includeFolders, deep, filter,session);
    }
}
