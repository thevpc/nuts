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
 *
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

/**
 * Repository Event
 *
 * @author thevpc
 * @app.category Events
 * @since 0.5.4
 */
public interface NRepositoryEvent {

    /**
     * current session
     *
     * @return current session
     */
    NSession getSession();

    /**
     * current workspace
     *
     * @return current workspace
     */
    NWorkspace getWorkspace();

    /**
     * Parent repository when this event is about creating
     * a new repository with a parent one.
     *
     * @return Parent repository
     */
    NRepository getParent();

    /**
     * repository that fires this event or the new repository
     * when creating a new one with parent.
     *
     * @return current repository
     */
    NRepository getRepository();

    /**
     * event property name
     *
     * @return event property name
     */
    String getPropertyName();

    /**
     * event property old value
     *
     * @return event property old value
     */
    Object getPropertyOldValue();

    /**
     * event property new value
     *
     * @return event property new value
     */
    Object getPropertyValue();
}
