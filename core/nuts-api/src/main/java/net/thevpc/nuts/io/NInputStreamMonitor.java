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

import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.spi.NComponent;
import net.thevpc.nuts.time.NProgressFactory;
import net.thevpc.nuts.time.NProgressListener;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NGetter;
import net.thevpc.nuts.util.NSetter;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;

/**
 * Monitor action enables monitoring a long lasting operation such as copying a big file.
 *
 * @author thevpc
 * @app.category Toolkit
 */
public interface NInputStreamMonitor extends NComponent {
    static NInputStreamMonitor of() {
        return NExtensions.of(NInputStreamMonitor.class);
    }


    /**
     * return action name
     *
     * @return action name
     */
    @NGetter
    NMsg name();

    /**
     * update action name
     *
     * @param name action name
     * @return {@code this} instance
     */
    NInputStreamMonitor name(NMsg name);

    /**
     * return source origin
     *
     * @return source origin
     */
    @NGetter
    Object origin();

    /**
     * update action source origin
     *
     * @param origin source origin
     * @return {@code this} instance
     */
    NInputStreamMonitor origin(Object origin);

    /**
     * return operation length
     *
     * @return {@code this} instance
     */
    @NGetter
    long length();

    /**
     * update operation length
     *
     * @param len operation length
     * @return {@code this} instance
     */
    @NSetter
    NInputStreamMonitor length(long len);

    NInputStreamMonitor source(NPath inputSource);

    /**
     * update operation source
     *
     * @param path operation source
     * @return {@code this} instance
     */
    NInputStreamMonitor source(Path path);

    /**
     * update operation source
     *
     * @param path operation source
     * @return {@code this} instance
     */
    NInputStreamMonitor source(File path);

    /**
     * update operation source
     *
     * @param path operation source
     * @return {@code this} instance
     */
    NInputStreamMonitor source(InputStream path);

    NInputStreamMonitor source(NInputSource source);

    /**
     * Create monitored input stream
     *
     * @return monitored input stream
     */
    InputStream create();

    @NGetter
    String sourceTypeName();

    @NGetter
    NInputSource source();

    @NSetter
    NInputStreamMonitor sourceTypeName(String sourceType);


    /**
     * return true if log progress on terminal
     *
     * @return true if log progress on terminal
     */
    boolean isLogProgress();

    /**
     * when true, will include default factory (console) even if progressFactory is defined
     *
     * @param value value
     * @return {@code this} instance
     * @since 0.5.8
     */
    @NSetter
    NInputStreamMonitor logProgress(boolean value);

    /**
     * return true if trace progress on terminal
     *
     * @return true if log progress on terminal
     */
    boolean isTraceProgress();

    /**
     * when true, will trace progress
     *
     * @param value value
     * @return {@code this} instance
     * @since 0.5.8
     */
    @NSetter
    NInputStreamMonitor traceProgress(boolean value);

    /**
     * return progress factory responsible of creating progress monitor
     *
     * @return progress factory responsible of creating progress monitor
     * @since 0.5.8
     */
    @NGetter
    NProgressFactory progressFactory();

    /**
     * set progress factory responsible of creating progress monitor
     *
     * @param value new value
     * @return {@code this} instance
     * @since 0.5.8
     */
    @NSetter
    NInputStreamMonitor progressFactory(NProgressFactory value);

    /**
     * set progress monitor. Will create a singleton progress monitor factory
     *
     * @param value new value
     * @return {@code this} instance
     * @since 0.5.8
     */
    NInputStreamMonitor progressMonitor(NProgressListener value);
}
