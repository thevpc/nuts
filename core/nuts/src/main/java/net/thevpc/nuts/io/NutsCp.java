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
import net.thevpc.nuts.spi.NutsComponent;
import net.thevpc.nuts.spi.NutsComponentScope;
import net.thevpc.nuts.spi.NutsComponentScopeType;
import net.thevpc.nuts.util.NutsProgressFactory;
import net.thevpc.nuts.util.NutsProgressListener;
import net.thevpc.nuts.util.NutsUtils;

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
public interface NutsCp extends NutsComponent {
    static NutsCp of(NutsSession session) {
        NutsUtils.requireSession(session);
        return session.extensions().createSupported(NutsCp.class, true, null);
    }

    /**
     * source object to copy from. It may be any of the supported types.
     *
     * @return source object to copy from
     */
    NutsInputSource getSource();

    /**
     * update source to copy from
     *
     * @param source source to copy from
     * @return {@code this} instance
     * @throws NutsUnsupportedArgumentException if unsupported type
     */
    NutsCp setSource(NutsPath source);

    /**
     * update source to copy from
     *
     * @param source source to copy from
     * @return {@code this} instance
     */
    NutsCp setSource(InputStream source);

    /**
     * update source to copy from
     *
     * @param source source to copy from
     * @return {@code this} instance
     */
    NutsCp setSource(File source);

    /**
     * update source to copy from
     *
     * @param source source to copy from
     * @return {@code this} instance
     */
    NutsCp setSource(Path source);

    /**
     * update source to copy from
     *
     * @param source source to copy from
     * @return {@code this} instance
     */
    NutsCp setSource(URL source);

    /**
     * update source to copy from
     *
     * @param source source to copy from
     * @return {@code this} instance
     */
    NutsCp setSource(String source);

    /**
     * update source to copy from
     *
     * @param source source to copy from
     * @return {@code this} instance
     * @since 0.8.3
     */
    NutsCp setSource(byte[] source);

    NutsCp from(NutsInputSource source);

    /**
     * update source to copy from
     *
     * @param source source to copy from
     * @return {@code this} instance
     */
    NutsCp from(NutsPath source);

    /**
     * update source to copy from
     *
     * @param source source to copy from
     * @return {@code this} instance
     */
    NutsCp from(InputStream source);

    /**
     * update source to copy from
     *
     * @param source source to copy from
     * @return {@code this} instance
     */
    NutsCp from(File source);

    /**
     * update source to copy from
     *
     * @param source source to copy from
     * @return {@code this} instance
     */
    NutsCp from(Path source);

    /**
     * update source to copy from
     *
     * @param source source to copy from
     * @return {@code this} instance
     */
    NutsCp from(URL source);

    /**
     * update source to copy from
     *
     * @param source source to copy from
     * @return {@code this} instance
     * @since 0.8.3
     */
    NutsCp from(byte[] source);

    /**
     * source object to copy to. It may be of any of the supported types.
     *
     * @return target object to copy to
     */
    NutsOutputTarget getTarget();


    /**
     * update target to copy from
     *
     * @param target target to copy to
     * @return {@code this} instance
     */
    NutsCp setTarget(OutputStream target);

    /**
     * update target to copy from
     *
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
     *
     * @param target target to copy to
     * @return {@code this} instance
     */
    NutsCp setTarget(NutsPath target);

    /**
     * update target to copy from
     *
     * @param target target to copy to
     * @return {@code this} instance
     */
    NutsCp setTarget(Path target);

    /**
     * update target to copy from
     *
     * @param target target to copy to
     * @return {@code this} instance
     */
    NutsCp setTarget(File target);


    NutsCp setTarget(NutsOutputTarget target);

    NutsCp setSource(NutsInputSource source);
    /**
     * update target
     *
     * @param target target
     * @return {@code this} instance
     */
    NutsCp to(OutputStream target);


    /**
     * update target
     *
     * @param target target
     * @return {@code this} instance
     */
    NutsCp to(NutsPrintStream target);

    /**
     * update target to copy from
     *
     * @param target target to copy to
     * @return {@code this} instance
     */
    NutsCp to(Path target);

    /**
     * update target to copy from
     *
     * @param target target to copy to
     * @return {@code this} instance
     */
    NutsCp to(NutsOutputTarget target);

    /**
     * update target to copy from
     *
     * @param target target to copy to
     * @return {@code this} instance
     */
    NutsCp to(File target);

    /**
     * update target to copy from
     *
     * @param target target to copy to
     * @return {@code this} instance
     */
    NutsCp to(NutsPath target);

    NutsCp addOptions(NutsPathOption...pathOptions);

    NutsCp removeOptions(NutsPathOption ...pathOptions);

    NutsCp clearOptions();

    Set<NutsPathOption> getOptions();

    /**
     * return validator
     *
     * @return validator
     */
    NutsCpValidator getValidator();

    /**
     * update validator
     *
     * @param validator validator
     * @return {@code this} instance
     */
    NutsCp setValidator(NutsCpValidator validator);

    boolean isRecursive();

    NutsCp setRecursive(boolean recursive);

    boolean isMkdirs();

    NutsCp setMkdirs(boolean mkdirs);

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
    NutsCp setSession(NutsSession session);

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
    NutsCp run();

    /**
     * return progress factory responsible of creating progress monitor
     *
     * @return progress factory responsible of creating progress monitor
     * @since 0.5.8
     */
    NutsProgressFactory getProgressFactory();

    /**
     * set progress factory responsible of creating progress monitor
     *
     * @param value new value
     * @return {@code this} instance
     * @since 0.5.8
     */
    NutsCp setProgressFactory(NutsProgressFactory value);

    /**
     * set progress monitor. Will create a singleton progress monitor factory
     *
     * @param value new value
     * @return {@code this} instance
     * @since 0.5.8
     */
    NutsCp setProgressMonitor(NutsProgressListener value);

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
     * interrupt last created stream. An exception is throws when the stream is read.
     *
     * @return {@code this} instance
     */
    NutsCp interrupt();

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
    NutsCp setSourceOrigin(Object sourceOrigin) ;

    /**
     * action message
     * @return action message
     */
    NutsMessage getActionMessage() ;

    /**
     * actionMessage
     * @param actionMessage actionMessage
     * @return {@code this} instance
     */
    NutsCp setActionMessage(NutsMessage actionMessage) ;


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
    NutsCp setSourceTypeName(String sourceTypeName) ;
}
