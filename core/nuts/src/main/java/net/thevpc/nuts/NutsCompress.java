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
import java.net.URL;
import java.nio.file.Path;
import java.util.List;

/**
 * I/O Action that help monitored compress
 * of one or multiple resource types.
 * Default implementation should handle
 *
 * @author thevpc
 * @since 0.5.4
 * @app.category Toolkit
 */
public interface NutsCompress extends NutsComponent {
    static NutsCompress of(NutsSession session) {
        NutsApiUtils.checkSession(session);
        return session.extensions().createSupported(NutsCompress.class, true, null);
    }

    /**
     * update format option
     * @param option option name
     * @param value value
     * @return {@code this} instance
     */
    NutsCompress setFormatOption(String option, Object value);

    /**
     * return format option
     * @param option option name
     * @return option value
     */
    Object getFormatOption(String option);

    /**
     * format
     * @return format
     */
    String getFormat();

    /**
     * update format
     * @param format format
     * @return {@code this} instance
     */
    NutsCompress setFormat(String format);

    /**
     * sources to compress
     *
     * @return sources to compress
     */
    List<Object> getSources();

    /**
     * add source to compress
     *
     * @param source source
     * @return {@code this} instance
     * @throws NutsUnsupportedArgumentException if unsupported type
     */
    NutsCompress addSource(String source);

    /**
     * add source to compress
     *
     * @param source source
     * @return {@code this} instance
     * @throws NutsUnsupportedArgumentException if unsupported type
     */
    NutsCompress addSource(InputStream source);

    /**
     * add source to compress
     *
     * @param source source
     * @return {@code this} instance
     * @throws NutsUnsupportedArgumentException if unsupported type
     */
    NutsCompress addSource(File source);

    /**
     * add source to compress
     *
     * @param source source
     * @return {@code this} instance
     * @throws NutsUnsupportedArgumentException if unsupported type
     */
    NutsCompress addSource(Path source);

    /**
     * add source to compress
     *
     * @param source source
     * @return {@code this} instance
     * @throws NutsUnsupportedArgumentException if unsupported type
     */
    NutsCompress addSource(URL source);

    /**
     * add source to compress
     *
     * @param source source
     * @return {@code this} instance
     * @throws NutsUnsupportedArgumentException if unsupported type
     */
    NutsCompress addSource(NutsPath source);

    /**
     * target to compress to
     *
     * @return target to compress to
     */
    Object getTarget();

    /**
     * update target
     *
     * @param target target
     * @return {@code this} instance
     */
    NutsCompress setTarget(OutputStream target);

    /**
     * update target
     *
     * @param target target
     * @return {@code this} instance
     */
    NutsCompress setTarget(Path target);

    /**
     * update target
     *
     * @param target target
     * @return {@code this} instance
     */
    NutsCompress setTarget(File target);

    /**
     * update target
     *
     * @param target target
     * @return {@code this} instance
     */
    NutsCompress setTarget(String target);

    /**
     * update target
     *
     * @param target target
     * @return {@code this} instance
     */
    NutsCompress setTarget(NutsPath target);

    /**
     * update target
     *
     * @param target target
     * @return {@code this} instance
     */
    NutsCompress to(NutsPath target);

    /**
     * update target
     *
     * @param target target
     * @return {@code this} instance
     */
    NutsCompress to(OutputStream target);

    /**
     * update target
     *
     * @param target target
     * @return {@code this} instance
     */
    NutsCompress to(String target);

    /**
     * update target
     *
     * @param target target
     * @return {@code this} instance
     */
    NutsCompress to(Path target);

    /**
     * update target
     *
     * @param target target
     * @return {@code this} instance
     */
    NutsCompress to(File target);

    /**
     * return current session
     *
     * @return current session
     */
    NutsSession getSession();

    /**
     * update current session
     *
     * @param session current session
     * @return {@code this} instance
     */
    NutsCompress setSession(NutsSession session);

    /**
     * run this Compress action
     *
     * @return {@code this} instance
     */
    NutsCompress run();

    /**
     * true if log progress flag is armed
     *
     * @return true if log progress flag is armed
     */
    boolean isLogProgress();

    /**
     * switch log progress flag to {@code value}.
     *
     * @param value value
     * @return {@code this} instance
     */
    NutsCompress setLogProgress(boolean value);

    /**
     * return progress factory responsible of creating progress monitor
     *
     * @return progress factory responsible of creating progress monitor
     * @since 0.5.8
     */
    NutsProgressFactory getProgressMonitorFactory();

    /**
     * set progress factory responsible of creating progress monitor
     *
     * @param value new value
     * @return {@code this} instance
     * @since 0.5.8
     */
    NutsCompress setProgressMonitorFactory(NutsProgressFactory value);

    /**
     * set progress monitor. Will create a singleton progress monitor factory
     *
     * @param value new value
     * @return {@code this} instance
     * @since 0.5.8
     */
    NutsCompress setProgressMonitor(NutsProgressMonitor value);

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
    NutsCompress setSafe(boolean value);

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
    NutsCompress setSkipRoot(boolean value);
}
