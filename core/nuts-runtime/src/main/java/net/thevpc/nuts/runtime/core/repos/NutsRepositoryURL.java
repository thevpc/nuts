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
package net.thevpc.nuts.runtime.core.repos;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author thevpc
 */
public class NutsRepositoryURL {

    protected static final Pattern NAME_PATTERN = Pattern.compile("(?<n>[A-Za-z-])=(?<r>.*)");
    protected static final Pattern UTC_PATTERN = Pattern.compile("([A-Z]:.*)|(\\.*)|(/.*)");
    protected static final Pattern URL_PATTERN = Pattern.compile("(?<p>[a-z-]+):(?<r>.*)");

    private final String name;
    private final String protocol;
    private final NutsRepositoryType repositoryType;
    private final Set<String> pathProtocols = new TreeSet<String>();
    private final String location;

    private NutsRepositoryURL(String name, String protocol, NutsRepositoryType repositoryType, Set<String> pathProtocols, String location) {
        this.name = name;
        this.protocol = protocol;
        this.repositoryType = repositoryType;
        this.pathProtocols.addAll(pathProtocols);
        this.location = location;
    }

    public NutsRepositoryURL(String url) {
        if (url == null) {
            url = "";
        }
        Matcher nm = NAME_PATTERN.matcher(url);
        if (nm.find()) {
            name = nm.group("n");
            url = nm.group("r");
        } else {
            name = "";
        }
        if (UTC_PATTERN.matcher(url).matches()) {
            repositoryType = new NutsRepositoryType();
            this.location = url;
        } else {
            Matcher matcher = URL_PATTERN.matcher(url);
            Set<String> repoProtocols = new LinkedHashSet<String>();
            if (matcher.find()) {
                String protocol = matcher.group("p");
                String rest = matcher.group("r");
                for (String s : protocol.split("[+]")) {
                    switch (s) {
                        case "https":
                        case "http":
                        case "ftp":
                        case "file":
                        case "ssh": {
                            pathProtocols.add(s);
                            break;
                        }
                        default: {
                            repoProtocols.add(s);
                            break;
                        }
                    }
                }
                repositoryType = new NutsRepositoryType(repoProtocols.toArray(new String[0]));
                this.location = String.join("+", pathProtocols) + ":" + rest;
            } else {
                repositoryType = new NutsRepositoryType();
                this.location = url;
            }
        }
        String p = repositoryType.isEmpty() ? "" : repositoryType.toString();
        if (!repositoryType.isEmpty() && pathProtocols.size() > 0) {
            p += "+";
        }
        this.protocol = p + String.join("+", pathProtocols);
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

    public String getProtocol() {
        return protocol;
    }

    public NutsRepositoryType getRepositoryType() {
        return repositoryType;
    }

    public String getPathProtocol() {
        return String.join("+", pathProtocols);
    }

    public Set<String> getPathProtocols() {
        return Collections.unmodifiableSet(pathProtocols);
    }

    public Set<String> getProtocols() {
        Set<String> s = new LinkedHashSet<>(repositoryType.getProtocols());
        s.addAll(pathProtocols);
        return Collections.unmodifiableSet(s);
    }

    public String getURLString() {
        if (repositoryType.isEmpty()) {
            return location;
        }
        if (pathProtocols.size() > 0) {
            return repositoryType + "+" + location;
        }
        return repositoryType + ":" + location;
    }

    @Override
    public String toString() {
        return name.isEmpty()
                ? getURLString()
                : getName() + "=" + getURLString();
    }

    public NutsRepositoryURL changeName(String s) {
        return new NutsRepositoryURL(s == null ? "" : s, protocol, repositoryType, pathProtocols, location);
    }
}
