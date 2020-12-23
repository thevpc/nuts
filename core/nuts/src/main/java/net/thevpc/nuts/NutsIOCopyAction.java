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
 *
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
 * @category Input Output
 */
public interface NutsIOCopyAction {

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
    NutsIOCopyAction setSource(Object source);

    NutsIOCopyAction setSource(NutsInput source);

    /**
     * update source to copy from
     * @param source source to copy from
     * @return {@code this} instance
     */
    NutsIOCopyAction setSource(InputStream source);

    /**
     * update source to copy from
     * @param source source to copy from
     * @return {@code this} instance
     */
    NutsIOCopyAction setSource(File source);

    /**
     * update source to copy from
     * @param source source to copy from
     * @return {@code this} instance
     */
    NutsIOCopyAction setSource(Path source);

    /**
     * update source to copy from
     * @param source source to copy from
     * @return {@code this} instance
     */
    NutsIOCopyAction setSource(URL source);

    /**
     * update source to copy from
     * @param source source to copy from
     * @return {@code this} instance
     */
    NutsIOCopyAction setSource(String source);

    /**
     * update source to copy from
     * @param source source to copy from
     * @return {@code this} instance
     * @throws NutsUnsupportedArgumentException if unsupported type
     */
    NutsIOCopyAction from(Object source);

    /**
     * update source to copy from
     * @param source source to copy from
     * @return {@code this} instance
     */
    NutsIOCopyAction from(String source);

    /**
     * update source to copy from
     * @param source source to copy from
     * @return {@code this} instance
     */
    NutsIOCopyAction from(NutsInput source);

    /**
     * update source to copy from
     * @param source source to copy from
     * @return {@code this} instance
     */
    NutsIOCopyAction from(InputStream source);

    /**
     * update source to copy from
     * @param source source to copy from
     * @return {@code this} instance
     */
    NutsIOCopyAction from(File source);

    /**
     * update source to copy from
     * @param source source to copy from
     * @return {@code this} instance
     */
    NutsIOCopyAction from(Path source);

    /**
     * update source to copy from
     * @param source source to copy from
     * @return {@code this} instance
     */
    NutsIOCopyAction from(URL source);

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
    NutsIOCopyAction setTarget(OutputStream target);

    /**
     * update target to copy from
     * @param target target to copy to
     * @return {@code this} instance
     */
    NutsIOCopyAction setTarget(NutsOutput target);

    /**
     * update target to copy from
     * @param target target to copy to
     * @return {@code this} instance
     */
    NutsIOCopyAction setTarget(Path target);

    /**
     * update target to copy from
     * @param target target to copy to
     * @return {@code this} instance
     */
    NutsIOCopyAction setTarget(File target);

    /**
     * update target
     * @param target target
     * @return {@code this} instance
     */
    NutsIOCopyAction to(Object target);

    /**
     * update target
     * @param target target
     * @return {@code this} instance
     */
    NutsIOCopyAction to(OutputStream target);

    /**
     * update target to copy from
     * @param target target to copy to
     * @return {@code this} instance
     */
    NutsIOCopyAction to(String target);

    /**
     * update target to copy from
     * @param target target to copy to
     * @return {@code this} instance
     */
    NutsIOCopyAction to(Path target);

    /**
     * update target to copy from
     * @param target target to copy to
     * @return {@code this} instance
     */
    NutsIOCopyAction to(File target);

    /**
     * update target to copy from
     * @param target target to copy to
     * @return {@code this} instance
     */
    NutsIOCopyAction to(NutsOutput target);

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
    NutsIOCopyAction validator(NutsIOCopyValidator validator);

    /**
     * update validator
     * @param validator validator
     * @return {@code this} instance
     */
    NutsIOCopyAction setValidator(NutsIOCopyValidator validator);

    /**
     * return true if safe copy flag is armed
     * @return true if safe copy flag is armed
     */
    boolean isSafe();

    /**
     * arm safe copy flag
     * @return {@code this} instance
     */
    NutsIOCopyAction safe();

    /**
     * switch safe copy flag to {@code value}
     * @param value value
     * @return {@code this} instance
     */
    NutsIOCopyAction safe(boolean value);

    /**
     * switch safe copy flag to {@code value}
     * @param value value
     * @return {@code this} instance
     */
    NutsIOCopyAction setSafe(boolean value);

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
    NutsIOCopyAction setSession(NutsSession session);

    /**
     * run this copy action with {@link java.io.ByteArrayOutputStream} target and return bytes result
     * @return {@code this} instance
     */
    byte[] getByteArrayResult();

    /**
     * run this copy action
     * @return {@code this} instance
     */
    NutsIOCopyAction run();

    /**
     * switch log progress to {@code value}
     * @param value log progress
     * @return {@code this} instance
     */
    NutsIOCopyAction logProgress(boolean value);

    /**
     * switch log progress flag to to true.
     * @return {@code this} instance
     */
    NutsIOCopyAction logProgress();

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
    NutsIOCopyAction setLogProgress(boolean value);

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
    NutsIOCopyAction setProgressMonitorFactory(NutsProgressFactory value);

    /**
     * set progress factory responsible of creating progress monitor
     *
     * @param value new value
     * @return {@code this} instance
     * @since 0.5.8
     */
    NutsIOCopyAction progressMonitorFactory(NutsProgressFactory value);

    /**
     * set progress monitor. Will create a singleton progress monitor factory
     *
     * @param value new value
     * @return {@code this} instance
     * @since 0.5.8
     */
    NutsIOCopyAction setProgressMonitor(NutsProgressMonitor value);

    /**
     * set progress monitor. Will create a singleton progress monitor factory
     *
     * @param value new value
     * @return {@code this} instance
     * @since 0.5.8
     */
    NutsIOCopyAction progressMonitor(NutsProgressMonitor value);

    /**
     * set skip root flag to {@code value}
     *
     * @param value new value
     * @return {@code this} instance
     * @since 0.5.8
     */
    NutsIOCopyAction skipRoot(boolean value);

    /**
     * set skip root flag to {@code true}
     *
     * @return {@code this} instance
     * @since 0.5.8
     */
    NutsIOCopyAction skipRoot();

    /**
     * set skip root flag to {@code value}
     *
     * @param value new value
     * @return {@code this} instance
     * @since 0.5.8
     */
    NutsIOCopyAction setSkipRoot(boolean value);

    /**
     * return true if skip root flag is armed.
     *
     * @return true if skip root flag is armed
     * @since 0.5.8
     */
    boolean isSkipRoot();


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
    NutsIOCopyAction setInterruptible(boolean interruptible);

    /**
     * interrupt last created stream. An exception is throws when the stream is read.
     * @return {@code this} instance
     */
    NutsIOCopyAction interrupt();
}
