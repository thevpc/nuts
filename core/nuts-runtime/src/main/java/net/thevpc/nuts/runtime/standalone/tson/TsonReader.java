package net.thevpc.nuts.runtime.standalone.tson;

import java.io.*;
import java.net.URL;
import java.nio.file.Path;

public interface TsonReader {
    TsonReader setOption(String name, Object value);

    boolean isSkipHeader();

    TsonReader setSkipHeader(boolean skipHeader);

    boolean isSkipComments();

    TsonReader setSkipComments(boolean skipComments);


    <T> T read(TsonElement  tson, Class<? extends T> clazz);

    <T> T read(InputStream stream, Class<? extends T> clazz) ;

    <T> T read(CharSequence string, Class<? extends T> clazz);

    <T> T read(InputStream stream, String encoding, Class<? extends T> clazz) ;

    <T> T read(Reader reader, Class<? extends T> clazz) ;

    <T> T read(File file, Class<? extends T> clazz) ;

    <T> T read(Path file, Class<? extends T> clazz) ;

    <T> T read(URL url, Class<? extends T> clazz) ;

    TsonElement readElement(InputStream stream) ;

    TsonElement readElement(CharSequence string);

    TsonElement readElement(InputStream stream, String encoding) ;

    TsonElement readElement(Reader reader) ;

    TsonElement readElement(File file) ;

    TsonElement readElement(Path file) ;

    TsonElement readElement(URL url) ;

    TsonDocument readDocument(InputStream stream) ;

    TsonDocument readDocument(CharSequence string);

    TsonDocument readDocument(InputStream stream, String encoding) ;

    TsonDocument readDocument(Reader reader) ;

    TsonDocument readDocument(File file) ;

    TsonDocument readDocument(Path file) ;

    TsonDocument readDocument(URL url) ;

    void visitElement(InputStream stream, TsonParserVisitor visitor) ;

    void visitElement(CharSequence string, TsonParserVisitor visitor);

    void visitElement(InputStream stream, String encoding, TsonParserVisitor visitor) ;

    void visitElement(Reader reader, TsonParserVisitor visitor) ;

    void visitElement(File file, TsonParserVisitor visitor) ;

    void visitElement(Path file, TsonParserVisitor visitor) ;

    void visitElement(URL url, TsonParserVisitor visitor) ;

    void visitDocument(InputStream stream, TsonParserVisitor visitor) ;

    void visitDocument(CharSequence string, TsonParserVisitor visitor);

    void visitDocument(InputStream stream, String encoding, TsonParserVisitor visitor) ;

    void visitDocument(Reader reader, TsonParserVisitor visitor) ;

    void visitDocument(File file, TsonParserVisitor visitor) ;

    void visitDocument(Path file, TsonParserVisitor visitor) ;

    void visitDocument(URL url, TsonParserVisitor visitor) ;

    void visitElement(String tsonString,TsonParserVisitor visitor);

    void visitDocument(String tsonString,TsonParserVisitor visitor);
}
