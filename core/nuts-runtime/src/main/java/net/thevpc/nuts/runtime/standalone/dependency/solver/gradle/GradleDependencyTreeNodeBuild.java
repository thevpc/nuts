package net.thevpc.nuts.runtime.standalone.dependency.solver.gradle;

import java.util.ArrayList;
import java.util.List;

import net.thevpc.nuts.artifact.NDefinition;
import net.thevpc.nuts.artifact.NDependency;
import net.thevpc.nuts.artifact.NDependencyBuilder;
import net.thevpc.nuts.artifact.NDependencyTreeNode;
import net.thevpc.nuts.artifact.NDescriptor;
import net.thevpc.nuts.artifact.NDescriptorEffectiveConfig;
import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.core.NWorkspace;
import net.thevpc.nuts.runtime.standalone.dependency.DefaultNDependencyTreeNode;
import net.thevpc.nuts.runtime.standalone.dependency.solver.maven.NDependencyInfo;
import net.thevpc.nuts.text.NI18n;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NIllegalArgumentException;

/**
 * Gradle-specific dependency tree node builder.
 * Similar to Maven's NDependencyTreeNodeBuild but adapted for Gradle resolution.
 */
class GradleDependencyTreeNodeBuild {

    private final GradleNDependencySolver gradleSolver;
    GradleDependencyTreeNodeBuild parent;
    NId id;
    NDefinition def;
    NDependency dependency;
    NDependency effDependency;
    List<GradleDependencyTreeNodeBuild> children = new ArrayList<>();
    List<NId> exclusions = new ArrayList<>();
    boolean alreadyVisited;
    boolean provided;
    boolean optional;
    int depth;
    NDescriptor effDescriptor;
    NDependencyInfo key;
    boolean built;
    boolean includedInClassPath;

    public GradleDependencyTreeNodeBuild(GradleNDependencySolver gradleSolver, GradleDependencyTreeNodeBuild parent, NDependency dependency, NDefinition def, int depth) {
        this.gradleSolver = gradleSolver;
        this.dependency = dependency;
        this.parent = parent;
        this.depth = depth;
        this.def = def;
        this.id = def != null ? def.getId() : dependency.toId();
        this.provided = (dependency.isProvided()) || (parent != null && parent.provided);
        this.optional = (dependency.isOptional()) || (parent != null && parent.optional);
    }

    public void build0() {
        if (built) {
            return;
        }
        try {
            if (this.def == null) {
                this.def = gradleSolver.searchOne(this.dependency);
            }
            if (this.def == null) {
                this.key = new NDependencyInfo(this.dependency, this.depth, this.optional, this.provided);
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
                        .setScope(gradleSolver.combineScopes(parent.effDependency.getScope(), dependency.getScope()))
                        .setVersion(def.getId().getVersion())
                        .setProperty("provided-by", parent.id.toString())
                        .build();
            }
            this.key = new NDependencyInfo(this.dependency, this.depth, this.optional, this.provided);
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
            return gradleSolver.effDependencyFilter.acceptDependency(dependency, getEffectiveId());
        } else {
            // In Gradle, test dependencies can be propagated in test configurations
            // but for simplicity we'll keep Maven's behavior for now
            if (dependency.isAnyTest()) {
                return false;
            }
            return gradleSolver.effDependencyFilter.acceptDependency(dependency, getEffectiveId());
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
        return "GradleDependencyTreeNodeBuild{" +
                "dependency=" + d.build() +
                (provided ? (", provided=" + provided) : "") +
                (optional ? (", optional=" + optional) : "") +
                ", depth=" + depth +
                '}';
    }

    NId getEffectiveId() {
        return getEffectiveDescriptor().getId();
    }

    void setPreloadedDescriptor(NDescriptor descriptor) {
        this.effDescriptor = descriptor;
    }

    NDescriptor getEffectiveDescriptor() {
        if (effDescriptor == null && def != null) {
            effDescriptor = def.getEffectiveDescriptor().orNull();
            if (effDescriptor == null) {
                effDescriptor = NWorkspace.of().resolveEffectiveDescriptor(def.getDescriptor(),
                        new NDescriptorEffectiveConfig()
                                .setIgnoreCurrentEnvironment(gradleSolver.isIgnoreCurrentEnvironment())
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
            GradleDependencyTreeNodeBuild e = children.get(i);
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
