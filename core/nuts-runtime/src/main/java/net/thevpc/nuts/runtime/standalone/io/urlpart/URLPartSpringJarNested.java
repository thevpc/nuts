package net.thevpc.nuts.runtime.standalone.io.urlpart;

import net.thevpc.nuts.format.NVisitResult;
import net.thevpc.nuts.io.NIOException;
import net.thevpc.nuts.runtime.standalone.io.util.CoreIOUtils;
import net.thevpc.nuts.runtime.standalone.io.util.ZipUtils;
import net.thevpc.nuts.io.NIOUtils;
import net.thevpc.nuts.runtime.standalone.web.DefaultNWebCli;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

class URLPartSpringJarNested extends URLPart {
    private URLPart base0;
    private String base1;

    public URLPartSpringJarNested(String path, URL obj, URLPart base0, String base1) {
        super(null, Type.FS_FILE, !path.startsWith("/") ? ("/" + path) : path, obj);
        this.base0 = base0;
        this.base1 = base1;
    }

    public URLPart rootSibling(String path) {
        if(path.startsWith("/META-INF/") || path.equals("/META-INF")) {
            return new URLPartJar(path, null, base0);
        }
        if(path.startsWith("META-INF/") || path.equals("META-INF")) {
            return new URLPartJar(path, null, base0);
            //return new URLPartSpringJarNested(path, null, base0, path);
        }
        //remove leading
        return new URLPartSpringJarNested(path, null, base0, path.substring(1));
    }

    public InputStream getInputStream() {
        List<InputStream> found = new ArrayList<>();
        if (obj instanceof URL) {
            try {
                return DefaultNWebCli.prepareGlobalOpenStream(((URL) obj));
            } catch (IOException e) {
                throw new NIOException(e);
            }
        }
        URLPart pp = base0;
        String path1 = CoreIOUtils.concatPath(base1 , path);
        ZipUtils.visitZipStream(pp.getInputStream(), (path, inputStream) -> {
            if (path.equals(path1)) {
                found.add(new ByteArrayInputStream(NIOUtils.readBytes(inputStream)));
                return NVisitResult.TERMINATE;
            }
            return NVisitResult.CONTINUE;
        });

        if (found.isEmpty()) {
            return null;
        }
        return found.get(0);
    }

    public URLPart[] getChildren(boolean includeFolders, boolean deep, final Predicate<URLPart> filter) {
        try (InputStream is = base0.getInputStream()) {
            return URLPartHelper.searchStream(is, s -> {
                if (s.startsWith(base1 + "/")) {
                    URLPartSpringJarNested urlPartSpringJarNested = new URLPartSpringJarNested(s.substring(base1.length()), null, base0, base1);
                    InputStream inputStream = urlPartSpringJarNested.getInputStream();
                    if(inputStream==null){
                        inputStream = urlPartSpringJarNested.getInputStream();
                        throw new IllegalArgumentException("Error");
                    }
                    return urlPartSpringJarNested;
                }
                return null;
            }, includeFolders, deep, filter);
        } catch (IOException e) {
            throw new NIOException(e);
        }
    }

    @Override
    public String toString() {
        return "jar:nested:" + base0 + "!" + base1 + "!" + path;
    }
}
