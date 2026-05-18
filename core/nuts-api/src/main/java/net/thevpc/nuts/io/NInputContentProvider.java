package net.thevpc.nuts.io;

public interface NInputContentProvider extends NInputStreamProvider{
    String name();
    String contentType();
    String charset();
}
