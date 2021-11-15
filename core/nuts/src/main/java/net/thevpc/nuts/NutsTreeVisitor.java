package net.thevpc.nuts;

public interface NutsTreeVisitor<T> {
    NutsTreeVisitResult preVisitDirectory(T dir, NutsSession session);

    NutsTreeVisitResult visitFile(T file, NutsSession session);

    NutsTreeVisitResult visitFileFailed(T file, Exception exc, NutsSession session);

    NutsTreeVisitResult postVisitDirectory(T dir, Exception exc, NutsSession session);
}
