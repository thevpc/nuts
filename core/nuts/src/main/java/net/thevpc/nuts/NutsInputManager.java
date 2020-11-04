package net.thevpc.nuts;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;

public interface NutsInputManager {
    NutsInput of(Object any);

    NutsInput of(String resource);

    NutsInput of(byte[] bytes);

    NutsInput of(InputStream stream);

    NutsInput of(URL stream);

    NutsInput of(File stream);

    NutsInput of(Path stream);

    NutsInput of(NutsInput stream);

    String getName();

    String getTypeName();

    NutsInputManager setName(String name);

    NutsInputManager setTypeName(String typeName);

    NutsInputManager setMultiRead(boolean value);

    boolean isMultiRead();
}
