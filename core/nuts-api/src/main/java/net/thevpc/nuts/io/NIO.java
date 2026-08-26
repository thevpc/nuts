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
 *
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

import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.spi.NComponent;
import net.thevpc.nuts.spi.NPathFactorySPI;
import net.thevpc.nuts.spi.base.NSystemTerminalBase;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;

/**
 * NIO interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NIO extends NComponent {
    /**
     * Creates a new instance of of.
     *
     * @return of result
     */
    static NIO of() {
        return NExtensions.of(NIO.class);
    }

    /**
     * Creates a new instance of of null raw input stream.
     *
     * @return of null raw input stream result
     */
    static InputStream ofNullRawInputStream() {
        return NullInputStream.INSTANCE;
    }

    /**
     * Creates a new instance of of null raw output stream.
     *
     * @return of null raw output stream result
     */
    static OutputStream ofNullRawOutputStream() {
        return NullOutputStream.INSTANCE;
    }

    /**
     * Checks if is stdin.
     *
     * @param in in
     * @return is stdin result
     */
    boolean isStdin(InputStream in);

    /**
     * Stdin.
     *
     * @return stdin result
     */
    InputStream stdin();


    /**
     *
     * @since 0.8.9
     */
    OutputStream unwrapOutputStream(OutputStream out);

    /**
     *
     * @since 0.8.9
     */
    InputStream unwrapInputStream(InputStream in);

    /**
     *
     * @since 0.8.9
     */
    boolean isStdout(OutputStream out);

    /**
     * Checks if is stdout.
     *
     * @param out out
     * @return is stdout result
     */
    boolean isStdout(NPrintStream out);

    /**
     *
     * @since 0.8.9
     */
    boolean isStderr(OutputStream out);

    /**
     * Checks if is stderr.
     *
     * @param out out
     * @return is stderr result
     */
    boolean isStderr(NPrintStream out);

    /**
     * Stdout.
     *
     * @return stdout result
     */
    NPrintStream stdout();

    /**
     * Stderr.
     *
     * @return stderr result
     */
    NPrintStream stderr();

    /**
     * return workspace system terminal.
     *
     * @return workspace system terminal
     */
    NSystemTerminal systemTerminal();

    /**
     * update workspace wide system terminal
     *
     * @param terminal system terminal
     * @return {@code this} instance
     */
    NIO systemTerminal(NSystemTerminalBase terminal);

    /**
     * return workspace default terminal
     *
     * @return workspace default terminal
     */
    NTerminal defaultTerminal();

    /**
     * update workspace wide terminal
     *
     * @param terminal terminal
     * @return {@code this} instance
     */
    NIO defaultTerminal(NTerminal terminal);

    /**
     * Adds the specified path factory.
     *
     * @param pathFactory path factory
     * @return add path factory result
     */
    NIO addPathFactory(NPathFactorySPI pathFactory);

    /**
     * Removes the specified path factory.
     *
     * @param pathFactory path factory
     * @return remove path factory result
     */
    NIO removePathFactory(NPathFactorySPI pathFactory);

    /**
     * Probe content type.
     *
     * @param path path
     * @return probe content type result
     */
    String probeContentType(URL path);

    /**
     * Probe content type.
     *
     * @param path path
     * @return probe content type result
     */
    String probeContentType(File path);

    /**
     * Probe content type.
     *
     * @param path path
     * @return probe content type result
     */
    String probeContentType(Path path);

    /**
     * Probe content type.
     *
     * @param path path
     * @return probe content type result
     */
    String probeContentType(NPath path);

    /**
     * Probe content type.
     *
     * @param stream stream
     * @return probe content type result
     */
    String probeContentType(InputStream stream);

    /**
     * Probe content type.
     *
     * @param stream stream
     * @return probe content type result
     */
    String probeContentType(byte[] stream);

    /// /////
    /**
     * Probe charset.
     *
     * @param path path
     * @return probe charset result
     */
    String probeCharset(URL path);

    /**
     * Probe charset.
     *
     * @param path path
     * @return probe charset result
     */
    String probeCharset(File path);

    /**
     * Probe charset.
     *
     * @param path path
     * @return probe charset result
     */
    String probeCharset(Path path);

    /**
     * Probe charset.
     *
     * @param path path
     * @return probe charset result
     */
    String probeCharset(NPath path);

    /**
     * Probe charset.
     *
     * @param stream stream
     * @return probe charset result
     */
    String probeCharset(InputStream stream);

    /**
     * Probe charset.
     *
     * @param stream stream
     * @return probe charset result
     */
    String probeCharset(byte[] stream);

    /**
     * Finds the find extensions by content type.
     *
     * @param contentType content type
     * @return find extensions by content type result
     */
    List<String> findExtensionsByContentType(String contentType);

    /**
     * Finds the find content types by extension.
     *
     * @param extension extension
     * @return find content types by extension result
     */
    List<String> findContentTypesByExtension(String extension);

    /**
     * returns a memory or file (depending on size) output stream
     *
     * @return a memory or file (depending on size) output stream
     */
    NTempOutputStream ofTempOutputStream();
}
