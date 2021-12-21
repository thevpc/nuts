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

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author thevpc
 */
public class NutsRepositoryLocation implements Comparable<NutsRepositoryLocation>, NutsBlankable {

    protected static final Pattern FULL_PATTERN = Pattern.compile("((?<n>[a-zA-Z][a-zA-Z0-9_-]*)?=)?((?<t>[a-zA-Z][a-zA-Z0-9_-]*)?@)?(?<r>.*)");

    private final String name;
    private final String type;
    private final Set<String> pathProtocols = new TreeSet<String>();
    private final String location;

    private NutsRepositoryLocation(String name, String type, String location) {
        this.name = NutsUtilStrings.trimToNull(name);
        this.type = NutsUtilStrings.trimToNull(type);
        this.location = NutsUtilStrings.trimToNull(location);
        this.pathProtocols.addAll(detectedProtocols(this.location));
    }

    protected NutsRepositoryLocation(String url) {
        if (url == null) {
            url = "";
        }
        Matcher nm = FULL_PATTERN.matcher(url);
        if (nm.find()) {
            name = NutsUtilStrings.trimToNull(nm.group("n"));
            type = NutsUtilStrings.trimToNull(nm.group("t"));
            location = NutsUtilStrings.trimToNull(nm.group("r"));
        } else {
            name = null;
            type = null;
            location = NutsUtilStrings.trimToNull(url);
        }
        pathProtocols.addAll(detectedProtocols(this.location));
    }

    public static NutsRepositoryLocation of(String url) {
        return new NutsRepositoryLocation(url);
    }

    public static NutsRepositoryLocation of(String name, String url) {
        NutsRepositoryLocation q = of(url);
        if (name != null) {
            q = q.setName(name);
        }
        return q;
    }

    public static NutsRepositoryLocation of(String expression, NutsRepositoryDB db, NutsSession session) {
        String name = null;
        String url = null;
        if (expression == null) {
            if (session == null) {
                throw new IllegalArgumentException("invalid null repository");
            } else {
                throw new NutsIllegalArgumentException(session, NutsMessage.plain("invalid null repository"));
            }
        }
        expression = expression.trim();
        if (expression.startsWith("-")
                || expression.startsWith("+")
                || expression.startsWith("=")
                || expression.indexOf(',') >= 0
                || expression.indexOf(';') >= 0) {
            if (session == null) {
                throw new IllegalArgumentException("invalid selection syntax");
            } else {
                throw new NutsIllegalArgumentException(session, NutsMessage.plain("invalid repository syntax"));
            }
        }
        Matcher matcher = Pattern.compile("(?<name>[a-zA-Z-_]+)=(?<value>.+)").matcher(expression);
        if (matcher.find()) {
            name = matcher.group("name");
            url = matcher.group("value");
        } else {
            if (expression.matches("[a-zA-Z-_]+")) {
                name = expression;
                String u = db.getRepositoryURLByName(name);
                if (u == null) {
                    url = name;
                } else {
                    url = u;
                }
            } else {
                url = expression;
                String n = db.getRepositoryNameByURL(url);
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

    public static NutsRepositoryLocation[] ofAll(String expression, NutsRepositoryLocation[] input, NutsRepositoryDB db, NutsSession session) {
        return NutsRepositorySelectorList.of(expression, db, session)
                .resolve(input, db);
    }

    private static Set<String> detectedProtocols(String location) {
        Set<String> pathProtocols = new TreeSet<String>();
        if (location != null) {
            int x = 0;
            while (x < location.length()) {
                if (location.charAt(x) == ':') {
                    break;
                }
                int z = location.indexOf(':', x);
                if (z >= 0) {
                    pathProtocols.add(location.substring(x, z));
                    x = z + 1;
                } else {
                    break;
                }
            }
        }
        return pathProtocols;
    }

    public String getName() {
        return name;
    }

    public NutsRepositoryLocation setName(String s) {
        return new NutsRepositoryLocation(s, type, location);
    }

    public String getLocation() {
        return location;
    }

    public NutsRepositoryLocation setLocation(String location) {
        return new NutsRepositoryLocation(name, type, location);
    }

    public String getType() {
        return type;
    }

    public NutsRepositoryLocation setType(String type) {
        return new NutsRepositoryLocation(name, type, location);
    }

    public Set<String> getPathProtocols() {
        return Collections.unmodifiableSet(pathProtocols);
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NutsRepositoryLocation that = (NutsRepositoryLocation) o;
        return Objects.equals(toString(), that.toString());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (!NutsBlankable.isBlank(name)) {
            sb.append(name);
            sb.append("=");
        }
        if (!NutsBlankable.isBlank(type)) {
            sb.append(type);
            sb.append("@");
        }
        sb.append(location);
        return sb.toString();
    }

    @Override
    public boolean isBlank() {
        if (!NutsBlankable.isBlank(name)) {
            return false;
        }
        if (!NutsBlankable.isBlank(type)) {
            return false;
        }
        if (!NutsBlankable.isBlank(location)) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(NutsRepositoryLocation o) {
        if (o == null) {
            return 1;
        }
        return toString().compareTo(o.toString());
    }

}
