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
 *
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
package net.thevpc.nuts.runtime.standalone.util;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.filters.CoreFilterUtils;
import net.thevpc.nuts.runtime.core.filters.NutsIdAndNutsDependencyFilterItem;
import net.thevpc.nuts.runtime.standalone.util.io.ByteArrayPrintStream;

import java.io.PrintStream;
import java.util.*;
import java.util.logging.Level;

public class NutsIdGraph {
    private final NutsLogger LOG;
    private final Set<NutsId> visited = new LinkedHashSet<>();
    private final Set<NutsIdNode> wildIds = new LinkedHashSet<>();
    private final NutsSession session;
    private final boolean failFast;
    private NutsIdGraphContext context;
    private int maxComplexity = 300;

    public NutsIdGraph(NutsSession session, boolean failFast) {
        this.session = session;
        this.failFast = failFast;
        context = new NutsIdGraphContext(session);
        LOG = session.getWorkspace().log().of(NutsIdGraph.class);
    }

    private static NutsId cleanup(NutsId id) {
        if (id == null) {
            return null;
        }
        id = id.builder().setNamespace(null).build();
        Map<String, String> m = id.getProperties();
        if (m != null && !m.isEmpty()) {
            m.remove(NutsConstants.IdProperties.FACE);
            id = id.builder().setProperties(m).build();
        }
        return id;
    }

    private void reset() {
        visited.clear();
        wildIds.clear();
        ;
        maxComplexity = 300;

    }

    public NutsId[] resolveDependencies(List<NutsId> ids, NutsDependencyFilter _dependencyFilter) {
        reset();
        push(ids, _dependencyFilter);
        return collect(ids, ids);
    }

    public NutsId[] resolveDependencies(NutsId id, NutsDependencyFilter _dependencyFilter) {
        return resolveDependencies(Arrays.asList(id), _dependencyFilter);
    }

    public NutsIdInfo resolveBest(Set<NutsIdInfo> ids) {
        NutsIdInfo best = null;
        for (NutsIdInfo n : ids) {
            if (best == null) {
                best = n;
            } else {
                if (n.compareTo(best) < 0) {
                    best = n;
                }
            }
        }
        return best;
    }

    private <T> void fixConflicts() {
        for (SimpleNutsIdInfo value : context.snutsIds.values().toArray(new SimpleNutsIdInfo[0])) {
            if (value.nodes.size() > 1) {
                Set<NutsIdInfo> list = value.nodes;
                NutsIdInfo best = resolveBest(list);
                if (best != null) {
                    for (NutsIdInfo nutsIdInfo : list.toArray(new NutsIdInfo[0])) {
                        context.replace(nutsIdInfo, best);
                    }
                }
            }
        }

        Map<String, NutsIdNode> wildIds = new HashMap<>();
        for (NutsIdNode wildeId : this.wildIds) {
            wildIds.put(cleanup(wildeId.id).toString(), wildeId);
        }
        for (SimpleNutsIdInfo node1 : context.snutsIds.values()) {
            for (NutsIdInfo node2 : node1.nodes) {
                for (NutsIdNode node3 : node2.nodes) {
                    if (!node3.getVersion().isSingleValue()) {
                        String key = cleanup(node3.id).toString();
                        wildIds.put(key, node3);
                    }
                }
            }
        }
        Set<NutsIdNode> toaddOk = new HashSet<>();
        for (NutsIdNode nutsId : wildIds.values()) {
            try {
                NutsId nutsId1 = session.getWorkspace().fetch().setId(nutsId.id).setSession(session).getResultId();
                context.getNutsIdInfo(nutsId.id, true).refTo = context.getNutsIdInfo(nutsId1, true);
                toaddOk.add(new NutsIdNode(nutsId1, nutsId.path, nutsId.filter, session, nutsId.optional));
            } catch (NutsNotFoundException ex) {
                NutsDependency dep = session.getWorkspace().dependency().builder().setId(nutsId.id).build();
                if (!dep.isOptional()) {
                    throw ex;
                }
            }
        }
        if (!toaddOk.isEmpty()) {
            maxComplexity--;
            if (maxComplexity < 0) {
                //System.out.println("Why");
            }
            push0(toaddOk);
        }
    }

    public void add(NutsIdNode from, NutsIdNode to) {
        context.register(from);
        context.register(to);
        context.getNutsIdInfo(from.id, true)
                .connect(context.getNutsIdInfo(to.id, true));
    }

    public NutsId[] collect() {
        List<NutsId> all = new ArrayList<>();
        for (NutsIdInfo root : getRoots()) {
            all.add(root.getBest().id);
        }
        return collect(all, null);
    }

    private NutsId uniformNutsId(NutsId id) {
        NutsIdBuilder b = id.builder();
        Map<String, String> m = b.getProperties();
        Map<String, String> ok = new HashMap<>();
        ok.put(NutsConstants.IdProperties.ARCH, m.get(NutsConstants.IdProperties.ARCH));
        ok.put(NutsConstants.IdProperties.OSDIST, m.get(NutsConstants.IdProperties.OSDIST));
        ok.put(NutsConstants.IdProperties.OS, m.get(NutsConstants.IdProperties.OS));
        ok.put(NutsConstants.IdProperties.PLATFORM, m.get(NutsConstants.IdProperties.PLATFORM));
//        ok.put(NutsConstants.IdProperties.ALTERNATIVE, m.get(NutsConstants.IdProperties.ALTERNATIVE));
        ok.put(NutsConstants.IdProperties.CLASSIFIER, m.get(NutsConstants.IdProperties.CLASSIFIER));
        b.setNamespace(null);
        b.setProperties(ok);
        return b.build();
    }

    public NutsId[] collect(Collection<NutsId> ids, Collection<NutsId> exclude) {
        Set<NutsIdInfo> collected = new HashSet<>();
        for (NutsId id : ids) {
            visit(context.getNutsIdInfoResult(id), collected);
        }
        Set<NutsId> excludeSet = new HashSet<>();
        if (exclude != null) {
            for (NutsId nutsId : exclude) {
                excludeSet.add(uniformNutsId(nutsId));
            }
        }
        List<NutsId> r = new ArrayList<>();
        for (NutsIdInfo n : collected) {
            if (!excludeSet.contains(uniformNutsId(n.id))) {
                r.add(n.id);
            }
        }
        return r.toArray(new NutsId[0]);
    }

    public void visit(NutsIdInfo id, Collection<NutsIdInfo> collected) {
        Set<NutsId> visited = new HashSet<>();
        Stack<NutsIdInfo> stack = new Stack<>();
        stack.push(id);
        visited.add(id.id.getLongNameId());
        while (!stack.isEmpty()) {
            NutsIdInfo i = stack.pop();
            if (i != null) {
                collected.add(i);
                List<NutsIdInfo> next = i.output;
                if (next != null) {
                    for (NutsIdInfo j : next) {
                        NutsId vv = j.id.getLongNameId();
                        if (!visited.contains(vv)) {
                            visited.add(vv);
                            stack.push(j);
                        }
                    }
                }
            }
        }
    }

    public void push(Collection<NutsId> ids, NutsDependencyFilter dependencyFilter) {
        Collection<NutsIdNode> n = new ArrayList<>();
        int order = 0;
        for (NutsId x : ids) {
            n.add(new NutsIdNode(x, Collections.EMPTY_LIST, order++, null, dependencyFilter, session,
                    x.toDependency().isOptional()
            ));
        }
        push0(n);
    }

    private boolean acceptVisit(NutsIdAndNutsDependencyFilterItem curr) {
        NutsId id2 = cleanup(curr.id.id);
        if (!visited.contains(id2)) {
            visited.add(id2);
            return true;
        }
        return false;
    }

    private void push0(Collection<NutsIdNode> ids) {
        if (ids.isEmpty()) {
            return;
        }
        Stack<NutsIdAndNutsDependencyFilterItem> stack = new Stack<>();
        for (NutsIdNode id : ids) {
            stack.push(new NutsIdAndNutsDependencyFilterItem(id, null));
        }
        int processed = 0;
        while (!stack.isEmpty()) {
            NutsIdAndNutsDependencyFilterItem curr = stack.pop();
            if (acceptVisit(curr)) {
                if (curr.id.getVersion().isSingleValue()) {
                    SearchTraceHelper.progressIndeterminate("search for deps of " + session.getWorkspace().id().formatter(curr.id.id.getLongNameId()).format(), session);
                    NutsDescriptor effDescriptor = null;
                    try {
                        effDescriptor = curr.getEffDescriptor(session);
                    } catch (NutsNotFoundException ex) {
                        NutsDependency dep = session.getWorkspace().dependency().builder().setId(curr.id.id).build();
                        if (!dep.isOptional() && !curr.optional && failFast) {
                            if (curr.parent == null) {
                                throw ex;
                            }
                            throw new NutsNotFoundException(ex.getWorkspace(),
                                    curr.parent.id.id,
                                    new NutsNotFoundException.NutsIdInvalidDependency[]{ex.toInvalidDependency()},
                                    null, null
                            );
                        }
                    }
                    if (effDescriptor != null) {
                        processed++;
                        context.register(curr.id);
                        int currentOrder = 0;
                        NutsDependency[] dependencies = CoreFilterUtils.filterDependencies(effDescriptor.getId(), effDescriptor.getDependencies(),
                                curr.id.filter, session);
                        for (NutsDependency dept : dependencies) {
                            NutsId[] exclusions = dept.getExclusions();

                            NutsDependencyFilter filter2 = curr.id.filter;
                            if (exclusions != null && exclusions.length > 0) {
                                filter2 = session.getWorkspace().dependency().filter().byExclude(curr.id.filter,
                                        Arrays.stream(exclusions).map(NutsId::getFullName).toArray(String[]::new)
                                );
                            }
                            if (curr.id.filter == null || curr.id.filter.acceptDependency(curr.id.id, dept, session)) {
                                NutsId item = dept.toId();
                                NutsIdNode nextNode = new NutsIdNode(prepareDepId(dept, item), curr.id.path, currentOrder++, curr.id.id, filter2, session,
                                        dept.isOptional()
                                );
                                if (!item.getVersion().isSingleValue()) {
                                    this.add(curr.id, nextNode);
                                } else {
                                    try {
                                        this.add(curr.id, nextNode);
                                        stack.push(new NutsIdAndNutsDependencyFilterItem(nextNode, curr));
                                    } catch (NutsNotFoundException ex) {
                                        if (dept.isOptional()) {
                                            LOG.with().session(session).level(Level.FINE).error(ex).log("Unable to resolve optional dependency {0} for {1}", dept.toId(), curr.id);
                                        } else {
                                            stack.push(new NutsIdAndNutsDependencyFilterItem(nextNode, curr));
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    processed++;
                    wildIds.add(curr.id);
                }
            }
        }
        if (processed > 0) {
            this.fixConflicts();
        }
    }

    private NutsId prepareDepId(NutsDependency dept, NutsId item) {
        if (!NutsDependencyScopes.isDefaultScope(dept.getScope())) {
            item = item.builder().setProperty("scope", dept.getScope()).build();
        }
        if (dept.isOptional()) {
            item = item.builder().setProperty(NutsConstants.IdProperties.OPTIONAL, "true").build();
        }
        return item;
    }

    public List<NutsIdInfo> getNutsIdInfoList() {
        List<NutsIdInfo> all = new ArrayList<>();
        for (SimpleNutsIdInfo node1 : context.snutsIds.values()) {
            all.addAll(node1.nodes);
        }
        return all;
    }

    public List<NutsIdNode> getNutsIdNodeList() {
        List<NutsIdNode> all = new ArrayList<>();
        for (SimpleNutsIdInfo node1 : context.snutsIds.values()) {
            for (NutsIdInfo node2 : node1.nodes) {
                all.addAll(node2.nodes);
            }
        }
        return all;
    }

    public Set<NutsIdInfo> getRoots() {
        LinkedHashSet<NutsIdInfo> all = new LinkedHashSet<>();
        for (NutsIdInfo nutsIdNode : getNutsIdInfoList()) {
            if (nutsIdNode.input.isEmpty()) {
                all.add(nutsIdNode);
            }
        }
        return all;
    }

    @Override
    public String toString() {
        ByteArrayPrintStream out = new ByteArrayPrintStream();
        print(out);
        return out.toString();
    }

    public void print(PrintStream out) {
        out.println("-------------------------------------------------------------");
        HashSet<NutsId> visited = new HashSet<>();
        for (NutsIdInfo root : getRoots()) {
            print(out, root, "", visited);
        }
    }

    public void print(PrintStream out, NutsIdInfo x, String prefix, Set<NutsId> visited) {
        out.print(prefix + x.id);
        List<NutsIdInfo> nutsList = x.output;
        if (nutsList == null) {
            out.println(" ==> NULL");
        } else {
            out.println("");
            if (!visited.contains(x.id)) {
                visited.add(x.id);
                for (NutsIdInfo y : nutsList) {
                    print(out, y, prefix + "  ", visited);
                }
            }
        }
    }

    public static class SimpleNutsIdInfo {

        private String id;
        private Set<NutsIdInfo> nodes = new HashSet<>();

        public SimpleNutsIdInfo(String id) {
            this.id = id;
        }

        @Override
        public int hashCode() {
            return Objects.hash(id);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            SimpleNutsIdInfo that = (SimpleNutsIdInfo) o;
            return Objects.equals(id, that.id);
        }
    }

    public static class NutsIdInfo {

        public List<NutsIdInfo> input = new ArrayList<>();
        public List<NutsIdInfo> output = new ArrayList<>();
        public NutsIdInfo refTo;
        private NutsId id;
        private Set<NutsIdNode> nodes = new HashSet<>();

        public NutsIdInfo(NutsId id) {
            this.id = id;
        }

        public int compareTo(NutsIdInfo other) {
            return getBest().compareTo(other.getBest());
        }

        public NutsIdNode getBest() {
            NutsIdNode b = null;
            for (NutsIdNode node : nodes) {
                if (b == null || node.compareTo(b) < 0) {
                    b = node;
                }
            }
            return b;
        }

        @Override
        public int hashCode() {
            return Objects.hash(id);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            NutsIdInfo that = (NutsIdInfo) o;
            return Objects.equals(id, that.id);
        }

        @Override
        public String toString() {
            return "NutsIdInfo{"
                    + "id=" + id
                    + ", nodes=" + nodes.size()
                    + ", input=" + input.size()
                    + ", output=" + output.size()
                    + '}';
        }

        public void replaceBy(NutsIdInfo other) {
            for (NutsIdInfo nutsIdNode : input.toArray(new NutsIdInfo[0])) {
                nutsIdNode.disconnect(this);
                nutsIdNode.connect(other);
            }
            for (NutsIdInfo nutsIdNode : output.toArray(new NutsIdInfo[0])) {
                disconnect(nutsIdNode);
                other.connect(nutsIdNode);
            }
        }

        public void disconnect(NutsIdInfo other) {
            output.remove(other);
            other.input.remove(this);
        }

        public void connect(NutsIdInfo other) {
            output.add(other);
            other.input.add(this);
        }
    }

    public static class NutsIdGraphContext {

        private Set<NutsIdNode> nodes = new HashSet<>();
        private Map<NutsId, NutsIdInfo> nutsIds = new HashMap<>();
        private Map<String, SimpleNutsIdInfo> snutsIds = new HashMap<>();
        private NutsSession session;

        public NutsIdGraphContext(NutsSession session) {
            this.session = session;
        }

        SimpleNutsIdInfo getSimpleNutsIdInfo(NutsId id, boolean create) {
            String ii = id.getShortName();
            SimpleNutsIdInfo p = snutsIds.get(ii);
            if (p == null) {
                if (create) {
                    p = new SimpleNutsIdInfo(ii);
                    snutsIds.put(ii, p);
                }
            }
            return p;
        }

        NutsIdInfo getNutsIdInfoResult(NutsId id) {
            NutsIdInfo p = getNutsIdInfo(id, false);
            if (p == null) {
                throw new NutsNotFoundException(session.getWorkspace(), id);
            }
            if (p.refTo != null) {
                return p.refTo;
            }
            return p;
        }

        NutsIdInfo getNutsIdInfo(NutsId id, boolean create) {
            NutsId ii = cleanup(id);
            NutsIdInfo p = nutsIds.get(ii);
            if (p == null) {
                if (create) {
                    p = new NutsIdInfo(ii);
                    nutsIds.put(ii, p);
                }
            }
            return p;
        }

        boolean contains(NutsId n) {
            return nutsIds.containsKey(n);
        }

        boolean contains(NutsIdNode n) {
            return nodes.contains(n);
        }

        void register(NutsIdNode n) {
            if (!nodes.contains(n)) {
                nodes.add(n);
                NutsIdInfo a = getNutsIdInfo(n.id, true);
                a.nodes.add(n);
                getSimpleNutsIdInfo(n.id, true).nodes.add(a);
            }
        }

        void unregister(NutsIdInfo n) {
            if (nutsIds.containsKey(n.id)) {
                for (NutsIdNode node : n.nodes) {
                    nodes.remove(node);
                }
                n.nodes.clear();
                SimpleNutsIdInfo a = getSimpleNutsIdInfo(n.id, true);
                a.nodes.remove(n);
            }
        }

        void replace(NutsIdInfo a, NutsIdInfo b) {
            if (!a.equals(b)) {
                a.replaceBy(b);
                unregister(a);
            }
        }
    }

    public static class NutsIdNode {

        public NutsId id0;
        public NutsId id;
        public List<Integer> path;
        public NutsId parent;
        public NutsDependencyFilter filter;
        public NutsSession session;
        public boolean optional;

        public NutsIdNode(NutsId id, List<Integer> path, NutsDependencyFilter dependencyFilter, NutsSession session, boolean optional) {
            if (!CoreNutsUtils.isEffectiveId(id)) {
                throw new NutsIllegalArgumentException(null, id + " is a non effective Id");
            }
            this.id0 = id;
            this.id = cleanup(id0);
            this.path = new ArrayList<>(path);
            this.parent = null;
            this.filter = dependencyFilter;
            this.session = session;
            this.optional = optional;
        }

        public NutsIdNode(NutsId id, List<Integer> parentPath, int order, NutsId parent, NutsDependencyFilter dependencyFilter, NutsSession session, boolean optional) {
            if (!CoreNutsUtils.isEffectiveId(id)) {
                throw new NutsIllegalArgumentException(null, id + " is a non effective Id");
            }
            this.id0 = id;
            this.id = cleanup(id0);
            this.path = new ArrayList<>();
            if (parentPath != null) {
                this.path.addAll(parentPath);
            }
            this.path.add(order);
            this.parent = parent;
            this.filter = dependencyFilter;
            this.session = session;
            this.optional = optional;
        }

        public String getSimpleName() {
            return id.getShortName();
        }

        public NutsId getLongNameId() {
            return id.getLongNameId();
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, path);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            NutsIdNode that = (NutsIdNode) o;
            return Objects.equals(id, that.id)
                    && Objects.equals(path, that.path);
        }

        @Override
        public String toString() {
            return id.toString() + path.toString();
        }

        public NutsVersion getVersion() {
            return id.getVersion();
        }

        public int compareTo(NutsIdNode other) {
            if (other.id.getShortName().equals(this.id.getShortName())) {
                NutsId id1 = this.id;
                NutsId id2 = other.id;
                if (id1.getVersion().isBlank()) {
                    return 1;
                }
                if (id2.getVersion().isBlank()) {
                    return -1;
                }
                int xx = Integer.compare(this.path.size(), other.path.size());
                if (xx != 0) {
                    return xx;
                }
                int max = this.path.size();
                for (int i = 0; i < max; i++) {
                    int a = this.path.get(i);
                    int b = other.path.get(i);
                    int x = Integer.compare(a, b);
                    if (x != 0) {
                        return x;
                    }
                }
                if (id2.getVersion().equals(id1.getVersion())) {
                    return 0;
                }
                if (id1.getVersion().isSingleValue() && id2.getVersion().isSingleValue()) {
                    int x = id1.getVersion().compareTo(id2.getVersion());
                    int c = NutsDependencyScopes.compareScopes(id1.getProperties().get("scope"), id2.getProperties().get("scope"));
                    if (x != 0) {
                        if (c == 0) {
                            //better version with same scope
                            return x < 0 ? 1 : -1;
                        } else {
                            return c < 0 ? 1 : -1;
//                String s=c<0?id1.getQueryMap().get("scope"):id2.getQueryMap().get("scope");
//                return (x<0?id1:id2).setQueryProperty("scope",s);
                        }
                    }
                    return c < 0 ? 1 : -1;
                }
                if (id1.getVersion().filter().acceptVersion(id2.getVersion(), session)) {
                    return 1;
                }
                if (id2.getVersion().filter().acceptVersion(id1.getVersion(), session)) {
                    return -1;
                }

                throw new NutsException(session.getWorkspace(), "error");
            } else {
                throw new NutsException(session.getWorkspace(), "error");
            }
        }

    }
}
