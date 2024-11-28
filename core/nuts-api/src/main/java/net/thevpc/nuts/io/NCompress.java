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

import net.thevpc.nuts.*;
import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.spi.NComponent;
import net.thevpc.nuts.time.NProgressFactory;
import net.thevpc.nuts.time.NProgressListener;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

/**
 * I/O Action that help monitored compress
 * of one or multiple resource types.
 * Default implementation should handle
 *
 * @author thevpc
 * @app.category Toolkit
 * @since 0.5.4
 */
public interface NCompress extends NComponent {
    static NCompress of() {
        return NExtensions.of(NCompress.class);
    }

    /**
     * update format option
     *
     * @param option option name
     * @param value  value
     * @return {@code this} instance
     */
    NCompress setFormatOption(String option, Object value);

    /**
     * return format option
     *
     * @param option option name
     * @return option value
     */
    Object getFormatOption(String option);

    /**
     * format
     *
     * @return format
     */
    String getPackaging();

    /**
     * update packaging
     *
     * @param packaging packaging
     * @return {@code this} instance
     */
    NCompress setPackaging(String packaging);

    /**
     * sources to compress
     *
     * @return sources to compress
     */
    List<NInputSource> getSources();

    NCompress addSource(NInputSource source);

    /**
     * add source to compress
     *
     * @param source source
     * @return {@code this} instance
     * @throws NUnsupportedArgumentException if unsupported type
     */
    NCompress addSource(InputStream source);

    /**
     * add source to compress
     *
     * @param source source
     * @return {@code this} instance
     * @throws NUnsupportedArgumentException if unsupported type
     */
    NCompress addSource(File source);

    /**
     * add source to compress
     *
     * @param source source
     * @return {@code this} instance
     * @throws NUnsupportedArgumentException if unsupported type
     */
    NCompress addSource(Path source);

    /**
     * add source to compress
     *
     * @param source source
     * @return {@code this} instance
     * @throws NUnsupportedArgumentException if unsupported type
     */
    NCompress addSource(URL source);

    /**
     * add source to compress
     *
     * @param source source
     * @return {@code this} instance
     * @throws NUnsupportedArgumentException if unsupported type
     */
    NCompress addSource(NPath source);

    /**
     * target to compress to
     *
     * @return target to compress to
     */
    NOutputTarget getTarget();

    /**
     * update target
     *
     * @param target target
     * @return {@code this} instance
     */
    NCompress setTarget(OutputStream target);

    /**
     * update target
     *
     * @param target target
     * @return {@code this} instance
     */
    NCompress setTarget(Path target);

    NCompress setTarget(NOutputTarget target);

    /**
     * update target
     *
     * @param target target
     * @return {@code this} instance
     */
    NCompress setTarget(File target);

    /**
     * update target
     *
     * @param target target
     * @return {@code this} instance
     */
    NCompress setTarget(String target);

    /**
     * update target
     *
     * @param target target
     * @return {@code this} instance
     */
    NCompress setTarget(NPath target);

    /**
     * update target
     *
     * @param target target
     * @return {@code this} instance
     */
    NCompress to(NPath target);

    /**
     * update target
     *
     * @param target target
     * @return {@code this} instance
     */
    NCompress to(OutputStream target);

    /**
     * update target
     *
     * @param target target
     * @return {@code this} instance
     */
    NCompress to(String target);

    /**
     * update target
     *
     * @param target target
     * @return {@code this} instance
     */
    NCompress to(Path target);

    /**
     * update target
     *
     * @param target target
     * @return {@code this} instance
     */
    NCompress to(File target);

    /**
     * run this Compress action
     *
     * @return {@code this} instance
     */
    NCompress run();

    /**
     * return progress factory responsible of creating progress monitor
     *
     * @return progress factory responsible of creating progress monitor
     * @since 0.5.8
     */
    NProgressFactory getProgressFactory();

    /**
     * set progress factory responsible of creating progress monitor
     *
     * @param value new value
     * @return {@code this} instance
     * @since 0.5.8
     */
    NCompress setProgressFactory(NProgressFactory value);

    /**
     * set progress monitor. Will create a singleton progress monitor factory
     *
     * @param value new value
     * @return {@code this} instance
     * @since 0.5.8
     */
    NCompress setProgressMonitor(NProgressListener value);

    /**
     * return true if safe copy flag is armed
     *
     * @return true if safe copy flag is armed
     */
    boolean isSafe();

    /**
     * switch safe copy flag to {@code value}
     *
     * @param value safe value
     * @return {@code this} instance
     */
    NCompress setSafe(boolean value);

    /**
     * return true if skip root flag is armed.
     *
     * @return true if skip root flag is armed
     * @since 0.5.8
     */
    boolean isSkipRoot();

    /**
     * set skip root flag to {@code value}
     *
     * @param value new value
     * @return {@code this} instance
     * @since 0.5.8
     */
    NCompress setSkipRoot(boolean value);

    NCompress addOptions(NPathOption... pathOptions);

    NCompress removeOptions(NPathOption... pathOptions);

    NCompress clearOptions();

    Set<NPathOption> getOptions();
}
