package net.thevpc.nuts.runtime.standalone.tson;

import java.io.*;
import java.nio.file.Path;

public interface TsonWriter {
    TsonWriter setOptionCompact(boolean configValue);

    TsonWriter setOption(String configName, Object configValue);

    void write(Appendable sb, Object any);

    void write(PrintStream sb, Object any);

    void write(Path file, Object any);

    void write(File file, Object any);

    void write(OutputStream stream, Object any);

    void write(OutputStream stream, String encoding, Object any);

    void write(Writer writer, Object any);

    void writeDocument(Appendable sb, Object any);

    void writeDocument(Path file, Object any);

    void writeDocument(File file, Object any);

    void writeDocument(OutputStream stream, Object any);

    void writeDocument(OutputStream stream, String encoding, Object any);

    void writeDocument(Writer writer, Object any);

    void write(PrintStream sb, TsonElement any);

    void write(Appendable sb, TsonElement any);

    void write(Path file, TsonElement any);

    void write(File file, TsonElement any);

    void write(OutputStream stream, TsonElement any);

    void write(OutputStream stream, String encoding, TsonElement any);

    void write(Writer writer, TsonElement any) ;

    void writeDocument(Appendable sb, TsonDocument any) ;

    void writeDocument(Path file, TsonDocument any) ;

    void writeDocument(File file, TsonDocument any) ;

    void writeDocument(OutputStream stream, TsonDocument any) ;

    void writeDocument(OutputStream stream, String encoding, TsonDocument any) ;

    void writeDocument(Writer writer, TsonDocument any) ;
}
