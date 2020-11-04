package net.thevpc.nuts;

import java.io.File;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Path;

public interface NutsOutputManager {
    NutsOutput of(Object any);

    NutsOutput of(String resource);

    NutsOutput of(OutputStream stream);

    NutsOutput of(URL stream);

    NutsOutput of(File stream);

    NutsOutput of(Path stream);

    String getName();

    String getTypeName();

    NutsOutputManager setName(String name);

    NutsOutputManager setTypeName(String typeName);
}
