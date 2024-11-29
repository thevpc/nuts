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
package net.thevpc.nuts.boot;

import net.thevpc.nuts.boot.reserved.util.NBootMsg;
import net.thevpc.nuts.boot.reserved.util.NBootRepositoryDB;
import net.thevpc.nuts.boot.reserved.util.NBootStringUtils;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Immutable repository location
 *
 * @author thevpc
 */
public class NBootRepositoryLocation implements Comparable<NBootRepositoryLocation>, Cloneable {

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
    public NBootRepositoryLocation(String name, String locationType, String path) {
        this.name = NBootStringUtils.trimToNull(name);
        this.locationType = NBootStringUtils.trimToNull(locationType);
        this.path = NBootStringUtils.trimToNull(path);
    }

    /**
     * Create a new NutsRepositoryLocation
     *
     * @param locationString location string in the format
     *                       {@code name=locationType:path}
     */
    protected NBootRepositoryLocation(String locationString) {
        if (locationString == null) {
            locationString = "";
        }
        Matcher nm = FULL_PATTERN.matcher(locationString);
        if (nm.find()) {
            name = NBootStringUtils.trimToNull(nm.group("n"));
            locationType = NBootStringUtils.trimToNull(nm.group("t"));
            path = NBootStringUtils.trimToNull(nm.group("r"));
        } else {
            name = null;
            locationType = null;
            path = NBootStringUtils.trimToNull(locationString);
        }
    }

    /**
     * Create a new NutsRepositoryLocation
     *
     * @param locationString location string in the format
     *                       {@code name=locationType:path}
     * @return new Instance
     */
    public static NBootRepositoryLocation of(String locationString) {
        return new NBootRepositoryLocation(locationString);
    }

    public static NBootRepositoryLocation ofName(String name) {
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
    public static NBootRepositoryLocation of(String name, String fullLocation) {
        NBootRepositoryLocation q = of(fullLocation);
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
     *                       {@code name=locationType:path}
     * @param db             repository database
     * @return new Instance
     */
    public static NBootRepositoryLocation of(String locationString, NBootRepositoryDB db) {
        String name = null;
        String url = null;
        if (locationString == null) {
            return null;
        }
        locationString = locationString.trim();
        if (locationString.startsWith("-")
                || locationString.startsWith("+")
                || locationString.startsWith("=")
                || locationString.indexOf(',') >= 0
                || locationString.indexOf(';') >= 0) {
            throw new NBootException(NBootMsg.ofC("invalid selection syntax : %s", locationString));
        }
        Matcher matcher = Pattern.compile("(?<name>[a-zA-Z-_]+)=(?<value>.+)").matcher(locationString);
        if (matcher.find()) {
            name = matcher.group("name");
            url = matcher.group("value");
        } else {
            if (locationString.matches("[a-zA-Z][a-zA-Z0-9-_]+")) {
                name = locationString;
                NBootAddRepositoryOptions ro = db.getRepositoryOptionsByName(name);
                String u = ro==null?null:ro.getConfig().getLocation().getFullLocation();
                if (u == null) {
                    url = name;
                } else {
                    url = u;
                }
            } else {
                url = locationString;
                NBootAddRepositoryOptions ro = db.getRepositoryOptionsByLocation(name);
                String n = ro==null?null:ro.getName();
                if (n == null) {
                    name = null;
                } else {
                    name = n;
                }
            }
        }
        if (url.length() > 0) {
            return NBootRepositoryLocation.of(name, url);
        }
        return null;
    }

    /**
     * @param repositorySelectionExpression expression in the form +a,-b,=c
     * @param available                     available (default) locations
     * @param db                            repository database
     * @return repository location list from db that include available/defaults
     * and fulfills the condition {@code repositorySelectionExpression}
     */
    public static NBootRepositoryLocation[] of(String repositorySelectionExpression, NBootRepositoryLocation[] available, NBootRepositoryDB db) {
        NBootRepositorySelectorList li = NBootRepositorySelectorList.of(repositorySelectionExpression, db);
        if(li==null){
            return new NBootRepositoryLocation[0];
        }
        return li.resolve(available, db);
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
    public NBootRepositoryLocation setName(String name) {
        return new NBootRepositoryLocation(name, locationType, path);
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
    public NBootRepositoryLocation setPath(String path) {
        return new NBootRepositoryLocation(name, locationType, path);
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
    public NBootRepositoryLocation setLocationType(String locationType) {
        return new NBootRepositoryLocation(name, locationType, path);
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
        NBootRepositoryLocation that = (NBootRepositoryLocation) o;
        return Objects.equals(toString(), that.toString());
    }

    /**
     * return location prefixed with location locationType if specified
     *
     * @return location prefixed with location locationType if specified
     */
    public String getFullLocation() {
        StringBuilder sb = new StringBuilder();
        if (!NBootStringUtils.isBlank(locationType)) {
            sb.append(locationType);
            sb.append("@");
        }
        if (!NBootStringUtils.isBlank(path)) {
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
        if (!NBootStringUtils.isBlank(name)) {
            sb.append(name);
            sb.append("=");
        }
        if (!NBootStringUtils.isBlank(locationType)) {
            sb.append(locationType);
            sb.append("@");
        }
        if (!NBootStringUtils.isBlank(path)) {
            sb.append(path);
        }
        return sb.toString();
    }

    /**
     * true when all of name, locationType and location are blank
     *
     * @return true when all of name, locationType and location are blank
     */
    public boolean isBlank() {
        if (!NBootStringUtils.isBlank(name)) {
            return false;
        }
        if (!NBootStringUtils.isBlank(locationType)) {
            return false;
        }
        if (!NBootStringUtils.isBlank(path)) {
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
    public int compareTo(NBootRepositoryLocation o) {
        if (o == null) {
            return 1;
        }
        return toString().compareTo(o.toString());
    }

    public NBootRepositoryLocation copy() {
        return clone();
    }

    @Override
    protected NBootRepositoryLocation clone() {
        try {
            return (NBootRepositoryLocation) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
}
