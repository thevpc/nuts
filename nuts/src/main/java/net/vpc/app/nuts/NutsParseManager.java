package net.vpc.app.nuts;

import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URL;

public interface NutsParseManager {
    NutsId parseId(String id);

    NutsDescriptor parseDescriptor(URL url);

    NutsDescriptor parseDescriptor(byte[] bytes);

    NutsDescriptor parseDescriptor(File file);

    NutsDescriptor parseDescriptor(InputStream stream);

    NutsDescriptor parseDescriptor(InputStream stream,boolean close);

    NutsDescriptor parseDescriptor(String descriptorString);

    NutsDependency parseDependency(String dependency);

    NutsVersion parseVersion(String version);

    NutsId parseRequiredId(String nutFormat);

    /**
     * parseExecutionEntries
     *
     * @param file jar file
     * @return execution entries (class names with main method)
     */
    NutsExecutionEntry[] parseExecutionEntries(File file);

    NutsExecutionEntry[] parseExecutionEntries(InputStream inputStream, String type);

    /**
     * this method removes all  {@link NutsFormattedPrintStream}'s special formatting sequences and returns the raw
     * string to be printed on an ordinary {@link PrintStream}
     *
     * @param value input string
     * @return string without any escape sequences so that the text printed correctly on any non formatted {@link PrintStream}
     */
    String filterText(String value);

    /**
     * This method escapes all special characters that are interpreted by {@link NutsFormattedPrintStream} so that
     * this exact string is printed on such print streams
     * When str is null, an empty string is return
     *
     * @param value input string
     * @return string with escaped characters so that the text printed correctly on {@link NutsFormattedPrintStream}
     */
    String escapeText(String value);
}
