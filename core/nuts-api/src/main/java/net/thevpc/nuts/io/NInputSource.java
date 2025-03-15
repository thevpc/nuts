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

import net.thevpc.nuts.reserved.rpi.NIORPI;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

/**
 * I/O input stream base.
 *
 * @author thevpc
 * @app.category Input Output
 * @since 0.5.5
 */
public interface NInputSource extends NContentMetadataProvider, NInputContentProvider {

    static NInputSource of(File file) {
        return file == null ? null : NPath.of(file);
    }

    static NInputSource of(Path file) {
        return file == null ? null : NPath.of(file);
    }

    static NInputSource of(URL file) {
        return file == null ? null : NPath.of(file);
    }

    static NInputSource of(byte[] bytes) {
        return bytes == null ? null : NIORPI.of().ofInputSource(bytes);
    }

    static NInputSource of(InputStream inputSource) {
        return inputSource == null ? null : NIORPI.of().ofInputSource(inputSource);
    }

    static NInputSource ofMultiRead(NInputSource source) {
        return source == null ? null : NIORPI.of().ofMultiRead(source);
    }

    static NInputSource of(InputStream inputStream, NContentMetadata metadata) {
        return inputStream == null ? null : NIORPI.of().ofInputSource(inputStream, metadata);
    }

    static NInputSource of(Reader reader, NContentMetadata metadata) {
        return reader == null ? null : NIORPI.of().ofInputSource(reader, metadata);
    }

    static NInputSource of(Reader reader) {
        return reader == null ? null : NIORPI.of().ofInputSource(reader);
    }


    static NInputSource of(byte[] bytes, NContentMetadata metadata) {
        return bytes == null ? null : NIORPI.of().ofInputSource(bytes, metadata);
    }

    static NInputSource of(NInputStreamProvider other) {
        return of(other, null);
    }

    static NInputSource of(NInputStreamProvider other, NContentMetadata metadata) {
        return other == null ? null :
                (other instanceof NInputSource && metadata == null) ? null :
                        NIORPI.of().ofInputSource(other, metadata);
    }

    byte[] readBytes();

    default String readString() {
        return new String(readBytes());
    }

    default String readString(Charset cs) {
        return cs == null ? new String(readBytes()) : new String(readBytes(), cs);
    }

    boolean isMultiRead();

    boolean isKnownContentLength();

    long contentLength();

    Stream<String> getLines(Charset cs);

    Stream<String> getLines();

    Reader getReader();

    Reader getReader(Charset cs);


    BufferedReader getBufferedReader();

    BufferedReader getBufferedReader(Charset cs);

    List<String> head(int count, Charset cs);

    List<String> head(int count);

    List<String> tail(int count, Charset cs);

    List<String> tail(int count);


    default void dispose() {
    }

    byte[] getDigest();

    byte[] getDigest(String algo);

    String getDigestString();

    String getDigestString(String algo);
}
