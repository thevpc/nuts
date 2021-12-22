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
 *
 * <br>
 * <p>
 * Copyright [2020] [thevpc]
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain a
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
 */
package net.thevpc.nuts.spi;

import net.thevpc.nuts.NutsBlankable;
import net.thevpc.nuts.NutsSession;

import java.util.*;

public class NutsRepositorySelectorList {

    private final List<NutsRepositorySelector> selectors = new ArrayList<>();

    public NutsRepositorySelectorList() {
    }

    public NutsRepositorySelectorList(NutsRepositorySelector[] a) {
        for (NutsRepositorySelector repoDefString : a) {
            if (repoDefString != null) {
                selectors.add(repoDefString);
            }
        }
    }
    public static NutsRepositorySelectorList ofAll(String[] expressions, NutsRepositoryDB db, NutsSession session) {
        if (expressions == null) {
            return new NutsRepositorySelectorList();
        }
        NutsRepositorySelectorList result = new NutsRepositorySelectorList();
        for (String t : expressions) {
            result = result.merge(of(t,db,session));
        }
        return result;
    }

    public static NutsRepositorySelectorList of(String expression, NutsRepositoryDB db, NutsSession session) {
        if (NutsBlankable.isBlank(expression)) {
            return new NutsRepositorySelectorList();
        }
        NutsSelectorOp op = NutsSelectorOp.EXACT;
        List<NutsRepositorySelector> all = new ArrayList<>();
        for (String s : expression.split("[,;]")) {
            s = s.trim();
            if (s.length() > 0) {
                if (s.startsWith("+")) {
                    op = NutsSelectorOp.INCLUDE;
                    s = s.substring(1);
                } else if (s.startsWith("-")) {
                    op = NutsSelectorOp.EXCLUDE;
                    s = s.substring(1);
                } else if (s.startsWith("=")) {
                    op = NutsSelectorOp.EXACT;
                    s = s.substring(1);
                }
                NutsRepositoryLocation z = NutsRepositoryLocation.of(s,db,session);
                if (z != null) {
                    all.add(new NutsRepositorySelector(op, z.getName(), z.getLocation()));
                }
            }
        }
        return new NutsRepositorySelectorList(all.toArray(new NutsRepositorySelector[0]));
    }

    public NutsRepositorySelectorList merge(NutsRepositorySelectorList other) {
        if (other == null || other.selectors.isEmpty()) {
            return this;
        }
        List<NutsRepositorySelector> result = new ArrayList<>();
        result.addAll(selectors);
        result.addAll(other.selectors);
        return new NutsRepositorySelectorList(result.toArray(new NutsRepositorySelector[0]));
    }

    public NutsRepositoryLocation[] resolve(NutsRepositoryLocation[] input, NutsRepositoryDB db) {
        NutsRepositoryURLList current = new NutsRepositoryURLList();
        if (input != null) {
            for (NutsRepositoryLocation entry : input) {
                String k = entry.getName();
                String v = entry.getLocation();
                if (NutsBlankable.isBlank(v) && !NutsBlankable.isBlank(k)) {
                    String u2 = db.getRepositoryURLByName(k);
                    if (u2 != null) {
                        v = u2;
                    } else {
                        v = k;
                    }
                } else if (!NutsBlankable.isBlank(v) && NutsBlankable.isBlank(k)) {
                    String u2 = db.getRepositoryNameByURL(k);
                    if (u2 != null) {
                        k = u2;
                    }
                }
                current.add(NutsRepositoryLocation.of(k, v));
            }
        }
        List<NutsRepositoryLocation> result = new ArrayList<>();
        Set<String> visited=new HashSet<>();
        for (NutsRepositorySelector r : selectors) {
            Set<String> allNames = db.getAllNames(r.getName());
            if (r.getOp() != NutsSelectorOp.EXCLUDE) {
                if (!NutsBlankable.isBlank(r.getName())) {
                    String u2 = r.getUrl();
                    int i = current.indexOfNames(allNames.toArray(new String[0]), 0);
                    if (i >= 0) {
                        NutsRepositoryLocation ss = current.removeAt(i);
                        if (ss != null && u2 == null) {
                            u2 = ss.getLocation();
                        }
                    }
                    boolean visitedFlag = isVisitedFlag(allNames, visited);
                    if(!visitedFlag) {
                        visited.addAll(allNames);
                        result.add(NutsRepositoryLocation.of(r.getName(), u2));
                    }
                } else if (r.getOp() == NutsSelectorOp.EXACT) {
                    if(!isVisitedFlag(allNames, visited)) {
                        visited.addAll(allNames);
                        result.add(NutsRepositoryLocation.of(r.getName(), r.getUrl()));
                    }
                }
            }
        }
        for (NutsRepositoryLocation e : current.toArray()) {
            if (acceptExisting(e)) {
                Set<String> allNames = db.getAllNames(e.getName());
                if(!isVisitedFlag(allNames, visited)) {
                    visited.addAll(allNames);
                    result.add(e);
                }
            }
        }
        return result.toArray(new NutsRepositoryLocation[0]);
    }

    private boolean isVisitedFlag(Set<String> allNames, Set<String> visited) {
        boolean visitedFlag=false;
        for (String allName : allNames) {
            if (visited.contains(allName)) {
                visitedFlag = true;
                break;
            }
        }
        return visitedFlag;
    }

    public boolean acceptExisting(NutsRepositoryLocation ss) {
        String n = ss.getName();
        String url = ss.getLocation();
        boolean includeOthers = true;
        for (NutsRepositorySelector s : selectors) {
            if (s.matches(n, url)) {
                switch (s.getOp()) {
                    case EXACT:
                    case INCLUDE:
                        return true;
                    case EXCLUDE:
                        return false;
                }
            }
            if (s.getOp() == NutsSelectorOp.EXACT) {
                includeOthers = false;
            }
        }
        return includeOthers;
    }

    public NutsRepositorySelector[] toArray() {
        return selectors.toArray(new NutsRepositorySelector[0]);
    }


}
