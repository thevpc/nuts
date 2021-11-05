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
import java.nio.file.Paths;
import java.util.Objects;


/**
 * Default Content implementation.
 * @author thevpc
 * @since 0.5.4
 * @app.category Descriptor
 */
public class NutsDefaultContent implements NutsContent {

    private final NutsPath location;
    private final boolean cached;
    private final boolean temporary;

    /**
     * Default Content implementation constructor
     * @param location content file path
     * @param cached true if the file is cached (may be not up to date)
     * @param temporary true if file is temporary (should be deleted later)
     */
    public NutsDefaultContent(NutsPath location, boolean cached, boolean temporary) {
        this.location = location;
        this.cached = cached;
        this.temporary = temporary;
    }

    /**
     * content path location
     * @return content path location
     */
    @Override
    public Path getFile() {
        return location == null ? null : Paths.get(location.toString());
    }

    @Override
    public NutsPath getPath() {
        return location;
    }

    @Override
    public URL getURL() {
        return location == null ? null : getPath().toURL();
    }

    @Override
    public NutsPath getLocation() {
        return location;
    }

    /**
     * true if the file is cached (may be not up to date)
     * @return true if the file is cached (may be not up to date)
     */
    @Override
    public boolean isCached() {
        return cached;
    }

    /**
     * true if file is temporary (should be deleted later)
     * @return true if file is temporary (should be deleted later)
     */
    @Override
    public boolean isTemporary() {
        return temporary;
    }

    @Override
    public int hashCode() {
        return Objects.hash(location, cached, temporary);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NutsDefaultContent that = (NutsDefaultContent) o;
        return cached == that.cached &&
                temporary == that.temporary &&
                Objects.equals(location, that.location);
    }

    @Override
    public String toString() {
        return "Content{" + "file=" + location + ", cached=" + cached + ", temporary=" + temporary + '}';
    }
}
