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

import net.thevpc.nuts.NSession;
import net.thevpc.nuts.format.NFormattable;
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
public interface NInputSource extends NFormattable, NContentMetadataProvider, NInputContentProvider {

    static NInputSource of(File file, NSession session) {
        return file == null ? null : NPath.of(file, session);
    }

    static NInputSource of(Path file, NSession session) {
        return file == null ? null :NPath.of(file, session);
    }

    static NInputSource of(URL file, NSession session) {
        return file == null ? null :NPath.of(file, session);
    }

    static NInputSource of(byte[] bytes, NSession session) {
        return bytes == null ? null : NIORPI.of(session).ofInputSource(bytes);
    }

    static NInputSource of(InputStream inputSource, NSession session) {
        return inputSource == null ? null : NIORPI.of(session).ofInputSource(inputSource);
    }

    static NInputSource ofMultiRead(NInputSource source,NSession session) {
        return source==null?null: NIORPI.of(session).ofMultiRead(source);
    }

    static NInputSource of(InputStream inputStream, NContentMetadata metadata,NSession session){
        return inputStream==null?null: NIORPI.of(session).ofInputSource(inputStream,metadata);
    }


    static NInputSource of(byte[] bytes, NContentMetadata metadata,NSession session){
        return bytes==null?null: NIORPI.of(session).ofInputSource(bytes,metadata);
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

    long getContentLength();

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
