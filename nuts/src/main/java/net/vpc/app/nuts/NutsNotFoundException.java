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
 * <p>
 * Copyright (C) 2016-2020 thevpc
 * <p>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <br>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <br>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Exception thrown when the package could not be resolved
 *
 * @category Exception
 * @since 0.5.4
 */
public class NutsNotFoundException extends NutsException {

    private final String id;
    private Set<NutsIdInvalidLocation> locations = Collections.emptySet();
    private Set<NutsIdInvalidDependency> missingDependencies = Collections.emptySet();

    /**
     * Constructs a new NutsNotFoundException exception
     *
     * @param workspace workspace
     * @param id        artifact id
     */
    public NutsNotFoundException(NutsWorkspace workspace, NutsId id) {
        this(workspace, id == null ? null : id.toString());
    }

    /**
     * Constructs a new NutsNotFoundException exception
     *
     * @param workspace workspace
     * @param id        artifact id
     * @param cause     cause
     */
    public NutsNotFoundException(NutsWorkspace workspace, NutsId id, Exception cause) {
        this(workspace, id == null ? null : id.toString(), null, null, cause);
    }

    /**
     * Constructs a new NutsNotFoundException exception
     *
     * @param workspace workspace
     * @param id        artifact id
     */
    public NutsNotFoundException(NutsWorkspace workspace, String id) {
        this(workspace, id, null, null, null);
    }


    /**
     * Constructs a new NutsNotFoundException exception
     *
     * @param workspace workspace
     * @param id        artifact id
     */
    public NutsNotFoundException(NutsWorkspace workspace, NutsId id, NutsIdInvalidDependency[] dependencies, NutsIdInvalidLocation[] locations, Exception cause) {
        this(workspace, id == null ? null : id.toString(), dependencies, locations, cause);
    }

    /**
     * Constructs a new NutsNotFoundException exception
     *
     * @param workspace workspace
     * @param id        artifact id
     */
    public NutsNotFoundException(NutsWorkspace workspace, String id, NutsIdInvalidDependency[] dependencies, NutsIdInvalidLocation[] locations, Exception cause) {
        super(workspace, "Artifact not found: " + (id == null ? "<null>" : id)
                        + dependenciesToString(dependencies)
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
     * @param workspace workspace
     * @param id        artifact id
     * @param message   message
     * @param cause     cause
     */
    public NutsNotFoundException(NutsWorkspace workspace, String id, String message, Exception cause) {
        super(
                workspace, PrivateNutsUtils.isBlank(message) ? "No such nuts : " + (id == null ? "<null>" : id) : message,
                cause);
        this.id = id;
    }

    /**
     * Constructs a new NutsNotFoundException exception
     *
     * @param workspace workspace
     * @param id        artifact id
     * @param message   message
     * @param cause     cause
     */
    public NutsNotFoundException(NutsWorkspace workspace, NutsId id, String message, Exception cause) {
        this(workspace, id == null ? null : id.toString(), message, cause);
    }

    protected static String dependenciesToString(NutsIdInvalidDependency[] dependencies) {
        Set<NutsIdInvalidDependency> missingDependencies0 = dependencies == null ?
                Collections.emptySet() : Collections.unmodifiableSet(Arrays.stream(dependencies).filter(Objects::nonNull).collect(Collectors.toSet()));
        if (missingDependencies0.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder(" : missing dependencies :");
        for (NutsIdInvalidDependency d2 : missingDependencies0) {
            sb.append("\n").append(dependenciesToString("  ", d2));
        }
        return sb.toString();
    }

    protected static String dependenciesToString(String prefix, NutsIdInvalidDependency d) {
        StringBuilder sb = new StringBuilder();
        sb.append(prefix).append(d.id);
        for (NutsIdInvalidDependency d2 : d.getCause()) {
            sb.append("\n").append(dependenciesToString(prefix + "  ", d2));
        }
        return sb.toString();
    }

    /**
     * artifact id
     *
     * @return artifact id
     */
    public NutsIdInvalidDependency toInvalidDependency() {
        return new NutsNotFoundException.NutsIdInvalidDependency(getId(),
                getMissingDependencies()
        );
    }

    public String getId() {
        return id;
    }

    public Set<NutsIdInvalidDependency> getMissingDependencies() {
        return missingDependencies;
    }

    public Set<NutsIdInvalidLocation> getLocations() {
        return locations;
    }

    public static class NutsIdInvalidDependency implements Serializable {
        private String id;
        private Set<NutsIdInvalidDependency> cause;

        public NutsIdInvalidDependency(String id, Set<NutsIdInvalidDependency> cause) {
            this.id = id;
            this.cause = cause == null ? Collections.emptySet() : cause;
        }

        public String getId() {
            return id;
        }

        public Set<NutsIdInvalidDependency> getCause() {
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
            NutsIdInvalidDependency that = (NutsIdInvalidDependency) o;
            return Objects.equals(id, that.id) &&
                    Objects.equals(cause, that.cause);
        }
    }

    public static class NutsIdInvalidLocation implements Serializable {
        private String repository;
        private String url;
        private String message;

        public NutsIdInvalidLocation(String repository, String url, String message) {
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
            NutsIdInvalidLocation that = (NutsIdInvalidLocation) o;
            return Objects.equals(repository, that.repository) &&
                    Objects.equals(url, that.url) &&
                    Objects.equals(message, that.message);
        }
    }
}
