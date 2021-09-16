package net.thevpc.nuts;

import net.thevpc.nuts.boot.NutsApiUtils;

import java.util.stream.Stream;

/**
 * @app.category Format
 */
public interface NutsUtilManager {
    static NutsUtilManager of(NutsSession session) {
        NutsApiUtils.checkSession(session);
        return session.getWorkspace().util();
    }

    NutsSession getSession();

    NutsUtilManager setSession(NutsSession session);

    NutsVal valOf(Object str);

    <T> NutsStream<T> streamOf(T[] str, String name);

    <T> NutsStream<T> streamOf(Iterable<T> str, String name);

    <T> NutsStream<T> streamOf(Stream<T> str, String name);

}
