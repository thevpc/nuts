package net.vpc.app.nuts;

import java.io.File;
import java.nio.file.Path;

public interface NutsDeleteAction {
    Object getTarget();

    NutsDeleteAction setTarget(Object target);

    NutsDeleteAction setTarget(File target);

    NutsDeleteAction setTarget(Path target);

    NutsDeleteAction target(Object target);

    NutsSession getSession();

    NutsDeleteAction session(NutsSession session);

    NutsDeleteAction setSession(NutsSession session);

    NutsDeleteAction run();

    boolean isFailFast();

    NutsDeleteAction setFailFast(boolean failFast);

    NutsDeleteAction failFast(boolean failFast);
}
