package net.thevpc.nuts.runtime.standalone.workspace.cmd.bundle;

import net.thevpc.nuts.*;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NStream;

import java.util.*;

class ResultingIds {
    LinkedHashMap<NId, NDefinition> classPath = new LinkedHashMap<>();
    Set<NId> executableAppIds = new LinkedHashSet<>();
    Set<NId> baseIds = new LinkedHashSet<>();


    private NId findNutsApiId() {
        for (NId resultId : classPath.keySet()) {
            if (resultId.getShortName().equals(NConstants.Ids.NUTS_API)) {
                return resultId;
            }
        }
        return null;
    }

    NId findNutsAppId() {
        for (NId resultId : classPath.keySet()) {
            if (resultId.getShortName().equals(NConstants.Ids.NUTS_APP)) {
                return resultId;
            }
        }
        return null;
    }

    private NId findNutsRuntimeId() {
        for (NId resultId : classPath.keySet()) {
            if (resultId.getShortName().equals(NConstants.Ids.NUTS_RUNTIME)) {
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
            if (classPath.containsKey(id.getLongId())) {
                return this;
            }
            NDefinition imdef = NFetchCmd.of(id)
                    .setDependencyFilter(NDependencyFilters.of().byRunnable(false, true))
                    .getResultDefinition();
            if (!classPath.containsKey(imdef.getId().getLongId())) {
                NId resultId = imdef.getId();
                if (imdef.getDescriptor().isPlatformApplication() || imdef.getDescriptor().isNutsApplication()) {
                    if (isBaseId(resultId)) {
                        executableAppIds.add(resultId);
                    }
                }
                classPath.put(resultId.getLongId(), imdef);
            }
            for (NId parent : imdef.getDescriptor().getParents()) {
                add(parent);
            }
            for (NDependency standardDependency : imdef.getEffectiveDescriptor().get().getStandardDependencies()) {
                if (NDependencyScope.parse(standardDependency.getScope()).orElse(NDependencyScope.API) == NDependencyScope.IMPORT) {
                    addBomId(standardDependency.toId());
                }
            }
        }
        return this;
    }

    public ResultingIds add(NId id) {
        if (!NBlankable.isBlank(id)) {
            if (classPath.containsKey(id.getLongId())) {
                return this;
            }
            List<NDefinition> list = NSearchCmd.of(id)
                    .setLatest(true)
                    .setDistinct(true)
                    .setDependencyFilter(NDependencyFilters.of().byRunnable(false, true))
                    .setInlineDependencies(true)
                    .setIgnoreCurrentEnvironment(true)
                    .getResultDefinitions().toList();
            if (list.isEmpty()) {
                throw new NNotFoundException(id);
            }
            for (NDefinition def : list) {
                if (!classPath.containsKey(def.getId().getLongId())) {
                    NId resultId = def.getId();
                    if (def.getDescriptor().isPlatformApplication() || def.getDescriptor().isNutsApplication()) {
                        if (isBaseId(resultId)) {
                            executableAppIds.add(resultId);
                        }
                    }
                    classPath.put(resultId.getLongId(), def);
                }
                for (NId parent : def.getDescriptor().getParents()) {
                    add(parent);
                }
                for (NDependency standardDependency : def.getEffectiveDescriptor().get().getStandardDependencies()) {
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
            if (baseId.getLongName().equals(resultId.getShortName())) {
                return true;
            }
        }
        for (NId baseId : baseIds) {
            if (baseId.getShortName().equals(resultId.getShortName())) {
                return true;
            }
        }
        for (NId baseId : baseIds) {
            if (NBlankable.isBlank(baseId.getGroupId()) && baseId.getArtifactId().equals(resultId.getArtifactId())) {
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
                if (resultIdDef.getId().getShortName().equals(NConstants.Ids.NUTS_API)) {
                    if (resultIdDef.getId().getLongName().equals(session.getWorkspace().getAppId().getLongName())) {
                        add(session.getWorkspace().getRuntimeId());
                    } else {
                        add(session.getWorkspace().getRuntimeId().builder().setVersion(resultIdDef.getId().getVersion() + ".0").build());
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
                if (resultIdDef.getId().getShortName().equals(NConstants.Ids.NUTS_API)) {
                    if (resultIdDef.getId().getLongName().equals(session.getWorkspace().getAppId().getLongName())) {
                        add(session.getWorkspace().getAppId());
                    } else {
                        NVersion v = resultIdDef.getId().getVersion();
                        if (v.compareTo("0.8.5") < 0) {
                            //do nothing
                        } else {
                            NId appId = NWorkspace.of().getAppId();
                            add(appId.builder().setVersion(resultIdDef.getId().getVersion()).build());
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
