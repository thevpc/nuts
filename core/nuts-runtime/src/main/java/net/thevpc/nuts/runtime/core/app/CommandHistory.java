package net.thevpc.nuts.runtime.core.app;

import java.io.*;

public interface CommandHistory {

    void load() throws IOException;

    void save() throws IOException;

    void append(String command) throws IOException;

    String[] list() throws IOException;

    String[] tail(int n) throws IOException;

    String[] head(int n) throws IOException;
}
