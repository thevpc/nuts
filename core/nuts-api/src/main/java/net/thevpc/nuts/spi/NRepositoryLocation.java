/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . It's based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
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
 * <br> ====================================================================
 */
package net.thevpc.nuts.spi;

import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NGetter;
import net.thevpc.nuts.util.NStringUtils;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Immutable repository location
 *
 * @author thevpc
 */
public class NRepositoryLocation implements Comparable<NRepositoryLocation>, NBlankable, Cloneable {

    protected static final Pattern FULL_PATTERN = Pattern.compile("((?<n>[a-zA-Z][a-zA-Z0-9_-]*)?=)?((?<t>[a-zA-Z][a-zA-Z0-9_-]*)?@)?(?<r>.*)");

    private final String name;
    private final String locationType;
    private final String path;

    /**
     * Create a new NutsRepositoryLocation
     *
     * @param name         repository name
     * @param locationType location type such as 'maven' or 'nuts'
     * @param path         repository location (file, URL or any NPath valid
     *                     location)
     */
    public NRepositoryLocation(String name, String locationType, String path) {
        this.name = NStringUtils.trimToNull(name);
        this.locationType = NStringUtils.trimToNull(locationType);
        this.path = NStringUtils.trimToNull(path);
    }

    /**
     * Create a new NutsRepositoryLocation
     *
     * @param locationString location string in the format
     *                       {@code name=locationType:path}
     */
    protected NRepositoryLocation(String locationString) {
        if (locationString == null) {
            locationString = "";
        }
        Matcher nm = FULL_PATTERN.matcher(locationString);
        if (nm.find()) {
            name = NStringUtils.trimToNull(nm.group("n"));
            locationType = NStringUtils.trimToNull(nm.group("t"));
            path = NStringUtils.trimToNull(nm.group("r"));
        } else {
            name = null;
            locationType = null;
            path = NStringUtils.trimToNull(locationString);
        }
    }

    /**
     * Create a new NutsRepositoryLocation
     *
     * @param locationString location string in the format
     *                       {@code name=locationType:path}
     * @return new Instance
     */
    public static NRepositoryLocation of(String locationString) {
        return new NRepositoryLocation(locationString);
    }

    public static NRepositoryLocation ofName(String name) {
        return of(name, (String) null);
    }

    /**
     * Create a new NutsRepositoryLocation. When the name is null,
     * {@code fullLocation} will preserve any existing name (where
     * {@code fullLocation} is entered as a {@code locationString})
     *
     * @param name         new name (or null)
     * @param fullLocation location string in the format
     *                     {@code locationType:path}
     * @return new Instance
     */
    public static NRepositoryLocation of(String name, String fullLocation) {
        NRepositoryLocation q = of(fullLocation);
        if (name != null) {
            q = q.name(name);
        }
        return q;
    }


    /**
     * location name
     *
     * @return location name
     */
    public String name() {
        return name;
    }

    /**
     * return a new instance with the updated name
     *
     * @param name name
     * @return a new instance with the updated name
     */
    public NRepositoryLocation name(String name) {
        return new NRepositoryLocation(name, locationType, path);
    }

    /**
     * location path
     *
     * @return location name
     */
    public String path() {
        return path;
    }

    /**
     * return a new instance with the location
     *
     * @param path location
     * @return a new instance with the updated location
     */
    public NRepositoryLocation path(String path) {
        return new NRepositoryLocation(name, locationType, path);
    }

    /**
     * location type ('maven','nuts', etc.)
     *
     * @return location type
     */
    public String locationType() {
        return locationType;
    }

    /**
     * return a new instance with the updated location type
     *
     * @param locationType locationType
     * @return a new instance with the updated location type
     */
    public NRepositoryLocation locationType(String locationType) {
        return new NRepositoryLocation(name, locationType, path);
    }

    /**
     * hashcode based on the string representation
     *
     * @return hashCode
     */
    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    /**
     * true when the string representation is equivalent
     *
     * @param o other
     * @return true when the string representation is equivalent
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        NRepositoryLocation that = (NRepositoryLocation) o;
        return Objects.equals(toString(), that.toString());
    }

    /**
     * return location prefixed with location locationType if specified
     *
     * @return location prefixed with location locationType if specified
     */
    @NGetter
    public String fullLocation() {
        StringBuilder sb = new StringBuilder();
        if (!NBlankable.isBlank(locationType)) {
            sb.append(locationType);
            sb.append("@");
        }
        if (!NBlankable.isBlank(path)) {
            sb.append(path);
        }
        return sb.toString();
    }

    /**
     * return string representation of the location
     *
     * @return string representation of the location
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (!NBlankable.isBlank(name)) {
            sb.append(name);
            sb.append("=");
        }
        if (!NBlankable.isBlank(locationType)) {
            sb.append(locationType);
            sb.append("@");
        }
        if (!NBlankable.isBlank(path)) {
            sb.append(path);
        }
        return sb.toString();
    }

    /**
     * true when all of name, locationType and location are blank
     *
     * @return true when all of name, locationType and location are blank
     */
    @Override
    public boolean isBlank() {
        if (!NBlankable.isBlank(name)) {
            return false;
        }
        if (!NBlankable.isBlank(locationType)) {
            return false;
        }
        if (!NBlankable.isBlank(path)) {
            return false;
        }
        return true;
    }

    /**
     * compare string representations of the locations
     *
     * @param o other location
     * @return 1, -1 or 0 by comparing string representations of the locations
     */
    @Override
    public int compareTo(NRepositoryLocation o) {
        if (o == null) {
            return 1;
        }
        return toString().compareTo(o.toString());
    }

    public NRepositoryLocation copy() {
        return clone();
    }

    @Override
    protected NRepositoryLocation clone() {
        try {
            return (NRepositoryLocation) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
}
