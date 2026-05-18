package net.thevpc.nuts.io;

import net.thevpc.nuts.util.NSetter;

import java.io.InputStream;

public interface NInputStreamProvider {
    @NSetter
    InputStream inputStream();
}
