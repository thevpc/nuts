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

import net.thevpc.nuts.NutsBlankable;
import net.thevpc.nuts.NutsIllegalArgumentException;
import net.thevpc.nuts.NutsMessage;
import net.thevpc.nuts.NutsSession;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author thevpc
 */
public class NutsRepositoryURL {

    protected static final Pattern FULL_PATTERN = Pattern.compile("((?<n>[a-z]+)=)?((?<t>[a-z]+)@)?(?<r>.*)");

    private final String name;
    private final String type;
    private final Set<String> pathProtocols = new TreeSet<String>();
    private final String location;

    private NutsRepositoryURL(String name, String type, String location) {
        this.name = name == null ? "" : name;
        this.type = type;
        this.location = location;
        this.pathProtocols.addAll(detectedProtocols(location));
    }

    protected NutsRepositoryURL(String url) {
        if (url == null) {
            url = "";
        }
        Matcher nm = FULL_PATTERN.matcher(url);
        if (nm.find()) {
            name = nm.group("n");
            type = nm.group("t");
            location = nm.group("r");
        } else {
            location = url;
            name = "";
            type = null;
        }
        pathProtocols.addAll(detectedProtocols(location));
    }

    public static NutsRepositoryURL of(String url) {
        return new NutsRepositoryURL(url);
    }

    public static NutsRepositoryURL of(String name, String url) {
        return of(url).setName(name);
    }

    public static NutsRepositoryURL of(String expression, NutsRepositoryDB db, NutsSession session) {
        String name = null;
        String url = null;
        if (expression == null) {
            if(session==null) {
                throw new IllegalArgumentException("invalid null repository");
            }else{
                throw new NutsIllegalArgumentException(session, NutsMessage.plain("invalid null repository"));
            }
        }
        expression = expression.trim();
        if (expression.startsWith("-")
                || expression.startsWith("+")
                || expression.startsWith("=")
                || expression.indexOf(',') >= 0
                || expression.indexOf(';') >= 0) {
            if(session==null) {
                throw new IllegalArgumentException("invalid selection syntax");
            }else{
                throw new NutsIllegalArgumentException(session,NutsMessage.plain("invalid repository syntax"));
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
            return NutsRepositoryURL.of(name, url);
        }
        return null;
    }

    public static NutsRepositoryURL[] ofAll(String expression,NutsRepositoryURL[] input, NutsRepositoryDB db,NutsSession session) {
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

    public String getLocation() {
        return location;
    }

    public String getType() {
        return type;
    }

    public Set<String> getPathProtocols() {
        return Collections.unmodifiableSet(pathProtocols);
    }

    public String getURLString() {
        if (NutsBlankable.isBlank(type)) {
            return location;
        }
        return type + "@" + location;
    }

    @Override
    public String toString() {
        return (name == null || name.isEmpty())
                ? getURLString()
                : getName() + "=" + getURLString();
    }

    public NutsRepositoryURL setName(String s) {
        return new NutsRepositoryURL(s, type, location);
    }
}
