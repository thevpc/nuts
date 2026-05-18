package net.thevpc.nuts.runtime.standalone.workspace.cmd.bundle;

import net.thevpc.nuts.artifact.*;
import net.thevpc.nuts.command.NFetch;
import net.thevpc.nuts.command.NSearch;
import net.thevpc.nuts.core.NConstants;
import net.thevpc.nuts.core.NSession;
import net.thevpc.nuts.core.NWorkspace;
import net.thevpc.nuts.util.NBlankable;

import java.util.*;

class ResultingIds {
    LinkedHashMap<NId, NDefinition> classPath = new LinkedHashMap<>();
    Set<NId> executableAppIds = new LinkedHashSet<>();
    Set<NId> baseIds = new LinkedHashSet<>();


    private NId findNutsApiId() {
        for (NId resultId : classPath.keySet()) {
            if (resultId.shortName().equals(NConstants.Ids.NUTS_API)) {
                return resultId;
            }
        }
        return null;
    }

    NId findNutsAppId() {
        for (NId resultId : classPath.keySet()) {
            if (resultId.shortName().equals(NConstants.Ids.NUTS_APP)) {
                return resultId;
            }
        }
        return null;
    }

    private NId findNutsRuntimeId() {
        for (NId resultId : classPath.keySet()) {
            if (resultId.shortName().equals(NConstants.Ids.NUTS_RUNTIME)) {
                return resultId;
            }
        }
        return null;
    }


    public ResultingIds add(String ids) {
        if (!NBlankable.isBlank(ids)) {
            for (NId id : NId.ofList(ids)) {
                add(id);
            }
        }
        return this;
    }

    private ResultingIds addBomId(NId id) {
        if (!NBlankable.isBlank(id)) {
            if (classPath.containsKey(id.longId())) {
                return this;
            }
            NDefinition imdef = NFetch.of(id)
                    .setDependencyFilter(NDependencyFilters.of().byRunnable(false, true))
                    .getResultDefinition();
            if (!classPath.containsKey(imdef.id().longId())) {
                NId resultId = imdef.id();
                if (imdef.descriptor().isPlatformApplication() || imdef.descriptor().isNutsApplication()) {
                    if (isBaseId(resultId)) {
                        executableAppIds.add(resultId);
                    }
                }
                classPath.put(resultId.longId(), imdef);
            }
            for (NId parent : imdef.descriptor().getParents()) {
                add(parent);
            }
            for (NDependency standardDependency : imdef.effectiveDescriptor().get().getStandardDependencies()) {
                if (NDependencyScope.parse(standardDependency.getScope()).orElse(NDependencyScope.API) == NDependencyScope.IMPORT) {
                    addBomId(standardDependency.toId());
                }
            }
        }
        return this;
    }

    public ResultingIds add(NId id) {
        if (!NBlankable.isBlank(id)) {
            if (classPath.containsKey(id.longId())) {
                return this;
            }
            List<NDefinition> list = NSearch.of(id)
                    .latest(true)
                    .distinct(true)
                    .setDependencyFilter(NDependencyFilters.of().byRunnable(false, true))
                    .setInlineDependencies(true)
                    .setIgnoreCurrentEnvironment(true)
                    .getResultDefinitions().toList();
            if (list.isEmpty()) {
                throw new NArtifactNotFoundException(id.longId());
            }
            for (NDefinition def : list) {
                if (!classPath.containsKey(def.id().longId())) {
                    NId resultId = def.id();
                    if (def.descriptor().isPlatformApplication() || def.descriptor().isNutsApplication()) {
                        if (isBaseId(resultId)) {
                            executableAppIds.add(resultId);
                        }
                    }
                    classPath.put(resultId.longId(), def);
                }
                for (NId parent : def.descriptor().getParents()) {
                    add(parent);
                }
                for (NDependency standardDependency : def.effectiveDescriptor().get().getStandardDependencies()) {
                    if (NDependencyScope.parse(standardDependency.getScope()).orElse(NDependencyScope.API) == NDependencyScope.IMPORT) {
                        addBomId(standardDependency.toId());
                    }
                }
            }
        }
        return this;
    }

    public boolean isBaseId(NId resultId) {
        for (NId baseId : baseIds) {
            if (baseId.longName().equals(resultId.shortName())) {
                return true;
            }
        }
        for (NId baseId : baseIds) {
            if (baseId.shortName().equals(resultId.shortName())) {
                return true;
            }
        }
        for (NId baseId : baseIds) {
            if (NBlankable.isBlank(baseId.groupId()) && baseId.artifactId().equals(resultId.artifactId())) {
                return true;
            }
        }
        return false;
    }

    public ResultingIds addAllId(String[] ids) {
        for (String id : ids) {
            if (!NBlankable.isBlank(id)) {
                baseIds.add(NId.of(id));
            }
        }
        for (String id : ids) {
            add(id);
        }
        return this;
    }
    public ResultingIds addAllLibs(String[] ids) {
        for (String id : ids) {
            add(id);
        }
        return this;
    }

    public void build() {
        NSession session = NSession.of();
        //ensure there is a full nuts workspace runtime (nuts-runtime)
        if (findNutsRuntimeId() == null) {
            for (NDefinition resultIdDef : new ArrayList<>(classPath.values())) {
                if (resultIdDef.id().shortName().equals(NConstants.Ids.NUTS_API)) {
                    if (resultIdDef.id().longName().equals(session.getWorkspace().getAppId().longName())) {
                        add(session.getWorkspace().getRuntimeId());
                    } else {
                        add(session.getWorkspace().getRuntimeId().builder().setVersion(resultIdDef.id().version() + ".0").build());
                    }
                    break;
                }
            }
        }
        if (findNutsRuntimeId() == null) {
            add(session.getWorkspace().getRuntimeId());
        }
        if (findNutsAppId() == null) {
            for (NDefinition resultIdDef : new ArrayList<>(classPath.values())) {
                if (resultIdDef.id().shortName().equals(NConstants.Ids.NUTS_API)) {
                    if (resultIdDef.id().longName().equals(session.getWorkspace().getAppId().longName())) {
                        add(session.getWorkspace().getAppId());
                    } else {
                        NVersion v = resultIdDef.id().version();
                        if (v.compareTo("0.8.5") < 0) {
                            //do nothing
                        } else {
                            NId appId = NWorkspace.of().getAppId();
                            add(appId.builder().setVersion(resultIdDef.id().version()).build());
                        }
                    }
                    break;
                }
            }
        }
        if (findNutsRuntimeId() == null) {
            add(session.getWorkspace().getAppId());
        }
    }
}
