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
package net.thevpc.nuts;

import net.thevpc.nuts.util.NMsg;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Exception thrown when the package could not be resolved
 *
 * @app.category Exceptions
 * @since 0.5.4
 */
public class NNotFoundException extends NException {

    private final NId id;
    private Set<NIdInvalidLocation> locations = Collections.emptySet();
    private Set<NIdInvalidDependency> missingDependencies = Collections.emptySet();

    /**
     * Constructs a new NutsNotFoundException exception
     *
     * @param id artifact id
     */
    public NNotFoundException(NId id) {
        this(id, (NMsg) null);
    }

    /**
     * Constructs a new NutsNotFoundException exception
     *
     * @param id    artifact id
     * @param cause cause
     */
    public NNotFoundException(NId id, Throwable cause) {
        this(id, null, null, cause);
    }


    /**
     * Constructs a new NutsNotFoundException exception
     *
     * @param id           artifact id
     * @param dependencies dependencies
     * @param locations    locations
     * @param cause        cause
     */
    public NNotFoundException(NId id, NIdInvalidDependency[] dependencies, NIdInvalidLocation[] locations, Throwable cause) {
        super(
                NMsg.ofC("artifact not found: %s%s", (id == null ? "<null>" : id), dependenciesToString(dependencies))
                , cause);
        this.id = id;
        if (locations != null) {
            this.locations = Collections.unmodifiableSet(Arrays.stream(locations).filter(Objects::nonNull).collect(Collectors.toSet()));
        }
        if (dependencies != null) {
            this.missingDependencies = Collections.unmodifiableSet(Arrays.stream(dependencies).filter(Objects::nonNull).collect(Collectors.toSet()));
        }
    }

    /**
     * Constructs a new NutsNotFoundException exception
     *
     * @param id      artifact id
     * @param message message
     * @param cause   cause
     */
    public NNotFoundException(NId id, NMsg message, Throwable cause) {
        super(
                message == null ? NMsg.ofC("no such nuts : %s", (id == null ? "<null>" : id)) : message,
                cause);
        this.id = id;
    }

    /**
     * Constructs a new NutsNotFoundException exception
     *
     * @param id      artifact id
     * @param message message
     */
    public NNotFoundException(NId id, NMsg message) {
        this(id, message, null);
    }

    protected static String dependenciesToString(NIdInvalidDependency[] dependencies) {
        Set<NIdInvalidDependency> missingDependencies0 = dependencies == null ?
                Collections.emptySet() : Collections.unmodifiableSet(Arrays.stream(dependencies).filter(Objects::nonNull).collect(Collectors.toSet()));
        if (missingDependencies0.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder(" : missing dependencies :");
        for (NIdInvalidDependency d2 : missingDependencies0) {
            sb.append("\n").append(dependenciesToString("  ", d2));
        }
        return sb.toString();
    }

    protected static String dependenciesToString(String prefix, NIdInvalidDependency d) {
        StringBuilder sb = new StringBuilder();
        sb.append(prefix).append(d.id);
        for (NIdInvalidDependency d2 : d.getCause()) {
            sb.append("\n").append(dependenciesToString(prefix + "  ", d2));
        }
        return sb.toString();
    }

    /**
     * artifact id
     *
     * @return artifact id
     */
    public NIdInvalidDependency toInvalidDependency() {
        return new NIdInvalidDependency(getId(),
                getMissingDependencies()
        );
    }

    public NId getId() {
        return id;
    }

    public Set<NIdInvalidDependency> getMissingDependencies() {
        return missingDependencies;
    }

    public Set<NIdInvalidLocation> getLocations() {
        return locations;
    }

    /**
     * @app.category Exceptions
     */
    public static class NIdInvalidDependency implements Serializable {
        private final NId id;
        private final Set<NIdInvalidDependency> cause;

        public NIdInvalidDependency(NId id, Set<NIdInvalidDependency> cause) {
            this.id = id;
            this.cause = cause == null ? Collections.emptySet() : cause;
        }

        public NId getId() {
            return id;
        }

        public Set<NIdInvalidDependency> getCause() {
            return cause;
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, cause);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            NIdInvalidDependency that = (NIdInvalidDependency) o;
            return Objects.equals(id, that.id) &&
                    Objects.equals(cause, that.cause);
        }
    }

    /**
     * @app.category Exceptions
     */
    public static class NIdInvalidLocation implements Serializable {
        private final String repository;
        private final String url;
        private final String message;

        public NIdInvalidLocation(String repository, String url, String message) {
            this.repository = repository;
            this.url = url;
            this.message = message;
        }

        public String getRepository() {
            return repository;
        }

        public String getUrl() {
            return url;
        }

        public String getMessage() {
            return message;
        }

        @Override
        public int hashCode() {
            return Objects.hash(repository, url, message);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            NIdInvalidLocation that = (NIdInvalidLocation) o;
            return Objects.equals(repository, that.repository) &&
                    Objects.equals(url, that.url) &&
                    Objects.equals(message, that.message);
        }
    }
}
