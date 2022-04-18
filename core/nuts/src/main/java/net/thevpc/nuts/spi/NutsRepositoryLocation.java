/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 * <p>
 * Copyright [2020] [thevpc] Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.spi;

import net.thevpc.nuts.*;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Immutable repository location
 *
 * @author thevpc
 */
public class NutsRepositoryLocation implements Comparable<NutsRepositoryLocation>, NutsBlankable {

    protected static final Pattern FULL_PATTERN = Pattern.compile("((?<n>[a-zA-Z][a-zA-Z0-9_-]*)?=)?((?<t>[a-zA-Z][a-zA-Z0-9_-]*)?@)?(?<r>.*)");

    private final String name;
    private final String locationType;
    private final String path;

    /**
     * Create a new NutsRepositoryLocation
     *
     * @param name repository name
     * @param locationType location type such as 'maven' or 'nuts'
     * @param path repository location (file, URL or any NutsPath valid
     * location)
     */
    private NutsRepositoryLocation(String name, String locationType, String path) {
        this.name = NutsUtilStrings.trimToNull(name);
        this.locationType = NutsUtilStrings.trimToNull(locationType);
        this.path = NutsUtilStrings.trimToNull(path);
    }

    /**
     * Create a new NutsRepositoryLocation
     *
     * @param locationString location string in the format
     * {@code name=locationType:path}
     */
    protected NutsRepositoryLocation(String locationString) {
        if (locationString == null) {
            locationString = "";
        }
        Matcher nm = FULL_PATTERN.matcher(locationString);
        if (nm.find()) {
            name = NutsUtilStrings.trimToNull(nm.group("n"));
            locationType = NutsUtilStrings.trimToNull(nm.group("t"));
            path = NutsUtilStrings.trimToNull(nm.group("r"));
        } else {
            name = null;
            locationType = null;
            path = NutsUtilStrings.trimToNull(locationString);
        }
    }

    /**
     * Create a new NutsRepositoryLocation
     *
     * @param locationString location string in the format
     * {@code name=locationType:path}
     * @return new Instance
     */
    public static NutsRepositoryLocation of(String locationString) {
        return new NutsRepositoryLocation(locationString);
    }

    /**
     * Create a new NutsRepositoryLocation. When the name is null,
     * {@code fullLocation} will preserve any existing name (where
     * {@code fullLocation} is entered as a {@code locationString})
     *
     * @param name new name (or null)
     * @param fullLocation location string in the format
     * {@code locationType:path}
     * @return new Instance
     */
    public static NutsRepositoryLocation of(String name, String fullLocation) {
        NutsRepositoryLocation q = of(fullLocation);
        if (name != null) {
            q = q.setName(name);
        }
        return q;
    }

    /**
     * Create a new NutsRepositoryLocation. When the name is null,
     * {@code fullLocation} will preserve any existing name (where
     * {@code fullLocation} is entered as a {@code locationString})
     *
     * @param locationString location string in the format
     * {@code name=locationType:path}
     * @param db repository database
     * @param session session or null
     * @return new Instance
     */
    public static NutsRepositoryLocation of(String locationString, NutsRepositoryDB db, NutsSession session) {
        String name = null;
        String url = null;
        if (locationString == null) {
            if (session == null) {
                throw new IllegalArgumentException("invalid null repository");
            } else {
                throw new NutsIllegalArgumentException(session, NutsMessage.plain("invalid null repository"));
            }
        }
        locationString = locationString.trim();
        if (locationString.startsWith("-")
                || locationString.startsWith("+")
                || locationString.startsWith("=")
                || locationString.indexOf(',') >= 0
                || locationString.indexOf(';') >= 0) {
            if (session == null) {
                throw new IllegalArgumentException("invalid selection syntax");
            } else {
                throw new NutsIllegalArgumentException(session, NutsMessage.plain("invalid repository syntax"));
            }
        }
        Matcher matcher = Pattern.compile("(?<name>[a-zA-Z-_]+)=(?<value>.+)").matcher(locationString);
        if (matcher.find()) {
            name = matcher.group("name");
            url = matcher.group("value");
        } else {
            if (locationString.matches("[a-zA-Z][a-zA-Z0-9-_]+")) {
                name = locationString;
                String u = db.getRepositoryLocationByName(name);
                if (u == null) {
                    url = name;
                } else {
                    url = u;
                }
            } else {
                url = locationString;
                String n = db.getRepositoryNameByLocation(url);
                if (n == null) {
                    name = url;
                } else {
                    name = n;
                }
            }
        }
        if (url.length() > 0) {
            return NutsRepositoryLocation.of(name, url);
        }
        return null;
    }

    /**
     *
     * @param repositorySelectionExpression expression in the form +a,-b,=c
     * @param available available (default) locations
     * @param db repository database
     * @param session session (or null)
     * @return repository location list from db that include available/defaults
     * and fulfills the condition {@code repositorySelectionExpression}
     */
    public static NutsRepositoryLocation[] ofAll(String repositorySelectionExpression, NutsRepositoryLocation[] available, NutsRepositoryDB db, NutsSession session) {
        return NutsRepositorySelectorList.of(repositorySelectionExpression, db, session)
                .resolve(available, db);
    }

    /**
     * location name
     *
     * @return location name
     */
    public String getName() {
        return name;
    }

    /**
     * return a new instance with the updated name
     *
     * @param name name
     * @return a new instance with the updated name
     */
    public NutsRepositoryLocation setName(String name) {
        return new NutsRepositoryLocation(name, locationType, path);
    }

    /**
     * location path
     *
     * @return location name
     */
    public String getPath() {
        return path;
    }

    /**
     * return a new instance with the location
     *
     * @param path location
     * @return a new instance with the updated location
     */
    public NutsRepositoryLocation setPath(String path) {
        return new NutsRepositoryLocation(name, locationType, path);
    }

    /**
     * location type ('maven','nuts', etc.)
     *
     * @return location type
     */
    public String getLocationType() {
        return locationType;
    }

    /**
     * return a new instance with the updated location type
     *
     * @param locationType locationType
     * @return a new instance with the updated location type
     */
    public NutsRepositoryLocation setLocationType(String locationType) {
        return new NutsRepositoryLocation(name, locationType, path);
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
        NutsRepositoryLocation that = (NutsRepositoryLocation) o;
        return Objects.equals(toString(), that.toString());
    }

    /**
     * return location prefixed with location locationType if specified
     *
     * @return location prefixed with location locationType if specified
     */
    public String getFullLocation() {
        StringBuilder sb = new StringBuilder();
        if (!NutsBlankable.isBlank(locationType)) {
            sb.append(locationType);
            sb.append("@");
        }
        if (!NutsBlankable.isBlank(path)) {
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
        if (!NutsBlankable.isBlank(name)) {
            sb.append(name);
            sb.append("=");
        }
        if (!NutsBlankable.isBlank(locationType)) {
            sb.append(locationType);
            sb.append("@");
        }
        if (!NutsBlankable.isBlank(path)) {
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
        if (!NutsBlankable.isBlank(name)) {
            return false;
        }
        if (!NutsBlankable.isBlank(locationType)) {
            return false;
        }
        if (!NutsBlankable.isBlank(path)) {
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
    public int compareTo(NutsRepositoryLocation o) {
        if (o == null) {
            return 1;
        }
        return toString().compareTo(o.toString());
    }

}
