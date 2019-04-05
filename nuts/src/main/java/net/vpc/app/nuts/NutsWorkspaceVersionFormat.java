package net.vpc.app.nuts;

import java.io.File;
import java.io.PrintStream;
import java.io.Writer;
import java.nio.file.Path;
import java.util.Properties;

public interface NutsWorkspaceVersionFormat {

    NutsWorkspaceVersionFormat addProperty(String key, String value);

    NutsWorkspaceVersionFormat addProperties(Properties p);

    NutsWorkspaceVersionFormat addOption(String o);

    NutsWorkspaceVersionFormat addOptions(String... o);

    @Override
    String toString();
    
//    String formatString();

    void format(PrintStream out);

    void format(Writer out);

    void format(Path out);

    void format(File out);

    void format();

    void format(NutsTerminal terminal);
}
