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

import net.thevpc.nuts.NId;
import net.thevpc.nuts.NRepository;
import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.spi.NComponent;
import net.thevpc.nuts.spi.NPathFactorySPI;
import net.thevpc.nuts.spi.NPathSPI;
import net.thevpc.nuts.spi.NSystemTerminalBase;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;

public interface NIO extends NComponent {
    static NIO of() {
        return NExtensions.of(NIO.class);
    }
//    static NPrintStream out(){return NSession.get().out();}
//
//    static InputStream in(){return NSession.get().in();}
//
//    /**
//     * current error stream
//     *
//     * @return current error stream
//     */
//    static NPrintStream err(){return NSession.get().err();}
//
//    static NPrintStream println(NMsg b){
//        return out().println(b);
//    }
//
//    static NPrintStream println(NText b){
//        return out().println(b);
//    }
//
//    static NPrintStream println(String b){
//        return out().println(b);
//    }
//
//    static NPrintStream println(Object b){
//        return out().println(b);
//    }

    static InputStream ofNullRawInputStream(){
        return NullInputStream.INSTANCE;
    }

    static OutputStream ofNullRawOutputStream(){
        return NullOutputStream.INSTANCE;
    }

    boolean isStdin(InputStream in);

    InputStream stdin();


    boolean isStdout(NPrintStream out);

    boolean isStderr(NPrintStream out);

    NPrintStream stdout();

    NPrintStream stderr();

    /**
     * return workspace system terminal.
     *
     * @return workspace system terminal
     */
    NSystemTerminal getSystemTerminal();

    /**
     * update workspace wide system terminal
     *
     * @param terminal system terminal
     * @return {@code this} instance
     */
    NIO setSystemTerminal(NSystemTerminalBase terminal);

    /**
     * return workspace default terminal
     *
     * @return workspace default terminal
     */
    NTerminal getDefaultTerminal();

    /**
     * update workspace wide terminal
     *
     * @param terminal terminal
     * @return {@code this} instance
     */
    NIO setDefaultTerminal(NTerminal terminal);

    /**
     * expand path to Workspace Location
     *
     * @param path path to expand
     * @return expanded path
     */
    NPath createPath(String path);

    NPath createPath(File path);

    NPath createPath(Path path);

    NPath createPath(URL path);

    NPath createPath(String path, ClassLoader classLoader);

    NPath createPath(NPathSPI path);

    NIO addPathFactory(NPathFactorySPI pathFactory);

    NIO removePathFactory(NPathFactorySPI pathFactory);

    /**
     * create temp file in the repositoryId's temp folder
     *
     * @param name file name
     * @return newly created file path
     */
    NPath ofTempFile(String name);

    /**
     * create temp file in the repositoryId's temp folder
     *
     * @return newly created file path
     */
    NPath ofTempFile();

    /**
     * create temp folder in the workspace's temp folder
     *
     * @param name folder name
     * @return newly created temp folder
     */
    NPath ofTempFolder(String name);

    /**
     * create temp folder in the workspace's temp folder
     *
     * @return newly created temp folder
     */
    NPath ofTempFolder();

    /**
     * create temp file in the repositoryId's temp folder
     *
     * @param name file name
     * @return newly created file path
     */
    NPath ofTempRepositoryFile(String name, NRepository repository);

    /**
     * create temp file in the repositoryId's temp folder
     *
     * @return newly created file path
     */
    NPath ofTempRepositoryFile(NRepository repository);

    /**
     * create temp folder in the repository's temp folder
     *
     * @param name folder name
     * @return newly created temp folder
     */
    NPath ofTempRepositoryFolder(String name, NRepository repository);

    /**
     * create temp folder in the repository's temp folder
     *
     * @return newly created temp folder
     */
    NPath ofTempRepositoryFolder(NRepository repository);


    String probeContentType(URL path);

    String probeContentType(File path);

    NPath ofTempIdFile(String name, NId repository);

    NPath ofTempIdFolder(String name, NId repository);

    NPath ofTempIdFile(NId repository);

    NPath ofTempIdFolder(NId repository);

    String probeContentType(Path path);

    String probeContentType(NPath path);

    String probeContentType(InputStream stream);

    String probeContentType(byte[] stream);

    ////////
    String probeCharset(URL path);

    String probeCharset(File path);

    String probeCharset(Path path);

    String probeCharset(NPath path);

    String probeCharset(InputStream stream);

    String probeCharset(byte[] stream);

    List<String> findExtensionsByContentType(String contentType);
    List<String> findContentTypesByExtension(String extension);
}
