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
package net.thevpc.nuts.io;

import net.thevpc.nuts.*;
import net.thevpc.nuts.spi.NComponent;
import net.thevpc.nuts.util.NProgressFactory;
import net.thevpc.nuts.util.NProgressListener;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.util.Set;

/**
 * I/O Action that help monitored uncompress of one or multiple resource types.
 *
 * @author thevpc
 * @app.category Input Output
 * @since 0.5.8
 */
public interface NUncompress extends NComponent, NSessionProvider {
    static NUncompress of(NSession session) {
        return NExtensions.of(session).createComponent(NUncompress.class).get();
    }

    /**
     * format
     *
     * @return format
     */
    String getFormat();

    /**
     * update format
     *
     * @param format format
     * @return {@code this} instance
     */
    NUncompress setFormat(String format);

    /**
     * update format option
     *
     * @param option option name
     * @param value  value
     * @return {@code this} instance
     */
    NUncompress setFormatOption(String option, Object value);

    /**
     * return format option
     *
     * @param option option name
     * @return option value
     */
    Object getFormatOption(String option);

    /**
     * source to uncompress
     *
     * @return source to uncompress
     */
    NInputSource getSource();

    NUncompress setSource(NInputSource source);

    NUncompress setTarget(NOutputTarget target);

    /**
     * update source to uncompress from
     *
     * @param source source to uncompress from
     * @return {@code this} instance
     */
    NUncompress setSource(InputStream source);

    /**
     * update source to uncompress from
     *
     * @param source source to uncompress from
     * @return {@code this} instance
     */
    NUncompress setSource(NPath source);

    /**
     * update source to uncompress from
     *
     * @param source source to uncompress from
     * @return {@code this} instance
     */
    NUncompress setSource(File source);

    /**
     * update source to uncompress from
     *
     * @param source source to uncompress from
     * @return {@code this} instance
     */
    NUncompress setSource(Path source);

    /**
     * update source to uncompress from
     *
     * @param source source to uncompress from
     * @return {@code this} instance
     */
    NUncompress setSource(URL source);

    /**
     * update source to uncompress from
     *
     * @param source source to uncompress from
     * @return {@code this} instance
     */
    NUncompress from(InputStream source);

    /**
     * update source to uncompress from
     *
     * @param source source to uncompress from
     * @return {@code this} instance
     */
    NUncompress from(File source);

    /**
     * update source to uncompress from
     *
     * @param source source to uncompress from
     * @return {@code this} instance
     */
    NUncompress from(Path source);

    /**
     * update source to uncompress from
     *
     * @param source source to uncompress from
     * @return {@code this} instance
     */
    NUncompress from(URL source);

    NUncompress to(NPath target);

    /**
     * target to uncompress to
     *
     * @return target to uncompress to
     */
    NOutputTarget getTarget();

    /**
     * update target
     *
     * @param target target
     * @return {@code this} instance
     */
    NUncompress setTarget(Path target);

    /**
     * update target
     *
     * @param target target
     * @return {@code this} instance
     */
    NUncompress setTarget(File target);

    NUncompress setTarget(NPath target);

    /**
     * update target
     *
     * @param target target
     * @return {@code this} instance
     */
    NUncompress to(Path target);

    /**
     * update target
     *
     * @param target target
     * @return {@code this} instance
     */
    NUncompress to(File target);

    /**
     * update source to uncompress from
     *
     * @param source source to uncompress from
     * @return {@code this} instance
     */
    NUncompress from(NPath source);

    /**
     * update current session
     *
     * @param session current session
     * @return {@code this} instance
     */
    NUncompress setSession(NSession session);

    /**
     * run this uncompress action
     *
     * @return {@code this} instance
     */
    NUncompress run();

    NUncompress visit(NUncompressVisitor visitor);

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
    NUncompress setSkipRoot(boolean value);

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
    NUncompress setProgressFactory(NProgressFactory value);

    /**
     * set progress monitor. Will create a singleton progress monitor factory
     *
     * @param value new value
     * @return {@code this} instance
     * @since 0.5.8
     */
    NUncompress setProgressMonitor(NProgressListener value);

    /**
     * set progress monitor. Will create a singleton progress monitor factory
     *
     * @param value new value
     * @return {@code this} instance
     * @since 0.5.8
     */
    NUncompress progressMonitor(NProgressListener value);

    /**
     * return true if safe flag is armed
     *
     * @return true if safe flag is armed
     */
    boolean isSafe();

    /**
     * switch safe flag to {@code value}
     *
     * @param value value
     * @return {@code this} instance
     */
    NUncompress setSafe(boolean value);

    NUncompress addOptions(NPathOption... pathOptions);

    NUncompress removeOptions(NPathOption... pathOptions);

    NUncompress clearOptions();

    Set<NPathOption> getOptions();
}
