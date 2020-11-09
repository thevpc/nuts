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
import java.nio.file.Path;

/**
 * Monitor action enables monitoring a long lasting operation such as copying a big file.
 *
 * @author vpc
 */
public interface NutsMonitorAction {

    /**
     * update current session
     *
     * @param session session
     * @return {@code this} instance
     */
    NutsMonitorAction setSession(NutsSession session);

    /**
     * return current session
     *
     * @return current session
     */
    NutsSession getSession();

    /**
     * update action name
     *
     * @param name action name
     * @return {@code this} instance
     */
    NutsMonitorAction name(String name);

    /**
     * update action name
     *
     * @param name action name
     * @return {@code this} instance
     */
    NutsMonitorAction setName(String name);

    /**
     * return action name
     *
     * @return action name
     */
    String getName();

    /**
     * update action source origin
     *
     * @param origin source origin
     * @return {@code this} instance
     */
    NutsMonitorAction origin(Object origin);

    /**
     * update action source origin
     *
     * @param origin source origin
     * @return {@code this} instance
     */
    NutsMonitorAction setOrigin(Object origin);

    /**
     * return source origin
     *
     * @return source origin
     */
    Object getOrigin();

    /**
     * update operation length
     *
     * @param len operation length
     * @return {@code this} instance
     */
    NutsMonitorAction length(long len);

    /**
     * update operation length
     *
     * @param len operation length
     * @return {@code this} instance
     */
    NutsMonitorAction setLength(long len);

    /**
     * return operation length
     *
     * @return {@code this} instance
     */
    long getLength();

    /**
     * update operation source
     *
     * @param source operation source
     * @return {@code this} instance
     */
    NutsMonitorAction source(NutsInput source);

    NutsMonitorAction source(String path);

    /**
     * update operation source
     *
     * @param path operation source
     * @return {@code this} instance
     */
    NutsMonitorAction source(Path path);

    /**
     * update operation source
     *
     * @param path operation source
     * @return {@code this} instance
     */
    NutsMonitorAction source(File path);

    /**
     * update operation source
     * TODO: should this handle only streams?
     * @param path operation source
     * @return {@code this} instance
     */
    NutsMonitorAction source(InputStream path);

    /**
     * update operation source
     *
     * @param path operation source
     * @return {@code this} instance
     */
    NutsMonitorAction setSource(String path);

    NutsMonitorAction setSource(NutsInput inputSource);

    /**
     * update operation source
     *
     * @param path operation source
     * @return {@code this} instance
     */
    NutsMonitorAction setSource(Path path);

    /**
     * update operation source
     *
     * @param path operation source
     * @return {@code this} instance
     */
    NutsMonitorAction setSource(File path);

    /**
     * update operation source
     *
     * @param path operation source
     * @return {@code this} instance
     */
    NutsMonitorAction setSource(InputStream path);

    /**
     * Create monitored input stream
     *
     * @return monitored input stream
     */
    InputStream create();

    String getSourceTypeName();

    NutsMonitorAction setSourceTypeName(String sourceType);

    /**
     *
     * @return
     * @since  0.8.0
     */
    NutsInput createSource();

    /**
     * return true if log progress on terminal
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
    NutsMonitorAction setLogProgress(boolean value);

    /**
     * when true, will include default factory (console) even if progressFactory is defined
     *
     * @param value value
     * @return {@code this} instance
     * @since 0.5.8
     */
    NutsMonitorAction logProgress(boolean value);

    /**
     * will include default factory (console) even if progressFactory is defined
     *
     * @return {@code this} instance
     * @since 0.5.8
     */
    NutsMonitorAction logProgress();

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
    NutsMonitorAction setProgressFactory(NutsProgressFactory value);

    /**
     * set progress factory responsible of creating progress monitor
     *
     * @param value new value
     * @return {@code this} instance
     * @since 0.5.8
     */
    NutsMonitorAction progressFactory(NutsProgressFactory value);

    /**
     * set progress monitor. Will create a singleton progress monitor factory
     *
     * @param value new value
     * @return {@code this} instance
     * @since 0.5.8
     */
    NutsMonitorAction setProgressMonitor(NutsProgressMonitor value);

    /**
     * set progress monitor. Will create a singleton progress monitor factory
     *
     * @param value new value
     * @return {@code this} instance
     * @since 0.5.8
     */
    NutsMonitorAction progressMonitor(NutsProgressMonitor value);
}
