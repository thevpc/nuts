package net.thevpc.nuts.elem;

import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.text.NContentType;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.spi.NComponent;
import net.thevpc.nuts.time.NProgressFactory;

import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.nio.file.Path;
import java.util.function.Consumer;

public interface NElementReader extends NComponent {
    static NElementReader of() {
        return NExtensions.of(NElementReader.class);
    }

    static NElementReader ofJson() {
        return of().json();
    }

    static NElementReader ofTson() {
        return of().tson();
    }

    static NElementReader ofYaml() {
        return of().yaml();
    }

    static NElementReader ofXml() {
        return of().xml();
    }


    boolean isNtf();

    NElementReader setNtf(boolean ntf);

    boolean isLogProgress();

    NElementReader setLogProgress(boolean logProgress);

    boolean isTraceProgress();

    NElementReader setTraceProgress(boolean traceProgress);

    /**
     * return parse content type
     *
     * @return content type
     * @since 0.8.1
     */
    NContentType getContentType();

    /**
     * set the parse content type. defaults to JSON. Non structured content
     * types are not allowed.
     *
     * @param contentType contentType
     * @return {@code this} instance
     * @since 0.8.1
     */
    NElementReader setContentType(NContentType contentType);

    NElementReader json();

    NElementReader yaml();

    NElementReader tson();

    NElementReader xml();


    NElementMapperStore mapperStore();

    NElementReader doWithMapperStore(Consumer<NElementMapperStore> doWith);

    /*
     * parse url as a valid object of the given type
     *
     * @param url   source url
     * @param clazz target type
     * @param <T>   target type
     * @return new instance of the given class
     */
    <T> T read(URL url, Class<T> clazz);

    /**
     * parse path as a valid object of the given type
     *
     * @param path  source path
     * @param clazz target type
     * @param <T>   target type
     * @return new instance of the given class
     */
    <T> T read(NPath path, Class<T> clazz);

    /**
     * parse inputStream as a valid object of the given type
     *
     * @param inputStream source inputStream
     * @param clazz       target type
     * @param <T>         target type
     * @return new instance of the given class
     */
    <T> T read(InputStream inputStream, Class<T> clazz);

    <T> T readWithSource(InputStream inputStream, Class<T> clazz, Object source);

    /**
     * parse inputStream as a valid object of the given type
     *
     * @param string source as json string
     * @param clazz  target type
     * @param <T>    target type
     * @return new instance of the given class
     */
    <T> T read(String string, Class<T> clazz);

    <T> T readWithSource(String string, Class<T> clazz, Object source);

    /**
     * parse bytes as a valid object of the given type
     *
     * @param bytes source bytes
     * @param clazz target type
     * @param <T>   target type
     * @return new instance of the given class
     */
    <T> T read(byte[] bytes, Class<T> clazz);

    <T> T readWithSource(byte[] bytes, Class<T> clazz, Object source);

    /**
     * parse reader as a valid object of the given type
     *
     * @param reader source reader
     * @param clazz  target type
     * @param <T>    target type
     * @return new instance of the given class
     */
    <T> T read(Reader reader, Class<T> clazz);

    <T> T readWithSource(Reader reader, Class<T> clazz, Object source);

    /**
     * parse file as a valid object of the given type
     *
     * @param file  source url
     * @param clazz target type
     * @param <T>   target type
     * @return new instance of the given class
     */
    <T> T read(Path file, Class<T> clazz);

    /**
     * parse file as a valid object of the given type
     *
     * @param file  source URL
     * @param clazz target type
     * @param <T>   target type
     * @return new instance of the given class
     */
    <T> T read(File file, Class<T> clazz);

    /**
     * parse url as a valid object of the given type
     *
     * @param url source url
     * @return element
     */
    NElement read(URL url);

    /**
     * parse inputStream as a valid object of the given type
     *
     * @param inputStream source inputStream
     * @return element
     */
    NElement read(InputStream inputStream);

    NElement readWithSource(InputStream inputStream, Object source);

    /**
     * parse string as a valid NutsElement.
     * If the string is null, NutsElement.NULL is returned
     *
     * @param string source as json string
     * @return element
     */
    NElement read(String string);

    /**
     * parse bytes as a valid object of the given type
     *
     * @param bytes source bytes
     * @return element
     */
    NElement read(byte[] bytes);

    /**
     * parse reader as a valid object of the given type
     *
     * @param reader source reader
     * @return element
     */
    NElement read(Reader reader);

    /**
     * parse file as a valid object of the given type
     *
     * @param file source url
     * @return element
     */
    NElement read(Path file);

    /**
     * parse file as a valid object of the given type
     *
     * @param file source URL
     * @return element
     */
    NElement read(File file);

    /**
     * parse file as a valid object of the given type
     *
     * @param file source URL
     * @return element
     */
    NElement read(NPath file);


    NElement readWithSource(String string, Object source);

    NElement readWithSource(byte[] bytes, Object source);

    NElement readWithSource(Reader reader, Object source);

    NProgressFactory getProgressFactory();

    NElementReader setProgressFactory(NProgressFactory progressFactory);
}
