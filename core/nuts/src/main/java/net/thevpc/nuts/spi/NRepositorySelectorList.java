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

import net.thevpc.nuts.NBlankable;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.util.NStringUtils;

import java.util.*;

public class NRepositorySelectorList {

    private final List<NRepositorySelector> selectors = new ArrayList<>();

    public NRepositorySelectorList() {
    }

    public NRepositorySelectorList(NRepositorySelector[] a) {
        for (NRepositorySelector repoDefString : a) {
            if (repoDefString != null) {
                selectors.add(repoDefString);
            }
        }
    }
    public static NRepositorySelectorList ofAll(List<String> expressions, NRepositoryDB db, NSession session) {
        if (expressions == null) {
            return new NRepositorySelectorList();
        }
        NRepositorySelectorList result = new NRepositorySelectorList();
        for (String t : expressions) {
            result = result.merge(of(t,db,session));
        }
        return result;
    }

    public static NRepositorySelectorList of(String expression, NRepositoryDB db, NSession session) {
        if (NBlankable.isBlank(expression)) {
            return new NRepositorySelectorList();
        }
        NSelectorOp op = NSelectorOp.EXACT;
        List<NRepositorySelector> all = new ArrayList<>();
        for (String s : NStringUtils.split(expression,",;",true,true)) {
            s = s.trim();
            if (s.length() > 0) {
                if (s.startsWith("+")) {
                    op = NSelectorOp.INCLUDE;
                    s = s.substring(1);
                } else if (s.startsWith("-")) {
                    op = NSelectorOp.EXCLUDE;
                    s = s.substring(1);
                } else if (s.startsWith("=")) {
                    op = NSelectorOp.EXACT;
                    s = s.substring(1);
                }
                NRepositoryLocation z = NRepositoryLocation.of(s,db,session);
                if (z != null) {
                    all.add(new NRepositorySelector(op, z.getName(), z.getFullLocation()));
                }
            }
        }
        return new NRepositorySelectorList(all.toArray(new NRepositorySelector[0]));
    }

    public NRepositorySelectorList merge(NRepositorySelectorList other) {
        if (other == null || other.selectors.isEmpty()) {
            return this;
        }
        List<NRepositorySelector> result = new ArrayList<>();
        result.addAll(selectors);
        result.addAll(other.selectors);
        return new NRepositorySelectorList(result.toArray(new NRepositorySelector[0]));
    }

    public NRepositoryLocation[] resolve(NRepositoryLocation[] available, NRepositoryDB db) {
        NRepositoryURLList current = new NRepositoryURLList();
        if (available != null) {
            for (NRepositoryLocation entry : available) {
                String k = entry.getName();
                String v = entry.getFullLocation();
                if (NBlankable.isBlank(v) && !NBlankable.isBlank(k)) {
                    String u2 = db.getRepositoryLocationByName(k);
                    if (u2 != null) {
                        v = u2;
                    } else {
                        v = k;
                    }
                } else if (!NBlankable.isBlank(v) && NBlankable.isBlank(k)) {
                    String u2 = db.getRepositoryNameByLocation(k);
                    if (u2 != null) {
                        k = u2;
                    }
                }
                current.add(NRepositoryLocation.of(k, v));
            }
        }
        List<NRepositoryLocation> result = new ArrayList<>();
        Set<String> visited=new HashSet<>();
        for (NRepositorySelector r : selectors) {
            Set<String> allNames = db.getAllNames(r.getName());
            if (r.getOp() != NSelectorOp.EXCLUDE) {
                if (!NBlankable.isBlank(r.getName())) {
                    String u2 = r.getUrl();
                    int i = current.indexOfNames(allNames.toArray(new String[0]), 0);
                    if (i >= 0) {
                        NRepositoryLocation ss = current.removeAt(i);
                        if (ss != null && u2 == null) {
                            u2 = ss.getPath();
                        }
                    }
                    boolean visitedFlag = isVisitedFlag(allNames, visited);
                    if(!visitedFlag) {
                        visited.addAll(allNames);
                        result.add(NRepositoryLocation.of(r.getName(), u2));
                    }
                } else if (r.getOp() == NSelectorOp.EXACT) {
                    if(!isVisitedFlag(allNames, visited)) {
                        visited.addAll(allNames);
                        result.add(NRepositoryLocation.of(r.getName(), r.getUrl()));
                    }
                }
            }
        }
        for (NRepositoryLocation e : current.toArray()) {
            if (acceptExisting(e)) {
                Set<String> allNames = db.getAllNames(e.getName());
                if(!isVisitedFlag(allNames, visited)) {
                    visited.addAll(allNames);
                    result.add(e);
                }
            }
        }
        return result.toArray(new NRepositoryLocation[0]);
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

    public boolean acceptExisting(NRepositoryLocation location) {
        String n = location.getName();
        String path = location.getPath();
        boolean includeOthers = true;
        for (NRepositorySelector s : selectors) {
            if (s.matches(n, path)) {
                switch (s.getOp()) {
                    case EXACT:
                    case INCLUDE:
                        return true;
                    case EXCLUDE:
                        return false;
                }
            }
            if (s.getOp() == NSelectorOp.EXACT) {
                includeOthers = false;
            }
        }
        return includeOthers;
    }

    public NRepositorySelector[] toArray() {
        return selectors.toArray(new NRepositorySelector[0]);
    }


}
