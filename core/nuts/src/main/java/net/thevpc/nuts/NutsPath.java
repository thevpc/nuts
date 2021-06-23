package net.thevpc.nuts;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Path;

public interface NutsPath extends NutsFormattable {
    String name();

    String location();

    NutsPath compressedForm();

    String toString();

    URL toURL();

    Path toFilePath();

    URL asURL();

    Path asFilePath();

    InputStream inputStream();

    OutputStream outputStream();
}
