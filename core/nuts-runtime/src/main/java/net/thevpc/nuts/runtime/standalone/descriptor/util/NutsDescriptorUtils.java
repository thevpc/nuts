package net.thevpc.nuts.runtime.standalone.descriptor.util;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.id.util.NutsIdUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NutsWorkspaceExt;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;

public class NutsDescriptorUtils {
    public static boolean isNoContent(NutsDescriptor desc) {
        return desc!=null && "pom".equals(desc.getPackaging());
    }

    public static NutsDescriptor getEffectiveDescriptor(NutsDefinition def,NutsSession session) {
        final NutsDescriptor d = def.getEffectiveDescriptor();
        if (d == null) {
            return NutsWorkspaceExt.of(session).resolveEffectiveDescriptor(def.getDescriptor(), session);
        }
        return d;
    }

    public static Map<String, String> getPropertiesMap(NutsDescriptorProperty[] list, NutsSession session) {
        Map<String, String> m = new LinkedHashMap<>();
        if (list != null) {
            for (NutsDescriptorProperty property : list) {
                if (property.getCondition() == null || property.getCondition().isBlank()) {
                    m.put(property.getName(), property.getValue());
                } else {
                    throw new NutsIllegalArgumentException(session, NutsMessage.plain("unexpected properties with conditions. probably a bug"));
                }
            }
        }
        return m;
    }

    public static NutsDescriptor checkDescriptor(NutsDescriptor nutsDescriptor, NutsSession session) {
        NutsId id = nutsDescriptor.getId();
        String groupId = id == null ? null : id.getGroupId();
        String artifactId = id == null ? null : id.getArtifactId();
        NutsVersion version = id == null ? null : id.getVersion();
        if (groupId == null || artifactId == null || NutsBlankable.isBlank(version)) {
            switch (session.getConfirm()) {
                case ASK:
                case ERROR: {
                    if (groupId == null) {
                        groupId = session.getTerminal().ask()
                                .forString(NutsMessage.cstyle("group id"))
                                .setDefaultValue(groupId)
                                .setHintMessage(NutsBlankable.isBlank(groupId) ? null : NutsMessage.plain(groupId))
                                .getValue();
                    }
                    if (artifactId == null) {
                        artifactId = session.getTerminal().ask()
                                .forString(NutsMessage.cstyle("artifact id"))
                                .setDefaultValue(artifactId)
                                .setHintMessage(NutsBlankable.isBlank(artifactId) ? null : NutsMessage.plain(artifactId))
                                .getValue();
                    }
                    if (NutsBlankable.isBlank(version)) {
                        String ov = version == null ? null : version.getValue();
                        String v = session.getTerminal().ask()
                                .forString(NutsMessage.cstyle("version"))
                                .setDefaultValue(ov)
                                .setHintMessage(NutsBlankable.isBlank(ov) ? null : NutsMessage.plain(ov))
                                .getValue();
                        version = NutsVersionParser.of(session)
                                .setAcceptBlank(true)
                                .setAcceptIntervals(true)
                                .setLenient(true).parse(v);
                    }
                    break;
                }
                case NO:
                case YES: {
                    //silently return null
                }
            }
        }
        if (groupId == null || artifactId == null || NutsBlankable.isBlank(version)) {
            throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("invalid descriptor id %s:%s#%s", groupId, artifactId, version));
        }
        return nutsDescriptor.builder()
                .setId(NutsIdBuilder.of(session).setGroupId(groupId).setArtifactId(artifactId).setVersion(version).build())
                .build();
    }

    public static void checkValidEffectiveDescriptor(NutsDescriptor effectiveDescriptor,NutsSession session) {
        if(effectiveDescriptor==null){
            throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("unable to evaluate effective null descriptor"));
        }
        try{
            for (NutsId parent : effectiveDescriptor.getParents()) {
                NutsIdUtils.checkValidEffectiveId(parent,session);
            }
            NutsIdUtils.checkValidEffectiveId(effectiveDescriptor.getId(),session);
            for (NutsDependency dependency : effectiveDescriptor.getDependencies()) {
                NutsIdUtils.checkValidEffectiveId(dependency.toId(),session);
            }
            for (NutsDependency dependency : effectiveDescriptor.getStandardDependencies()) {
                //NutsIdUtils.checkValidEffectiveId(dependency.toId(),session);
                // replace direct call to checkValidEffectiveId with the following...
                if (dependency == null) {
                    throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("unable to evaluate effective null id"));
                }
                if (dependency.toString().contains("${")) {
                    // some times the variable is defined later in the pom that uses this POM standard Dependencies
                    // so just log a warning, this is not an error but a very bad practice from the dependency maintainer!
                    NutsLoggerOp.of(NutsDescriptorUtils.class,session)
                            .verb(NutsLogVerb.WARNING).level(Level.FINE)
                            .log(NutsMessage.jstyle("{0} is using {1} which defines an unresolved variable. This is a potential bug.",
                                    effectiveDescriptor.getId(),
                                    dependency
                            ));
                }
            }
        }catch (Exception ex) {
            throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("unable to evaluate effective descriptor for %s", effectiveDescriptor.getId()),ex);
        }

    }

    public static boolean isValidEffectiveDescriptor(NutsDescriptor effectiveDescriptor,NutsSession session) {
        try{
            checkValidEffectiveDescriptor(effectiveDescriptor,session);
            return true;
        }catch (Exception ex){
            //
        }
        return false;
    }
}
