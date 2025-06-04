package net.thevpc.nuts.elem;

import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.format.NContentType;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.spi.NComponent;
import net.thevpc.nuts.time.NProgressFactory;

import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.nio.file.Path;
import java.util.function.Predicate;

public interface NElementParser extends NComponent {
    static NElementParser of() {
        return NExtensions.of(NElementParser.class);
    }

    static NElementParser ofJson() {
        return of().json();
    }

    static NElementParser ofTson() {
        return of().tson();
    }

    static NElementParser ofYaml() {
        return of().yaml();
    }

    static NElementParser ofXml() {
        return of().xml();
    }


    boolean isNtf();

    NElementParser setNtf(boolean ntf);

    boolean isLogProgress();

    NElementParser setLogProgress(boolean logProgress);

    boolean isTraceProgress();

    NElementParser setTraceProgress(boolean traceProgress);

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
    NElementParser setContentType(NContentType contentType);

    NElementParser json();

    NElementParser yaml();

    NElementParser tson();

    NElementParser xml();

    NElementParser setIndestructibleFormat();

    NElementParser setIndestructibleObjects(Predicate<Class<?>> destructTypeFilter);

    NElementMapperStore mappers();

    /*
     * parse url as a valid object of the given type
     *
     * @param url   source url
     * @param clazz target type
     * @param <T>   target type
     * @return new instance of the given class
     */
    <T> T parse(URL url, Class<T> clazz);

    /**
     * parse path as a valid object of the given type
     *
     * @param path  source path
     * @param clazz target type
     * @param <T>   target type
     * @return new instance of the given class
     */
    <T> T parse(NPath path, Class<T> clazz);

    /**
     * parse inputStream as a valid object of the given type
     *
     * @param inputStream source inputStream
     * @param clazz       target type
     * @param <T>         target type
     * @return new instance of the given class
     */
    <T> T parse(InputStream inputStream, Class<T> clazz);

    /**
     * parse inputStream as a valid object of the given type
     *
     * @param string source as json string
     * @param clazz  target type
     * @param <T>    target type
     * @return new instance of the given class
     */
    <T> T parse(String string, Class<T> clazz);

    /**
     * parse bytes as a valid object of the given type
     *
     * @param bytes source bytes
     * @param clazz target type
     * @param <T>   target type
     * @return new instance of the given class
     */
    <T> T parse(byte[] bytes, Class<T> clazz);

    /**
     * parse reader as a valid object of the given type
     *
     * @param reader source reader
     * @param clazz  target type
     * @param <T>    target type
     * @return new instance of the given class
     */
    <T> T parse(Reader reader, Class<T> clazz);

    /**
     * parse file as a valid object of the given type
     *
     * @param file  source url
     * @param clazz target type
     * @param <T>   target type
     * @return new instance of the given class
     */
    <T> T parse(Path file, Class<T> clazz);

    /**
     * parse file as a valid object of the given type
     *
     * @param file  source URL
     * @param clazz target type
     * @param <T>   target type
     * @return new instance of the given class
     */
    <T> T parse(File file, Class<T> clazz);

    /**
     * parse url as a valid object of the given type
     *
     * @param url source url
     * @return element
     */
    NElement parse(URL url);

    /**
     * parse inputStream as a valid object of the given type
     *
     * @param inputStream source inputStream
     * @return element
     */
    NElement parse(InputStream inputStream);

    /**
     * parse string as a valid NutsElement.
     * If the string is null, NutsElement.NULL is returned
     *
     * @param string source as json string
     * @return element
     */
    NElement parse(String string);

    /**
     * parse bytes as a valid object of the given type
     *
     * @param bytes source bytes
     * @return element
     */
    NElement parse(byte[] bytes);

    /**
     * parse reader as a valid object of the given type
     *
     * @param reader source reader
     * @return element
     */
    NElement parse(Reader reader);

    /**
     * parse file as a valid object of the given type
     *
     * @param file source url
     * @return element
     */
    NElement parse(Path file);

    /**
     * parse file as a valid object of the given type
     *
     * @param file source URL
     * @return element
     */
    NElement parse(File file);

    /**
     * parse file as a valid object of the given type
     *
     * @param file source URL
     * @return element
     */
    NElement parse(NPath file);

    NProgressFactory getProgressFactory();

    NElementParser setProgressFactory(NProgressFactory progressFactory);
}
