package net.vpc.app.nuts;

import java.io.File;
import java.io.PrintStream;
import java.io.Writer;
import java.nio.file.Path;
import java.util.Properties;

public interface NutsWorkspaceVersionFormat {

    NutsWorkspaceVersionFormat addProperty(String key, String value);

    NutsWorkspaceVersionFormat addProperties(Properties p);

    @Override
    String toString();

    String format();

//    String formatString();
    void print(PrintStream out);

    void print(Writer out);

    void print(Path out);

    void print(File out);

    void print();

    void print(NutsTerminal terminal);

    void println(Writer w);

    void println(PrintStream out);

    void println(Path path);

    void println();

    void println(NutsTerminal terminal);

    void println(File file);

    NutsWorkspaceVersionFormat setFormatType(NutsFormatType formatType);

    NutsFormatType getFormatType();

    NutsWorkspaceVersionFormat parseOptions(String[] args);
    
    NutsWorkspaceVersionFormat setMinimal(boolean minimal);

    boolean isMinimal();

    NutsWorkspaceVersionFormat setPretty(boolean pretty);

    boolean isPretty();
}
