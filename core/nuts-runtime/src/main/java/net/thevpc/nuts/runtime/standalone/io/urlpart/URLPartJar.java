package net.thevpc.nuts.runtime.standalone.io.urlpart;

import net.thevpc.nuts.NSession;
import net.thevpc.nuts.format.NVisitResult;
import net.thevpc.nuts.io.NIOException;
import net.thevpc.nuts.runtime.standalone.io.util.ZipUtils;
import net.thevpc.nuts.util.NIOUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

class URLPartJar extends URLPart {
    private URLPart base0;

    public URLPartJar(String path, Object obj, URLPart base0) {
        super(null, Type.FS_FILE, path, obj);
        this.base0 = base0;
    }

    public URLPart rootSibling(String path) {
        return new URLPartJar(path, obj, base0);
    }

    public InputStream getInputStream(NSession session) {
        List<InputStream> found = new ArrayList<>();
        URLPart pp = (URLPart) obj;
        ZipUtils.visitZipStream(pp.getInputStream(session), (path, inputStream) -> {
            if (path.equals(URLPartJar.this.path)) {
                found.add(new ByteArrayInputStream(NIOUtils.readBytes(inputStream)));
                return NVisitResult.TERMINATE;
            }
            return NVisitResult.CONTINUE;
        }, session);
        if (found.isEmpty()) {
            return null;
        }
        return found.get(0);
    }

    public URLPart[] getChildren(boolean includeFolders, boolean deep, final Predicate<URLPart> filter, NSession session) {
        try(InputStream is=base0.getInputStream(session)) {
            return URLPartHelper.searchStream(is,
                    s -> new URLPartJar(s, base0, base0)
                    , includeFolders, deep, filter,session);
        } catch (IOException e) {
            throw new NIOException(session, e);
        }
    }
}
