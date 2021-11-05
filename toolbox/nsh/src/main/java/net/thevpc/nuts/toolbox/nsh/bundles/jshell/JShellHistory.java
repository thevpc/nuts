package net.thevpc.nuts.toolbox.nsh.bundles.jshell;

import net.thevpc.nuts.NutsPath;

import java.io.*;
import java.util.List;

public interface JShellHistory {
    NutsPath getHistoryFile();

    JShellHistory setHistoryFile(NutsPath historyFile);

    void add(String e);

    void removeDuplicates();

    List<String> getElements();

    List<String> getElements(int maxElements);

    //    void save();
//
//    void load();
//
    int size();

    void clear();

    void remove(int index);

    void load() throws IOException;

    void load(NutsPath reader) throws IOException;

    void load(Reader reader) throws IOException;

    void save() throws IOException;

    void save(NutsPath writer) throws IOException;

    void save(PrintWriter writer) throws IOException;

    void save(PrintStream writer) throws IOException;

    void append(JShellHistory other);

    boolean isEmpty();

    String get(int index);

    String getLast();
}
