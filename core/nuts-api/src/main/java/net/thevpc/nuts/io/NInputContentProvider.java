package net.thevpc.nuts.io;

public interface NInputContentProvider extends NInputStreamProvider{
    String getName();
    String contentType();
    String getCharset();
}
