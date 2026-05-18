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
public interface NUncompress extends NComponent {
    static NUncompress of() {
        return NExtensions.of(NUncompress.class);
    }

    /**
     * format
     *
     * @return format
     */
    String packaging();

    /**
     * update packaging
     *
     * @param packaging packaging
     * @return {@code this} instance
     */
    NUncompress packaging(String packaging);

    /**
     * update format option
     *
     * @param option option name
     * @param value  value
     * @return {@code this} instance
     */
    NUncompress formatOption(String option, Object value);

    /**
     * return format option
     *
     * @param option option name
     * @return option value
     */
    Object formatOption(String option);

    /**
     * source to uncompress
     *
     * @return source to uncompress
     */
    NInputSource source();

    NUncompress source(NInputSource source);

    NUncompress target(NOutputTarget target);

    /**
     * update source to uncompress from
     *
     * @param source source to uncompress from
     * @return {@code this} instance
     */
    NUncompress source(InputStream source);

    /**
     * update source to uncompress from
     *
     * @param source source to uncompress from
     * @return {@code this} instance
     */
    NUncompress source(NPath source);

    /**
     * update source to uncompress from
     *
     * @param source source to uncompress from
     * @return {@code this} instance
     */
    NUncompress source(File source);

    /**
     * update source to uncompress from
     *
     * @param source source to uncompress from
     * @return {@code this} instance
     */
    NUncompress source(Path source);

    /**
     * update source to uncompress from
     *
     * @param source source to uncompress from
     * @return {@code this} instance
     */
    NUncompress source(URL source);

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
    NUncompress target(Path target);

    /**
     * update target
     *
     * @param target target
     * @return {@code this} instance
     */
    NUncompress target(File target);

    NUncompress target(NPath target);

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
    NUncompress skipRoot(boolean value);

    /**
     * return progress factory responsible of creating progress monitor
     *
     * @return progress factory responsible of creating progress monitor
     * @since 0.5.8
     */
    NProgressFactory progressFactory();

    /**
     * set progress factory responsible of creating progress monitor
     *
     * @param value new value
     * @return {@code this} instance
     * @since 0.5.8
     */
    NUncompress progressFactory(NProgressFactory value);

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
    NUncompress safe(boolean value);

    NUncompress options(NPathOption... pathOptions);
    NUncompress addOptions(NPathOption... pathOptions);

    NUncompress removeOptions(NPathOption... pathOptions);

    NUncompress clearOptions();

    Set<NPathOption> getOptions();
}
