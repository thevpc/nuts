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

import java.net.URL;
import java.nio.file.Path;
import java.util.Objects;


/**
 * Default Content implementation.
 *
 * @author thevpc
 * @app.category Descriptor
 * @since 0.5.4
 */
public class DefaultNutsContent implements NutsContent {

    private final NutsPath path;
    private final boolean cached;
    private final boolean temporary;

    /**
     * Default Content implementation constructor
     *
     * @param path  content file path
     * @param cached    true if the file is cached (may be not up to date)
     * @param temporary true if file is temporary (should be deleted later)
     */
    public DefaultNutsContent(NutsPath path, boolean cached, boolean temporary) {
        this.path = path;
        this.cached = cached;
        this.temporary = temporary;
    }

    /**
     * content path location
     *
     * @return content path location
     */
    @Override
    public Path getFile() {
        return path == null ? null : path.toFile();
    }

    @Override
    public NutsPath getPath() {
        return path;
    }

    @Override
    public URL getURL() {
        return path == null ? null : getPath().toURL();
    }

    /**
     * true if the file is cached (may be not up to date)
     *
     * @return true if the file is cached (may be not up to date)
     */
    @Override
    public boolean isCached() {
        return cached;
    }

    /**
     * true if file is temporary (should be deleted later)
     *
     * @return true if file is temporary (should be deleted later)
     */
    @Override
    public boolean isTemporary() {
        return temporary;
    }

    @Override
    public int hashCode() {
        return Objects.hash(path, cached, temporary);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefaultNutsContent that = (DefaultNutsContent) o;
        return cached == that.cached &&
                temporary == that.temporary &&
                Objects.equals(path, that.path);
    }

    @Override
    public String toString() {
        return "Content{" + "file=" + path + ", cached=" + cached + ", temporary=" + temporary + '}';
    }
}
