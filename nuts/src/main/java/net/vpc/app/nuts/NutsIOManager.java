package net.vpc.app.nuts;

import java.io.*;

public interface NutsIOManager extends NutsComponent<Object> {

    InputStream monitorInputStream(String path, String name, NutsSession session);

    InputStream monitorInputStream(InputStream stream, long length, String name, NutsSession session);

    InputStream monitorInputStream(String path, Object source, NutsSession session);

    void writeJson(Object obj, Writer out, boolean pretty);

    <T> T readJson(Reader reader, Class<T> cls);

    <T> T readJson(File file, Class<T> cls);

    <T> void writeJson(Object obj, File file, boolean pretty);

    <T> void writeJson(Object obj, PrintStream printStream, boolean pretty);

    String expandPath(String path);

    String expandPath(String path, String baseFolder);

    String getResourceString(String resource, Class cls, String defaultValue);

    String computeHash(InputStream input);

    InputStream createNullInputStream();

    PrintStream createNullPrintStream();

    PrintStream createPrintStream(File out);

    PrintStream createPrintStream(OutputStream out, NutsTerminalMode mode);

    NutsSessionTerminal createTerminal();

    NutsSessionTerminal createTerminal(NutsTerminalBase parent);

    void downloadPath(String from, File to, Object source, NutsSession session);

    File createTempFile(String name);

    File createTempFile(String name, NutsRepository repository);

    String getSHA1(NutsDescriptor descriptor);
}
