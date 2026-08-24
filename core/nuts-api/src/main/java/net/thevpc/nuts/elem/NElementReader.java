package net.thevpc.nuts.elem;

import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.text.NContentType;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.spi.NComponent;
import net.thevpc.nuts.mon.NProgressFactory;

import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.nio.file.Path;
import java.util.function.Consumer;

/**
 * NElementReader interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NElementReader extends NComponent {
    /**
     * Creates a new instance of of.
     *
     * @return of result
     */
    static NElementReader of() {
        return NExtensions.of(NElementReader.class);
    }

    /**
     * Creates a new instance of of json.
     *
     * @return of json result
     */
    static NElementReader ofJson() {
        /**
         * Creates a new instance of of.
         *
         * @param ).json( ).json(
         * @return of result
         */
        return of().json();
    }

    /**
     * Creates a new instance of of tson.
     *
     * @return of tson result
     */
    static NElementReader ofTson() {
        /**
         * Creates a new instance of of.
         *
         * @param ).tson( ).tson(
         * @return of result
         */
        return of().tson();
    }

    /**
     * Creates a new instance of of yaml.
     *
     * @return of yaml result
     */
    static NElementReader ofYaml() {
        /**
         * Creates a new instance of of.
         *
         * @param ).yaml( ).yaml(
         * @return of result
         */
        return of().yaml();
    }

    /**
     * Creates a new instance of of xml.
     *
     * @return of xml result
     */
    static NElementReader ofXml() {
        /**
         * Creates a new instance of of.
         *
         * @param ).xml( ).xml(
         * @return of result
         */
        return of().xml();
    }


    /**
     * Checks if is ntf.
     *
     * @return is ntf result
     */
    boolean isNtf();

    /**
     * Ntf.
     *
     * @param ntf ntf
     * @return ntf result
     */
    NElementReader ntf(boolean ntf);

    /**
     * Checks if is log progress.
     *
     * @return is log progress result
     */
    boolean isLogProgress();

    /**
     * Log progress.
     *
     * @param logProgress log progress
     * @return log progress result
     */
    NElementReader logProgress(boolean logProgress);

    /**
     * Checks if is trace progress.
     *
     * @return is trace progress result
     */
    boolean isTraceProgress();

    /**
     * Trace progress.
     *
     * @param traceProgress trace progress
     * @return trace progress result
     */
    NElementReader traceProgress(boolean traceProgress);

    /**
     * return parse content type
     *
     * @return content type
     * @since 0.8.1
     */
    NContentType contentType();

    /**
     * set the parse content type. defaults to JSON. Non structured content
     * types are not allowed.
     *
     * @param contentType contentType
     * @return {@code this} instance
     * @since 0.8.1
     */
    NElementReader contentType(NContentType contentType);

    /**
     * Json.
     *
     * @return json result
     */
    NElementReader json();

    /**
     * Yaml.
     *
     * @return yaml result
     */
    NElementReader yaml();

    /**
     * Tson.
     *
     * @return tson result
     */
    NElementReader tson();

    /**
     * Xml.
     *
     * @return xml result
     */
    NElementReader xml();


    /**
     * Mapper store.
     *
     * @return mapper store result
     */
    NElementMapperStore mapperStore();

    /**
     * Do with mapper store.
     *
     * @param doWith do with
     * @return do with mapper store result
     */
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

    /**
     * Read with source.
     *
     * @param inputStream input stream
     * @param clazz clazz
     * @param source source
     * @return read with source result
     */
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

    /**
     * Read with source.
     *
     * @param string string
     * @param clazz clazz
     * @param source source
     * @return read with source result
     */
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

    /**
     * Read with source.
     *
     * @param bytes bytes
     * @param clazz clazz
     * @param source source
     * @return read with source result
     */
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

    /**
     * Read with source.
     *
     * @param reader reader
     * @param clazz clazz
     * @param source source
     * @return read with source result
     */
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

    /**
     * Read with source.
     *
     * @param inputStream input stream
     * @param source source
     * @return read with source result
     */
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


    /**
     * Read with source.
     *
     * @param string string
     * @param source source
     * @return read with source result
     */
    NElement readWithSource(String string, Object source);

    /**
     * Read with source.
     *
     * @param bytes bytes
     * @param source source
     * @return read with source result
     */
    NElement readWithSource(byte[] bytes, Object source);

    /**
     * Read with source.
     *
     * @param reader reader
     * @param source source
     * @return read with source result
     */
    NElement readWithSource(Reader reader, Object source);

    /**
     * Progress factory.
     *
     * @return progress factory result
     */
    NProgressFactory progressFactory();

    /**
     * Progress factory.
     *
     * @param progressFactory progress factory
     * @return progress factory result
     */
    NElementReader progressFactory(NProgressFactory progressFactory);
}
