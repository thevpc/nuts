package net.thevpc.nuts.toolbox.nsh.bundles.jshell;

import java.io.*;
import java.util.List;

public interface JShellHistory {
    File getHistoryFile();

    JShellHistory setHistoryFile(File historyFile);

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

    void load(File file) throws IOException;

    void load(Reader reader) throws IOException;

    void save() throws IOException;

    void save(PrintWriter writer) throws IOException;

    void save(PrintStream writer) throws IOException;

    void save(File writer) throws IOException;

    void append(JShellHistory other);

    boolean isEmpty();

    String get(int index);

    String getLast();
}
