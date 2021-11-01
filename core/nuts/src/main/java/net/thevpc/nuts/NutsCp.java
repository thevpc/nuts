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

/**
 * I/O Action that help monitored copy of one or multiple resource types.
 * Implementation should at least handle the following types as valid sources :
 * <ul>
 *     <li>InputStream</li>
 *     <li>string (as path or url)</li>
 *     <li>File (file or directory)</li>
 *     <li>Path (file or directory)</li>
 *     <li>URL</li>
 * </ul>
 * and the following types as valid targets :
 * <ul>
 *     <li>OutputStream</li>
 *     <li>string (as path or url)</li>
 *     <li>File (file or directory)</li>
 *     <li>Path (file or directory)</li>
 * </ul>
 * @author thevpc
 * @since 0.5.4
 * @app.category Input Output
 */
public interface NutsCp extends NutsComponent<Object> {
    static NutsCp of(NutsSession session) {
        NutsApiUtils.checkSession(session);
        return session.extensions().createSupported(NutsCp.class, true, null);
    }

    /**
     * source object to copy from. It may be of any of the supported types.
     * @return source object to copy from
     */
    Object getSource();

    /**
     * update source to copy from
     * @param source source to copy from
     * @return {@code this} instance
     * @throws NutsUnsupportedArgumentException if unsupported type
     */
    NutsCp setSource(NutsPath source);

    /**
     * update source to copy from
     * @param source source to copy from
     * @return {@code this} instance
     */
    NutsCp setSource(InputStream source);

    /**
     * update source to copy from
     * @param source source to copy from
     * @return {@code this} instance
     */
    NutsCp setSource(File source);

    /**
     * update source to copy from
     * @param source source to copy from
     * @return {@code this} instance
     */
    NutsCp setSource(Path source);

    /**
     * update source to copy from
     * @param source source to copy from
     * @return {@code this} instance
     */
    NutsCp setSource(URL source);

    /**
     * update source to copy from
     * @param source source to copy from
     * @return {@code this} instance
     */
    NutsCp setSource(String source);

    /**
     * update source to copy from
     * @param source source to copy from
     * @return {@code this} instance
     * @throws NutsUnsupportedArgumentException if unsupported type
     */
//    NutsCp from(NutsPath source);

    /**
     * update source to copy from
     * @param source source to copy from
     * @return {@code this} instance
     */
    NutsCp from(String source);

    /**
     * update source to copy from
     * @param source source to copy from
     * @return {@code this} instance
     */
    NutsCp from(NutsPath source);

    /**
     * update source to copy from
     * @param source source to copy from
     * @return {@code this} instance
     */
    NutsCp from(InputStream source);

    /**
     * update source to copy from
     * @param source source to copy from
     * @return {@code this} instance
     */
    NutsCp from(File source);

    /**
     * update source to copy from
     * @param source source to copy from
     * @return {@code this} instance
     */
    NutsCp from(Path source);

    /**
     * update source to copy from
     * @param source source to copy from
     * @return {@code this} instance
     */
    NutsCp from(URL source);

    /**
     * update source to copy from
     * @param source source to copy from
     * @return {@code this} instance
     * @since 0.8.3
     */
    NutsCp from(byte[] source);

    /**
     * update source to copy from
     * @param source source to copy from
     * @return {@code this} instance
     * @since 0.8.3
     */
    NutsCp setSource(byte[] source);

    /**
     * source object to copy to. It may be of any of the supported types.
     * @return target object to copy to
     */
    Object getTarget();

    /**
     * update target to copy from
     * @param target target to copy to
     * @return {@code this} instance
     */
    NutsCp setTarget(OutputStream target);

    /**
     * update target to copy from
     * @param target target to copy to
     * @return {@code this} instance
     */
    NutsCp setTarget(NutsPrintStream target);

    /**
     * update target to copy from
     * @param target target to copy to
     * @return {@code this} instance
     */
//    NutsCp setTarget(NutsOutput target);

    /**
     * update target to copy from
     * @param target target to copy to
     * @return {@code this} instance
     */
    NutsCp setTarget(NutsPath target);

    /**
     * update target to copy from
     * @param target target to copy to
     * @return {@code this} instance
     */
    NutsCp setTarget(Path target);

    /**
     * update target to copy from
     * @param target target to copy to
     * @return {@code this} instance
     */
    NutsCp setTarget(String target);

    /**
     * update target to copy from
     * @param target target to copy to
     * @return {@code this} instance
     */
    NutsCp setTarget(File target);

    /**
     * update target
     * @param target target
     * @return {@code this} instance
     */
//    NutsCp to(NutsPath target);

    /**
     * update target
     * @param target target
     * @return {@code this} instance
     */
    NutsCp to(OutputStream target);


    /**
     * update target
     * @param target target
     * @return {@code this} instance
     */
    NutsCp to(NutsPrintStream target);

    /**
     * update target to copy from
     * @param target target to copy to
     * @return {@code this} instance
     */
    NutsCp to(String target);

    /**
     * update target to copy from
     * @param target target to copy to
     * @return {@code this} instance
     */
    NutsCp to(Path target);

    /**
     * update target to copy from
     * @param target target to copy to
     * @return {@code this} instance
     */
    NutsCp to(File target);

    /**
     * update target to copy from
     * @param target target to copy to
     * @return {@code this} instance
     */
    NutsCp to(NutsPath target);

    /**
     * return validator
     * @return validator
     */
    NutsIOCopyValidator getValidator();

    /**
     * update validator
     * @param validator validator
     * @return {@code this} instance
     */
    NutsCp setValidator(NutsIOCopyValidator validator);

    boolean isRecursive();

    NutsCp setRecursive(boolean recursive);

    boolean isMkdirs();

    NutsCp setMkdirs(boolean mkdirs);

    /**
     * return true if safe copy flag is armed
     * @return true if safe copy flag is armed
     */
    boolean isSafe();

    /**
     * switch safe copy flag to {@code value}
     * @param value value
     * @return {@code this} instance
     */
    NutsCp setSafe(boolean value);

    /**
     * return current session
     * @return current session
     */
    NutsSession getSession();

    /**
     * update current session
     * @param session current session
     * @return {@code this} instance
     */
    NutsCp setSession(NutsSession session);

    /**
     * run this copy action with {@link java.io.ByteArrayOutputStream} target and return bytes result
     * @return {@code this} instance
     */
    byte[] getByteArrayResult();

    /**
     * run this copy action
     * @return {@code this} instance
     */
    NutsCp run();


    /**
     * true if log progress flag is armed
     * @return true if log progress flag is armed
     */
    boolean isLogProgress();

    /**
     * switch log progress flag to {@code value}.
     * @param value value
     * @return {@code this} instance
     */
    NutsCp setLogProgress(boolean value);

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
    NutsCp setProgressMonitorFactory(NutsProgressFactory value);

    /**
     * set progress monitor. Will create a singleton progress monitor factory
     *
     * @param value new value
     * @return {@code this} instance
     * @since 0.5.8
     */
    NutsCp setProgressMonitor(NutsProgressMonitor value);

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
    NutsCp setSkipRoot(boolean value);

    /**
     * return true created stream should be marked as interruptible
     * @return {@code this} instance
     */
    boolean isInterruptible();

    /**
     * mark created stream as interruptible so that one can call {@link #interrupt()}
     * @param interruptible new value
     * @return {@code this} instance
     */
    NutsCp setInterruptible(boolean interruptible);

    /**
     * interrupt last created stream. An exception is throws when the stream is read.
     * @return {@code this} instance
     */
    NutsCp interrupt();
}
