package net.thevpc.nuts.util;

public interface NAskParseContext<T> {
    Object response();

    NAsk<T> question();
}
