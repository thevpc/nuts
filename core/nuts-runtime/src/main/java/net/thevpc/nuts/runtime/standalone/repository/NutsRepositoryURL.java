/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 *
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
package net.thevpc.nuts.runtime.standalone.repository;

import net.thevpc.nuts.NutsBlankable;

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author thevpc
 */
public class NutsRepositoryURL {

    protected static final Pattern FULL_PATTERN = Pattern.compile("((?<n>[a-z]+)=)?((?<t>[a-z]+)@)?(?<r>.*)");

    private final String name;
    private final String type;
//    private final String protocol;
//    private final NutsRepositoryType repositoryType;
    private final Set<String> pathProtocols = new TreeSet<String>();
    private final String location;

    private NutsRepositoryURL(String name, String type, String location) {
        this.name = name==null?"":name;
        this.type = type;
        this.location = location;
        this.pathProtocols.addAll(detectedProtocols(location));
    }

    public NutsRepositoryURL(String url) {
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
            name="";
            type=null;
        }
        pathProtocols.addAll(detectedProtocols(location));
    }
    private static Set<String> detectedProtocols(String location){
        Set<String> pathProtocols = new TreeSet<String>();
        int x=0;
        while (x<location.length()) {
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
        return pathProtocols;
    }

    public String getName() {
        return name;
    }

    public String getLocation() {
        return location;
    }

    public boolean isHttp() {
        return pathProtocols.contains("http") || pathProtocols.contains("https");
    }

    public String getType() {
        return type;
    }

    public Set<String> getPathProtocols() {
        return Collections.unmodifiableSet(pathProtocols);
    }

    public String getURLString() {
        if(NutsBlankable.isBlank(type)){
            return location;
        }
        return type+"@"+location;
    }

    @Override
    public String toString() {
        return (name==null || name.isEmpty())
                ? getURLString()
                : getName() + "=" + getURLString();
    }

    public NutsRepositoryURL changeName(String s) {
        return new NutsRepositoryURL(s, type, location);
    }
}
