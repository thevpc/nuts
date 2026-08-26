/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <br>
 * <p>
 * Copyright [2020] [thevpc]
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License");
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
 */
package net.thevpc.nuts.io;

import net.thevpc.nuts.internal.rpi.NIORPI;
import net.thevpc.nuts.pipeline.NStream;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Path;

/**
 * I/O input stream base.
 *
 * @author thevpc
 * @app.category Input Output
 * @since 0.5.5
 */
public interface NInputSource extends NContentMetadataProvider, NInputContentProvider {

    /**
     * Creates a new instance of of.
     *
     * @param file file
     * @return of result
     */
    static NInputSource of(File file) {
        return file == null ? null : NPath.of(file);
    }

    /**
     * Creates a new instance of of.
     *
     * @param file file
     * @return of result
     */
    static NInputSource of(Path file) {
        return file == null ? null : NPath.of(file);
    }

    /**
     * Creates a new instance of of.
     *
     * @param file file
     * @return of result
     */
    static NInputSource of(URL file) {
        return file == null ? null : NPath.of(file);
    }

    /**
     * Creates a new instance of of.
     *
     * @param bytes bytes
     * @return of result
     */
    static NInputSource of(byte[] bytes) {
        return bytes == null ? null : NIORPI.of().createInputSource(bytes);
    }

    /**
     * input source from chars
     * @param chars chars
     * @return input source
     * @since 1.0.0
     */
    static NInputSource of(char[] chars) {
        return chars == null ? null : NIORPI.of().createInputSource(chars);
    }

    /**
     * input source from string value
     * @param stringValue string value
     * @return input source
     * @since 1.0.0
     */
    static NInputSource of(String stringValue) {
        return stringValue == null ? null : NIORPI.of().createInputSource(stringValue);
    }

    /**
     * Creates a new instance of of empty.
     *
     * @return of empty result
     */
    static NInputSource ofEmpty() {
        return NIORPI.of().createEmptyInputSource();
    }

    /**
     * Creates a new instance of of.
     *
     * @param inputSource input source
     * @return of result
     */
    static NInputSource of(InputStream inputSource) {
        return inputSource == null ? null : NIORPI.of().createInputSource(inputSource);
    }

    /**
     * Creates a new instance of of multi read.
     *
     * @param source source
     * @return of multi read result
     */
    static NInputSource ofMultiRead(NInputSource source) {
        return source == null ? null : NIORPI.of().createMultiRead(source);
    }

    /**
     * Creates a new instance of of.
     *
     * @param inputStream input stream
     * @param metadata metadata
     * @return of result
     */
    static NInputSource of(InputStream inputStream, NContentMetadata metadata) {
        return inputStream == null ? null : NIORPI.of().createInputSource(inputStream, metadata);
    }

    /**
     * Creates a new instance of of.
     *
     * @param reader reader
     * @param metadata metadata
     * @return of result
     */
    static NInputSource of(Reader reader, NContentMetadata metadata) {
        return reader == null ? null : NIORPI.of().createInputSource(reader, metadata);
    }

    /**
     * Creates a new instance of of.
     *
     * @param reader reader
     * @return of result
     */
    static NInputSource of(Reader reader) {
        return reader == null ? null : NIORPI.of().createInputSource(reader);
    }


    /**
     * Creates a new instance of of.
     *
     * @param bytes bytes
     * @param metadata metadata
     * @return of result
     */
    static NInputSource of(byte[] bytes, NContentMetadata metadata) {
        return bytes == null ? null : NIORPI.of().createInputSource(bytes, metadata);
    }

    /**
     * Creates a new instance of of.
     *
     * @param other other
     * @return of result
     */
    static NInputSource of(NInputStreamProvider other) {
        /**
         * Creates a new instance of of.
         *
         * @param other other
         * @param null null
         * @return of result
         */
        return of(other, null);
    }

    /**
     * Creates a new instance of of.
     *
     * @param other other
     * @param metadata metadata
     * @return of result
     */
    static NInputSource of(NInputStreamProvider other, NContentMetadata metadata) {
        return other == null ? null :
                (other instanceof NInputSource && metadata == null) ? (NInputSource) other :
                        NIORPI.of().createInputSource(other, metadata);
    }

    /**
     * Creates a new instance of of.
     *
     * @param other other
     * @return of result
     */
    static NInputSource of(NReaderProvider other) {
        /**
         * Creates a new instance of of.
         *
         * @param other other
         * @param null null
         * @return of result
         */
        return of(other, null);
    }

    /**
     * Creates a new instance of of.
     *
     * @param other other
     * @param metadata metadata
     * @return of result
     */
    static NInputSource of(NReaderProvider other, NContentMetadata metadata) {
        return other == null ? null :
                (other instanceof NInputSource && metadata == null) ? (NInputSource) other :
                        NIORPI.of().createInputSource(other, metadata);
    }

    /**
     * Read bytes.
     *
     * @return read bytes result
     */
    byte[] readBytes();

    /**
     * Read string.
     *
     * @return read string result
     */
    default String readString() {
        return new String(readBytes());
    }

    /**
     * Read string.
     *
     * @param cs cs
     * @return read string result
     */
    default String readString(Charset cs) {
        return cs == null ? new String(readBytes()) : new String(readBytes(), cs);
    }

    /**
     * Checks if is multi read.
     *
     * @return is multi read result
     */
    boolean isMultiRead();

    /**
     * Checks if is known content length.
     *
     * @return is known content length result
     */
    boolean isKnownContentLength();

    /**
     * Content length.
     *
     * @return content length result
     */
    long contentLength();

    /**
     * Lines.
     *
     * @param cs cs
     * @return lines result
     */
    NStream<String> lines(Charset cs);

    /**
     * Lines.
     *
     * @return lines result
     */
    NStream<String> lines();

    /**
     * Lines.
     *
     * @param from from
     * @param to to
     * @return lines result
     */
    NStream<String> lines(Long from, Long to);

    /**
     * Lines.
     *
     * @param from from
     * @param to to
     * @param cs cs
     * @return lines result
     */
    NStream<String> lines(Long from, Long to, Charset cs);

    /**
     * Reversed lines.
     *
     * @param cs cs
     * @return reversed lines result
     */
    NStream<String> reversedLines(Charset cs);

    /**
     * Reversed lines.
     *
     * @return reversed lines result
     */
    NStream<String> reversedLines();

    /**
     * As reader.
     *
     * @return as reader result
     */
    Reader asReader();

    /**
     * As reader.
     *
     * @param cs cs
     * @return as reader result
     */
    Reader asReader(Charset cs);


    /**
     * As buffered reader.
     *
     * @return as buffered reader result
     */
    BufferedReader asBufferedReader();

    /**
     * As buffered reader.
     *
     * @param cs cs
     * @return as buffered reader result
     */
    BufferedReader asBufferedReader(Charset cs);

    /**
     * Head.
     *
     * @param count count
     * @param cs cs
     * @return head result
     */
    NStream<String> head(long count, Charset cs);

    /**
     * Head.
     *
     * @param count count
     * @return head result
     */
    NStream<String> head(long count);

    /**
     * Tail.
     *
     * @param count count
     * @param cs cs
     * @return tail result
     */
    NStream<String> tail(long count, Charset cs);

    /**
     * Tail.
     *
     * @param count count
     * @return tail result
     */
    NStream<String> tail(long count);


    /**
     * Dispose.
     */
    default void dispose() {
    }

    /**
     * Digest.
     *
     * @return digest result
     */
    byte[] digest();

    /**
     * Returns the digest.
     *
     * @param algo algo
     * @return get digest result
     */
    byte[] getDigest(String algo);

    /**
     * Digest string.
     *
     * @return digest string result
     */
    String digestString();

    /**
     * Returns the digest string.
     *
     * @param algo algo
     * @return get digest string result
     */
    String getDigestString(String algo);
}
