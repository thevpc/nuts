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
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain a
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
 */
package net.thevpc.nuts;

import net.thevpc.nuts.boot.NutsApiUtils;
import net.thevpc.nuts.spi.NutsComponent;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.security.MessageDigest;

/**
 * I/O command to hash contents.
 *
 * @author thevpc
 * @app.category Input Output
 * @since 0.5.5
 */
public interface NutsHash extends NutsComponent<Object> {
    static NutsHash of(NutsSession session) {
        NutsApiUtils.checkSession(session);
        return session.extensions().createSupported(NutsHash.class, true, null);
    }

    /**
     * source stream to  hash
     *
     * @param input source stream to  hash
     * @return {@code this} instance
     */
    NutsHash setSource(InputStream input);

    /**
     * file to  hash
     *
     * @param file source file to  hash
     * @return {@code this} instance
     */
    NutsHash setSource(File file);

    /**
     * file to  hash
     *
     * @param path source path to  hash
     * @return {@code this} instance
     */
    NutsHash setSource(Path path);

    /**
     * file to  hash
     *
     * @param path source path to  hash
     * @return {@code this} instance
     */
    NutsHash setSource(NutsPath path);

    /**
     * file to  hash
     *
     * @param path source path to  hash
     * @return {@code this} instance
     * @since 0.8.3
     */
    NutsHash setSource(byte[] path);

    /**
     * source stream to  hash
     *
     * @param descriptor source descriptor to  hash
     * @return {@code this} instance
     */
    NutsHash setSource(NutsDescriptor descriptor);

    /**
     * compute hash digest and return it as hexadecimal string
     *
     * @return hash digest
     */
    String computeString();

    /**
     * compute hash digest and return it as byte array
     *
     * @return hash digest
     */
    byte[] computeBytes();

    /**
     * compute hash and writes it to the output stream
     *
     * @param out output stream
     * @return {@code this} instance
     */
    NutsHash writeHash(OutputStream out);

    /**
     * select MD5 hash algorithm
     *
     * @return {@code this} instance
     */
    NutsHash md5();

    NutsSession getSession();

    NutsHash setSession(NutsSession session);

    /**
     * select SHA1 hash algorithm
     *
     * @return {@code this} instance
     */
    NutsHash sha1();

    /**
     * select SHA256 hash algorithm
     *
     * @return {@code this} instance
     */
    NutsHash sha256();

    /**
     * select hash algorithm.
     *
     * @param algorithm hash algorithm. may be any algorithm supported by
     *                  {@link MessageDigest#getInstance(String)}
     *                  including 'MD5' and 'SHA1'
     * @return {@code this} instance
     */
    NutsHash algorithm(String algorithm);

    /**
     * @return selected algorithm. default is 'SHA1'
     */
    String getAlgorithm();

    /**
     * select hash algorithm.
     *
     * @param algorithm hash algorithm. may be any algorithm supported by
     *                  {@link MessageDigest#getInstance(String)}
     *                  including 'MD5' and 'SHA1'
     * @return {@code this} instance
     */
    NutsHash setAlgorithm(String algorithm);
}
