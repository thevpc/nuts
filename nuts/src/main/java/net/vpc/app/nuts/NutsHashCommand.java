/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.security.MessageDigest;

/**
 * I/O command to hash contents.
 * @author vpc
 * @since 0.5.5
 */
public interface NutsHashCommand {

    /**
     * source stream to  hash
     * @param input source stream to  hash
     * @return {@code this} instance
     */
    NutsHashCommand source(InputStream input);

    /**
     * file to  hash
     * @param file source file to  hash
     * @return {@code this} instance
     */
    NutsHashCommand source(File file);

    /**
     * file to  hash
     * @param path source path to  hash
     * @return {@code this} instance
     */
    NutsHashCommand source(Path path);

    /**
     * source stream to  hash
     * @param descriptor source descriptor to  hash
     * @return {@code this} instance
     */
    NutsHashCommand source(NutsDescriptor descriptor);

    /**
     * compute hash digest and return it as hexadecimal string
     * @return hash digest
     */
    String computeString();

    /**
     * compute hash digest and return it as byte array
     * @return hash digest
     */
    byte[] computeBytes();

    /**
     * compute hash and writes it to the output stream
     * @param out output stream
     * @return {@code this} instance
     */
    NutsHashCommand writeHash(OutputStream out);

    /**
     * select MD5 hash algorithm
     * @return {@code this} instance
     */
    NutsHashCommand md5();

    /**
     * select MD5 hash algorithm
     * @return {@code this} instance
     */
    NutsHashCommand sha1();

    /**
     * select hash algorithm.
     * @param algorithm hash algorithm. may be any algorithm supported by
     *             {@link MessageDigest#getInstance(String)}
     *             including 'MD5' and 'SHA1'
     * @return {@code this} instance
     */
    NutsHashCommand setAlgorithm(String algorithm);

    /**
     * select hash algorithm.
     * @param algorithm hash algorithm. may be any algorithm supported by
     *             {@link MessageDigest#getInstance(String)}
     *             including 'MD5' and 'SHA1'
     * @return {@code this} instance
     */
    NutsHashCommand algorithm(String algorithm);

    /**
     *
     * @return selected algorithm. default is 'SHA1'
     */
    String getAlgorithm();
}
