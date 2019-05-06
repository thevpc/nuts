/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts;

import java.io.File;
import java.io.PrintStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Path;

/**
 *
 * @author vpc
 */
public interface NutsJsonCommand {

    boolean isPretty();

    NutsJsonCommand pretty();

    NutsJsonCommand pretty(boolean pretty);

    NutsJsonCommand setPretty(boolean pretty);

    String toJsonString(Object obj);

    <T> T read(Reader reader, Class<T> cls);

    <T> T read(Path file, Class<T> cls);

    <T> T read(File file, Class<T> cls);

    void write(Object obj, Writer out);

    <T> void write(Object obj, Path file);

    <T> void write(Object obj, File file);

    <T> void write(Object obj, PrintStream printStream);

}
