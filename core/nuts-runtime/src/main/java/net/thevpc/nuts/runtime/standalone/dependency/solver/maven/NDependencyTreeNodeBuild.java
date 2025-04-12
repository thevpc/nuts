package net.thevpc.nuts.runtime.standalone.dependency.solver.maven;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.dependency.DefaultNDependencyTreeNode;
import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.util.NMsg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class NDependencyTreeNodeBuild {

    private final MavenNDependencySolver mavenNDependencySolver;
    NDependencyTreeNodeBuild parent;
    NId id;
    NDefinition def;
    NDependency dependency;
    NDependency effDependency;
    List<NDependencyTreeNodeBuild> children = new ArrayList<>();
    List<NId> exclusions = new ArrayList<>();
    boolean alreadyVisited;
    boolean provided;
    boolean optional;
    int depth;
    NDescriptor effDescriptor;
    NDependencyInfo key;
    boolean built;
    boolean includedInClassPath;

    public NDependencyTreeNodeBuild(MavenNDependencySolver mavenNDependencySolver, NDependencyTreeNodeBuild parent, NDependency dependency, NDefinition def, int depth) {
        this.mavenNDependencySolver = mavenNDependencySolver;
        this.dependency = dependency;
        this.parent = parent;
        this.depth = depth;
        this.def = def;
        this.id = def != null ? def.getId() : dependency.toId();
        this.provided = (dependency.isProvided()) || (parent != null && parent.provided);
        this.optional = (dependency.isOptional()) || (parent != null && parent.optional);
    }

//    public NDependencyTreeNodeBuild(MavenNDependencySolver mavenNDependencySolver, NDependencyTreeNodeBuild parent, NDefinition def, NDependency dependency, NDependency effDependency, int depth) {
//        this.mavenNDependencySolver = mavenNDependencySolver;
//        this.parent = parent;
//        this.def = def;
//        this.dependency = dependency;
//        this.effDependency = effDependency;
//        this.depth = depth;
//    }

    public void build0() {
        if (built) {
            return;
        }
        try {
            if (this.def == null) {
                this.def = mavenNDependencySolver.searchOne(this.dependency);
            }
            if (this.def == null) {
                this.key = NDependencyInfo.of(this);
                effDependency = dependency;
                if (parent != null) {
                    this.exclusions.addAll(parent.exclusions);
                }
                this.addExclusions(dependency);
                if (optional) {
                    return;
                } else {
                    throw new NIllegalArgumentException(NMsg.ofC(NI18n.of("missing non optional dependency %s"), dependency));
                }
            }
            this.id = def.getId();
            effDependency = dependency;
            if (parent == null) {
                effDependency = dependency.builder()
                        .setVersion(def.getId().getVersion())
                        .build();
            } else {
                effDependency = dependency.builder()
                        .setScope(mavenNDependencySolver.combineScopes(parent.effDependency.getScope(), dependency.getScope()))
                        .setVersion(def.getId().getVersion())
                        .setProperty("provided-by", parent.id.toString())
                        .build();
            }
//        this.id = def != null ? def.getId() : dependency != null ? dependency.toId() : null;
            this.key = NDependencyInfo.of(this);
            if (parent != null) {
                this.exclusions.addAll(parent.exclusions);
            }
            this.addExclusions(dependency);
        } finally {
            this.built = true;
        }
    }

    public boolean isAcceptableDependency(NDependency dependency) {
        if (exclusions.contains(NDependencyInfo.normalizedId(dependency))) {
            return false;
        }
        if (depth == 0) {
            return mavenNDependencySolver.effDependencyFilter.acceptDependency(dependency, getEffectiveId());
        } else {
            // in maven test dependencies are not propagated;
            if (dependency.isAnyTest()) {
                return false;
            }
            return mavenNDependencySolver.effDependencyFilter.acceptDependency(dependency, getEffectiveId());
        }

    }

    @Override
    public String toString() {
        NDependencyBuilder d = dependency.builder();
        d.getCondition().setArch(new ArrayList<>());
        d.getCondition().setOs(new ArrayList<>());
        d.getCondition().setOsDist(new ArrayList<>());
        d.getCondition().setDesktopEnvironment(new ArrayList<>());
        d.getCondition().setPlatform(new ArrayList<>());
        return "NDependencyTreeNodeBuild{" +
                "dependency=" + d.build() +
                (provided ? (", provided=" + provided) : "") +
                (optional ? (", optional=" + optional) : "") +
                ", depth=" + depth +
                '}';
    }

    NId getEffectiveId() {
        return getEffectiveDescriptor().getId();
    }

    NDescriptor getEffectiveDescriptor() {
        if (effDescriptor == null && def != null) {
            effDescriptor = def.getEffectiveDescriptor().orNull();
            if (effDescriptor == null) {
                effDescriptor = NWorkspace.of().resolveEffectiveDescriptor(def.getDescriptor(),
                        new NDescriptorEffectiveConfig()
                                .setIgnoreCurrentEnvironment(mavenNDependencySolver.isIgnoreCurrentEnvironment())
                );
                if (effDescriptor == null) {
                    throw new NIllegalArgumentException(
                            NMsg.ofC(NI18n.of("expected an effective definition for %s"), def.getId()));
                }
            }
        }
        return effDescriptor;
    }

    NDependencyTreeNode build() {
        List<NDependencyTreeNode> nchildren = new ArrayList<>();
        for (int i = 0; i < children.size(); i++) {
            NDependencyTreeNodeBuild e = children.get(i);
            if (e.includedInClassPath) {
                nchildren.add(e.build());
            }
        }
        return new DefaultNDependencyTreeNode(effDependency, nchildren, alreadyVisited, optional, provided);
    }

    public void addExclusions(NDependency dependency) {
        for (NId exclusion : dependency.getExclusions()) {
            this.exclusions.add(NDependencyInfo.normalizedId(exclusion));
        }
    }
}
