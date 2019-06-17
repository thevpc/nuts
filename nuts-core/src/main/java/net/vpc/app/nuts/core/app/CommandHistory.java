package net.vpc.app.nuts.core.app;

import java.io.*;
import java.util.LinkedHashSet;
import java.util.Map;

public interface CommandHistory {

    void load() throws IOException;

    void save() throws IOException;

    void append(String command) throws IOException;

    String[] list() throws IOException;

    String[] tail(int n) throws IOException;

    String[] head(int n) throws IOException;
}
