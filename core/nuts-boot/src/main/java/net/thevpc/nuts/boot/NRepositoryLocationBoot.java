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

import net.thevpc.nuts.boot.reserved.NMsgBoot;
import net.thevpc.nuts.boot.reserved.NReservedBootRepositoryDB;
import net.thevpc.nuts.boot.reserved.util.NStringUtilsBoot;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Immutable repository location
 *
 * @author thevpc
 */
public class NRepositoryLocationBoot implements Comparable<NRepositoryLocationBoot>, Cloneable {

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
    public NRepositoryLocationBoot(String name, String locationType, String path) {
        this.name = NStringUtilsBoot.trimToNull(name);
        this.locationType = NStringUtilsBoot.trimToNull(locationType);
        this.path = NStringUtilsBoot.trimToNull(path);
    }

    /**
     * Create a new NutsRepositoryLocation
     *
     * @param locationString location string in the format
     *                       {@code name=locationType:path}
     */
    protected NRepositoryLocationBoot(String locationString) {
        if (locationString == null) {
            locationString = "";
        }
        Matcher nm = FULL_PATTERN.matcher(locationString);
        if (nm.find()) {
            name = NStringUtilsBoot.trimToNull(nm.group("n"));
            locationType = NStringUtilsBoot.trimToNull(nm.group("t"));
            path = NStringUtilsBoot.trimToNull(nm.group("r"));
        } else {
            name = null;
            locationType = null;
            path = NStringUtilsBoot.trimToNull(locationString);
        }
    }

    /**
     * Create a new NutsRepositoryLocation
     *
     * @param locationString location string in the format
     *                       {@code name=locationType:path}
     * @return new Instance
     */
    public static NRepositoryLocationBoot of(String locationString) {
        return new NRepositoryLocationBoot(locationString);
    }

    public static NRepositoryLocationBoot ofName(String name) {
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
    public static NRepositoryLocationBoot of(String name, String fullLocation) {
        NRepositoryLocationBoot q = of(fullLocation);
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
    public static NRepositoryLocationBoot of(String locationString, NReservedBootRepositoryDB db) {
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
            throw new NBootException(NMsgBoot.ofC("invalid selection syntax : %s", locationString));
        }
        Matcher matcher = Pattern.compile("(?<name>[a-zA-Z-_]+)=(?<value>.+)").matcher(locationString);
        if (matcher.find()) {
            name = matcher.group("name");
            url = matcher.group("value");
        } else {
            if (locationString.matches("[a-zA-Z][a-zA-Z0-9-_]+")) {
                name = locationString;
                NAddRepositoryOptionsBoot ro = db.getRepositoryOptionsByName(name);
                String u = ro==null?null:ro.getConfig().getLocation().getFullLocation();
                if (u == null) {
                    url = name;
                } else {
                    url = u;
                }
            } else {
                url = locationString;
                NAddRepositoryOptionsBoot ro = db.getRepositoryOptionsByLocation(name);
                String n = ro==null?null:ro.getName();
                if (n == null) {
                    name = null;
                } else {
                    name = n;
                }
            }
        }
        if (url.length() > 0) {
            return NRepositoryLocationBoot.of(name, url);
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
    public static NRepositoryLocationBoot[] of(String repositorySelectionExpression, NRepositoryLocationBoot[] available, NReservedBootRepositoryDB db) {
        NRepositorySelectorListBoot li = NRepositorySelectorListBoot.of(repositorySelectionExpression, db);
        if(li==null){
            return new NRepositoryLocationBoot[0];
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
    public NRepositoryLocationBoot setName(String name) {
        return new NRepositoryLocationBoot(name, locationType, path);
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
    public NRepositoryLocationBoot setPath(String path) {
        return new NRepositoryLocationBoot(name, locationType, path);
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
    public NRepositoryLocationBoot setLocationType(String locationType) {
        return new NRepositoryLocationBoot(name, locationType, path);
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
        NRepositoryLocationBoot that = (NRepositoryLocationBoot) o;
        return Objects.equals(toString(), that.toString());
    }

    /**
     * return location prefixed with location locationType if specified
     *
     * @return location prefixed with location locationType if specified
     */
    public String getFullLocation() {
        StringBuilder sb = new StringBuilder();
        if (!NStringUtilsBoot.isBlank(locationType)) {
            sb.append(locationType);
            sb.append("@");
        }
        if (!NStringUtilsBoot.isBlank(path)) {
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
        if (!NStringUtilsBoot.isBlank(name)) {
            sb.append(name);
            sb.append("=");
        }
        if (!NStringUtilsBoot.isBlank(locationType)) {
            sb.append(locationType);
            sb.append("@");
        }
        if (!NStringUtilsBoot.isBlank(path)) {
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
        if (!NStringUtilsBoot.isBlank(name)) {
            return false;
        }
        if (!NStringUtilsBoot.isBlank(locationType)) {
            return false;
        }
        if (!NStringUtilsBoot.isBlank(path)) {
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
    public int compareTo(NRepositoryLocationBoot o) {
        if (o == null) {
            return 1;
        }
        return toString().compareTo(o.toString());
    }

    public NRepositoryLocationBoot copy() {
        return clone();
    }

    @Override
    protected NRepositoryLocationBoot clone() {
        try {
            return (NRepositoryLocationBoot) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
}
