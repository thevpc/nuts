package net.vpc.app.nuts;

import java.io.*;
import java.nio.file.Path;

public interface NutsIOManager extends NutsComponent<Object> {

    InputStream monitorInputStream(String path, String name, NutsTerminalProvider session);

    InputStream monitorInputStream(InputStream stream, long length, String name, NutsTerminalProvider session);

    InputStream monitorInputStream(String path, Object source, NutsTerminalProvider session);

    String toJsonString(Object obj, boolean pretty);

    void writeJson(Object obj, Writer out, boolean pretty);

    <T> T readJson(Reader reader, Class<T> cls);

    <T> T readJson(Path file, Class<T> cls);

    <T> T readJson(File file, Class<T> cls);

    <T> void writeJson(Object obj, Path file, boolean pretty);

    <T> void writeJson(Object obj, File file, boolean pretty);

    <T> void writeJson(Object obj, PrintStream printStream, boolean pretty);

    String expandPath(Path path);

    String expandPath(String path);

    String expandPath(String path, String baseFolder);

    String getResourceString(String resource, Class cls, String defaultValue);

    String computeHash(InputStream input);

    InputStream createNullInputStream();

    PrintStream createNullPrintStream();

    PrintStream createPrintStream(Path out);

    PrintStream createPrintStream(File out);

    PrintStream createPrintStream(OutputStream out, NutsTerminalMode mode);

    NutsSessionTerminal createTerminal();

    NutsSessionTerminal createTerminal(NutsTerminalBase parent);

    Path createTempFile(String name);

    Path createTempFolder(String name);

    Path createTempFile(String name, NutsRepository repository);

    Path createTempFolder(String name, NutsRepository repository);

    String getSHA1(NutsDescriptor descriptor);

    NutsIOCopyAction copy();
    
    Path path(String first, String... more);
}
