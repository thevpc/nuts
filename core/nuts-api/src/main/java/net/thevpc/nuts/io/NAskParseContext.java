package net.thevpc.nuts.io;

public interface NAskParseContext<T> {
    Object response();

    NAsk<T> question();
}
