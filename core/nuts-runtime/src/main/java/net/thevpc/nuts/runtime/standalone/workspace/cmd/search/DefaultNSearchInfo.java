package net.thevpc.nuts.runtime.standalone.workspace.cmd.search;

import net.thevpc.nuts.artifact.NDefinitionFilter;
import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.core.NRepositoryFilter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class DefaultNSearchInfo {

    private final NRepositoryFilter repositoryFilter;
    private final NDefinitionFilter definitionFilter;
    private final RegularId[] ids;
    private final List<NId> dependsOn;
    public DefaultNSearchInfo(RegularId[] ids, NRepositoryFilter repositoryFilter,
                              NDefinitionFilter definitionFilter, List<NId> dependsOn) {
        this.ids = ids;
        this.repositoryFilter = repositoryFilter;
        this.definitionFilter = definitionFilter;
        this.dependsOn = dependsOn == null ? Collections.emptyList() : dependsOn;
    }

    /**
     * depends-on constraints, kept out of {@link #getDefinitionFilter()} so
     * that they are evaluated lazily (newest first when searching the latest
     * version) rather than on every candidate version.
     *
     * @return depends-on ids, never null
     */
    public List<NId> getDependsOn() {
        return dependsOn;
    }

    public NRepositoryFilter getRepositoryFilter() {
        return repositoryFilter;
    }

    public NDefinitionFilter getDefinitionFilter() {
        return definitionFilter;
    }

    public RegularId[] getRegularIds() {
        return ids;
    }

    public static class RegularId{
        NId regularId;
        NId[] expandedIds;

        public RegularId(NId regularId, NId[] expandedIds) {
            this.regularId = regularId;
            this.expandedIds = expandedIds;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            RegularId regularId1 = (RegularId) o;
            return Objects.equals(regularId, regularId1.regularId) && Objects.deepEquals(expandedIds, regularId1.expandedIds);
        }

        @Override
        public int hashCode() {
            return Objects.hash(regularId, Arrays.hashCode(expandedIds));
        }
    }
}
