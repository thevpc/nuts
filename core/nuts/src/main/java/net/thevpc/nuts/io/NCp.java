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
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Path;
import java.util.Set;

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
 *
 * @author thevpc
 * @app.category Input Output
 * @since 0.5.4
 */
public interface NCp extends NComponent {
    static NCp of(NSession session) {
       return NExtensions.of(session).createSupported(NCp.class);
    }

    /**
     * source object to copy from. It may be any of the supported types.
     *
     * @return source object to copy from
     */
    NInputSource getSource();

    /**
     * update source to copy from
     *
     * @param source source to copy from
     * @return {@code this} instance
     * @throws NUnsupportedArgumentException if unsupported type
     */
    NCp setSource(NPath source);

    /**
     * update source to copy from
     *
     * @param source source to copy from
     * @return {@code this} instance
     */
    NCp setSource(InputStream source);

    /**
     * update source to copy from
     *
     * @param source source to copy from
     * @return {@code this} instance
     */
    NCp setSource(File source);

    /**
     * update source to copy from
     *
     * @param source source to copy from
     * @return {@code this} instance
     */
    NCp setSource(Path source);

    /**
     * update source to copy from
     *
     * @param source source to copy from
     * @return {@code this} instance
     */
    NCp setSource(URL source);

    /**
     * update source to copy from
     *
     * @param source source to copy from
     * @return {@code this} instance
     */
    NCp setSource(String source);

    /**
     * update source to copy from
     *
     * @param source source to copy from
     * @return {@code this} instance
     * @since 0.8.3
     */
    NCp setSource(byte[] source);

    NCp from(NInputSource source);

    /**
     * update source to copy from
     *
     * @param source source to copy from
     * @return {@code this} instance
     */
    NCp from(NPath source);

    /**
     * update source to copy from
     *
     * @param source source to copy from
     * @return {@code this} instance
     */
    NCp from(InputStream source);

    /**
     * update source to copy from
     *
     * @param source source to copy from
     * @return {@code this} instance
     */
    NCp from(File source);

    /**
     * update source to copy from
     *
     * @param source source to copy from
     * @return {@code this} instance
     */
    NCp from(Path source);

    /**
     * update source to copy from
     *
     * @param source source to copy from
     * @return {@code this} instance
     */
    NCp from(URL source);

    /**
     * update source to copy from
     *
     * @param source source to copy from
     * @return {@code this} instance
     * @since 0.8.3
     */
    NCp from(byte[] source);

    /**
     * source object to copy to. It may be of any of the supported types.
     *
     * @return target object to copy to
     */
    NOutputTarget getTarget();


    /**
     * update target to copy from
     *
     * @param target target to copy to
     * @return {@code this} instance
     */
    NCp setTarget(OutputStream target);

    /**
     * update target to copy from
     *
     * @param target target to copy to
     * @return {@code this} instance
     */
    NCp setTarget(NStream target);

    /**
     * update target to copy from
     * @param target target to copy to
     * @return {@code this} instance
     */
//    NutsCp setTarget(NutsOutput target);

    /**
     * update target to copy from
     *
     * @param target target to copy to
     * @return {@code this} instance
     */
    NCp setTarget(NPath target);

    /**
     * update target to copy from
     *
     * @param target target to copy to
     * @return {@code this} instance
     */
    NCp setTarget(Path target);

    /**
     * update target to copy from
     *
     * @param target target to copy to
     * @return {@code this} instance
     */
    NCp setTarget(File target);


    NCp setTarget(NOutputTarget target);

    NCp setSource(NInputSource source);
    /**
     * update target
     *
     * @param target target
     * @return {@code this} instance
     */
    NCp to(OutputStream target);


    /**
     * update target
     *
     * @param target target
     * @return {@code this} instance
     */
    NCp to(NStream target);

    /**
     * update target to copy from
     *
     * @param target target to copy to
     * @return {@code this} instance
     */
    NCp to(Path target);

    /**
     * update target to copy from
     *
     * @param target target to copy to
     * @return {@code this} instance
     */
    NCp to(NOutputTarget target);

    /**
     * update target to copy from
     *
     * @param target target to copy to
     * @return {@code this} instance
     */
    NCp to(File target);

    /**
     * update target to copy from
     *
     * @param target target to copy to
     * @return {@code this} instance
     */
    NCp to(NPath target);

    NCp addOptions(NPathOption...pathOptions);

    NCp removeOptions(NPathOption...pathOptions);

    NCp clearOptions();

    Set<NPathOption> getOptions();

    /**
     * return validator
     *
     * @return validator
     */
    NCpValidator getValidator();

    /**
     * update validator
     *
     * @param validator validator
     * @return {@code this} instance
     */
    NCp setValidator(NCpValidator validator);

    boolean isRecursive();

    NCp setRecursive(boolean recursive);

    boolean isMkdirs();

    NCp setMkdirs(boolean mkdirs);

    /**
     * return current session
     *
     * @return current session
     */
    NSession getSession();

    /**
     * update current session
     *
     * @param session current session
     * @return {@code this} instance
     */
    NCp setSession(NSession session);

    /**
     * run this copy action with {@link java.io.ByteArrayOutputStream} target and return bytes result
     *
     * @return {@code this} instance
     */
    byte[] getByteArrayResult();

    String getStringResult();

    /**
     * run this copy action
     *
     * @return {@code this} instance
     */
    NCp run();

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
    NCp setProgressFactory(NProgressFactory value);

    /**
     * set progress monitor. Will create a singleton progress monitor factory
     *
     * @param value new value
     * @return {@code this} instance
     * @since 0.5.8
     */
    NCp setProgressMonitor(NProgressListener value);

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
    NCp setSkipRoot(boolean value);

    /**
     * interrupt last created stream. An exception is throws when the stream is read.
     *
     * @return {@code this} instance
     */
    NCp interrupt();

    /**
     * source origin is a hint for logger about the source object for the given "from" source
     * @return source origin
     */
    Object getSourceOrigin() ;

    /**
     * source origin is a hint for logger about the source object for the given "from" source
     * @param sourceOrigin sourceOrigin
     * @return {@code this} instance
     */
    NCp setSourceOrigin(Object sourceOrigin) ;

    /**
     * action message
     * @return action message
     */
    NMsg getActionMessage() ;

    /**
     * actionMessage
     * @param actionMsg actionMessage
     * @return {@code this} instance
     */
    NCp setActionMessage(NMsg actionMsg) ;


    /**
     * source type name used in logging
     * @return source type name used in logging
     */
    String getSourceTypeName();

    /**
     * source type name used in logging
     * @param sourceTypeName sourceTypeName
     * @return {@code this} instance
     */
    NCp setSourceTypeName(String sourceTypeName) ;
}
