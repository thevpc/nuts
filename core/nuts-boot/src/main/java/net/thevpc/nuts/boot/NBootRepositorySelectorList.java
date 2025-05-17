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
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License");
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
 */
package net.thevpc.nuts.boot;

import net.thevpc.nuts.boot.reserved.util.NBootRepositoryDB;
import net.thevpc.nuts.boot.reserved.util.NBootUtils;

import java.util.*;
import java.util.stream.Collectors;

public class NBootRepositorySelectorList {

    private final List<NBootRepositorySelector> selectors = new ArrayList<>();

    public NBootRepositorySelectorList() {
    }

    public NBootRepositorySelectorList(NBootRepositorySelector[] a) {
        for (NBootRepositorySelector repoDefString : a) {
            if (repoDefString != null) {
                selectors.add(repoDefString);
            }
        }
    }

    public static NBootRepositorySelectorList of(List<String> expressions) {
        return of(expressions, NBootRepositoryDB.of());
    }

    public static NBootRepositorySelectorList of(List<String> expressions, NBootRepositoryDB db) {
        if (expressions == null) {
            return new NBootRepositorySelectorList();
        }
        NBootRepositorySelectorList result = new NBootRepositorySelectorList();
        for (String t : expressions) {
            if (t != null) {
                t = t.trim();
                if (!t.isEmpty()) {
                    NBootRepositorySelectorList r = of(t, db);
                    if (r != null) {
                        result = result.merge(r);
                    }
                }
            }
        }
        return result;
    }

    public static NBootRepositorySelectorList of(String expression, NBootRepositoryDB db) {
        if (NBootUtils.isBlank(expression)) {
            return new NBootRepositorySelectorList();
        }
        String op = "INCLUDE";
        List<NBootRepositorySelector> all = new ArrayList<>();
        for (String s : NBootUtils.split(expression, ",;", true, true)) {
            s = s.trim();
            if (s.length() > 0) {
                NBootRepositorySelector oe = NBootRepositorySelector.of(op, s, db);
                if (oe == null) {
                    return null;
//                    return NOptional.ofError(() -> NMsgBoot.ofC("invalid selector list : %s", expression));
                }
                NBootRepositorySelector e = oe;
                op = e.getOp();
                all.add(e);
            }
        }
        return new NBootRepositorySelectorList(all.toArray(new NBootRepositorySelector[0]));
    }

    public NBootRepositorySelectorList merge(NBootRepositorySelectorList other) {
        if (other == null || other.selectors.isEmpty()) {
            return this;
        }
        List<NBootRepositorySelector> result = new ArrayList<>();
        result.addAll(selectors);
        result.addAll(other.selectors);
        return new NBootRepositorySelectorList(result.toArray(new NBootRepositorySelector[0]));
    }

    public NBootRepositoryLocation[] resolve(NBootRepositoryLocation[] available, NBootRepositoryDB db) {
        NBootRepositoryLocationList current = new NBootRepositoryLocationList();
        if (available != null) {
            for (NBootRepositoryLocation entry : available) {
                if (entry != null) {
                    String k = entry.getName();
                    String v = entry.getFullLocation();
                    if (NBootUtils.isBlank(v) && !NBootUtils.isBlank(k)) {
                        NBootAddRepositoryOptions ro = db.getRepositoryOptionsByName(k);
                        String u = ro == null ? null : ro.getConfig().getLocation().getFullLocation();
                        if (u != null) {
                            v = u;
                        } else {
                            v = k;
                        }
                    } else if (!NBootUtils.isBlank(v) && NBootUtils.isBlank(k)) {
                        NBootAddRepositoryOptions ro = db.getRepositoryOptionsByLocation(k);
                        String n = ro == null ? null : ro.getName();
                        if (n != null) {
                            k = n;
                        }
                    }
                    current.add(NBootRepositoryLocation.of(k, v));
                }
            }
        }
        List<NBootRepositoryLocation> result = new ArrayList<>();
        Set<String> visited = new HashSet<>();

        List<NBootRepositorySelector> selectorsExclude = new ArrayList<>();
        List<NBootRepositorySelector> selectorsInclude = new ArrayList<>();
        boolean exact = false;
        for (NBootRepositorySelector selector : selectors) {
            switch (selector.getOp()) {
                case "EXACT": {
                    exact = true;
                    selectorsInclude.add(selector);
                    break;
                }
                case "INCLUDE": {
                    selectorsInclude.add(selector);
                    break;
                }
                case "EXCLUDE": {
                    selectorsExclude.add(selector);
                    break;
                }
            }
        }
        if (exact) {
            current.clear();
        }

        //now remove all excluded
        for (NBootRepositorySelector r : selectorsExclude) {
            Set<String> allNames = getAllNames(r, db);
            int i = current.indexOfNames(allNames.toArray(new String[0]), 0);
            if (i >= 0) {
                current.removeAt(i);
            }
        }
        //finally add included in the defined order


        for (NBootRepositorySelector r : selectorsInclude) {
            Set<String> allNames = getAllNames(r, db);
            if (!isVisitedFlag(allNames, visited)) {
                visited.addAll(allNames);
                NBootAddRepositoryOptions fo = null;
                for (String n : allNames) {
                    fo = db.getRepositoryOptionsByName(n);
                    break;
                }
                String newName = r.getName() == null ? (fo == null ? null : fo.getName()) : r.getName();
                NBootRepositoryLocation newLocation = r.getLocation();
                if (fo != null && fo.getConfig() != null && fo.getConfig().getLocation() != null) {
                    if (fo.getConfig().getLocation().getLocationType() != null) {
                        newLocation = newLocation.setLocationType(fo.getConfig().getLocation().getLocationType());
                    }
                    if (fo.getConfig().getLocation().getLocationType() != null) {
                        //name is the same as path, so move it to the path...
                        if (NBootUtils.isBlank(newLocation.getPath())
                                || Objects.equals(newLocation.getPath(), fo.getConfig().getLocation().getName())
                                || Objects.equals(newLocation.getPath(), fo.getConfig().getLocation().getLocationType())
                        ) {
                            newLocation = newLocation.setPath(fo.getConfig().getLocation().getPath());
                        }
                    }
                }
                result.add(NBootRepositoryLocation.of(newName,newLocation.getFullLocation()));
            }
        }
        for (NBootRepositoryLocation e : current.toArray()) {
            if (acceptExisting(e)) {
                Set<String> allNames = db.findAllNamesByName(e.getName());
                if (!isVisitedFlag(allNames, visited)) {
                    visited.addAll(allNames);
                    result.add(e);
                }
            }
        }
        return result.toArray(new NBootRepositoryLocation[0]);
    }

    private Set<String> getAllNames(NBootRepositorySelector r, NBootRepositoryDB db) {
        if (!NBootUtils.isBlank(r.getName())) {
            NBootAddRepositoryOptions lo = db.getRepositoryOptionsByName(r.getName());
            if (lo == null && !NBootUtils.isBlank(r.getLocation().getTypeAndPath())) {
                lo = db.getRepositoryOptionsByName(r.getLocation().getTypeAndPath());
            }
            if (lo != null) {
                return db.findAllNamesByName(lo.getName());
            }
            return Collections.singleton(r.getName());
        } else if (!NBootUtils.isBlank(r.getLocation().getTypeAndPath())) {
            NBootAddRepositoryOptions lo = db.getRepositoryOptionsByLocation(r.getLocation().getTypeAndPath());
            if (lo == null && !NBootUtils.isBlank(r.getName())) {
                lo = db.getRepositoryOptionsByLocation(r.getName());
            }
            if (lo == null) {
                lo = db.getRepositoryOptionsByName(r.getLocation().getTypeAndPath());
            }
            String name = lo == null ? null : lo.getName();
            return db.findAllNamesByName(name);
        }

        return Collections.emptySet();
    }

    private boolean isVisitedFlag(Set<String> allNames, Set<String> visited) {
        boolean visitedFlag = false;
        for (String allName : allNames) {
            if (visited.contains(allName)) {
                visitedFlag = true;
                break;
            }
        }
        return visitedFlag;
    }

    public boolean acceptExisting(NBootRepositoryLocation location) {
        boolean includeOthers = true;
        for (NBootRepositorySelector s : selectors) {
            if (s.matches(location)) {
                switch (s.getOp()) {
                    case "EXACT":
                    case "INCLUDE":
                        return true;
                    case "EXCLUDE":
                        return false;
                }
            }
            if (s.getOp().equals("EXACT")) {
                includeOthers = false;
            }
        }
        return includeOthers;
    }

    public NBootRepositorySelector[] toArray() {
        return selectors.toArray(new NBootRepositorySelector[0]);
    }


    @Override
    public String toString() {
        return selectors.stream().map(x -> String.valueOf(x)).collect(Collectors.joining(","));
    }
}
